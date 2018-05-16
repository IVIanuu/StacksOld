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
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ivianuu.stacks.StateChange

/**
 * A [FrameLayout] which blocks touch events while a [ViewChangeHandler] does it's job
 * Just add it as a [ViewStateChanger.ViewChangeListener] in the [ViewStateChanger]
 */
open class ChangeHandlerFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ViewStateChanger.ViewChangeListener {

    private var changeCount = 0

    override fun preViewChange(
        stateChange: StateChange,
        container: ViewGroup,
        previousView: View?,
        newView: View
    ) {
        changeCount++
    }

    override fun postViewChange(
        stateChange: StateChange,
        container: ViewGroup,
        previousView: View?,
        newView: View
    ) {
        changeCount--
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean =
        changeCount > 0 || super.onInterceptTouchEvent(ev)

}