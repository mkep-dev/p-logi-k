plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("java-library")
    id("maven-publish")
    id("org.owasp.dependencycheck")
}

dependencies {
    api(kotlin("stdlib"))

    api(project(":api"))
    implementation(project(":services.grpc:protos"))
    implementation(libs.kotlin.logging)
    compileOnly(libs.slf4j.api)

    runtimeOnly(libs.grpc.netty)

    testImplementation(project(":core"))
    testImplementation(project(":services.grpc:client"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.log4j.slf4j)

}

val customArtifactId = "services-grpc-server"

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
        register<MavenPublication>("services-grpc-server") {
            from(components["java"])
            artifactId = customArtifactId
            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)
        }
    }
}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
}
