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

@file:JvmName("StacksViews")

package com.ivianuu.stacks.views

import android.view.View
import com.ivianuu.stacks.Router

/**
 * Returns the [Router] which is associated with the [view] or null
 */
fun View.getRouter(): Router? {
    return StacksViewContextWrapper.getRouter(context)
}

/**
 * Returns the [Router] which is associated with the [view]
 */
fun View.getRouterOrThrow(): Router {
    return getRouter() ?: throw IllegalStateException("no router found for $this")
}

/**
 * Returns the key [T] which associated with the [view] or null
 */
fun <T> View.getKey(): T? {
    return StacksViewContextWrapper.getKey(context)
}

/**
 * Returns the key [T] which associated with the [view] or null
 */
fun <T> View.getKeyOrThrow(): T {
    return getKey() ?: throw IllegalStateException("no key found for $this")
}