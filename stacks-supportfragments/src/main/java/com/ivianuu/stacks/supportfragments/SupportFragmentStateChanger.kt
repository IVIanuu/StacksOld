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

package com.ivianuu.stacks.supportfragments

import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.View
import com.ivianuu.stacks.Direction
import com.ivianuu.stacks.Router
import com.ivianuu.stacks.StateChange
import com.ivianuu.stacks.StateChanger

/**
 * A simple [StateChanger] for [Fragment]'s
 */
open class SupportFragmentStateChanger(
    val fm: FragmentManager,
    val containerId: Int
) : StateChanger {

    private val handler = Handler()

    override fun handleStateChange(
        stateChange: StateChange,
        completionCallback: StateChanger.CompletionCallback,
        router: Router
    ) {
        handler.post {
            val previousState = stateChange.previousState
            val newState = stateChange.newState

            val transaction = fm.beginTransaction().disallowAddToBackStack()

            for (oldKey in previousState) {
                val fragment = fm.findFragmentByTag(getFragmentTag(oldKey))
                if (fragment != null) {
                    if (!newState.contains(oldKey)) {
                        transaction.remove(fragment)
                    } else if (!fragment.isDetached && newState.last() != oldKey) {
                        transaction.detach(fragment)
                    }
                }
            }

            for (newKey in newState) {
                var fragment = fm.findFragmentByTag(getFragmentTag(newKey))
                if (newKey == stateChange.newState.last()) {
                    if (fragment != null) {
                        if (fragment.isDetached) {
                            transaction.attach(fragment)
                        }
                    } else {
                        fragment = createFragment(newKey, stateChange, router)
                        transaction.add(containerId, fragment, getFragmentTag(newKey))
                    }
                } else {
                    if (fragment != null && !fragment.isDetached) {
                        transaction.detach(fragment)
                    }
                }
            }

            transaction.commitNow()
            completionCallback.onChangeCompleted()
        }
    }

    protected open fun createFragment(key: Any,
                                      stateChange: StateChange,
                                      router: Router) : Fragment {
        if (key is SupportFragmentKey) {
            return key.newInstance()
        } else {
            throw IllegalStateException("$key is not a SupportFragmentKey")
        }
    }

    protected open fun getFragmentTag(key: Any) : String {
        if (key is SupportFragmentKey) {
            return key.fragmentTag
        } else {
            throw IllegalStateException("$key is not a SupportFragmentKey")
        }
    }
}