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

package com.ivianuu.stacks.views.changehandlers

import android.annotation.TargetApi
import android.os.Build
import android.transition.Transition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import com.ivianuu.stacks.Direction
import com.ivianuu.stacks.views.ViewChangeHandler


/**
 * A [ViewChangeHandler] which uses [Transition]'s
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
abstract class TransitionViewChangeHandler @JvmOverloads constructor(
    private val duration: Long = -1
) : ViewChangeHandler {
    override fun performViewChange(
        container: ViewGroup,
        from: View,
        to: View,
        direction: Direction,
        callback: ViewChangeHandler.CompletionCallback
    ) {
        container.addView(to)
        runAnimation(container, from, to, direction, object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }
            override fun onTransitionPause(transition: Transition) {
            }
            override fun onTransitionResume(transition: Transition) {
            }
            override fun onTransitionCancel(transition: Transition?) {
            }

            override fun onTransitionEnd(transition: Transition?) {
                container.removeView(from)
                callback.onCompleted()
            }
        })
    }

    protected abstract fun createTransition(
        container: ViewGroup,
        from: View,
        to: View,
        direction: Direction
    ): Transition

    private fun runAnimation(
        container: ViewGroup,
        from: View,
        to: View,
        direction: Direction,
        transitionListener: Transition.TransitionListener
    ) {
        createTransition(container, from, to, direction).run {
            if (this@TransitionViewChangeHandler.duration != -1L) {
                duration = this@TransitionViewChangeHandler.duration
            }
            addListener(transitionListener)
            TransitionManager.beginDelayedTransition(container, this)
        }
    }

}