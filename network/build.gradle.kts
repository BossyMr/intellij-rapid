plugins {
    `java-library`
}

group = "com.bossymr.rapid"
version = "com.bossymr"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("org.slf4j:slf4j-api:2.0.5")
    api("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.slf4j:slf4j-simple:2.0.5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}