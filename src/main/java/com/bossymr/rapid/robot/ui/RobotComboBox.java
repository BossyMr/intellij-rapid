package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.RapidBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.popup.ListSeparator;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.GroupedComboBoxRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.swing.*;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.ide.ui.laf.darcula.ui.DarculaJBPopupComboPopup.USE_LIVE_UPDATE_MODEL;

public class RobotComboBox extends ComboBox<RobotConnectPanel.Node> implements Closeable {

    private final List<RobotConnectPanel.Node> elements = new ArrayList<>();
    private List<JmDNS> scanners = new ArrayList<>();

    @SuppressWarnings("UnstableApiUsage")
    public RobotComboBox() {
        elements.add(new RobotConnectPanel.Action());
        setModel(new CollectionComboBoxModel<>(elements));
        /*
         * Use the IntelliJ popup instead of the default popup.
         */
        setSwingPopup(false);
        /*
         * Refresh the popup immediately. Therefore, one does not need to close the combobox for it to refresh.
         */
        putClientProperty(USE_LIVE_UPDATE_MODEL, true);
        setRenderer(new Renderer());
        start();
    }

    private void update() {
        /*
         * Create a new model and swap the used model. This is required in order to refresh the combobox immediately
         * (it doesn't work if the model is updated, i.e. if an element is added to the model).
         */
        ApplicationManager.getApplication().invokeLater(() -> {
            ComboBoxModel<RobotConnectPanel.Node> model = new CollectionComboBoxModel<>(elements);
            // Update the selected element, unless the previously selected element was removed.
            RobotConnectPanel.Node selectedItem = (RobotConnectPanel.Node) model.getSelectedItem();
            if (elements.contains(selectedItem)) {
                model.setSelectedItem(selectedItem);
            }
            setModel(model);
        }, ModalityState.any());
    }

    private void start() {
        /*
         * Start searching for robots. This needs to be done in a separate thread, since JmDNS will block for a short
         * delay when creating a new listener.
         */
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            List<InetAddress> addresses = NetworkInterface.networkInterfaces()
                    .flatMap(NetworkInterface::inetAddresses)
                    .toList();
            for (InetAddress address : addresses) {
                if (address.isLoopbackAddress()) {
                    continue;
                }
                JmDNS scanner;
                try {
                    scanner = JmDNS.create(address);
                } catch (IOException e) {
                    continue;
                }
                if (scanners == null) {
                    /*
                     * If this panel is closed, scanners will be set to null.
                     */
                    scanner.close();
                    break;
                }
                scanners.add(scanner);
                scanner.addServiceListener("_http._tcp.local.", new ScannerListener());
            }
            return null;
        });

    }

    @Override
    public void close() {
        List<JmDNS> scanners = List.copyOf(this.scanners);
        this.scanners = null;
        // Calling JmDNS#close() causes the current thread to block for a while.
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            for (JmDNS scanner : scanners) {
                scanner.close();
            }
            return null;
        });
    }

    private class Renderer extends GroupedComboBoxRenderer<RobotConnectPanel.Node> {

        @Override
        public @NotNull String getText(@Nullable RobotConnectPanel.Node item) {
            if (item == null) {
                return "";
            }
            return item.getName();
        }

        @Override
        public @Nullable String getSecondaryText(@Nullable RobotConnectPanel.Node item) {
            // Show IP address beside to the robot name.
            if (!(item instanceof RobotConnectPanel.State state)) {
                return null;
            }
            try {
                URI path = state.getPath();
                String secondaryText = path.getHost();
                if (path.getPort() == 80) {
                    return secondaryText;
                }
                return secondaryText + ":" + path.getPort();
            } catch (IllegalArgumentException e) {
                // The only way this could occur is if JmDNS resolved a service to an invalid IP address.
                throw new AssertionError(e);
            }
        }

        @Override
        public @Nullable Icon getIcon(@Nullable RobotConnectPanel.Node item) {
            if (item == null) {
                return null;
            }
            return item.getIcon();
        }

        @Override
        public @Nullable ListSeparator separatorFor(@Nullable RobotConnectPanel.Node value) {
            if (elements.indexOf(value) == 1) {
                /*
                 * The first node will always be the 'Add Robot' node. The second node will always be the first
                 * detected robot.
                 */
                return new ListSeparator(RapidBundle.message("robot.connect.dialog.separator"));
            }
            return null;
        }
    }

    private class ScannerListener implements ServiceListener {

        private static final String PREFIX = "RobotWebServices_";

        /*
         * A map containing all robots detected by this scanner instance. The key is computed as:
         * [type] '.' [name]. A service might be resolved more than once, in that case, as the state is
         * already stored in this map, it can be ignored.
         */
        private final Map<String, RobotConnectPanel.State> states = new HashMap<>();

        /*
         * We don't do anything when a new service is found since it hasn't been resolved yet. If we were
         * to add it to the model immediately, the model would need to be updated two times. Once when the
         * service is added and once more when an IP-address is associated with the service.
         */
        @Override
        public void serviceAdded(ServiceEvent event) {}

        @Override
        public void serviceRemoved(ServiceEvent event) {
            RobotConnectPanel.State node = states.remove(getServiceKey(event));
            if (node == null) {
                /*
                 * This service hasn't been added. This is either because it isn't a robot or because it hasn't been
                 * resolved.
                 */
                return;
            }
            elements.remove(node);
            update();
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            if (!(event.getName().startsWith(PREFIX))) {
                // This service doesn't represent a robot.
                return;
            }
            if (states.containsKey(getServiceKey(event))) {
                // This service is already resolved.
                return;
            }
            ServiceInfo serviceInfo = event.getDNS().getServiceInfo(event.getType(), event.getName());
            String[] addresses = serviceInfo.getURLs();
            if (addresses.length == 0) {
                // This service is unreachable.
                return;
            }
            String name = event.getName().substring(PREFIX.length());
            try {
                URI path = URI.create(addresses[0]);
                RobotConnectPanel.State state = new RobotConnectPanel.State(name, path);
                states.put(getServiceKey(event), state);
                elements.add(state);
            } catch (IllegalArgumentException e) {
                // The address is invalid.
                return;
            }
            update();
        }

        private @NotNull String getServiceKey(@NotNull ServiceEvent event) {
            return event.getName() + "." + event.getType();
        }
    }
}
