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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KLogging
import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IOReference
import org.github.mkep_dev.p_logi_k.services.api.IOService
import org.github.mkep_dev.p_logi_k.services.grpc.stub.*
import org.github.mkep_dev.p_logi_k.services.grpc.stub.IOServiceGrpc.IOServiceBlockingStub
import kotlin.random.Random

internal class GrpcIOServiceClient(private val grpcIoServiceStub: IOServiceBlockingStub) :
    IOService {

    private companion object : KLogging()

    @OptIn(ObsoleteCoroutinesApi::class)
    private val context = Dispatchers.IO

    private val scope = CoroutineScope(context)

    val subscriptionId = Random.Default.nextLong()

    private var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun getIOMap(): List<IOReference<Any>> {
        return grpcIoServiceStub.getIOMap(Empty.getDefaultInstance()).asSequence().toList().mapNotNull {

            val dir = when (it.direction) {
                Direction.IN -> DataDirection.IN
                Direction.OUT -> DataDirection.OUT
                else -> return@mapNotNull null
            }
            val clazz = when (it.type) {
                Type.BOOLEAN -> Boolean::class
                Type.INTEGER -> Int::class
                Type.DOUBLE -> Double::class
                Type.UNKNOWN, Type.UNRECOGNIZED -> return@mapNotNull null
                else -> return@mapNotNull null
            }
            IOReference(it.id, dir, clazz)
        }
    }

    override fun getIOValue(name: String): GenericValue<Any>? {
        val valueInfo = grpcIoServiceStub.getIOValue(
            StringValue.of(
                name
            )
        )
        return if (valueInfo.success) {
            GenericValueHelper.mapMsgToGenericValue(valueInfo.value)
        } else {
            null
        }
    }

    override fun getIOs(regex: Regex): Collection<IOElement<Any>> = runBlocking {
        val ios = mutableListOf<IOElement<Any>>()
        grpcIoServiceStub.findIOsMatchingRegex(StringValue.of(regex.pattern)).asFlow().map {
            mapMsgToIoElement(it)
        }.toList(ios)
    }

    override fun setInput(name: String, value: GenericValue<Any>) {
        logger.trace { "Try to update input '$name' to '$value'" }
        val feedback = grpcIoServiceStub.setInput(setInputRequest {
            this.name = name
            newValue = GenericValueHelper.mapGenericValueToMsg(value)
        })
        if(!feedback.success){
            logger.error { "Setting input '$name' to '$value' failed. See $feedback" }
        }else{
            logger.trace { "Successfully updated input '$name' to '$value'" }
        }
    }

    override fun addOnInputChangeListener(
        filter: Regex,
        listener: IOService.ValueChangeListener
    ): IOService.ValueChangeSubscriptionHandle? {
        // First check, if any io matches the regex
        if (runBlocking {
                grpcIoServiceStub.findIOsMatchingRegex(StringValue.of(filter.pattern)).asFlow()
                    .firstOrNull { it.direction == Direction.IN } == null
            }
        ) {
            return null
        }

        val feedback = grpcIoServiceStub.registerForInputChanges(subscriptionRequest {
            clientId = subscriptionId
            regex = filter.pattern
        })
        scope.launch(ioDispatcher) {
            while (kotlin.runCatching { feedback.hasNext()}.getOrDefault(false) && isActive) {
                val it = feedback.next()
                logger.trace { "Got input update for ${it.name}: $it." }
                val new = GenericValueHelper.mapMsgToGenericValue(it.new)
                val old = GenericValueHelper.mapMsgToGenericValue(it.old)
                if (new == null || old == null) {
                    logger.error { "Incoming message '$it' can't be converted to corresponding value object." }
                } else {
                    listener.onChange(it.name,old, new)
                }
            }
        }
        return object : IOService.ValueChangeSubscriptionHandle {
            override fun stop() {
                grpcIoServiceStub.unregisterForInputChanges(unsubscriptionRequest {
                    clientId = subscriptionId
                    regex = filter.pattern
                })
            }

        }
    }

    override fun addOnOutputChangeListener(
        filter: Regex,
        listener: IOService.ValueChangeListener
    ): IOService.ValueChangeSubscriptionHandle? {
        // First check, if any io matches the regex

        if (runBlocking {
                getIOs(filter).none { it.direction == DataDirection.OUT }
            }
        ) {
            return null
        }


        val feedback = grpcIoServiceStub.registerForOutputChanges(subscriptionRequest {
            clientId = subscriptionId
            regex = filter.pattern
        })
        scope.launch(ioDispatcher) {
            while (kotlin.runCatching { feedback.hasNext()}.getOrDefault(false) && isActive) {
                val it = feedback.next()
                logger.trace { "Got output update for ${it.name}: $it." }
                val new = GenericValueHelper.mapMsgToGenericValue(it.new)
                val old = GenericValueHelper.mapMsgToGenericValue(it.old)
                if (new == null || old == null) {
                    logger.error { "Incoming message '$it' can't be converted to corresponding value object." }
                } else {
                    logger.trace { "Pass the update ${it.name} to value '$new' to subs " }
                    listener.onChange(it.name,old, new)
                }
            }
        }
        return object : IOService.ValueChangeSubscriptionHandle {
            override fun stop() {
                grpcIoServiceStub.unregisterForOutputChanges(unsubscriptionRequest {
                    clientId = subscriptionId
                    regex = filter.pattern
                })
            }

        }
    }
}