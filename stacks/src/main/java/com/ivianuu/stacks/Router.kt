/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.stacks

import android.os.Bundle
import android.os.Parcelable
import com.ivianuu.stacks.internal.PendingStateChange
import com.ivianuu.stacks.internal.StateChangeIndexer
import java.util.*
import kotlin.collections.ArrayList

/**
 * The actual backstack implementation
 */
class Router {

    private val backstack = ArrayList<Any>()

    private lateinit var tag: String
    private var key: Any? = null

    private var stateChanger: StateChanger? = null

    private val childRouters = ArrayList<Router>()
    private var parentRouter: Router? = null

    private val stateChangeIndexer = StateChangeIndexer()
    private val stateChangeIndices = ArrayList<Int>()
    private val stateChangeListeners = ArrayList<StateChangeListener>()
    private val queuedStateChanges = LinkedList<PendingStateChange>()

    private var activityPaused = false

    fun handleBack(): Boolean {
        if (backstack.isNotEmpty()) {
            val top = backstack.last()

            val topRouters = childRouters
                .filter { it.key == top }
                .filter { it.stateChanger != null }
                .sortedByDescending { it.stateChangeIndices.last() }

            for (router in topRouters) {
                if (router.handleBack()) {
                    return true
                }
            }

            if (pop()) {
                return true
            }
        }

        return false
    }

    fun goTo(newKey: Any) {
        val newBackstack = ArrayList<Any>()
        val activeBackstack = selectActiveBackstack()

        if (activeBackstack.isNotEmpty()
            && activeBackstack.last() == newKey) {
            newBackstack.addAll(activeBackstack)
            enqueueStateChange(newBackstack, Direction.REPLACE)
            return
        }

        var isNewKey = true
        for (key in activeBackstack) {
            newBackstack.add(key)
            if (key == newKey) {
                isNewKey = false
                break
            }
        }
        val direction = if (isNewKey) {
            newBackstack.add(newKey)
            Direction.FORWARD
        } else {
            Direction.BACKWARD
        }

        enqueueStateChange(newBackstack, direction)
    }

    fun replaceTop(newTop: Any) {
        val newBackstack = ArrayList(selectActiveBackstack())
        if (newBackstack.isNotEmpty()) {
            newBackstack.removeAt(newBackstack.lastIndex)
        }
        newBackstack.add(newTop)
        setBackstack(newBackstack)
    }
    
    fun pop(): Boolean {
        if (hasPendingStateChange()) {
            return true
        }

        if (backstack.size <= 1) {
            return false
        }

        val newBackstack = ArrayList<Any>()
        val activeBackstack = ArrayList(selectActiveBackstack())
        (0 until activeBackstack.size - 1).mapTo(newBackstack) { activeBackstack[it] }
        enqueueStateChange(newBackstack, Direction.BACKWARD)

        return true
    }

    fun hasRoot() = backstack.isNotEmpty()

    fun setRoot(key: Any) {
        val size = selectActiveBackstack().size
        val direction = when (size) {
            0 -> Direction.FORWARD
            1 -> Direction.REPLACE
            else -> Direction.BACKWARD
        }
        setBackstack(listOf(key), direction)
    }

    fun setBackstack(newBackstack: List<Any>, direction: Direction = Direction.REPLACE) {
        enqueueStateChange(newBackstack, direction)
    }

    fun getBackstack() = ArrayList(backstack)

    @JvmOverloads
    fun getChildRouter(key: Any,
                       tag: String = "") : Router {
        if (!selectActiveBackstack().contains(key)) {
            throw IllegalArgumentException("$key must be in the backstack")
        }

        var router = childRouters.firstOrNull { it.key == key && it.tag == tag }

        if (router == null) {
            router = Router().apply {
                setKey(key)
                setTag(tag)
                setParentRouter(this@Router)
                if (activityPaused) {
                    activityPaused()
                } else {
                    activityResumed()
                }
            }

            childRouters.add(router)
        }

        return router
    }

    fun removeChildRouter(childRouter: Router) {
        removeChildRouter(childRouter.key!!, childRouter.tag)
    }

    fun removeChildRouter(key: Any, tag: String = "") {
        val childRouter = childRouters.firstOrNull { it.key == key && it.tag == tag }
        if (childRouter != null) {
            childRouter.destroy()
            childRouters.remove(childRouter)
        }
    }

    fun getRootRouter(): Router {
        return if (parentRouter != null) {
            parentRouter!!.getRootRouter()
        } else {
            this
        }
    }

    fun setStateChanger(stateChanger: StateChanger) {
        this.stateChanger = stateChanger
        if (!hasPendingStateChange()) {
            val stack = ArrayList(backstack)
            backstack.clear()
            enqueueStateChange(stack, Direction.REPLACE, true)
        } else {
            beginStateChangeIfPossible()
        }
    }

    fun getStateChanger() = stateChanger

    fun removeStateChanger() {
        this.stateChanger = null
    }

    fun hasStateChanger() = stateChanger != null

    fun addStateChangeListener(stateChangeListener: StateChangeListener) {
        if (!stateChangeListeners.contains(stateChangeListener)) {
            stateChangeListeners.add(stateChangeListener)
        }
    }

    fun removeStateChangeListener(stateChangeListener: StateChangeListener) {
        stateChangeListeners.remove(stateChangeListener)
    }

    internal fun setKey(key: Any) {
        this.key = key
    }

    internal fun setTag(tag: String) {
        this.tag = tag
    }

    internal fun setParentRouter(parentRouter: Router) {
        this.parentRouter = parentRouter
    }

    internal fun activityResumed() {
        this.activityPaused = false
        beginStateChangeIfPossible()
        childRouters.forEach { it.activityResumed() }
    }

    internal fun activityPaused() {
        this.activityPaused = true
        childRouters.forEach { it.activityPaused() }
    }

    internal fun activityDestroyed() {
        this.stateChanger = null
        childRouters.forEach { it.activityDestroyed() }
    }

    internal fun saveInstanceState(outState: Bundle) {
        key?.let { outState.putParcelable(KEY_KEY, keyParceler().toParcelable(it)) }

        outState.putString(KEY_TAG, tag)

        val backstack = getBackstack()
        val parcelledKeys = backstack.map { keyParceler().toParcelable(it) }
        outState.putParcelableArrayList(KEY_BACKSTACK, ArrayList(parcelledKeys))

        outState.putIntegerArrayList(KEY_STATE_CHANGE_INDICES, stateChangeIndices)

        val childRouterBundles = ArrayList<Bundle>()
        for (childRouter in childRouters) {
            val childRouterBundle = Bundle()
            childRouter.saveInstanceState(childRouterBundle)
            childRouterBundles.add(childRouterBundle)
        }

        outState.putParcelableArrayList(KEY_CHILD_ROUTERS, childRouterBundles)

        if (getRootRouter() == this) {
            stateChangeIndexer.saveInstanceState(outState)
        }
    }

    internal fun restoreInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.getParcelable<Parcelable>(KEY_KEY)?.let {
            setKey(keyParceler().fromParcelable(it))
        }

        savedInstanceState.getString(KEY_TAG)?.let { setTag(it) }

        savedInstanceState.getParcelableArrayList<Parcelable>(KEY_BACKSTACK)?.let { parcelledKeys ->
            val keys = parcelledKeys.map { keyParceler().fromParcelable(it) }
            backstack.clear()
            backstack.addAll(keys)
        }

        savedInstanceState.getIntegerArrayList(KEY_STATE_CHANGE_INDICES)?.let {
            stateChangeIndices.addAll(it)
        }

        savedInstanceState.getParcelableArrayList<Bundle>(KEY_CHILD_ROUTERS)?.let { childRouterBundles ->
            for (childRouterBundle in childRouterBundles) {
                val childRouter = Router().apply {
                    setParentRouter(this@Router)
                    restoreInstanceState(childRouterBundle)
                }

                childRouters.add(childRouter)
            }
        }

        if (getRootRouter() == this) {
            stateChangeIndexer.restoreInstanceState(savedInstanceState)
        }
    }

    internal fun destroy() {
        setBackstack(emptyList())
        this.stateChanger = null
    }

    private fun enqueueStateChange(
        newBackstack: List<Any>,
        direction: Direction,
        rebind: Boolean = false
    ) {
        val pendingStateChange = PendingStateChange(newBackstack, direction, rebind)
        queuedStateChanges.add(pendingStateChange)

        beginStateChangeIfPossible()
    }

    private fun selectActiveBackstack(): List<Any> {
        return if(queuedStateChanges.isEmpty()) {
            ArrayList(backstack)
        } else {
            ArrayList(queuedStateChanges.last.newBackstack)
        }
    }

    private fun beginStateChangeIfPossible(): Boolean {
        if (stateChanger != null && !activityPaused && hasPendingStateChange()) {
            val pendingStateChange = queuedStateChanges.first
            if (pendingStateChange.status == PendingStateChange.Status.ENQUEUED) {
                pendingStateChange.status = PendingStateChange.Status.IN_PROGRESS
                changeState(pendingStateChange)
                return true
            }
        }

        return false
    }

    private fun changeState(pendingStateChange: PendingStateChange) {
        val newBackstack = pendingStateChange.newBackstack
        val direction = pendingStateChange.direction
        val init = pendingStateChange.init

        val previousState = if (init) {
            emptyList<Any>()
        } else {
            ArrayList(backstack)
        }

        val stateChange = StateChange(
            previousState,
            newBackstack,
            direction
        )

        // remove child routers of removed keys
        val removedRouters = childRouters.filter { !newBackstack.contains(it.key) }
        for (removedRouter in removedRouters) {
            destroy()
        }
        childRouters.removeAll(removedRouters)

        // finally start the change
        val completionCallback = object : StateChanger.CompletionCallback {
            override fun onChangeCompleted() {
                completeStateChange(stateChange, init)
            }
        }

        pendingStateChange.completionCallback = completionCallback
        stateChangeListeners.forEach { it.preStateChange(stateChange) }
        stateChanger?.handleStateChange(stateChange, completionCallback, this)
    }

    private fun completeStateChange(stateChange: StateChange,
                                    init: Boolean) {
        backstack.clear()
        backstack.addAll(stateChange.newState)

        val pendingStateChange = queuedStateChanges.removeFirst()
        pendingStateChange.status = PendingStateChange.Status.COMPLETED

        if (!init) {
            when(stateChange.direction) {
                Direction.FORWARD -> stateChangeIndices.add(getStateChangeIndexer().nextIndex())
                Direction.BACKWARD -> stateChangeIndices.removeAt(stateChangeIndices.lastIndex)
                Direction.REPLACE -> {} // ignore
            }
        }

        stateChangeListeners.forEach { it.postStateChange(stateChange) }

        beginStateChangeIfPossible()
    }

    private fun hasPendingStateChange() = queuedStateChanges.isNotEmpty()

    private fun getStateChangeIndexer() = getRootRouter().stateChangeIndexer

    private fun keyParceler() = Stacks.getKeyParceler()

    /**
     * A listener for [StateChange]'s
     */
    interface StateChangeListener {
        fun preStateChange(stateChange: StateChange)
        fun postStateChange(stateChange: StateChange)
    }

    private companion object {
        private const val KEY_BACKSTACK = "Router.backstack"
        private const val KEY_KEY = "Router.key"
        private const val KEY_TAG = "Router.tag"
        private const val KEY_CHILD_ROUTERS = "Router.childRouters"
        private const val KEY_STATE_CHANGE_INDICES = "Router.stateChangeIndices"
    }
}