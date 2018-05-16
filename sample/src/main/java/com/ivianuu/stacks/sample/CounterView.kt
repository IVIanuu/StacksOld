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

package com.ivianuu.stacks.sample

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.LinearLayout
import com.ivianuu.stacks.views.DefaultViewKey
import com.ivianuu.stacks.views.changehandlers.NoOpViewChangeHandler
import com.ivianuu.stacks.views.getKeyOrThrow
import com.ivianuu.stacks.views.getRouterOrThrow
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.counter_view.view.*

@SuppressLint("ParcelCreator")
@Parcelize
data class CounterKey(val count: Int): DefaultViewKey, Parcelable {
    override fun layoutRes() = R.layout.counter_view
    override fun viewChangeHandler() = NoOpViewChangeHandler()
}

class CounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val router = getRouterOrThrow()
        val key = getKeyOrThrow<CounterKey>()

        title.text = key.count.toString()
        add.setOnClickListener { router.goTo(CounterKey(key.count + 1)) }
        remove.setOnClickListener { router.pop() }
    }
}