import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protoc

plugins {
    kotlin("jvm")
    id("com.google.protobuf")
    id("java-library")
    id("maven-publish")
    id("org.owasp.dependencycheck")
}

dependencies {
    api(project(":api"))

    api(kotlin("stdlib"))
    api(libs.kotlin.coroutines.core)

    api(libs.grpc.protobuf)
    api(libs.google.protobuf.java)
    api(libs.google.protobuf.kotlin)
    api(libs.grpc.kotlin)
}

publishing {
    publications {
        register<MavenPublication>("services-grpc-protos") {
            from(components["java"])
            artifactId = "services-grpc-protos"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets {
    val main by getting { }
    main.java.srcDirs("build/generated/source/proto/main/grpc")
    main.java.srcDirs("build/generated/source/proto/main/grpckt")
    main.java.srcDirs("build/generated/source/proto/main/java")
    main.java.srcDirs("build/generated/source/proto/main/kotlin")
}

protobuf {
    val versionCatalog = project.extensions.getByType(VersionCatalogsExtension::class).named("libs")
    val protoBufVersion = versionCatalog.findVersion("protobuf").get()
    val grpcVersion = versionCatalog.findVersion("grpc").get()
    val grpcKotlinVersion = versionCatalog.findVersion("grpckt").get()

    protobuf.protoc {
        artifact = "com.google.protobuf:protoc:${protoBufVersion}"
    }
    protobuf.plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${grpcKotlinVersion}:jdk7@jar"
        }
    }
    protobuf.generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}