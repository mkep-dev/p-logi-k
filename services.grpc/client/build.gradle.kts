plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("java-library")
    id("maven-publish")
    id("org.owasp.dependencycheck")
}

dependencies {
    api(kotlin("stdlib"))
    api(libs.grpc.api)
    implementation(project(":services.grpc:protos"))
    implementation(libs.kotlin.coroutines.core)
    runtimeOnly(libs.grpc.netty)

    implementation(libs.kotlin.logging)
    compileOnly(libs.slf4j.api)

}

val customArtifactId = "services-grpc-client"

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

tasks.dokkaHtml{
    moduleName.set(customArtifactId)
}

tasks.dokkaJavadoc{
    moduleName.set(customArtifactId)
}

publishing {
    publications {
        register<MavenPublication>("services-grpc-client") {
            from(components["java"])
            artifactId = customArtifactId
            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)
        }
    }
}
