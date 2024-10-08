/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.9.0")
    testImplementation("com.google.code.gson:gson:2.9.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
}

group = "icu.takeneko"
version = "1.6.0"
description = "omms-client-core"
java.sourceCompatibility = JavaVersion.VERSION_1_8


publishing {
    repositories {
        mavenLocal()
        maven {
            name = "NekoMavenRelease"
            url = uri("https://maven.takeneko.icu/releases")
            credentials {
                username = project.findProperty("nekomaven.user") as String? ?: System.getenv("NEKO_USERNAME")
                password = project.findProperty("nekomaven.password") as String? ?: System.getenv("NEKO_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
}
tasks{
    test{
        useJUnitPlatform()
//        jvmArgs("--enable-preview")
    }
}
