plugins {
    kotlin("android").version("1.8.21").apply(false)
    kotlin("multiplatform").version("1.8.21").apply(false)
    id("com.google.devtools.ksp") version "1.8.21-1.0.11"
    kotlin("plugin.serialization") version "1.8.21"
    id("maven-publish")
}
repositories {
    mavenCentral()
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.mnbjhu"
            version = "0.0.1-PREVIEW-1"
            artifactId = "SurrealKotlin"
        }
    }
}