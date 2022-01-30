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

package org.github.mkep_dev.p_logi_k.model.io

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KLogging
import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.io.TypeMismatchException
import kotlin.jvm.Throws
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


typealias IOUpdate<V> = Pair<Identifier, GenericValue<V>>
typealias ValueChangeSubscriptionFunction<V> = ((id: Identifier, old: GenericValue<V>, new: GenericValue<V>, direction: DataDirection) -> Unit)

/**
 * The Repository that stores the value of every IO the PLC (can) use.
 *
 * It is impossible to add or remove IOs after the instantiation
 * 
 * @constructor
 * The constructor
 *
 * @param elements the list of available IOs
 */
class IORepository(elements: Iterable<IOElement<Any>>) : Map<Identifier, IOElement<Any>> {

    /**
     * Mutex to protect the underlying map against concurrent changes. 
     */
    private val iosAccessMutex = Mutex()

    /**
     * Map with all IO values
     */
    private val ios = mutableMapOf<Identifier, IOElementValue<Any>>()

    /**
     * The edge polarities for all boolean inputs
     */
    private val polarities = mutableMapOf<Identifier, IoEdgePolarity>()

    /**
     * Mutex to protect the subscription list against concurrent changes.
     */
    private val subscriptionsMutex = Mutex()

    /**
     * The list of subscriptions
     */
    private val subscriptions = mutableMapOf<OnChangeSubscription, ValueChangeSubscriptionFunction<Any>>()

    init {
        // Fill repository
        elements.forEach {
            ios[it.identifier] = IOElementValue.fromIOElement(it)
        }
    }

    constructor(vararg element: IOElement<Any>) : this(element.asIterable())

    /**
     * Assign the IO with the [id] the [newValue]. 
     *
     * @param V the type of the value
     * @param id the identifier
     * @param newValue the new value
     * @return whether the operation succeeded
     */
    fun <V : Any> setIO(id: Identifier, newValue: GenericValue<V>) = setIO(id) { newValue }


    @Throws(TypeMismatchException::class, NoSuchElementException::class)
    operator fun set(identifier: Identifier, value: GenericValue<*>) {
        if(setIO(identifier, value)){
            val io = getIO(identifier) ?: throw NoSuchElementException("There is no IO with the identifier '$identifier'.")
            if(io.valueClass != value.valueClass) {
                throw TypeMismatchException(identifier,io.valueClass,value.valueClass)
            }
        }
    }

    /**
     * Apply the given transformation to the IO with the [id].
     *
     * @param id the identifier
     * @param transform the transformation function
     * @return whether the operation succeeded
     */
    fun setIO(id: Identifier, transform: (GenericValue<Any>) -> GenericValue<Any>): Boolean =
        changeIOs({ ioElement: IOElement<Any> -> ioElement.identifier == id }) { ioElement: IOElement<Any> ->
            transform.invoke(ioElement.value)
        }

    @Throws(TypeMismatchException::class)
    operator fun set(identifier: Identifier, transform: (GenericValue<Any>) -> GenericValue<Any>) {
        setIO(identifier, transform)
    }

    /**
     * Apply the given changes to all matching IOs. 
     * If just one update fails no IO will be changed.
     *
     * @param  changes the updates for every io
     * @return whether the operation succeeded
     */
    fun changeIOs(changes: Iterable<IOUpdate<Any>>): Boolean {
        // check is not concurrent but not necessary because repository entries can't be deleted at the moment
        return if (!changes.map { it.first }.all { ios.containsKey(it) }) {
            false
        } else {
            changeIOs(
                predicate = { ioElement: IOElement<Any> ->
                    changes.map { it.first }.any { it == ioElement.identifier }
                },
                transform = { ioElement: IOElement<Any> ->
                    changes.first { it.first == ioElement.identifier }.second
                })
        }
    }
    /**
     * Apply the given transformation to all IOs matching the given predicate.
     * If just one update fails no IO will be changed.
     *
     * @param  predicate the predicate that selects the values
     * @param  transform the updates for every io
     * @return whether the operation succeeded
     */
    fun changeIOs(predicate: (IOElement<Any>) -> Boolean, transform: (IOElement<Any>) -> GenericValue<Any>): Boolean =
        changeIOs {
            if (predicate.invoke(it)) {
                transform.invoke(it)
            } else {
                null
            }
        }

    /**
     * Changes the ios with the given transform function. If the function returns 'null' the value won't be changed
     *
     * @param transform
     * @return
     */
    fun changeIOs(transform: (IOElement<Any>) -> GenericValue<Any>?): Boolean = runBlocking {

        data class UpdateEvent<V : Any>(
            val id: Identifier,
            val old: GenericValue<V>,
            val new: GenericValue<V>,
            val direction: DataDirection
        )

        val updateEvents = mutableListOf<UpdateEvent<Any>>()

        val success = iosAccessMutex.withLock {

            val changes =
                ios.entries.map { createIOElementFromIOElementValue(it.key, it.value) }
                    .mapNotNull {
                        // drop nulls
                        transform.invoke(it)?.let { newValue ->
                            it.identifier to newValue
                        }
                    }

            // changes can't contain any transforms that are equal null because we applied a filter to ensure that no empty transforms remain

            // At first check for every io whether it exists and type matches
            if (!changes.all { ios[it.first]?.value?.let { v -> v.valueClass == it.second.valueClass } == true }) {
                return@withLock false
            }
            // apply changes
            ios.putAll(changes.map {
                val oldVal = ios[it.first]!!
                if (oldVal.direction.isInput() && oldVal.value.valueClass == Boolean::class) {
                    polarities[it.first] = when {
                        oldVal.value.value == false && it.second.value == true -> IoEdgePolarity.RISING
                        oldVal.value.value == true && it.second.value == false -> IoEdgePolarity.FALLING
                        else -> IoEdgePolarity.FLAT
                    }
                }
                // Store update events
                updateEvents.add(UpdateEvent(it.first, oldVal.value, it.second, oldVal.direction))
                if (oldVal.value != it.second) {
                    logger.trace { "Update value of '${it.first}' from '$oldVal' to '${it.second}'." }
                }
                it.first to oldVal.withValue(it.second)!!
            })

            true
        }

        // now propagate updates
        // use mutex to prevent that multiple subscription events are fire simultaneously
        subscriptionsMutex.withLock {
            updateEvents.forEach { change ->
                subscriptions.values.forEach { function ->
                    function.invoke(
                        change.id,
                        change.old,
                        change.new,
                        change.direction
                    )
                }
            }
        }

        success
    }

    /**
     * Returns the input that matches the given identifier.
     *
     * @param id the identifier
     * @return the input that matches the given identifier. Null if no input matches the identifier.
     */
    fun getInput(id: Identifier): IOElement<Any>? = getInput(id, Any::class)

    /**
     * Returns the input that matches the given identifier and the value type.
     *
     * @param id the identifier
     * @param valueType the value type
     * @return the input that matches the given identifier. Null if no input matches the identifier or the value type.
     */
    fun <V : Any> getInput(id: Identifier, valueType: KClass<V>): IOElement<V>? = runBlocking {
        iosAccessMutex.withLock {
            // if value exists and is input else null
            val result =
                ios[id]?.takeIf { it.direction == DataDirection.IN }?.let { createIOElementFromIOElementValue(id, it) }

            if (result != null && result.valueClass.isSubclassOf(valueType)) {
                @Suppress("UNCHECKED_CAST")  // already checked by result.valueClass.isSubclassOf(valueType)
                result as IOElement<V>
            } else {
                null
            }
        }
    }

    /**
     * Get the edge polarity of the input
     *
     * @param id the identifier
     * @return the edge polarity of the input. null if no boolean input with the id exists.
     */
    fun getInputPolarity(id: Identifier): IoEdgePolarity? = runBlocking {
        iosAccessMutex.withLock {
            polarities[id].takeIf { ios[id]?.direction?.isInput() ?: false }
        }
    }

    /**
     * Returns the output that matches the given identifier.
     *
     * @param id the identifier
     * @return the output that matches the given identifier. Null if no output matches the identifier.
     */
    fun getOutput(id: Identifier): IOElement<Any>? = getOutput(id, Any::class)

    /**
     * Returns the output that matches the given identifier and the value type.
     *
     * @param id the identifier
     * @param valueType the value type
     * @return the input that matches the given identifier. Null if no input matches the identifier or the value type.
     */
    fun <V : Any> getOutput(id: Identifier, valueType: KClass<V>): IOElement<V>? = runBlocking {
        iosAccessMutex.withLock {
            // if value exists and is output else null
            val result =
                ios[id]?.takeIf { it.direction == DataDirection.OUT }?.let { createIOElementFromIOElementValue(id, it) }

            if (result != null && result.valueClass.isSubclassOf(valueType)) {
                @Suppress("UNCHECKED_CAST")  // already checked by result.valueClass.isSubclassOf(valueType)
                result as IOElement<V>
            } else {
                null
            }
        }
    }

    /**
     * Returns the IO that matches the given identifier.
     *
     * @param id the identifier
     * @return the IO that matches the given identifier. Null if no IO matches the identifier.
     */
    fun getIO(id: Identifier): IOElement<Any>? = getIO(id, Any::class)

    /**
     * Returns the IO that matches the given identifier and the value type.
     *
     * @param id the identifier
     * @param valueType the value type
     * @return the IO that matches the given identifier. Null if no IO matches the identifier or the value type.
     */
    fun <V : Any> getIO(id: Identifier, valueType: KClass<V>): IOElement<V>? = runBlocking {
        iosAccessMutex.withLock {
            val result = ios[id]?.let { createIOElementFromIOElementValue(id, it) }

            if (result != null && result.valueClass.isSubclassOf(valueType)) {
                @Suppress("UNCHECKED_CAST")  // already checked by result.valueClass.isSubclassOf(valueType)
                result as IOElement<V>
            } else {
                null
            }
        }
    }

    override operator fun get(key: Identifier) = getIO(key)

    /**
     * Returns a list with all IOs.
     *
     * @return a list with all IOs.
     */
    fun getAll(): Set<IOElement<Any>> = runBlocking {
        iosAccessMutex.withLock {
            ios.entries.distinctBy { it.key }.map { createIOElementFromIOElementValue(it.key, it.value) }.toSet()
        }
    }

    override val entries: Set<Map.Entry<Identifier, IOElement<Any>>>
        get() = getAll().map {
            object : Map.Entry<Identifier, IOElement<Any>> {
                override val key: Identifier
                    get() = it.identifier
                override val value: IOElement<Any>
                    get() = it
            }
        }.toSet()
    override val keys: Set<Identifier>
        get() = runBlocking {
            iosAccessMutex.withLock {
                ios.keys
            }
        }
    override val size: Int
        get() = runBlocking {
            iosAccessMutex.withLock {
                ios.size
            }
        }
    override val values: Collection<IOElement<Any>>
        get() = getAll()

    override fun containsKey(key: Identifier): Boolean = runBlocking {
        iosAccessMutex.withLock {
            ios.containsKey(key)
        }
    }

    override fun containsValue(value: IOElement<Any>): Boolean = runBlocking {
        iosAccessMutex.withLock {
            ios.containsValue(IOElementValue.fromIOElement(value))
        }
    }

    override fun isEmpty(): Boolean = runBlocking {
        iosAccessMutex.withLock {
            ios.isEmpty()
        }
    }

    private fun removeSubscription(subscription: OnChangeSubscription) {
        runBlocking {
            subscriptionsMutex.withLock {
                subscriptions.remove(subscription)
            }
        }
    }

    /**
     * Adds a value change subsription that files the [onChange] for every change that matches the given filter.
     *
     * @param onlyDistinct only notify when the value of old and new differ
     * @param filter the filter predicate. Default is null -> notify for every change
     * @param onChange the subscription interface
     * @return a subscription handle object that can be used to stop the subscription
     */
    fun addOnValueChangeListener(
        onlyDistinct: Boolean = false,
        filter: ((id: Identifier, direction: DataDirection) -> Boolean)? = null,
        onChange: ValueChangeSubscriptionFunction<Any>
    ): OnChangeSubscription =
        runBlocking {

            val sub = AnonymousOnIORepositoryChangeSubscription(this@IORepository::removeSubscription)

            subscriptionsMutex.withLock {
                subscriptions[sub] =
                    { id: Identifier, old: GenericValue<Any>, new: GenericValue<Any>, direction: DataDirection ->
                        onChange
                            // Only call distinct or distinct not required
                            .takeIf { !onlyDistinct || old != new }
                            // If there is a filter it must be true
                            ?.takeIf { filter == null || filter.invoke(id, direction) }
                            // then call the
                            ?.invoke(id, old, new, direction)
                    }
            }

            return@runBlocking sub
        }

    // Internal extras

    /**
     * Handle for the value changes
     */
    interface OnChangeSubscription {
        /**
         * Stop the subscription
         */
        fun stop()
    }

    private class AnonymousOnIORepositoryChangeSubscription(val removeOperation: (OnChangeSubscription) -> Unit) :
        OnChangeSubscription {
        override fun stop() = removeOperation(this)
    }

    private data class IOElementValue<V : Any>(val direction: DataDirection, val value: GenericValue<V>) {
        fun withValue(newValue: GenericValue<V>): IOElementValue<V>? {
            return if (value.valueClass == newValue.valueClass) {
                copy(value = newValue)
            } else {
                null
            }
        }

        companion object {
            fun <V : Any> fromIOElement(ioElement: IOElement<V>): IOElementValue<V> =
                IOElementValue(ioElement.direction, ioElement.value)
        }
    }

    companion object : KLogging() {

        private fun <V : Any> createIOElementFromIOElementValue(id: Identifier, element: IOElementValue<V>) =
            IOElement(id, element.direction, element.value)
    }

}
