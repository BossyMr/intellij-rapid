plugins {
    id("org.jetbrains.intellij") version "1.7.0"
    id("org.jetbrains.grammarkit") version "2021.2.2"
}

sourceSets["main"].java.srcDirs("src/main/gen")

group = "io.github.bossymr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

idea {
    module {
        generatedSourceDirs.add(file("src/main/gen"))
    }
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

    generateLexer {
        source.set("src/main/java/io/github/bossymr/language/grammar/Rapid.flex")
        targetDir.set("src/main/gen/io/github/bossymr/language/lexer")
        targetClass.set("_RapidLexer")
        purgeOldFiles.set(true)
    }

    generateParser {
        source.set("src/main/java/io/github/bossymr/language/grammar/Rapid.bnf")
        targetRoot.set("src/main/gen")
        pathToParser.set("/io/github/bossymr/language/parser/RustParser.java")
        pathToPsiRoot.set("/io/github/bossymr/language/psi")
        purgeOldFiles.set(true)
    }
}
tasks.getByName<Test>("test") {
    useJUnitPlatform()
}