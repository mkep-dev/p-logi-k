enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "p-logi-k"

dependencyResolutionManagement {

    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {

            version("graphstream", "2.0")
            version("kotest", "5.0.3")
            version("junit", "5.8.2")
            version("protobuf", "3.19.1")
            version("grpc", "1.39.0")
            version("grpckt", "1.2.0")
            version("coroutines", "1.6.0")

            alias("kotlin-logging").to("io.github.microutils:kotlin-logging-jvm:2.1.21")
            alias("slf4j-api").to("org.slf4j:slf4j-api:1.7.32")
            alias("log4j-slf4j").to("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")

            alias("kotest-assertions").to("io.kotest", "kotest-assertions-core").versionRef("kotest")
            alias("junit-jupiter-api").to("org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
            alias("junit-jupiter-engine").to("org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
            alias("awaitility").to("org.awaitility:awaitility-kotlin:4.1.1")

            alias("grpc-protobuf").to("io.grpc", "grpc-protobuf").versionRef("grpc")
            alias("grpc-netty").to("io.grpc", "grpc-netty").versionRef("grpc")
            alias("grpc-api").to("io.grpc", "grpc-api").versionRef("grpc")
            alias("google-protobuf-java").to("com.google.protobuf", "protobuf-java-util").versionRef("protobuf")
            alias("google-protobuf-kotlin").to("com.google.protobuf", "protobuf-kotlin").versionRef("protobuf")
            alias("grpc-kotlin").to("io.grpc", "grpc-kotlin-stub").versionRef("grpckt")


            alias("kotlin-coroutines-core").to("org.jetbrains.kotlinx","kotlinx-coroutines-core").versionRef("coroutines")
            alias("kotlin-coroutines-javafx").to("org.jetbrains.kotlinx","kotlinx-coroutines-javafx").versionRef("coroutines")
            alias("kotlin-serialization-json").to("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")

        }
    }
}

include(
    "api",
    "core",
    "simulation-environment",
    "fsm-program",
    "services.grpc",
    "services.grpc:protos",
    "services.grpc:server",
    "services.grpc:client",
    "examples",
    "ui",
)
