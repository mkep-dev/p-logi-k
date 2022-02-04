import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0" apply false
    kotlin("multiplatform") version "1.6.0" apply false
    kotlin("plugin.serialization") version "1.6.0" apply false
    kotlin("kapt") version "1.6.10" apply false
    id("org.jetbrains.dokka") version "1.6.10"
    id("com.google.protobuf") version "0.8.18" apply false
    id("org.owasp.dependencycheck") version "6.5.3" apply false
}
group = "org.github.mkep-dev.p-logi-k"


subprojects {
    group = "org.github.mkep-dev.p-logi-k"


    plugins.withType(MavenPublishPlugin::class).whenPluginAdded {
        configure<PublishingExtension> {
            repositories {
                maven {
                    url = uri("https://maven.pkg.github.com/mkep-dev/p-logi-k")
                    credentials {
                        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }

    version = "1.0.0-SNAPSHOT"


    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}