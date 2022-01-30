/*
 * P-logi-K. A Kotlin(TM)-based PLC simulation.
 * Copyright (C)  2022.  Markus Keppeler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */



package org.github.mkep_dev.p_logi_k.examples.garageDoor

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.stage.Stage
import kotlinx.coroutines.*
import mu.KLogging
import org.github.mkep_dev.p_logi_k.io.BooleanValue
import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.services.api.IOService
import org.github.mkep_dev.p_logi_k.services.api.PLCExecutionService
import org.github.mkep_dev.p_logi_k.services.api.SimulationClockService
import org.github.mkep_dev.p_logi_k.simulation.SimulationEnvironment
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

class GarageDoorSimulation(
    simulationAcceleration: Double,
    ioService: IOService,
    simulationClockService: SimulationClockService,
    plcExecutionService: PLCExecutionService
) : SimulationEnvironment(simulationAcceleration, ioService, simulationClockService, plcExecutionService) {


    private companion object : KLogging()

    private lateinit var garageDoorBehaviorJob: Job

    private var motorUp: Boolean = false
    private var motorDown: Boolean = false

    private val cleanUpCalls = mutableListOf<(() -> Unit)>()

    private var endDown: Boolean? by Delegates.observable(null) { _, oldValue, newValue ->
        if (oldValue != newValue && newValue!= null) {
            ioService.setInput("bIO0/in/5", BooleanValue(newValue))
        }
    }

    private var endUp: Boolean? by Delegates.observable(null) { _, oldValue, newValue ->
        if (oldValue != newValue && newValue!= null) {
            ioService.setInput("bIO0/in/4", BooleanValue(newValue))
        }
    }

    private var hitSensor: Boolean? by Delegates.observable(null) { _, oldValue, newValue ->
        if (oldValue != newValue && newValue!= null) {
            ioService.setInput("bIO0/in/3", BooleanValue(newValue))
        }
    }
    private var downSwitch: Boolean? by Delegates.observable(null) { _, oldValue, newValue ->
        if (oldValue != newValue && newValue!= null) {
            ioService.setInput("bIO0/in/2", BooleanValue(newValue))
        }
    }

    private var upSwitch: Boolean? by Delegates.observable(null) { _, oldValue, newValue ->
        if (oldValue != newValue && newValue!= null) {
            ioService.setInput("bIO0/in/1", BooleanValue(newValue))
        }
    }

    /**
     * 100 = closed
     * 0 = open
     */
    private val positionPercent = AtomicInteger(100)

    override suspend fun prepare() = coroutineScope {
        logger.info { "Begin prepare." }
        upSwitch = false
        downSwitch = false
        hitSensor = false
    }

    override suspend fun run() = coroutineScope {
        logger.info { "RUN" }
        positionPercent.set(100)

        // add obervers for ios
        // motor up
        cleanUpCalls += ioService.addOnOutputChangeListener(
            Regex.fromLiteral("bIO0/out/1"),
            object : IOService.ValueChangeListener {
                override fun onChange(name: String, oldValue: GenericValue<Any>, newValue: GenericValue<Any>) {
                    logger.trace { "Upadate for motor up to '$newValue'." }
                    if(newValue.valueClass != Boolean::class){
                        throw IllegalStateException("Type must be boolean. Any other type is impossiblen")
                    }
                    @Suppress("UNCHECKED_CAST")
                    newValue as GenericValue<Boolean>
                    motorUp = newValue.value
                }
            })!!::stop
        // motor down
        cleanUpCalls += ioService.addOnOutputChangeListener(
            Regex.fromLiteral("bIO0/out/2"),
            object : IOService.ValueChangeListener {
                override fun onChange(name: String, oldValue: GenericValue<Any>, newValue: GenericValue<Any>) {
                    if(newValue.valueClass != Boolean::class){
                        throw IllegalStateException("Type must be boolean. Any other type is impossiblen")
                    }
                    @Suppress("UNCHECKED_CAST")
                    newValue as GenericValue<Boolean>
                    motorDown = newValue.value
                }
            })!!::stop



        garageDoorBehaviorJob = launch {
            var lastInc = 0L
            while (isActive) {
                when {
                    motorUp && motorDown -> {
                        logger.error { "Both motor signal were applied motor died :(" }
                    }
                    motorDown -> {
                        if ((currentMillis - lastInc) > 100) {
                            lastInc = currentMillis
                            positionPercent.getAndUpdate {
                                if (it < 100) {
                                    it + 1
                                } else {
                                    logger.info { "Hit bottom end sensor." }
                                    it
                                }
                            }
                        }
                    }
                    motorUp -> {
                        if ((currentMillis - lastInc) > 100) {
                            lastInc = currentMillis
                            positionPercent.getAndUpdate {
                                if (it > 0) {
                                    it - 1
                                } else {
                                    logger.info { "Hit upper end sensor." }
                                    it
                                }
                            }
                        }
                    }

                }
                positionPercent.get().apply {
                    endUp = this <= 0
                    endDown = this >= 100

                }
                sleep(10)

            }
        }

        Platform.runLater {
            val stage = Stage()
            val ui = GarageDoorUI(stage)
            stage.show()
            val updateJob = launch {
                while (isActive) {
                    ui.controller.setDir(
                        when {
                            !motorUp && motorDown -> -1
                            motorUp && !motorDown -> 1
                            !motorUp && !motorDown -> 0
                            else -> 2
                        }
                    )
                    ui.controller.progress = positionPercent.get().toDouble() / 100
                    ui.controller.setTime(currentMillis)
                    delay(100)
                }
            }
            cleanUpCalls.add(launch {
                ui.controller.btnUpChannel.collect {
                    logger.info { "Btn up state changed to $it" }
                    upSwitch = it
                }
            }::cancel)

            cleanUpCalls.add(launch {
                ui.controller.btnDownChannel.collect {
                    logger.info { "Btn down state changed to $it" }
                    downSwitch = it
                }
            }::cancel)
            cleanUpCalls.add(launch {
                ui.controller.hitBarChannel.collect {
                    logger.info { "Hit bar state changed to $it" }
                    hitSensor = it
                }
            }::cancel)
            //                    ui.controller.
            stage.onCloseRequest = EventHandler {
                logger.info { "UI window closed" }
                updateJob.cancel()
                destroy()
            }

        }

    }

    override fun destroy() {
        cleanUpCalls.forEach { it() }
        garageDoorBehaviorJob.cancel()
    }
}

