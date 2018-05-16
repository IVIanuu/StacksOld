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

package com.ivianuu.stacks

import android.app.Activity
import android.os.Bundle
import com.ivianuu.stacks.internal.RouterHolder

/**
 * Access point to attach [Router]'s
 */
object Stacks {

    private var keyParceler: KeyParceler = DefaultKeyParceler()

    /**
     * Attaches a new [Router]
     */
    @JvmStatic
    @JvmOverloads
    fun attachRouter(activity: Activity,
                     savedInstanceState: Bundle?,
                     tag: String = ""
    ): Router {
        val backstackHolder = RouterHolder.install(activity)
        return backstackHolder.getRouter(savedInstanceState, tag)
    }

    /**
     * Sets the global key parceler
     * If your keys are not parcelable this should called before the first call to [attachRouter]
     */
    fun setKeyParceler(keyParceler: KeyParceler) {
        this.keyParceler = keyParceler
    }

    /**
     * Returns the current [KeyParceler]
     */
    fun getKeyParceler() = keyParceler

}