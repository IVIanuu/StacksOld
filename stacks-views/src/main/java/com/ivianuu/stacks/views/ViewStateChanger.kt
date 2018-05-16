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

package com.ivianuu.stacks.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivianuu.stacks.Direction
import com.ivianuu.stacks.Router
import com.ivianuu.stacks.StateChange
import com.ivianuu.stacks.StateChanger
import com.ivianuu.stacks.views.changehandlers.NoOpViewChangeHandler

/**
 * The default [StateChanger] for [View]'s
 */
open class ViewStateChanger constructor(
    val baseContext: Context,
    val container: ViewGroup
) : StateChanger {

    protected val viewStateManager = ViewStateManager()
    private val viewChangeListeners = ArrayList<ViewChangeListener>()

    init {
        if (container is ViewChangeListener) {
            viewChangeListeners.add(container)
        }
    }

    override fun handleStateChange(
        stateChange: StateChange,
        completionCallback: StateChanger.CompletionCallback,
        router: Router
    ) {
        if (stateChange.newState.isEmpty()) {
            container.removeAllViews()
            completionCallback.onChangeCompleted()
            return
        }

        val newKey = stateChange.newState.last()
        val previousKey = stateChange.previousState.lastOrNull()

        val direction = stateChange.direction

        // persist previous view state
        val previousView = container.getChildAt(0)
        if (previousKey != null && previousView != null) {
            persistViewState(previousKey, previousView)
        }

        // inflate new view
        val newContext = createContext(newKey, stateChange, router)
        val newView = createView(newKey, stateChange, newContext, container)

        // restore new view
        restoreViewState(newKey, newView)

        viewChangeListeners.forEach {
            it.preViewChange(stateChange, container, previousView, newView)
        }

        if (previousView == null
            || previousKey == null) {
            // no previous view just add the new one
            container.addView(newView)
            completionCallback.onChangeCompleted()
            viewChangeListeners.forEach {
                it.postViewChange(stateChange, container, previousView, newView)
            }
        } else {
            // transition required get change handler
            val changeHandler = createChangeHandler(
                stateChange,
                previousKey,
                newKey,
                previousView,
                newView,
                direction)

            // let the change handler perform the change
            changeHandler.performViewChange(container,
                previousView,
                newView,
                direction,
                object : ViewChangeHandler.CompletionCallback {
                    override fun onCompleted() {
                        completionCallback.onChangeCompleted()
                        viewChangeListeners.forEach {
                            it.postViewChange(stateChange, container, previousView, newView)
                        }
                    }
                })
        }
    }

    fun addViewChangeListener(viewChangeListener: ViewChangeListener) {
        if (viewChangeListeners.contains(viewChangeListener)) {
            viewChangeListeners.add(viewChangeListener)
        }
    }

    fun removeViewChangeListener(viewChangeListener: ViewChangeListener) {
        viewChangeListeners.remove(viewChangeListener)
    }

    protected open fun createContext(key: Any,
                                     stateChange: StateChange,
                                     router: Router) : Context {
        return StacksViewContextWrapper(baseContext, key, router)
    }

    protected open fun createView(key: Any,
                                  stateChange: StateChange,
                                  context: Context,
                                  container: ViewGroup) : View {
        if (key is DefaultViewKey) {
            val inflater = LayoutInflater.from(context)
            return inflater.inflate(key.layoutRes(), container, false)
        } else {
            throw IllegalStateException("$key is not a ViewKey")
        }
    }

    protected open fun persistViewState(key: Any, view: View) {
        viewStateManager.persistViewState(key, view)
    }

    protected open fun restoreViewState(key: Any, view: View) {
        viewStateManager.restoreViewState(key, view)
    }

    protected open fun createChangeHandler(
        stateChange: StateChange,
        previousKey: Any,
        newKey: Any,
        previousView: View,
        newView: View,
        direction: Direction
    ) : ViewChangeHandler {
        return when {
            direction == Direction.FORWARD && newKey is DefaultViewKey -> newKey.viewChangeHandler()
            direction == Direction.BACKWARD && previousKey is DefaultViewKey -> previousKey.viewChangeHandler()
            else -> NO_OP_VIEW_CHANGE_HANDLER
        }
    }

    /**
     * A view change listener
     */
    interface ViewChangeListener {

        fun preViewChange(
            stateChange: StateChange,
            container: ViewGroup,
            previousView: View?,
            newView: View
        )

        fun postViewChange(
            stateChange: StateChange,
            container: ViewGroup,
            previousView: View?,
            newView: View
        )

    }

    companion object {
        private val NO_OP_VIEW_CHANGE_HANDLER = NoOpViewChangeHandler()
    }
}