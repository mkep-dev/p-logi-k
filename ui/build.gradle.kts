plugins {
    id("java")
    id("application")
    kotlin("jvm")
    id("org.openjfx.javafxplugin")
    id("org.beryx.jlink")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    applicationName  = "P-Logi-K UI"
    mainModule.set( "org.github.mkepdev.plogik.ui")
    mainClass.set( "org.github.mkepdev.plogik.ui.HelloApplication")
}


javafx {
    version = "11.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")


    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotest.assertions)
    testRuntimeOnly(libs.junit.jupiter.engine)

}

tasks.withType<Test> {
    useJUnitPlatform()
}


jlink {
    imageZip.set {
        project.file("${buildDir}/distributions/app-${javafx.platform.classifier}.zip") 
    }
    options.addAll("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        name = "app"
    }
}