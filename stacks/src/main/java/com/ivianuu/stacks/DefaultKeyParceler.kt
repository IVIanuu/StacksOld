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

import android.os.Parcelable

/**
 * Default implementation of an [KeyParceler] this assumes that all your keys are [Parcelable]
 */
open class DefaultKeyParceler : KeyParceler {
    override fun toParcelable(key: Any): Parcelable = key as Parcelable
    override fun fromParcelable(parcelable: Parcelable): Any = parcelable
}