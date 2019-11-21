val deployerJars by configurations.creating

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.41"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    `maven-publish`
    maven
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    maven {
        url = uri("http://maven.huygens.knaw.nl/repository")
    }
}

dependencies {
    deployerJars("org.apache.maven.wagon:wagon-ssh:2.2")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use parsec.
    implementation("lambdada:parsec:1.0")

    implementation("nl.knaw.huygens:visitei:0.6.2")
    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("org.apache.commons:commons-lang3:3.9")

    // Use Arrow for fp
    val arrowVersion = "0.10.3"
    implementation("io.arrow-kt:arrow-core:${arrowVersion}")
    implementation("io.arrow-kt:arrow-core-data:${arrowVersion}")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("commons-io:commons-io:2.6")
    testImplementation("nl.knaw.huygens.alexandria:alexandria-markup-core:2.3")
}

tasks.named<Upload>("uploadArchives") {
    repositories.withGroovyBuilder {
        "mavenDeployer" {
            setProperty("configuration", deployerJars)
            "repository"("url" to "scp://n-195-169-89-50.diginfra.net:/data/html/repository") {
                "authentication"("userName" to System.getenv("USER"), "password" to System.getenv("PASSWORD"))
            }
        }
    }
}

//java {
//    withJavadocJar()
//    withSourcesJar()
//}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "nl.knaw.huc.di.rd.tag"
            artifactId = "creole"
            version = "1.0"
            from(components["java"])
        }
    }
    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/repo")
        }
//        maven {
//            url = uri("sftp://n-195-169-89-50.diginfra.net:/data/html/repository")
//        }
    }
}