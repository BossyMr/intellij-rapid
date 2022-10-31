fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.9.0"
    id("org.jetbrains.grammarkit") version "2021.2.2"
    id("org.jetbrains.changelog") version "1.3.1"
    id("org.jetbrains.qodana") version "0.1.12"
}

sourceSets["main"].java.srcDirs("src/main/gen")

group = properties("pluginGroup")
version = properties("pluginGroup")

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    plugins.set(listOf("com.intellij.java"))
}

configurations {
    all {
        resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("io.github.rburgst:okhttp-digest:2.7")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.4")
}

// Configure Gradle Changelog Plugin
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// Configure Gradle Qodana Plugin
// Read more: https://github.com/JetBrains/gradle-qodana-plugin

tasks {
    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        changeNotes.set(provider {
            changelog.run {
                getOrNull(properties("pluginVersion")) ?: getLatest()
            }.toHTML()
        })
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }

    generateLexer {
        source.set("src/main/grammar/Rapid.flex")
        targetDir.set("src/main/gen/com/bossymr/rapid/language/lexer")
        targetClass.set("_RapidLexer")
        purgeOldFiles.set(true)
    }

    generateParser {
        source.set("src/main/grammar/Rapid.bnf")
        targetRoot.set("src/main/gen")
        pathToParser.set("/com/bossymr/rapid/language/parser/RapidParser.java")
        pathToPsiRoot.set("/com/bossymr/rapid/language/psi")
        purgeOldFiles.set(true)
    }
}