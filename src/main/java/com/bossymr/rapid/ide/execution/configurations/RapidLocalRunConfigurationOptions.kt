package com.bossymr.rapid.ide.execution.configurations

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.execution.configurations.ModuleBasedConfigurationOptions
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Transient

class TaskState() : BaseState() {

    constructor(name: String?, isEnabled: Boolean, moduleName: String?) : this() {
        this.name = name
        this.isEnabled = isEnabled
        this.moduleName = moduleName
    }

    var name by string()

    var isEnabled by property(true)

    var moduleName by string()
}

class RapidRunConfigurationOptions : ModuleBasedConfigurationOptions() {

    var tasks by list<TaskState>()

    var path by string("")

    // If the username is null, the authentication type is equal to RobotService#DEFAULT_CREDENTIALS
    var username by string(null)

    @Transient
    private var password: String? = null

    @Transient
    fun getPassword(): String? {
        if(password != null) {
            return password
        }
        val serviceName = generateServiceName("intellij-rapid", path!!)
        val attributes = CredentialAttributes(serviceName, username)
        return PasswordSafe.instance.getPassword(attributes)
    }

    fun setPassword(password: String) {
        if (PasswordSafe.instance.isRememberPasswordByDefault) {
            this.password = null
            val serviceName = generateServiceName("intellij-rapid", path!!)
            val attributes = CredentialAttributes(serviceName, username)
            PasswordSafe.instance.set(attributes, Credentials(username, password))
        } else {
            this.password = password
        }
    }
}