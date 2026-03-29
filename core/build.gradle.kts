import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Scanner
import org.apache.tools.ant.filters.ReplaceTokens
import proguard.gradle.ProGuardTask
import java.io.ByteArrayOutputStream
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.7.0")
    }
}
plugins {
    kotlin("plugin.lombok") version "2.1.20"
    id("io.freefair.lombok") version "8.10"
    kotlin("jvm") version "2.1.20"
    alias(libs.plugins.shadow)
}

group = "top.mcrw"

version = "V"
repositories {
    maven("https://maven.aliyun.com/repository/public/") {
        content {
            includeGroupByRegex(".*")
        }
    }
    mavenCentral()
    maven("https://maven.cleanroommc.com")
    maven("https://repo.crazycrew.us/releases")
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.inventivetalent.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.panda-lang.org/releases")
}
val gitVersion = rootProject.extra["gitVersionString"].toString()

tasks.register<ProGuardTask>("proguard") {
    configuration(file("proguard.pro"))

    injars(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    if (System.getProperty("java.version").startsWith("1.")) {
        libraryjars(file("${System.getProperty("java.home")}/lib/rt.jar"))
    } else {
        libraryjars(file("${System.getProperty("java.home")}/jmods/java.base.jmod"))
    }
    repositories
    libraryjars(configurations.compileClasspath)

    verbose()

    outjars(layout.buildDirectory.file("libs/proguard-tpu-minified.jar"))
}

tasks.named<ShadowJar>("shadowJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("ThePitGay-$version" + ".jar")
    exclude("META-INF/**")

    exclude("org.hamcrest")

    exclude("org.intellij")

    exclude("org.jetbrains")
    relocate("pku.yim.license", "net.mizukilab.pit.license")
    relocate("panda", "net.mizukilab.pit.libs")
    relocate("dev.rollczi", "net.mizukilab.pit.libs")
    relocate("cn.hutool", "net.mizukilab.pit.libs")
    relocate("net.kyori", "net.mizukilab.pit.libs")
    relocate("net.jodah", "net.mizukilab.pit.libs")
    relocate("net.jitse", "net.mizukilab.pit.libs")
    relocate("xyz.upperlevel.spigot", "net.mizukilab.pit.libs")
    exclude("kotlin/**", "junit/**", "org/junit/**")
    from("build/tmp/processed-resources")
    mergeServiceFiles()
}
dependencies {

    compileOnly(fileTree("../packLib"))
    compileOnly(fileTree(mapOf("dir" to "../libs", "include" to listOf("*.jar"))))
    implementation(libs.reflectionhelper)

    implementation(libs.hutool.crypto)
    implementation(libs.book)
    compileOnly(libs.slf4j)
    implementation(libs.litecommands)
    implementation("zone.rong:imaginebreaker:2.1")
    compileOnly(libs.adventure.bukkit)
    compileOnly("com.caoccao.javet:javet:3.1.4")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    compileOnly(libs.luckperms)
    compileOnly("com.github.f4b6a3:uuid-creator:6.0.0")
    compileOnly(libs.papi)
    compileOnly(libs.narshorn)
    compileOnly(libs.protocollib)
    compileOnly(libs.jedis)
    compileOnly("org.mongojack:mongojack:5.0.1")
    compileOnly("org.mongodb:mongodb-driver-sync:5.2.0")

    compileOnly(fileTree("libs"))

    compileOnly(libs.fastutil)
    compileOnly("us.crazycrew.crazycrates:api:0.7")
    compileOnly(libs.luckperms)
    compileOnly(libs.playerpoints)
    compileOnly(libs.decentholograms)
    implementation(libs.adventure.bukkit)
    implementation(kotlin("reflect"))
}
kotlin {
    jvmToolchain(17)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.build {
    dependsOn(tasks.shadowJar)
}