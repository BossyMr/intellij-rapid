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
    implementation("org.slf4j:slf4j-api:2.0.12")
    api("com.squareup.okhttp3:okhttp:5.0.0-alpha.12")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    testImplementation("org.wiremock:wiremock:3.3.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.slf4j:slf4j-simple:2.0.12")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    implementation("org.apache.tika:tika-core:2.9.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}