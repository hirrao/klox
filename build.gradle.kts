plugins {
    application
    kotlin("jvm") version "2.3.0"
}

group = "com.hirrao"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

application{
    mainClass = "com.hirrao.klox.MainKt"
}

tasks.test {
    useJUnitPlatform()
}