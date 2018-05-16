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

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View

/**
 * Manages the [SavedState]'s of [View]'s
 */
class ViewStateManager {

    private val viewStates = HashMap<Any, SavedState>()

    fun persistViewState(key: Any, view: View) {
        val viewHierarchyState = SparseArray<Parcelable>()
        view.saveHierarchyState(viewHierarchyState)

        val bundle = if (view is Bundleable) {
            val bundle = Bundle()
            view.toBundle(bundle)
            bundle
        } else {
            null
        }

        val savedState = SavedState(
            key,
            viewHierarchyState,
            bundle
        )

        viewStates[view] = savedState
    }

    fun restoreViewState(key: Any, view: View) {
        val savedState = getSavedState(key)
        view.restoreHierarchyState(savedState.viewHierarchyState)
        if (view is Bundleable) {
            savedState.bundle?.let { view.fromBundle(it) }
        }
    }

    private fun getSavedState(key: Any): SavedState {
        return viewStates.getOrPut(key) { SavedState(key) }
    }
}