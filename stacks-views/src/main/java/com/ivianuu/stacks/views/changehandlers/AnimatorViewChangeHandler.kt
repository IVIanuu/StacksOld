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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup
import com.ivianuu.stacks.Direction
import com.ivianuu.stacks.views.ViewChangeHandler

/**
 * A [ViewChangeHandler] which uses [Animator]'s
 */
abstract class AnimatorViewChangeHandler @JvmOverloads constructor(
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
        to.waitForMeasure {
            runAnimation(from, to, direction, object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    container.removeView(from)
                    callback.onCompleted()
                }
            })
        }
    }

    protected abstract fun createAnimator(
        from: View,
        to: View,
        direction: Direction
    ): Animator

    private fun runAnimation(
        from: View,
        to: View,
        direction: Direction,
        animatorListenerAdapter: AnimatorListenerAdapter
    ) {
        createAnimator(from, to, direction).run {
            if (this@AnimatorViewChangeHandler.duration != -1L) {
                duration = this@AnimatorViewChangeHandler.duration
            }
            addListener(animatorListenerAdapter)
            start()
        }
    }

}
