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

/**
 * A RobotConnectView is a dialog panel used to connect to a remote robot.
 */
class RobotConnectView @JvmOverloads constructor(val project: Project, val path: URI? = null) :
    DialogWrapper(project, false) {

    private val panel = RobotConnectPanel()

    init {
        if (path != null) {
            panel.host = path;
        }
        title = RapidBundle.message("robot.connect.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel
    }

    override fun doValidateAll(): MutableList<ValidationInfo> {
        return panel.validationInfo
    }

    override fun doOKAction() {
        super.doOKAction()
        val path = panel.host
        val credentials = panel.credentials
        object : Backgroundable(project, RapidBundle.message("robot.connect.progress.indicator.title", path)) {
            override fun run(indicator: ProgressIndicator) {
                RobotService.getInstance().connect(path, credentials)
            }

            override fun onThrowable(error: Throwable) = Unit
        }.queue()
    }

    override fun dispose() {
        panel.close()
        super.dispose()
    }
}