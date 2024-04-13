package com.bossymr.rapid.robot.ui

import com.bossymr.rapid.RapidBundle
import com.bossymr.rapid.RapidIcons
import com.bossymr.rapid.robot.RobotService
import com.bossymr.rapid.robot.api.client.security.Credentials
import com.intellij.icons.AllIcons
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selectedValueIs
import com.intellij.ui.layout.selectedValueMatches
import java.io.Closeable
import java.net.URI
import javax.swing.Icon

class RobotConnectPanel : Closeable {

    private lateinit var robotComboBox: RobotComboBox

    private lateinit var host: JBTextField

    private lateinit var port: JBTextField

    private lateinit var authenticationComboBox: ComboBox<AuthenticationType>

    private lateinit var username: JBTextField

    private lateinit var password: JBPasswordField

    private lateinit var rememberPassword: JBCheckBox

    val panel: DialogPanel = panel {
        row(RapidBundle.message("robot.connect.dialog.robot")) {
            robotComboBox = cell(RobotComboBox())
                .align(AlignX.FILL)
                .component
        }
        rowsRange {
            row(RapidBundle.message("robot.connect.host.label")) {
                host = textField()
                    .validationOnInput {
                        if(it.text.isNullOrBlank()) {
                            ValidationInfo(RapidBundle.message("robot.connect.host.empty"), it)
                        } else {
                            try {
                                URI("http", null, it.text, 80, null, null, null)
                                null
                            } catch (e: Throwable) {
                                ValidationInfo(RapidBundle.message("robot.connect.host.invalid"), it)
                            }
                        }
                    }
                    .align(AlignX.FILL)
                    .component
            }
            row(RapidBundle.message("robot.connect.host.port")) {
                port = intTextField(0..65535)
                    .component
            }
        }.visibleIf(robotComboBox.selectedValueMatches { it is Action })
        row(RapidBundle.message("robot.connect.authentication.type")) {
            authenticationComboBox = comboBox(CollectionComboBoxModel(AuthenticationType.entries))
                .align(AlignX.FILL)
                .component
        }
        rowsRange {
            row(RapidBundle.message("robot.connect.authentication.username")) {
                username = textField()
                    .component
            }
            row(RapidBundle.message("robot.connect.authentication.password")) {
                password = passwordField()
                    .component
            }
            row {
                rememberPassword = checkBox(RapidBundle.message("robot.connect.authentication.remember"))
                    .component
            }.layout(RowLayout.LABEL_ALIGNED)
        }.visibleIf(authenticationComboBox.selectedValueIs(AuthenticationType.PASSWORD))
    }

    fun onModified(callback: (URI?, Credentials) -> Unit) {
        var previousEntry = applyToSnapshot()
        panel.addPropertyChangeListener {
            val currentEntry = applyToSnapshot()
            if(currentEntry != previousEntry) {
                previousEntry = currentEntry
                callback.invoke(currentEntry.host, currentEntry.credentials)
            }
        }
    }

    fun validate(): List<ValidationInfo> {
        return panel.validateAll()
    }

    fun isModified(): Boolean {
        return panel.isModified()
    }

    fun apply(): Entry {
        if(authenticationComboBox.selectedItem === AuthenticationType.PASSWORD) {
            PasswordSafe.instance.isRememberPasswordByDefault = rememberPassword.isSelected
        }
        return applyToSnapshot()
    }

    fun applyToSnapshot(): Entry {
        val item = robotComboBox.selectedItem
        val host = if (item is State) {
            item.path
        } else {
            URI("http", null, host.text, Integer.parseInt(port.text), null, null, null)
        }
        val credentials = if (authenticationComboBox.selectedItem === AuthenticationType.DEFAULT) {
            RobotService.DEFAULT_CREDENTIALS
        } else {
            Credentials(username.text, password.password)
        }
        return Entry(host, credentials)
    }

    fun reset(entry: Entry) {
        panel.reset()
        robotComboBox.selectedItem = robotComboBox.getItemAt(0)
        host.text = entry.host?.host
        port.text = if(entry.host != null) { entry.host.port.toString() } else { "80" }
        if (entry.credentials === RobotService.DEFAULT_CREDENTIALS) {
            authenticationComboBox.selectedItem = AuthenticationType.DEFAULT
        } else {
            authenticationComboBox.selectedItem = AuthenticationType.PASSWORD
            username.text = entry.credentials.username
            password.text = null
            password.setPasswordIsStored(true)
        }
        rememberPassword.isSelected = PasswordSafe.instance.isRememberPasswordByDefault
    }

    override fun close() {
        robotComboBox.close()
    }

    data class Entry(val host: URI?, val credentials: Credentials)

    interface Node {

        val icon: Icon

        val name: String

    }

    class State(override val name: String, val path: URI) : Node {

        override val icon: Icon = RapidIcons.ROBOT

    }

    class Action : Node {

        override val icon: Icon = AllIcons.General.Add

        override val name: String = RapidBundle.message("robot.connect.dialog.manual")

    }
}