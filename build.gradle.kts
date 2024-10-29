import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    // Intellij Platform Gradle Plugin
    id("org.jetbrains.intellij.platform") version "2.1.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.2.0"
}

sourceSets {
    main {
        java {
            srcDir("src/main/gen")
        }
    }
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

// Configure project's dependencies
repositories {
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        instrumentationTools()
        pluginVerifier()
        zipSigner()

        testFramework(TestFrameworkType.JUnit5)
        testFramework(TestFrameworkType.Platform)
    }

    // Annotations used for static analysis
    compileOnly("org.jetbrains:annotations:26.0.1")
    // z3 is used for data flow analysis
    implementation(files("src/main/resources/lib/com.microsoft.z3.jar"))
    // Apache Tika is used to extract external documentation
    implementation("org.apache.tika:tika-core:2.9.2")
    implementation("org.apache.tika:tika-parser-microsoft-module:2.9.2")
    // Jsoup is used to reformat external documentation
    implementation("org.jsoup:jsoup:1.18.1")
    // JmDNS is used to discover robots on the local network
    implementation("org.jmdns:jmdns:3.5.9")
    // Sentry is used to report errors
    implementation(platform("io.sentry:sentry-bom:7.8.0"))
    implementation("io.sentry:sentry")
    // Junit is used for testing
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("junit:junit:4.13.2")
    //  Wiremock is used to test network API
    testImplementation("org.wiremock:wiremock:3.5.3")
}


// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

tasks {
    test {
        useJUnitPlatform()
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    val action: PrepareSandboxTask.() -> Unit = {
        from(layout.projectDirectory.file("src/main/resources/lib/libz3.dll")) {
            into(pluginName.map { "$it/lib" })
        }
        from(layout.projectDirectory.file("src/main/resources/lib/libz3java.dll")) {
            into(pluginName.map { "$it/lib" })
        }
        from(layout.projectDirectory.file("src/main/resources/lib/libz3.so")) {
            into(pluginName.map { "$it/lib" })
        }
        from(layout.projectDirectory.file("src/main/resources/lib/libz3java.so")) {
            into(pluginName.map { "$it/lib" })
        }
    }

    prepareSandbox(action)
    prepareTestSandbox(action)

    publishPlugin {
        dependsOn(patchChangelog)
    }
}

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders += CommandLineArgumentProvider {
                    listOf(
                        "-Drobot-server.port=8082",
                        "-Dide.mac.message.dialogs.as.sheets=false",
                        "-Djb.privacy.policy.text=<!--999.999-->",
                        "-Djb.consents.confirmation.enabled=false",
                    )
                }
            }

            plugins {
                robotServerPlugin()
            }
        }
    }
}