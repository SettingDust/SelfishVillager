import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    idea
    alias(catalog.plugins.idea.ext)

    alias(catalog.plugins.spotless)

    alias(catalog.plugins.semver)
}

group = "settingdust.selfishvillager"

version = semver.semVersion.toString()

allprojects { repositories { mavenCentral() } }

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        exclusiveContent {
            forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
            filter { includeGroup("maven.modrinth") }
        }
        maven("https://maven.terraformersmc.com/releases") {
            content { includeGroup("com.terraformersmc") }
        }
        maven("https://maven.isxander.dev/releases")
    }
}

spotless {
    java {
        target("*/src/**/*.java")
        palantirJavaFormat()
    }

    kotlin {
        target("*/src/**/*.kt", "*/*.gradle.kts", "*.gradle.kts")
        ktfmt().kotlinlangStyle()
    }
}

idea.project.settings.taskTriggers { afterSync(":forge:genIntellijRuns") }
