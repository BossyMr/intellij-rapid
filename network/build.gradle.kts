plugins {
    id("java")
}

group = "com.bossymr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("org.jetbrains:annotations:26.0.1")
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.wiremock:wiremock:3.9.2")
}

tasks {
    test {
        useJUnitPlatform()
    }
}