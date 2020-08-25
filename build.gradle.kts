import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
}

group = "io.suprgames"

description = "Serverless-KDSL-Generator"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("io.suprgames:serverless-kdsl:v0.5.0")
    implementation("org.reflections:reflections:0.9.12")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

publishing {

    publications {
        create<MavenPublication>("kotlin") {
            groupId = "io.suprgames"
            artifactId = "serverless-kdsl-generator"
            if (!System.getenv("NEW_VERSION").isNullOrBlank()) {
                version = System.getenv("NEW_VERSION")
            }
            from(components["kotlin"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/suprgames/serverless-kdsl-generator")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

}
