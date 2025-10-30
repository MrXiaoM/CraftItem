plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.0"
    id("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.0")
}
val base = top.mrxiaom.gradle.LibraryHelper(project)

group = "cn.jrmcdp"
version = "2.0.7"

val pluginBaseModules = listOf("library", "paper", "l10n", "actions", "gui", "misc")
val pluginBaseVersion = "1.7.0"
val targetJavaVersion = 8
allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.helpch.at/releases/")
        maven("https://jitpack.io")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }
}
repositories {
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://mvn.lumine.io/repository/maven/")
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")

    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
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

    base.library("com.zaxxer:HikariCP:4.0.3")
    base.library("net.kyori:adventure-api:4.22.0")
    base.library("net.kyori:adventure-platform-bukkit:4.4.0")
    base.library("net.kyori:adventure-text-minimessage:4.22.0")
    base.library("net.kyori:adventure-text-serializer-plain:4.22.0")

    implementation("de.tr7zw:item-nbt-api:2.15.3")
    for (artifact in pluginBaseModules) {
        implementation("top.mrxiaom.pluginbase:$artifact:$pluginBaseVersion")
    }
    implementation("top.mrxiaom:LibrariesResolver-Lite:$pluginBaseVersion")
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

java {
    withSourcesJar()
    withJavadocJar()
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        val lang = JavaLanguageVersion.of(targetJavaVersion)
        toolchain.languageVersion.set(lang)
    }
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}
tasks {
    shadowJar {
        from("LICENSE")
        mapOf(
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "top.mrxiaom.pluginbase" to "base",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "cn.jrmcdp.craftitem.libs.$target")
        }
    }
    val copyTask = create<Copy>("copyBuildArtifact") {
        dependsOn(shadowJar)
        from(shadowJar.get().outputs)
        rename { "CraftItem-$version.jar" }
        into(rootProject.file("out"))
    }
    build {
        dependsOn(copyTask)
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            links("https://hub.spigotmc.org/javadocs/spigot/")

            locale("zh_CN")
            encoding("UTF-8")
            docEncoding("UTF-8")
            addBooleanOption("keywords", true)
            addBooleanOption("Xdoclint:none", true)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenRelease") {
            from(components.getByName("java"))
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
}
