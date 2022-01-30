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

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging


class GarageDoorUI(val stage: Stage) {

    companion object : KLogging() {
    }


    private val upButton: Button
    private val downButton: Button
    private val hitButton: Button
    private val label: Label
    private val timeLabel: Label
    private val canvasScence: Scene
    private val canvasGroup: Group
    private val progress = SimpleDoubleProperty(1.0)

    val controller: Controller

    init {

        upButton = Button("UP")
        downButton = Button("DOWN")
        hitButton = Button("Hit")
        label = Label("STOP")
        timeLabel = Label("")

        canvasGroup = Group()
        canvasScence = Scene(canvasGroup, 250.0, 250.0)
        canvasGroup.apply {
            // Background Garage
            val widthDoor = 0.8 * canvasScence.width
            val heightDoor = 0.6 * canvasScence.height
            // garage
            children += Rectangle(
                0.0,
                (canvasScence.height - heightDoor) * 0.8,
                canvasScence.width,
                heightDoor * 1.2
            ).also { rect ->
                rect.fill = Color.BURLYWOOD
            }
            // Roof
            children += Polygon(
                0.5 * canvasScence.width,
                0.0,
                0.0,
                (canvasScence.height - heightDoor) * 0.8,
                canvasScence.width,
                (canvasScence.height - heightDoor) * 0.8
            ).also { triangle ->
                triangle.fill = Color.RED
            }
            // garage inside
            children += Rectangle(
                (canvasScence.width - widthDoor) / 2,
                canvasScence.height - heightDoor,
                widthDoor,
                heightDoor
            ).also { rect ->
                rect.fill = Color.LIGHTGRAY
            }
            children += Rectangle(
                (canvasScence.width - widthDoor) / 2,
                canvasScence.height - heightDoor,
                widthDoor,
                heightDoor
            ).also { door ->
                door.fill = Color.GRAY
                door.heightProperty().bind(progress.multiply(heightDoor * 0.9).add(0.1 * heightDoor))
            }

        }


        val root = VBox().apply {
            isFillWidth = true
            children += HBox().apply {
                isFillWidth = true
                children += upButton
                children += downButton
                children += hitButton
                children += label
            }
            children += HBox().apply {
                prefHeight = 150.0
                isFillWidth = true
                isFillHeight = true
                children += canvasGroup
            }
            children += timeLabel
        }

        controller = Controller(upButton, downButton, hitButton, label, timeLabel, progress)
        stage.title = "GarageDoor"
        stage.scene = Scene(root, 300.0, 500.0)
    }


    class Controller(
        val btnUp: Button,
        val btnDown: Button,
        val btnHit: Button,
        val dirLabel: Label,
        val timeLabel: Label,
        val progressProperty: DoubleProperty
    ) {

        var progress: Double
            set(value) {
                runBlocking {
                    launch(Dispatchers.Main) {
                        progressProperty.value = value
                    }
                }
            }
            get() = progressProperty.value

        private val _btnUpChannel = MutableStateFlow(false)
        val btnUpChannel
            get() = _btnUpChannel.asStateFlow()
        private val _btnDownChannel = MutableStateFlow(false)
        val btnDownChannel get() = _btnDownChannel.asStateFlow()


        private val _hitBarChannel = MutableStateFlow(false)
        val hitBarChannel = _hitBarChannel.asStateFlow()

        init {
            btnUp.pressedProperty().addListener { _, _, newValue ->
                _btnUpChannel.update { newValue }
            }
            btnDown.pressedProperty().addListener { _, _, newValue ->
                _btnDownChannel.update { newValue }
            }
            btnHit.pressedProperty().addListener { _, _, newValue ->
                _hitBarChannel.update { newValue }
            }
        }


        fun setTime(time: Long) {
            runBlocking {
                launch(Dispatchers.Main) {
                    timeLabel.text = "$time ms"
                }
            }

        }

        fun setDir(i: Int) {
            runBlocking {
                launch(Dispatchers.Main) {
                    when (i) {
                        1 -> dirLabel.text = "UP"
                        -1 -> dirLabel.text = "DOWN"
                        0 -> dirLabel.text = "STOP"
                        else -> dirLabel.text = "ERROR"
                    }
                }
            }
        }

    }
}