plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.github.hank9999"
version = "0.0.5"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}