// docs/build.gradle
// 1. Apply Orchid plugin
plugins {
    id("com.eden.orchidPlugin") version "0.21.2"
}

// 2. Include Orchid dependencies
dependencies {
    orchidRuntime("io.github.javaeden.orchid:OrchidDocs:0.21.2")
    orchidRuntime("io.github.javaeden.orchid:OrchidKotlindoc:0.21.2")
    orchidRuntime("io.github.javaeden.orchid:OrchidPluginDocs:0.21.2")
}

// 3. Get dependencies from JCenter and Kotlinx Bintray repo
repositories {
}

// 4. Use the 'Editorial' theme, and set the URL it will have on Github Pages
orchid {
    theme = "Editorial"
    baseUrl = "https://username.github.io/project"
    version = "1.0.0"
}
