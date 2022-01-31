plugins {
    kotlin("jvm")
    id("org.openjfx.javafxplugin")
    id("application")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":fsm-program"))
    implementation(project(":simulation-environment"))
    implementation(project(":services.grpc:server"))
    implementation(project(":services.grpc:client"))
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.javafx)



    implementation(libs.kotlin.logging)
    compileOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.slf4j)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotest.assertions)
    testRuntimeOnly(libs.junit.jupiter.engine)


}

javafx {
    version = "17"
    modules = listOf( "javafx.controls" )
}

tasks.withType<Test> {
    useJUnitPlatform()
}

task("runGarageDoorKotlin",JavaExec::class){
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set( "org.github.mkep_dev.p_logi_k.examples.garageDoor.StartKt")
}

task("runGarageDoorJava",JavaExec::class){
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set( "org.github.mkep_dev.p_logi_k.examples.garageDoor.StartJava")
}

task("runGarageDoorPLC",JavaExec::class){
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set( "org.github.mkep_dev.p_logi_k.examples.garageDoor.StartPLC")
}

task("runGarageDoorSim",JavaExec::class){
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set( "org.github.mkep_dev.p_logi_k.examples.garageDoor.StartSim")
}