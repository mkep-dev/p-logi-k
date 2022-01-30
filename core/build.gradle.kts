
plugins {
    kotlin("jvm")
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("org.owasp.dependencycheck")
}

dependencies {

    api(project(":api"))

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    api(libs.kotlin.coroutines.core)


    implementation(libs.kotlin.logging)
    compileOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.slf4j)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.awaitility)

    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(kotlin("test"))

    testImplementation(libs.kotest.assertions)
}

java {
    withSourcesJar()
}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-doc")
}

publishing {
    publications {
        register<MavenPublication>("core") {
            from(components["java"])
            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}