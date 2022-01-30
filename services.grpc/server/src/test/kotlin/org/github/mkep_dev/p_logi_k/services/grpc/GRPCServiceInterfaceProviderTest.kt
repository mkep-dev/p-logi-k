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

package org.github.mkep_dev.p_logi_k.services.grpc

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.github.mkep_dev.p_logi_k.io.BooleanValue
import org.github.mkep_dev.p_logi_k.io.GenericInput
import org.github.mkep_dev.p_logi_k.io.GenericOutput
import org.github.mkep_dev.p_logi_k.io.not
import org.github.mkep_dev.p_logi_k.model.io.BasicIOCard
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.program.api.CyclicPLCProgram
import org.github.mkep_dev.p_logi_k.program.api.IOAccessLayer
import org.github.mkep_dev.p_logi_k.services.api.IOService
import org.github.mkep_dev.p_logi_k.starter.PLCMaster
import org.junit.jupiter.api.Test

internal class GRPCServiceInterfaceProviderTest {

    @Test
    fun test() = runBlocking {
        val plcMaster = PLCMaster(listOf(BasicIOCard(0)), 50)
        plcMaster.simulationClockService.setAutoTicking(true)
        val inputID = "bIO0/in/1"
        val outputID = "bIO0/out/1"

        val simpleProgram = object : CyclicPLCProgram {

            val inputElement = IOElement(inputID, DataDirection.IN, BooleanValue(false))
            val outputElement = IOElement(outputID, DataDirection.IN, BooleanValue(false))

            lateinit var input: GenericInput<Boolean>
            lateinit var output: GenericOutput<Boolean>

            override fun initialize(ioAccess: IOAccessLayer) {
                input = ioAccess.getInput(inputElement.identifier, inputElement.valueClass)!!
                output = ioAccess.getOutput(outputElement.identifier, outputElement.valueClass)!!
            }

            override fun step(millis: Long) {
                output.setValue(!input.getValue())
            }

            override val name: String
                get() = "dummy"

            override fun getUsedInputs(): Set<IOElement<Any>> {
                return setOf(inputElement)
            }

            override fun getOutputs(): Set<IOElement<Any>> {
                return setOf(outputElement)
            }
        }

        plcMaster.plcProgramMemory.addProgram(simpleProgram)

        println("Load program")
        plcMaster.plcExecutionService.loadProgram(simpleProgram.name)
        plcMaster.plcExecutionService.goOn()

        val port = 5558
        println("Start grpc")
        plcMaster.startExtraInterfaces(listOf(GRPCServiceInterfaceProvider(port)))
        println("Grpc running")


        println("Start channel")
        val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()

        val ioService = GrpcServiceClient(channel).ioService

        ioService.addOnOutputChangeListener(".*".toRegex(), object : IOService.ValueChangeListener {
            override fun onChange(
                name: String,
                oldValue: org.github.mkep_dev.p_logi_k.io.GenericValue<Any>,
                newValue: org.github.mkep_dev.p_logi_k.io.GenericValue<Any>
            ) {
                if (oldValue != newValue) {
                    println("Value of $name changed from '${oldValue.value}' to '${newValue.value}'.")
                }
            }
        })!!

        ioService.setInput(inputID, BooleanValue(true))

        delay(500)

        println("Output is:${ioService.getIOValue(outputID)}")

        ioService.setInput(inputID, BooleanValue(false))

        runBlocking { delay(500) }

        println("Output is:${ioService.getIOValue(outputID)}")

        channel.shutdown()

        plcMaster.stop()

        coroutineContext.cancelChildren()

    }

}