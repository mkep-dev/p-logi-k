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

import com.google.protobuf.Empty
import com.google.protobuf.StringValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.github.mkep_dev.p_logi_k.model.PLCExecutionState
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.program.api.PLCProgramMeta
import org.github.mkep_dev.p_logi_k.services.api.PLCExecutionService
import org.github.mkep_dev.p_logi_k.services.grpc.stub.PLCExecutionServiceGrpcKt
import org.github.mkep_dev.p_logi_k.services.grpc.stub.PLCExecutionStateInfo

internal class GrpcPLCExecutionServiceClient(private val stub: PLCExecutionServiceGrpcKt.PLCExecutionServiceCoroutineStub) :
    PLCExecutionService {
    override fun reset() = runBlocking<Unit>(Dispatchers.IO) {
        stub.reset(Empty.getDefaultInstance())
    }

    override fun pause() = runBlocking<Unit>(Dispatchers.IO) {
        stub.pause(Empty.getDefaultInstance())
    }

    override fun resetAndPause() = runBlocking<Unit>(Dispatchers.IO) {
        stub.resetPause(Empty.getDefaultInstance())
    }

    override fun goOn() = runBlocking<Unit>(Dispatchers.IO) {
        stub.goOn(Empty.getDefaultInstance())
    }

    override fun getState(): PLCExecutionState = runBlocking(Dispatchers.IO) {
        when (stub.getState(Empty.getDefaultInstance()).state) {
            PLCExecutionStateInfo.State.INIT -> PLCExecutionState.INIT
            PLCExecutionStateInfo.State.RUNNING -> PLCExecutionState.RUNNING
            PLCExecutionStateInfo.State.RESETTING -> PLCExecutionState.RESETTING
            PLCExecutionStateInfo.State.PAUSED -> PLCExecutionState.PAUSED
            PLCExecutionStateInfo.State.ERROR -> PLCExecutionState.ERROR
            else -> PLCExecutionState.ERROR
        }
    }

    override fun loadProgram(name: String): Boolean = runBlocking(Dispatchers.IO) {
        stub.loadProgram(StringValue.of(name)).value
    }

    override fun getCurrentProgramName(): String? = runBlocking(Dispatchers.IO) {
        val feedback = stub.getCurrentProgramName(Empty.getDefaultInstance())
        if (feedback.programActive) {
            feedback.programName
        } else {
            null
        }
    }

    override fun listProgramNames(): List<String> = runBlocking<List<String>>(Dispatchers.IO) {
        stub.listProgramNames(Empty.getDefaultInstance()).getProgramNameList().map { it }
    }

    private class GrpcProgramMeta(
        override val name: String,
        private val usedInputs: Set<IOElement<Any>>,
        private val usedOutputs: Set<IOElement<Any>>
    ) : PLCProgramMeta {
        override fun getUsedInputs(): Set<IOElement<Any>> = usedInputs

        override fun getOutputs(): Set<IOElement<Any>> = usedOutputs
    }

    override fun getProgramMeta(name: String): PLCProgramMeta? = runBlocking(Dispatchers.IO) {
        val feedback = stub.getProgramMeta(StringValue.of(name))
        if (feedback.success) {
            kotlin.runCatching {
                GrpcProgramMeta(
                    feedback.programName,
                    feedback.getInputsList().map { mapMsgToIoElement(it) }.toSet(),
                    feedback.getOutputsList().map { mapMsgToIoElement(it) }.toSet()
                )
            }.getOrNull()
        } else {
            null
        }
    }
}