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
import com.google.protobuf.boolValue
import org.github.mkep_dev.p_logi_k.model.PLCExecutionState
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.services.api.PLCExecutionService
import org.github.mkep_dev.p_logi_k.services.grpc.stub.*

class GrpcPLCExecutionService(private val plcExecutionService: PLCExecutionService) :
    PLCExecutionServiceGrpcKt.PLCExecutionServiceCoroutineImplBase() {


    override suspend fun reset(request: Empty): Empty {
        plcExecutionService.reset()
        return Empty.getDefaultInstance()
    }

    override suspend fun pause(request: Empty): Empty {
        plcExecutionService.pause()
        return Empty.getDefaultInstance()
    }

    override suspend fun resetPause(request: Empty): Empty {
        plcExecutionService.resetAndPause()
        return Empty.getDefaultInstance()
    }

    override suspend fun goOn(request: Empty): Empty {
        plcExecutionService.goOn()
        return Empty.getDefaultInstance()
    }

    override suspend fun getState(request: Empty) = pLCExecutionStateInfo {
        state = when (plcExecutionService.getState()) {
            PLCExecutionState.INIT -> PLCExecutionStateInfo.State.INIT
            PLCExecutionState.RUNNING -> PLCExecutionStateInfo.State.RUNNING
            PLCExecutionState.RESETTING -> PLCExecutionStateInfo.State.RESETTING
            PLCExecutionState.PAUSED -> PLCExecutionStateInfo.State.PAUSED
            PLCExecutionState.ERROR -> PLCExecutionStateInfo.State.ERROR
        }
    }

    override suspend fun loadProgram(request: StringValue) = boolValue {
        value = plcExecutionService.loadProgram(request.value)
    }

    override suspend fun getCurrentProgramName(request: Empty) = currentlyActiveProgramInfo {
        val programName = plcExecutionService.getCurrentProgramName()
        if (programName != null) {
            programActive = true
            this.programName = programName
        } else {
            programActive = false
        }
    }

    override suspend fun listProgramNames(request: Empty) = existingProgramsList {
        programName += plcExecutionService.listProgramNames()
    }

    private fun mapIoElementToMsg(ioElement: org.github.mkep_dev.p_logi_k.model.io.IOElement<Any>) = iOElement {
        id = ioElement.identifier
        direction = when (ioElement.direction) {
            DataDirection.IN -> Direction.IN
            DataDirection.OUT ->Direction.OUT
        }
        value = GenericValueHelper.mapGenericValueToMsg(ioElement.value)
    }

    override suspend fun getProgramMeta(request: StringValue) = programMetaInfo {
        val programInfo = plcExecutionService.getProgramMeta(request.value)
        if (programInfo == null) {
            success = false
        } else {
            success = true
            programName = programInfo.name
            inputs.addAll(programInfo.getUsedInputs().map(::mapIoElementToMsg))
            outputs.addAll(programInfo.getOutputs().map(::mapIoElementToMsg))
        }
    }
}