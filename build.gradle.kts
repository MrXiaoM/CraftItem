plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.17")
}
val base = top.mrxiaom.gradle.LibraryHelper(project)

group = "cn.jrmcdp"
version = "2.1.1"

val pluginBaseModules = base.modules.run{ listOf(library, paper, l10n, actions, gui, misc) }
val targetJavaVersion = 8
allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.helpch.at/releases/")
        maven("https://jitpack.io")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
        maven("https://repo.momirealms.net/releases/")
    }
}
repositories {
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://mvn.lumine.io/repository/maven/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly(base.depend.annotations)

    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("org.black_ixx:playerpoints:3.2.7")
    compileOnly(files("libs/MPoints-1.2.2.jar"))
    compileOnly("com.github.nulli0n:ExcellentEconomy:c32f037025") // CoinsEngine
    compileOnly("com.github.blank038:NyEconomy:8e3f27c18f")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MascusJeoraly:LanguageUtils:1.9")

    // MMOItems
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10-SNAPSHOT")
    // MythicMobs
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.6.2")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")
    // ItemsAdder
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
    // CustomFishing
    compileOnly("net.momirealms:custom-fishing:2.3.3")
    // CraftEngine
    compileOnly("net.momirealms:craft-engine-core:0.0.67")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.67")

    base.library(base.depend.HikariCP)
    base.library("net.kyori:adventure-api:4.22.0")
    base.library("net.kyori:adventure-platform-bukkit:4.4.0")
    base.library("net.kyori:adventure-text-minimessage:4.22.0")
    base.library("net.kyori:adventure-text-serializer-plain:4.22.0")

    implementation(base.depend.nbtapi)
    for (artifact in pluginBaseModules) {
        implementation(artifact)
    }
    implementation(base.resolver.lite)
    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    implementation(project(":paper"))
}

buildConfig {
    className("BuildConstants")
    packageName("cn.jrmcdp.craftitem")

    base.doResolveLibraries()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}

top.mrxiaom.gradle.LibraryHelper.initJava(project, base, targetJavaVersion, true)
top.mrxiaom.gradle.LibraryHelper.initPublishing(project)

tasks {
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        from("LICENSE")
        mapOf(
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "top.mrxiaom.pluginbase" to "base",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "cn.jrmcdp.craftitem.libs.$target")
        }
    }
}
