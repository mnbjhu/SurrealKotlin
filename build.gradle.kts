
plugins {
    kotlin("android").version("1.8.21").apply(false)
    kotlin("multiplatform").version("1.8.21").apply(false)
    id("com.google.devtools.ksp") version "1.8.21-1.0.11"
    kotlin("plugin.serialization") version "1.8.21"
    signing
}
repositories {
    mavenCentral()
}

buildscript {
    dependencies{
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.25.2")
    }
}
