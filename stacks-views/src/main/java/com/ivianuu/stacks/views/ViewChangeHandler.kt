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

import android.view.View
import android.view.ViewGroup
import com.ivianuu.stacks.Direction

/**
 * An interface that represents the view change when a state change occurs.
 */
interface ViewChangeHandler {

    /**
     * Callback to notify the completion of a change
     */
    interface CompletionCallback {
        /**
         * Will be called on completion of the change
         */
        fun onCompleted()
    }

    /**
     * Perform'ss the change MUST call onCompleted after it's done
     */
    fun performViewChange(
        container: ViewGroup,
        from: View,
        to: View,
        direction: Direction,
        callback: CompletionCallback
    )
}
