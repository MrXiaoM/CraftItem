plugins {
    java
    `maven-publish`
    id("top.mrxiaom.shadow")
}

group = "cn.jrmcdp"
version = "2.0.4"

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

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")

    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MascusJeoraly:LanguageUtils:1.9")

    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.slf4j:slf4j-nop:2.0.16")
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("net.kyori:adventure-api:4.22.0")
    implementation("net.kyori:adventure-platform-bukkit:4.4.0")
    implementation("net.kyori:adventure-text-minimessage:4.22.0")
    implementation("de.tr7zw:item-nbt-api:2.15.1-SNAPSHOT")
    implementation("com.github.technicallycoded:FoliaLib:0.4.4")
    implementation("top.mrxiaom:PluginBase:1.5.0")
    implementation(project(":paper"))
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
    build {
        dependsOn(shadowJar)
    }
    jar {
        archiveClassifier.set("api")
    }
    shadowJar {
        from("LICENSE")
        archiveClassifier.set("")
        destinationDirectory.set(rootProject.file("out"))
        mapOf(
            "org.intellij.lang.annotations" to "annotations.intellij",
            "org.jetbrains.annotations" to "annotations.jetbrains",
            "com.zaxxer.hikari" to "hikari",
            "org.slf4j" to "slf4j",
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "top.mrxiaom.pluginbase" to "base",
            "com.tcoded.folialib" to "folialib",
            "net.kyori" to "kyori",
        ).forEach { (original, target) ->
            relocate(original, "cn.jrmcdp.craftitem.libs.$target")
        }
        ignoreRelocations("cn/jrmcdp/craftitem/utils/PaperInventoryFactory.class")
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
