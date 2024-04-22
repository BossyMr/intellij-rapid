import org.jetbrains.changelog.Changelog

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    // Java support
    id("java")
    // IntelliJ IDEA support
    id("idea")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.17.1"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.2.0"
}

sourceSets["main"].java.srcDirs("src/main/gen")

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
    @Suppress("UnstableApiUsage")
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.JETBRAINS
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

configurations {
    all {
        resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
    }
}

dependencies {
    // z3 is used for data flow analysis
    implementation(files("src/main/resources/lib/com.microsoft.z3.jar"))
    // Apache Tika is used to extract external documentation
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("org.apache.tika:tika-parser-microsoft-module:2.9.1")
    // Jsoup is used to reformat external documentation
    implementation("org.jsoup:jsoup:1.17.2")
    // OkHttp is used to communicate with a remote robot
    api("com.squareup.okhttp3:okhttp:5.0.0-alpha.12")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.12")
    // Junit is used for testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    //  Wiremock is used to test network API
    testImplementation("org.wiremock:wiremock:3.4.2")
    // JmDNS is used to discover robots on the local network
    implementation("org.jmdns:jmdns:3.5.9")
    // Sentry is used to report errors
    implementation(platform("io.sentry:sentry-bom:7.8.0"))
    implementation("io.sentry:sentry")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks {
    runIde {
        autoReloadPlugins = false
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    test {
        useJUnitPlatform()
    }

    prepareSandbox {
        from("/src/main/resources/lib/libz3.dll") {
            into("${intellij.pluginName.get()}/lib/")
        }
        from("/src/main/resources/lib/libz3java.dll") {
            into("${intellij.pluginName.get()}/lib/")
        }
        from("/src/main/resources/lib/libz3.so") {
            into("${intellij.pluginName.get()}/lib/")
        }
        from("/src/main/resources/lib/libz3java.so") {
            into("${intellij.pluginName.get()}/lib/")
        }
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
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
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels =
            properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
    }
}