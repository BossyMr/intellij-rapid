package com.bossymr.rapid.ide.execution.configurations

import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.XCollection

class TaskState() : BaseState() {

    constructor(name: String?, isEnabled: Boolean, moduleName: String?) : this() {
        this.name = name
        this.isEnabled = isEnabled
        this.moduleName = moduleName
    }

    @get:Attribute("name")
    var name by string()

    @get:Attribute("enabled")
    var isEnabled by property(true)

    @get:Attribute("module")
    var moduleName by string()
}

class RapidRunConfigurationOptions : LocatableRunConfigurationOptions() {

    @get:Property(surroundWithTag = false)
    @get:XCollection
    var robotTasks by list<TaskState>()

    @get:Attribute("path")
    var robotPath by string("")

}