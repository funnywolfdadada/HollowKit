plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("dependencies") {
            id = "com.funnywolf.hollowkit.dependencies"
            implementationClass = "DependenciesPlugin"
        }
    }
}