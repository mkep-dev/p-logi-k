plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("java-library")
    id("maven-publish")
    id("org.owasp.dependencycheck")
}

dependencies {
    api(project(":api"))
    implementation(kotlin("reflect"))
    implementation(project(":core"))
    implementation(libs.kotlin.coroutines.core)


    implementation(libs.kotlin.logging)
    compileOnly(libs.slf4j.api)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotest.assertions)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testRuntimeOnly(libs.log4j.slf4j)

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
        register<MavenPublication>("simulation-environment") {
            from(components["java"])
            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}