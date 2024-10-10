val minecraft = "1.20.1"
extra["minecraft"] = minecraft

apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/common.gradle.kts")

apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/kotlin.gradle.kts")

apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/fabric.gradle.kts")

apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/modmenu.gradle.kts")
apply("https://github.com/SettingDust/MinecraftGradleScripts/raw/main/yacl.gradle.kts")

dependencyResolutionManagement.versionCatalogs.named("catalog") {
    // https://modrinth.com/mod/reputation-viewer/versions
    library("reputation", "maven.modrinth", "reputation-viewer").version("0.4.2")
    // https://modrinth.com/mod/jade/versions
    library("jade", "maven.modrinth", "jade").version("11.12.0+fabric")

    // https://modrinth.com/mod/guard-villagers-(fabricquilt)/versions
    library("guard-villagers", "maven.modrinth", "guard-villagers-(fabricquilt)")
        .version("2.0.9-$minecraft")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val name: String by settings

rootProject.name = name
