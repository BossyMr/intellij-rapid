plugins {
    id("org.jetbrains.intellij") version "1.6.0"
    java
}

group = "io.github.bossymr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2021.3.2")
    plugins.set(listOf("com.intellij.java"))
}
tasks {
    patchPluginXml {
        changeNotes.set("""""".trimIndent())
    }
}
tasks.getByName<Test>("test") {
    useJUnitPlatform()
}