package com.bossymr.rapid.robot.ui

import com.bossymr.rapid.RapidBundle
import com.bossymr.rapid.robot.RapidRobot
import com.bossymr.rapid.robot.RobotService
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import java.net.URI
import javax.swing.JComponent

/**
 * A RobotConnectView is a dialog panel used to connect to a remote robot.
 */
class RobotConnectView @JvmOverloads constructor(val project: Project, val path: URI? = null) :
    DialogWrapper(project, false) {

    private val panel = RobotConnectPanel()

    init {
        val model = RobotConnectPanel.Model(path, RobotService.DEFAULT_CREDENTIALS)
        panel.reset(model)
        title = RapidBundle.message("robot.connect.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel.panel
    }

    override fun doValidateAll(): MutableList<ValidationInfo> {
        return panel.validate().toMutableList()
    }

    override fun doOKAction() {
        super.doOKAction()
        val (path, credentials) = panel.apply()
        if (path == null || credentials == null) {
            return
        }
        object : Backgroundable(project, RapidBundle.message("robot.connect.progress.indicator.title", path)) {
            override fun run(indicator: ProgressIndicator) {
                RapidRobot.setCredentials(path, credentials);
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