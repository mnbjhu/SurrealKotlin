plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.21"

    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":dsl"))
    implementation("com.squareup:kotlinpoet:1.13.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
    implementation("com.squareup:kotlinpoet-ksp:1.13.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.21-1.0.11")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}




