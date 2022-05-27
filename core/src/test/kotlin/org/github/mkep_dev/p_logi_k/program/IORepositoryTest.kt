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

package org.github.mkep_dev.p_logi_k.program

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.github.mkep_dev.p_logi_k.io.BooleanValue
import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IORepository
import org.github.mkep_dev.p_logi_k.model.io.IoEdgePolarity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IORepositoryTest {

    private lateinit var repository: IORepository

    private val in1 = IOElement("i1", DataDirection.IN, GenericValue.of(false))
    private val in2 = IOElement("i2", DataDirection.IN, GenericValue.of(1))
    private val in3 = IOElement("i3", DataDirection.IN, GenericValue.of(true))
    private val out1 = IOElement("o1", DataDirection.OUT, GenericValue.of(false))
    private val out2 = IOElement("o3", DataDirection.OUT, GenericValue.of(0))
    private val out3 = IOElement("o4", DataDirection.OUT, GenericValue.of(true))

    private val allElements = listOf(in1, in2, in3, out1, out2, out3)

    @BeforeEach
    fun testInit() {
        repository = IORepository(allElements)
    }

    @Test
    fun getAll() {
        // all element from list inside repository
        Assertions.assertTrue(repository.getAll().containsAll(allElements), "Missing element(s) in repository")
        // all elements from repository in list
        Assertions.assertTrue(allElements.containsAll(repository.getAll()), "Element in repository that wasn't added")
    }

    @Test
    fun getAllInputs() {
        // all element from list inside repository
        Assertions.assertTrue(allElements.filter { it.direction == DataDirection.IN }.map { it.identifier }
            .all { repository.getInput(it) != null }, "Missing element(s) in repository")
    }

    @Test
    fun getAllOutputs() {
        // all element from list inside repository
        Assertions.assertTrue(allElements.filter { it.direction == DataDirection.OUT }.map { it.identifier }
            .all { repository.getOutput(it) != null }, "Missing element(s) in repository")
    }

    @Test
    fun changeIOsList() {
        // every int will be increased and booleans will be negated
        fun dataManipulation(it: IOElement<Any>) =
            when (it.value.valueClass) {
                Boolean::class -> IOElement(
                    it.identifier,
                    it.direction,
                    GenericValue.of(it.value.toBoolean().value.not())
                )
                else -> IOElement(it.identifier, it.direction, GenericValue.of(it.value.toLong().value.inc()))
            }
        // Only change outputs
        val newOutputs = allElements.filter { it.direction.isOutput() }.map(::dataManipulation)

        // Apply manipulation in repo
        var success = repository.changeIOs(newOutputs.map { it.identifier to it.value })
        Assertions.assertTrue(success, "Changing outputs failed.")

        // check if manipulation was successful
        // 1. Outputs changed
        Assertions.assertTrue(
            repository.getAll().filter { it.direction.isOutput() }.containsAll(newOutputs),
            "Outputs not changed"
        )
        // 2. Inputs unchanged
        Assertions.assertTrue(repository.getAll().filter { it.direction.isInput() }
            .containsAll(allElements.filter { it.direction.isInput() }), "Input was changed")

        // change input that doesn't exist should fail
        success = repository.changeIOs(listOf("d3" to GenericValue.of(5)))
        Assertions.assertFalse(success, "Element was changed that wasn't inside the repository.")

        // change input with wrong value should fail
        success = repository.changeIOs(listOf(out1.identifier to GenericValue.of(5)))
        Assertions.assertFalse(success, "Changed that wasn't inside the repository.")

    }

    @Test
    fun changeIOsPredicate() {
        // every int will be increased and booleans will be negated
        fun dataManipulation(it: IOElement<Any>) =
            when (it.value.valueClass) {
                Boolean::class -> IOElement(
                    it.identifier,
                    it.direction,
                    GenericValue.of(it.value.toBoolean().value.not())
                )
                else -> IOElement(it.identifier, it.direction, GenericValue.of(it.value.toLong().value.inc()))
            }

        // only outputs
        fun onlyOutputs(it: IOElement<Any>) = it.direction.isOutput()
        // Only change outputs
        val newOutputs = allElements.filter(::onlyOutputs).map(::dataManipulation)

        // Apply manipulation in repo
        var success = repository.changeIOs(::onlyOutputs) { dataManipulation(it).value }
        Assertions.assertTrue(success, "Changing outputs failed.")

        // check if manipulation was successful
        // 1. Outputs changed
        Assertions.assertTrue(
            repository.getAll().filter(::onlyOutputs).containsAll(newOutputs),
            "Outputs not changed"
        )
        // 2. Inputs unchanged
        Assertions.assertTrue(repository.getAll().filter { it.direction.isInput() }
            .containsAll(allElements.filter { it.direction.isInput() }), "Input was changed")

        // change input that doesn't exist should fail
        success = repository.changeIOs(listOf("d3" to GenericValue.of(5)))
        Assertions.assertFalse(success, "Changed element that wasn't inside the repository.")

        // change input with wrong value should fail
        success = repository.changeIOs(listOf(out1.identifier to GenericValue.of(5)))
        Assertions.assertFalse(success, "Changed that wasn't inside the repository.")

    }

    @Test
    fun setIO() {
        val changedInput = in1
        // Simply change input 1
        val success =
            repository.setIO(changedInput.identifier, GenericValue.of(changedInput.value.toBoolean().value.not()))
        Assertions.assertTrue(success, "Changing IO failed.")
        // check if value was negated
        Assertions.assertTrue(
            repository.getInput(changedInput.identifier)!!.value.toBoolean().value == changedInput.value.toBoolean().value.not(),
            "Value not changed"
        )

        // check whether all other value are unchanged
        Assertions.assertTrue(
            allElements.filter { it.identifier != changedInput.identifier }
                .containsAll(repository.getAll().filter { it.identifier != changedInput.identifier }),
            "Element was falsely modified"
        )
        // Check concurrency
        val numberOfIncrements = 10
        val changedInt = repository.getAll().first { it.value.valueClass == Long::class }
        val changedIntId = changedInt.run { identifier }
        runBlocking {
            repeat(numberOfIncrements) {
                launch {
                    repository.setIO(changedIntId) { GenericValue.of(it.toLong().value.inc()) }
                }
            }
        }
        // Because of our good concurrent behaviour the int should be correct
        Assertions.assertEquals(
            numberOfIncrements.toLong(),
            repository.getIO(changedIntId)!!.value.toLong().value - changedInt.value.toLong().value,
            "Repository misbehaves with multiple threads"
        )
    }

    @Test
    fun checkPolarity() {
        val changedInput =
            allElements.filter { it.direction.isInput() && it.value.valueClass == Boolean::class }.random()
        // Simply change input 1
        repository.setIO(changedInput.identifier, BooleanValue(true)) shouldBe true
        repository.setIO(changedInput.identifier, BooleanValue(false)) shouldBe true

        // check for falling edge
        repository.getInputPolarity(changedInput.identifier) shouldBe IoEdgePolarity.FALLING

        repository.setIO(changedInput.identifier, BooleanValue(true)) shouldBe true

        // check for rising edge
        repository.getInputPolarity(changedInput.identifier) shouldBe IoEdgePolarity.RISING


        repository.setIO(changedInput.identifier, BooleanValue(true)) shouldBe true

        // check for flat edge
        repository.getInputPolarity(changedInput.identifier) shouldBe IoEdgePolarity.FLAT

        // should fail on output
        repository.getInputPolarity(out1.identifier) shouldBe null

        // should fail on non booleans
        repository.getInputPolarity(in2.identifier) shouldBe null

    }

    @Test
    fun checkSubscription() {
        fun dataManipulation(value: GenericValue<Any>) =
            when (value.valueClass) {
                Boolean::class -> GenericValue.of(value.toBoolean().value.not())
                else -> GenericValue.of(value.toLong().value.inc())
            }

        fun transform(it: IOElement<Any>) =
            if (it.direction.isOutput()) {
                null
            } else {
                dataManipulation(it.value)
            }


        val elementsToChange = allElements.filter { transform(it) != null }.map { it.identifier }.toMutableList()
        // Add subscription
        val sub = repository.addOnValueChangeListener { id, old, new, _ ->
            Assertions.assertEquals(
                allElements.find { it.identifier == id }!!.value,
                old,
                "Id and old value don't match"
            )
            Assertions.assertTrue(
                elementsToChange.remove(id),
                "Element occurred in subscription that shouldn't be changed"
            )
            Assertions.assertEquals(
                dataManipulation(old),
                new,
                "New value and old value don't match when applying transformation"
            )
        }

        // Apply manipulation in repo
        var success = repository.changeIOs(::transform)
        Assertions.assertTrue(success, "Changing IOs failed.")

        sub.stop()

        Assertions.assertTrue(elementsToChange.isEmpty(), "Subscription wasn't called for ever update")

        // now check for unsubscribing
        val failingSub =
            repository.addOnValueChangeListener { _, _, _, _ -> Assertions.fail("Subscription called even though it was removed") }
        failingSub.stop()
        // Apply manipulation in repo
        success = repository.changeIOs(::transform)
        Assertions.assertTrue(success, "Changing IOs failed.")


    }


}