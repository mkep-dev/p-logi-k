# Examples

## Garage door

This illustrates a PLC controlled garage door that can be controlled by two buttons.
Furthermore, the garage door is equipped with a hit bar that detects a collision while moving down.

The garage door moves down until it reached the end or the up button is pressed to stop the movement. If a collision is detected it stops and moves upwards.
While moving down up the door can be stopped with the down button.

The simulation and the program are written but using Java is also possible.
The simulation code is structured as follows:
 - [Finite state machine desription](src/main/kotlin/org/github/mkep_dev/p_logi_k/examples/garageDoor/GarageDoorEfsm.kt): This uses a finite state machine to describe the PLC behaviour.
 - [Finite state machine program](src/main/kotlin/org/github/mkep_dev/p_logi_k/examples/garageDoor/GarageDoorProgram.kt): This uses the state machine to define the PLC program using the FSMPlcProgram base class.
 - [Simulation environment](src/main/kotlin/org/github/mkep_dev/p_logi_k/examples/garageDoor/GarageDoorSimulation.kt): The simulation environment to simulate the behaviour of a real garage door and mimic the sensor values.
 - [Simulation UI](src/main/kotlin/org/github/mkep_dev/p_logi_k/examples/garageDoor/GarageDoorUI.kt): A minimalistic OpenJFX ui to visualize the garage door and the buttons. 

### Java example

The code can be found [here](src/main/java/org/github/mkep_dev/p_logi_k/examples/garageDoor/StartJava.java).

**Run it using gradle:**

```bash
./gradlew examples:runGarageDoorJava
```

## Kotlin example

The code can be found [here](src/main/kotlin/org/github/mkep_dev/p_logi_k/examples/garageDoor/Start.kt).

**Run it using gradle:**

```bash
./gradlew examples:runGarageDoorKotlin
```

## Kotlin grpc example

The code can be found [here](src/main/kotlin/org/github/mkep_dev/p_logi_k/examples/garageDoor/StartPLC.kt) and [here](src/main/kotlin/org/github/mkep_dev/p_logi_k/examples/garageDoor/StartSim.kt).

This illustrates the usage of the gRPC service to run the simulation environment and the PLC simulation on separate systems. 

**Run it using gradle:**

1. Start the PLC sim server
```bash
./gradlew examples:runGarageDoorPLC
```

2. Start the simulation environment
```bash
./gradlew examples:runGarageDoorSim
```