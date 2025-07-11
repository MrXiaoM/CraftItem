plugins {
    java
}

val targetJavaVersion = 8
repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        mavenContent { includeGroup("com.destroystokyo.paper") }
    }
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("top.mrxiaom:PluginBase:1.4.8")
    compileOnly("net.kyori:adventure-api:4.22.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.22.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}
