fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // IntelliJ IDEA support
    id("idea")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.17.1"
    // Gradle Sentry Plugin
    id("io.sentry.jvm.gradle") version "4.2.0"
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
    implementation("org.slf4j:slf4j-jdk14:2.0.12")
    implementation(files("src/main/resources/lib/com.microsoft.z3.jar"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.2")
}

sentry {
    // Enables more detailed log output, e.g. for sentry-cli.
    //
    // Default is false.
    debug.set(false)

    // Generates a source bundle and uploads it to Sentry.
    // This enables source context, allowing you to see your source
    // code as part of your stack traces in Sentry.
    //
    // Default is disabled. To enable, see the source context guide.
    includeSourceContext.set(false)

    // Includes additional source directories into the source bundle.
    // These directories are resolved relative to the project directory.
    // additionalSourceDirsForSourceContext.set(setOf("src/main/java"))

    // Disables or enables dependencies metadata reporting for Sentry.
    // If enabled, the plugin will collect external dependencies and
    // upload them to Sentry as part of events. If disabled, all the logic
    // related to the dependencies metadata report will be excluded.
    //
    // Default is enabled.
    includeDependenciesReport.set(true)

    // Includes additional source directories into the source bundle.
    // These directories are resolved relative to the project directory.
    additionalSourceDirsForSourceContext.set(setOf("src/main/java", "network/src/main/java"))

    org.set("sentry")
    projectName.set("intellij-rapid")

    // Automatically adds Sentry dependencies to your project.
    autoInstallation {
        enabled.set(true)
    }
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