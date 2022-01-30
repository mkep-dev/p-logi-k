plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("java-library")
    id("maven-publish")
    id("org.owasp.dependencycheck")
}

dependencies {

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation(libs.kotlin.coroutines.core)
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
        register<MavenPublication>("api") {
            from(components["java"])
            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)
        }
    }
}