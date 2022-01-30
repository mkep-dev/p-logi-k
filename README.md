# P-LOGI-K - A logical PLC simulation written in Kotlin™

## Motivation

The goal of this code base is to offer an easy ready to use system for open source projects. It should empower every
java/kotlin programmer to quickly write a PLC program that can be simulated.

## What is it capable of?

This is simple simulation framework that simulates a PLC with the following characteristics:

- Cyclic program execution (Run step ever 50ms)
    1. Read inputs
    2. Compute outputs
    3. Apply output changes
    4. Repeat
- Be extended to communicate with other systems
    - Simply extend the IOModule interface and add it to the PLCMaster
    - Introduce new service interfaces to control the simulation: Simply extend the ServicesInterfaceProvider interface.

## What it *can't* do

- Guarantee realtime behaviour (it runs in the JVM -> not possible to define hard time limits)
- Execute existing PLC programs from PLC vendors
- Used as Kotlin™ multiplatform library (maybe in near future)

## Structure

- **api**: The api to work with or extend p-logi-k
- **core**: The implementations that are necessary to execute then simulation
- **examples**: Some example how to work with p-logi-k
- **fsm-program**: An extension of p-logi-k to execute programs that are described by an extended finite state machine.
  See examples
- **services.grpc**: Offers a remote procedure interface to use the services to control the simulation on another PC or
  inside other JVM context.
- **simulation-environment**: The basics to create a simulation of a real world interface the PLC program must control.
  See examples for a working implementation

## Usage

### Include packages using github packages maven

Modify your gradle.build.kts file (or gradle.build analog) to match the following lines. 
Unfortunately github doesn't support unauthorized access. So you have to enter your credentials.
See https://docs.github.com/en/packages/learn-github-packages/introduction-to-github-packages#authenticating-to-github-packages

```kotlin
// ...
repositories {
  maven {
    url = uri("https://maven.pkg.github.com/mkep-dev/p-logi-k")
    credentials {
      username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
      password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
    }
  }
  // ...
}

dependencies {
  // the api package
  implementation("org.github.mkep-dev.p-logi-k:api:1.0.0")
  // the core package
  implementation("org.github.mkep-dev.p-logi-k:core:1.0.0")
  // the package to describe the PLC behaviour with an extended finite state machine (optional) 
  implementation("org.github.mkep-dev.p-logi-k:fsm-program:1.0.0")
  // the grpc service client package (optional) (not needed on server side)
  implementation("org.github.mkep-dev.p-logi-k:services-grpc-client:1.0.0")
  // the grpc service server package (optional)
  implementation("org.github.mkep-dev.p-logi-k:services-grpc-server:1.0.0")
  // the simulation environment package to create a new simulation environment
  implementation("org.github.mkep-dev.p-logi-k:simulation-environment:1.0.0")
    //...
}
// ...
```


### Include pacakges using maven local

Simplest way to quickly test the packages with your code.

First clone the repository anywhere on your computer and enter the project root folder.
Then execute the local publish.

```bash
./gradlew publishToMavenLocal
```

Modify your gradle.build.kts file (or gradle.build analog) to match the following lines.

```kotlin
// ...
repositories{
    mavenLocal()
    mavenCentral()
    // ...
}

dependencies {
  // the api package
  implementation("org.github.mkep-dev.p-logi-k:api:1.0.0")
  // the core package
  implementation("org.github.mkep-dev.p-logi-k:core:1.0.0")
  // the package to describe the PLC behaviour with an extended finite state machine (optional) 
  implementation("org.github.mkep-dev.p-logi-k:fsm-program:1.0.0")
  // the grpc service client package (optional) (not needed on server side)
  implementation("org.github.mkep-dev.p-logi-k:services-grpc-client:1.0.0")
  // the grpc service server package (optional)
  implementation("org.github.mkep-dev.p-logi-k:services-grpc-server:1.0.0")
  // the simulation environment package to create a new simulation environment
  implementation("org.github.mkep-dev.p-logi-k:simulation-environment:1.0.0")
    //...
}
// ...
```


### Cyclic program

``` kotlin
val simpleProgram = object : CyclicPLCProgram {
          // use id of an existing IO
            val inputElement = IOElement("bIO0/in/1", DataDirection.IN, BooleanValue(false))
            val outputElement = IOElement("bIO0/out/1", DataDirection.IN, BooleanValue(false))

            lateinit var input: GenericInput<Boolean>
            lateinit var output: GenericOutput<Boolean>

            // called once during init of the PLC or when loading a program
            override fun initialize(ioAccess: IOAccessLayer) {
                // here we get the ioAccessLayer to get the input and outputs
                input = ioAccess.getInput(inputElement.identifier, inputElement.valueClass)!!
                output = ioAccess.getOutput(outputElement.identifier, outputElement.valueClass)!!
            }
          
            // this method is called periodically
            override fun step(millis: Long) {
                output.setValue(!input.getValue())
            }

            override val name: String
                get() = "Negate-Input"

            // it is important to list all used inputs and outputs
            override fun getUsedInputs(): Set<IOElement<Any>> {
                return setOf(inputElement)
            }

            override fun getOutputs(): Set<IOElement<Any>> {
                return setOf(outputElement)
            }
        }

```

That's all now just load the program.

```kotlin
    val master = PLCMaster(listOf(BasicIOCard(0)), 50)
    master.plcProgramMemory.addProgram(simpleProgram)
    // activate auto ticking
    master.simulationClockService.setAutoTicking(true)
    // set acceleration to 1.0
    master.simulationClockService.setSimAcceleration(1.0)
    master.plcExecutionService.loadProgram(simpleProgram.name)
    // start program
    master.plcExecutionService.goOn()
    // check output
    println(master.ioService.getIOValue("bIO0/out/1"))
    // set input
    master.ioService.setInput("bIO0/in/1",BooleanValue(false))
    // wait 2 PLC cycles
    Thread.sleep(100)
    // check output again
    println(master.ioService.getIOValue("bIO0/out/1"))
```

### Finite state machine program

That's easy but long code, just check out the example code: [GarageDoor](examples/src/main/kotlin/org/github/mkep_dev/p_logi_k/examples/garageDoor) 

## Examples

- [GarageDoor](examples/src/main/kotlin/org/github/mkep_dev/p_logi_k/examples/garageDoor)

## Planned extensions

- UI to view PLC state and access IOs
- More examples
- Upload artifacts to maven central (GitHub packages need authentifcation)
- Whatever will be suggested by somebody

## Used libraries

- io.github.microutils:kotlin-logging-jvm
- org.slf4j:slf4j-api
- org.apache.logging.log4j:log4j-slf4j-impl
- io.kotest:kotest-assertions-core
- org.junit.jupiter:junit-jupiter-api
- org.junit.jupiter:junit-jupiter-engine
- gRPC™
    - io.grpc:grpc-protobuf
    - io.grpc:grpc-netty
    - io.grpc:grpc-api
    - com.google.protobuf:protobuf-java-util
    - com.google.protobuf:protobuf-kotlin
    - io.grpc:grpc-kotlin-stub
- org.jetbrains.kotlinx:kotlinx-coroutines-core
- org.jetbrains.kotlinx:kotlinx-coroutines-javafx
- org.jetbrains.kotlinx:kotlinx-serialization-json