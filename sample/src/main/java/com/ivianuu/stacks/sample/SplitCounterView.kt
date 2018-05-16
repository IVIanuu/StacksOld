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
import com.ivianuu.stacks.Router
import com.ivianuu.stacks.views.DefaultViewKey
import com.ivianuu.stacks.views.ViewStateChanger
import com.ivianuu.stacks.views.changehandlers.NoOpViewChangeHandler
import com.ivianuu.stacks.views.getKeyOrThrow
import com.ivianuu.stacks.views.getRouterOrThrow
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.split_counter.view.*

/**
 * @author Manuel Wrage (IVIanuu)
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class SplitCounterKey(private val id: String = "") : Parcelable, DefaultViewKey {
    override fun layoutRes() = R.layout.split_counter
    override fun viewChangeHandler() = NoOpViewChangeHandler()
}

class SplitCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var topRouter: Router
    private lateinit var bottomRouter: Router

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val router = getRouterOrThrow()
        val key = getKeyOrThrow<SplitCounterKey>()

        topRouter = router.getChildRouter(key, "top").apply {
            if (!hasRoot()) setRoot(CounterKey(1))
            setStateChanger(ViewStateChanger(context, top_container))
        }

        bottomRouter = router.getChildRouter(key, "bottom").apply {
            if (!hasRoot()) setRoot(CounterKey(1))
            setStateChanger(ViewStateChanger(context, bottom_container))
        }
    }

}