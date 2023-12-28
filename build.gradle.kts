fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // IntelliJ IDEA support
    id("idea")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.16.1"
    // Gradle GrammarKit Plugin
    id("org.jetbrains.grammarkit") version "2022.3.1"
    // Gradle Sentry Plugin
    id("io.sentry.jvm.gradle") version "3.12.0"
}

sourceSets["main"].java.srcDirs("src/main/gen")
sourceSets["main"].java.srcDirs("src/main/grammar")

group = properties("pluginGroup")
version = properties("pluginGroup")

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
    jvmToolchain(17)
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

configurations {
    all {
        resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
    }
}

dependencies {
    implementation(project(mapOf("path" to ":network")))
    implementation("org.slf4j:slf4j-jdk14:2.0.5")
    implementation(files("src/main/resources/lib/com.microsoft.z3.jar"))
    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.9.0")
    testImplementation("org.junit.platform:junit-platform-launcher:1.9.0")
}

sentry {
    // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
    // This enables source context, allowing you to see your source
    // code as part of your stack traces in Sentry.
    includeSourceContext = true

    org = "sentry"
    projectName = "intellij-rapid"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    test {
        useJUnitPlatform()
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
    }

    // Configure GrammarKit plugin
    // Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-grammar-kit-plugin.html
    generateLexer {
        sourceFile.set(file("src/main/grammar/Rapid.flex"))
        targetDir.set("src/main/gen/com/bossymr/rapid/language/lexer")
        targetClass.set("_RapidLexer")
        purgeOldFiles.set(true)
    }

    // Configure GrammarKit plugin
    // Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-grammar-kit-plugin.html
    generateParser {
        sourceFile.set(file("src/main/grammar/Rapid.bnf"))
        targetRoot.set("src/main/gen")
        pathToParser.set("/com/bossymr/rapid/language/parser/RapidParser.java")
        pathToPsiRoot.set("/com/bossymr/rapid/language/psi")
        purgeOldFiles.set(true)
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}