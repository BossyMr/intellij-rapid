plugins {
    id("java")
}

group = "com.bossymr"
version = "com.bossymr"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("io.github.rburgst:okhttp-digest:2.7")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.4")

    implementation("org.jetbrains:annotations:23.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}