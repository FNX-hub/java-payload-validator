plugins {
    id("java-library")
    id("jacoco")
    id("org.sonarqube") version "6.3.1.5724"
}

sonar {
    properties {
        property("sonar.projectKey", "FNX-hub_java-payload-validator")
        property("sonar.organization", "fnx-hub")
    }
}

group = "org.fnx.hub"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

val everit = "1.14.6"
val jackson = "2.20.0"
val byteBuddy = "1.15.0"
val autoService = "1.1.1"
val serviceAnnotation = "1.1.1"
val compileTesting = "0.23.0"
val obsgenesis = "3.4"
val mockito = "5.20.0"

repositories {
    mavenCentral()
}
dependencies {
    implementation("com.github.erosb:everit-json-schema:$everit")
    implementation("com.fasterxml.jackson.core:jackson-core:$jackson")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jackson")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jackson")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson")
    implementation("net.bytebuddy:byte-buddy:$byteBuddy")
    implementation("org.objenesis:objenesis:$obsgenesis")


    annotationProcessor("com.google.auto.service:auto-service:$autoService")
    compileOnly("com.google.auto.service:auto-service-annotations:$serviceAnnotation")

    testImplementation("com.google.testing.compile:compile-testing:$compileTesting")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockito")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "-javaagent:${
            project.configurations.testRuntimeClasspath.get().files.first { it.name.startsWith("mockito-core") }
        }"
    )
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}