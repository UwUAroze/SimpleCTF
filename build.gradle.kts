plugins {
    id("java")
}

group = "me.aroze"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}