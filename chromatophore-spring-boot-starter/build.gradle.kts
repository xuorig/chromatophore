plugins {
    kotlin("jvm") version "1.5.31"
}

version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":chromatophore-spring-boot-autoconfigure"))
    api(project(":chromatophore-core"))
}