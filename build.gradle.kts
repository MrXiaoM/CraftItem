plugins {
    java
    `maven-publish`
    id ("com.github.johnrengelman.shadow") version "7.0.0"
}

val targetJavaVersion = 8
allprojects {
    group = "cn.jrmcdp"
    version = "1.1.3"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}

fun DependencyHandlerScope.impl(dependencyNotation: Any): Dependency? {
    return implementation(dependencyNotation)
}
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")

    impl("net.kyori:adventure-api:4.15.0")
    impl("net.kyori:adventure-platform-bukkit:4.3.2")
    impl("net.kyori:adventure-text-minimessage:4.14.0")
    impl("net.objecthunter:exp4j:0.4.8")
    impl(project(":paper"))
}

java {
    withSourcesJar()
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
    shadowJar {
        from("LICENSE")
        archiveClassifier.set("")
        mapOf(
            "net.kyori" to "kyori",
            "net.objecthunter.exp4j" to "exp4j"
        ).forEach { (original, target) ->
            relocate(original, "cn.jrmcdp.craftitem.utils.$target")
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
    }

    register<Jar>("javadocJar") {
        dependsOn(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc.get().destinationDir)
    }
    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            links("https://docs.oracle.com/javase/8/docs/api/")
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

            artifact(tasks.getByName("javadocJar"))
        }
    }
}
