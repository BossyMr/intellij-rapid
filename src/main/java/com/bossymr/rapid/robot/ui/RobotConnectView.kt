package com.bossymr.rapid.robot.ui

import com.bossymr.rapid.RapidBundle
import com.bossymr.rapid.RapidIcons
import com.bossymr.rapid.robot.RobotService
import com.bossymr.rapid.robot.api.client.security.Credentials
import com.intellij.icons.AllIcons
import com.intellij.ide.ui.laf.darcula.ui.DarculaJBPopupComboPopup.USE_LIVE_UPDATE_MODEL
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.GroupedComboBoxRenderer
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selectedValueIs
import com.intellij.ui.layout.selectedValueMatches
import java.io.IOException
import java.net.NetworkInterface
import java.net.URI
 import java.net.URISyntaxException
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import javax.swing.ComboBoxModel
import javax.swing.Icon
import javax.swing.JComponent

private const val PREFIX = "RobotWebServices_"

/**
 * A RobotConnectView is a dialog panel used to connect to a remote robot.
 */
class RobotConnectView @JvmOverloads constructor(val project: Project, val path: URI? = null) :
    DialogWrapper(project, false) {

    private var disposed = false

    private val scanners: MutableList<JmDNS> = arrayListOf()

    private lateinit var comboBox: ComboBox<RobotNode>

    private val nodes = arrayListOf<RobotNode>()

    private val model: Model

    init {
        val connectNode = RobotConnectNode(RapidBundle.message("robot.connect.dialog.manual"))
        nodes.add(connectNode)
        model = Model(connectNode)
        if (path != null) {
            model.host = path.host
            model.port = path.port
        }
        title = RapidBundle.message("robot.connect.dialog.title")
        setupDiscovery()
        init()
    }

    private fun syncModel() {
        /*
         * Create a new model and swap the used model. This is required in order to refresh the combobox immediately
         * (it doesn't work if the model is updated, i.e. if an element is added to the model).
         */
        val model = CollectionComboBoxModel(nodes)
        model.selectedItem = comboBox.model.selectedItem
        comboBox.model = model
    }

    private fun setupDiscovery() {
        ApplicationManager.getApplication().executeOnPooledThread {
            val addresses = NetworkInterface.networkInterfaces()
                .flatMap(NetworkInterface::inetAddresses)
                .toList()
            for (inetAddress in addresses) {
                if (inetAddress.isLoopbackAddress) {
                    continue
                }
                if(disposed) {
                    break
                }
                val jmDNS = try {
                    JmDNS.create(inetAddress)
                } catch (e: IOException) {
                    continue
                }
                if(disposed) {
                    jmDNS.close()
                    break
                }
                scanners.add(jmDNS)
                jmDNS.addServiceListener("_http._tcp.local.", object : ServiceListener {

                    /*
                     * A map containing all robots detected by this scanner instance. The key is computed as:
                     * [type] '.' [name]. A service might be resolved more than once, in that case, as the state is
                     * already stored in this map, it can be ignored.
                     */
                    val states: HashMap<String, RobotStateNode> = HashMap()

                    /*
                     * We don't do anything when a new service is found since it hasn't been resolved yet. If we were
                     * to add it to the model immediately, the model would need to be updated two times. Once when the
                     *  service is added and once more when an IP-address is associated with the service.
                     */
                    override fun serviceAdded(event: ServiceEvent) = Unit

                    override fun serviceRemoved(event: ServiceEvent) {
                        val state = states.remove(event.name + "." + event.type) ?: return
                        nodes.remove(state)
                        syncModel()
                    }

                    override fun serviceResolved(event: ServiceEvent) {
                        if (!event.name.startsWith(PREFIX) || states.containsKey(event.name + "." + event.type)) return
                        val serviceInfo = event.dns.getServiceInfo(event.type, event.name)
                        val paths = serviceInfo.urLs
                        if (paths.isEmpty()) return
                        val name = event.name.substring(PREFIX.length)
                        val state = RobotStateNode(name, paths[0])
                        states[event.name + "." + event.type] = state
                        nodes.add(state)
                        syncModel()
                    }
                })
            }
        }
    }

    private fun disposeDiscovery() {
        ApplicationManager.getApplication().executeOnPooledThread {
            disposed = true
            for (jmDNS in scanners) {
                jmDNS.close()
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row(RapidBundle.message("robot.connect.dialog.robot")) {
                comboBox = cell(RobotComboBox(CollectionComboBoxModel(nodes), nodes))
                    .bindItem(model::node.toNullableProperty())
                    .component
            }
            row(RapidBundle.message("robot.connect.host.label")) {
                textField()
                    .bindText(model::host)
                    .validationOnApply { textField ->
                        if(textField.text.isNullOrBlank()) {
                            return@validationOnApply ValidationInfo(RapidBundle.message("robot.connect.host.invalid"))
                        }
                        try {
                            URI("http", null, textField.text.trim(), 80, null, null, null)
                            null
                        } catch (e: URISyntaxException) {
                            return@validationOnApply ValidationInfo(RapidBundle.message("robot.connect.host.invalid"))
                        }
                    }
            }.visibleIf(comboBox.selectedValueMatches { it is RobotConnectNode })
            row(RapidBundle.message("robot.connect.host.port")) {
                intTextField(IntRange(0, 65535))
                    .bindIntText(model::port)
            }.visibleIf(comboBox.selectedValueMatches { it is RobotConnectNode })
            lateinit var authenticationComboBox: ComboBox<AuthenticationType>
            row(RapidBundle.message("robot.connect.authentication.type")) {
                authenticationComboBox =
                    comboBox(AuthenticationType.entries, SimpleListCellRenderer.create("") { it.displayName })
                        .bindItem(model::authenticationType.toNullableProperty())
                        .component
            }
            row(RapidBundle.message("robot.connect.authentication.username")) {
                textField()
                    .bindText(model::username)
            }.visibleIf(authenticationComboBox.selectedValueIs(AuthenticationType.PASSWORD))
            row(RapidBundle.message("robot.connect.authentication.password")) {
                passwordField()
                    .bindText(model::password)
            }.visibleIf(authenticationComboBox.selectedValueIs(AuthenticationType.PASSWORD))
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        val node = model.node
        /*
         * Calculate the actual host path.
         */
        val path: URI = try {
            if (node is RobotStateNode) {
                URI(node.path)
            } else {
                URI("http", null, model.host.trim(), model.port, null, null, null)
            }
        } catch (e: URISyntaxException) {
            return
        }
        /*
         * Calculate the actual credentials.
         */
        val credentials = if (model.authenticationType == AuthenticationType.DEFAULT) {
            RobotService.DEFAULT_CREDENTIALS
        } else {
            Credentials(model.username, model.password.toCharArray())
        }
        object : Backgroundable(project, RapidBundle.message("robot.connect.progress.indicator.title", path)) {
            override fun run(indicator: ProgressIndicator) {
                RobotService.getInstance().connect(path, credentials)
            }

            override fun onThrowable(error: Throwable) = Unit
        }.queue()
    }

    override fun dispose() {
        disposeDiscovery()
        super.dispose()
    }
}

private class RobotComboBox(model: ComboBoxModel<RobotNode>, nodes: ArrayList<RobotNode>) : ComboBox<RobotNode>(model) {

    init {
        /*
         * Use the IntelliJ popup instead of the default popup.
         */
        isSwingPopup = false
        putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
        /*
         * Refresh the popup immediately. Therefore, one does not need to close the combobox for it to refresh.
         */
        @Suppress("UnstableApiUsage")
        putClientProperty(USE_LIVE_UPDATE_MODEL, true)
        renderer = object : GroupedComboBoxRenderer<RobotNode?>() {

            override fun getText(item: RobotNode?): String = item?.name ?: ""

            override fun getSecondaryText(item: RobotNode?): String? {
                val address = (item as? RobotStateNode)?.path ?: return null
                val path = URI.create(address)
                var secondaryText = path.host
                if (path.port != 80) {
                    secondaryText += ":" + path.port
                }
                return secondaryText
            }

            override fun getIcon(item: RobotNode?): Icon? {
                return when (item) {
                    is RobotStateNode -> RapidIcons.ROBOT
                    else -> AllIcons.General.Add
                }
            }

            override fun separatorFor(value: RobotNode?): ListSeparator? {
                if (nodes.indexOf(value) == 1) {
                    /*
                     * The first node will always be the 'Add Robot' node. The second node will always be the first
                     * detected robot.
                     */
                    return ListSeparator(RapidBundle.message("robot.connect.dialog.separator"))
                }
                return null
            }
        }
    }
}

private data class Model(
    var node: RobotNode,
    var host: String = "",
    var port: Int = 80,
    var authenticationType: AuthenticationType = AuthenticationType.DEFAULT,
    var username: String = "",
    var password: String = ""
)

private sealed interface RobotNode {
    val name: String
}

private data class RobotStateNode(override val name: String, var path: String) : RobotNode

private data class RobotConnectNode(override val name: String) : RobotNode
