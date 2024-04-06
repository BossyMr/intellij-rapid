package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.ListSeparator;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.GridBag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;

import static com.intellij.ide.ui.laf.darcula.ui.DarculaJBPopupComboPopup.USE_LIVE_UPDATE_MODEL;

public class RobotConnectPanel extends JPanel implements Closeable {

    private final List<RobotNode> elements = new ArrayList<>();
    private final ComboBox<RobotNode> robot;
    private final JBTextField host;
    private final JBTextField port;
    private final ComboBox<AuthenticationType> authenticationType;
    private final JBTextField username;
    private final JBPasswordField password;
    private List<JmDNS> scanners = new ArrayList<>();

    public RobotConnectPanel() {
        super(new GridBagLayout());
        elements.add(new RobotNode.ConnectAction());
        initialize();
        setMinimumSize(new Dimension(300, (int) getMinimumSize().getHeight()));
        GridBag constraints = new GridBag()
                .setDefaultFill(GridBagConstraints.HORIZONTAL)
                .setDefaultWeightX(1.0);
        robot = new RobotComboBox();
        host = new JBTextField();
        port = new JBTextField();
        port.setText("80");
        List<AuthenticationType> authenticationTypes = Arrays.stream(AuthenticationType.values()).toList();
        authenticationType = new ComboBox<>(new CollectionComboBoxModel<>(authenticationTypes));
        authenticationType.setRenderer(SimpleListCellRenderer.create("", AuthenticationType::getDisplayName));
        username = new JBTextField();
        password = new JBPasswordField();
        authenticationType.addItemListener(e -> {
            boolean childrenAreVisible = e.getItem() == AuthenticationType.PASSWORD;
            username.setVisible(childrenAreVisible);
            password.setVisible(childrenAreVisible);
        });
        robot.addItemListener(e -> {
            boolean childrenAreVisible = e.getItem() instanceof RobotNode.ConnectAction;
            host.setVisible(childrenAreVisible);
            port.setVisible(childrenAreVisible);
        });
        add(createLabel(RapidBundle.message("robot.connect.dialog.robot"), robot), constraints.nextLine().next());
        add(robot, constraints.next());
        add(createLabel(RapidBundle.message("robot.connect.host.label"), host), constraints.nextLine().next());
        add(host, constraints.next());
        add(createLabel(RapidBundle.message("robot.connect.host.port"), port), constraints.nextLine().next());
        add(port, constraints.next());
        add(createLabel(RapidBundle.message("robot.connect.authentication.type"), authenticationType), constraints.nextLine().next());
        add(authenticationType, constraints.next());
        add(createLabel(RapidBundle.message("robot.connect.authentication.username"), username), constraints.nextLine().next());
        add(username, constraints.next());
        add(createLabel(RapidBundle.message("robot.connect.authentication.password"), password), constraints.nextLine().next());
        add(password, constraints.next());
        authenticationType.setSelectedItem(AuthenticationType.DEFAULT);
    }

    private @NotNull JBLabel createLabel(@NotNull String text, @NotNull JComponent component) {
        JBLabel label = new JBLabel(text);
        component.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {}

            @Override
            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {
                label.setVisible(true);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                label.setVisible(false);
            }
        });
        return label;
    }

    public @NotNull URI getHost() {
        if (robot.getItem() instanceof RobotNode.State state) {
            return URI.create(state.path());
        }
        try {
            return new URI("http", null, host.getText(), Integer.parseInt(port.getText()), null, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalStateException();
        }
    }

    public @NotNull Credentials getCredentials() {
        if (authenticationType.getItem() == AuthenticationType.DEFAULT) {
            return RobotService.DEFAULT_CREDENTIALS;
        }
        return new Credentials(username.getText(), password.getPassword());
    }

    public void setHost(@NotNull URI address) {
        host.setText(address.getHost());
        port.setText(String.valueOf(address.getPort() >= 0 ? address.getPort() : 80));
    }

    public void setCredentials(@NotNull Credentials credentials) {
        if(credentials == RobotService.DEFAULT_CREDENTIALS) {
            authenticationType.setItem(AuthenticationType.DEFAULT);
        } else {
            authenticationType.setItem(AuthenticationType.PASSWORD);
            username.setText(credentials.username());
            password.setText(new String(credentials.password()));
        }
    }

    public @NotNull List<ValidationInfo> getValidationInfo() {
        List<ValidationInfo> result = new ArrayList<>();
        if (host.isVisible()) {
            if (host.getText().isEmpty()) {
                result.add(new ValidationInfo(RapidBundle.message("robot.connect.host.empty"), host));
            } else {
                try {
                    new URI("http", null, host.getText().trim(), 80, null, null, null);
                } catch (URISyntaxException e) {
                    result.add(new ValidationInfo(RapidBundle.message("robot.connect.host.invalid"), host));
                }
            }
        }
        if (port.isVisible()) {
            try {
                int value = Integer.parseInt(port.getText());
                if (value < 0 || value >= 65535) {
                    result.add(new ValidationInfo(UIBundle.message("please.enter.a.number.from.0.to.1", 0, 65535), port));
                }
            } catch (NumberFormatException e) {
                result.add(new ValidationInfo(UIBundle.message("please.enter.a.number"), port));
            }
        }
        return result;
    }

    private void syncModel() {
        /*
         * Create a new model and swap the used model. This is required in order to refresh the combobox immediately
         * (it doesn't work if the model is updated, i.e. if an element is added to the model).
         */
        ComboBoxModel<RobotNode> model = new CollectionComboBoxModel<>(elements);
        model.setSelectedItem(robot.getModel().getSelectedItem());
        robot.setModel(model);
    }

    private void initialize() {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            List<InetAddress> addresses = NetworkInterface.networkInterfaces()
                    .flatMap(NetworkInterface::inetAddresses)
                    .toList();
            for (InetAddress address : addresses) {
                if(address.isLoopbackAddress()) {
                    continue;
                }
                JmDNS scanner;
                try {
                    scanner = JmDNS.create(address);
                } catch (IOException e) {
                    continue;
                }
                if(scanners == null) {
                    scanner.close();
                    break;
                }
                scanners.add(scanner);
                scanner.addServiceListener("_http._tcp.local.", new ServiceListener() {

                    private static final String PREFIX = "RobotWebServices_";

                    /*
                     * A map containing all robots detected by this scanner instance. The key is computed as:
                     * [type] '.' [name]. A service might be resolved more than once, in that case, as the state is
                     * already stored in this map, it can be ignored.
                     */
                    private final Map<String, RobotNode.State> states = new HashMap<>();

                    /*
                     * We don't do anything when a new service is found since it hasn't been resolved yet. If we were
                     * to add it to the model immediately, the model would need to be updated two times. Once when the
                     * service is added and once more when an IP-address is associated with the service.
                     */
                    @Override
                    public void serviceAdded(ServiceEvent event) {}

                    @Override
                    public void serviceRemoved(ServiceEvent event) {
                        RobotNode.State node = states.remove(event.getName() + "." + event.getType());
                        if(node == null) {
                            return;
                        }
                        elements.remove(node);
                        syncModel();
                    }

                    @Override
                    public void serviceResolved(ServiceEvent event) {
                        if(!(event.getName().startsWith(PREFIX)) || states.containsKey(event.getName() + "." + event.getType())) {
                            return;
                        }
                        ServiceInfo serviceInfo = event.getDNS().getServiceInfo(event.getType(), event.getName());
                        String[] addresses = serviceInfo.getURLs();
                        if(addresses.length == 0) {
                            return;
                        }
                        String name = event.getName().substring(PREFIX.length());
                        RobotNode.State state = new RobotNode.State(name, addresses[0]);
                        states.put(event.getName() + "." + event.getType(), state);
                        elements.add(state);
                        syncModel();
                    }
                });
            }
            return null;
        });
    }

    @Override
    public void close() throws IOException {
        List<JmDNS> copy = List.copyOf(scanners);
        scanners = null;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            for (JmDNS scanner : copy) {
                scanner.close();
            }
            return null;
        });
    }

    private sealed interface RobotNode {

        @NotNull Icon icon();

        @NotNull String name();

        record State(@NotNull String name, @NotNull String path) implements RobotNode {
            @Override
            public @NotNull Icon icon() {
                return RapidIcons.ROBOT;
            }
        }

        final class ConnectAction implements RobotNode {
            @Override
            public @NotNull Icon icon() {
                return AllIcons.General.Add;
            }

            @Override
            public @NotNull String name() {
                return RapidBundle.message("robot.connect.dialog.manual");
            }
        }
    }

    private class RobotComboBox extends ComboBox<RobotNode> {

        @SuppressWarnings("UnstableApiUsage")
        public RobotComboBox() {
            super(new CollectionComboBoxModel<>(elements));
            /*
             * Use the IntelliJ popup instead of the default popup.
             */
            setSwingPopup(false);
            putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true);
            /*
             * Refresh the popup immediately. Therefore, one does not need to close the combobox for it to refresh.
             */
            putClientProperty(USE_LIVE_UPDATE_MODEL, true);
            setRenderer(new GroupedComboBoxRenderer<>() {
                @Override
                public @NotNull String getText(@Nullable RobotNode item) {
                    return item != null ? item.name() : "";
                }

                @Override
                public @Nullable String getSecondaryText(@Nullable RobotNode item) {
                    if (!(item instanceof RobotNode.State state)) {
                        return null;
                    }
                    try {
                        URI address = URI.create(state.path());
                        String secondaryText = address.getHost();
                        if (address.getPort() != 80) {
                            secondaryText += ":" + address.getPort();
                        }
                        return secondaryText;
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }

                @Override
                public @Nullable Icon getIcon(@Nullable RobotNode item) {
                    return item != null ? item.icon() : null;
                }

                @Override
                public @Nullable ListSeparator separatorFor(@Nullable RobotNode value) {
                    if (elements.indexOf(value) == 1) {
                        /*
                         * The first node will always be the 'Add Robot' node. The second node will always be the first
                         * detected robot.
                         */
                        return new ListSeparator(RapidBundle.message("robot.connect.dialog.separator"));
                    }
                    return null;
                }
            });
        }
    }
}
