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

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import com.ivianuu.stacks.Router

/**
 * A [ContextWrapper] for inflating [View]'s, containing the key and router inside it.
 */
class StacksViewContextWrapper(
    base: Context,
    private val key: Any,
    private val router: Router
) : ContextWrapper(base) {

    private val layoutInflater by lazy(LazyThreadSafetyMode.NONE) {
        LayoutInflater.from(baseContext).cloneInContext(this)
    }

    override fun getSystemService(name: String): Any? {
        return when {
            Context.LAYOUT_INFLATER_SERVICE == name -> layoutInflater
            KEY_KEY == name -> key
            KEY_ROUTER == name -> router
            else -> super.getSystemService(name)
        }
    }

    companion object {
        private const val KEY_KEY = "Router.key"
        private const val KEY_ROUTER = "Router.router"

        /**
         * Returns the key if present
         */
        @Suppress("UNCHECKED_CAST")
        @SuppressLint("WrongConstant")
        @JvmStatic
        fun <T> getKey(context: Context): T? {
            val key = context.getSystemService(KEY_KEY)
            return key as T?
        }

        /**
         * Returns the router if present
         */
        @SuppressLint("WrongConstant")
        @JvmStatic
        fun getRouter(context: Context): Router? {
            val router = context.getSystemService(KEY_ROUTER)
            return router as Router?
        }
    }
}
