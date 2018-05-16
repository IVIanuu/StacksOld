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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.ivianuu.stacks.Direction
import com.ivianuu.stacks.views.ViewChangeHandler

/**
 * A [ViewChangeHandler] which animates the translation x based on the direction
 */
open class HorizontalViewChangeHandler @JvmOverloads constructor(
    duration: Long = -1
) : AnimatorViewChangeHandler(duration) {
    override fun createAnimator(from: View, to: View, direction: Direction): Animator {
        return AnimatorSet().apply {
            if (direction == Direction.FORWARD) {
                play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, -from.width.toFloat()))
                play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, to.width.toFloat(), 0f))
            } else if (direction == Direction.BACKWARD) {
                play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, from.width.toFloat()))
                play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X,
                    from.translationX - to.width.toFloat(), 0f)
                )
            }
        }
    }

}
