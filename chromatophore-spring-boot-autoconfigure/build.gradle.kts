plugins {
    kotlin("jvm") version "1.5.31"
}

version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":chromatophore-core"))
    implementation("org.springframework.boot:spring-boot-starter-graphql:2.7.3")
}