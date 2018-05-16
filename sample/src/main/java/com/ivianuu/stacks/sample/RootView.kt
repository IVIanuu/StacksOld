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
import com.ivianuu.stacks.Router
import com.ivianuu.stacks.views.*
import com.ivianuu.stacks.views.changehandlers.NoOpViewChangeHandler
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class RootKey(private val dummy: String = "root") : Parcelable, DefaultViewKey {
    override fun layoutRes() = R.layout.root_view
    override fun viewChangeHandler() = NoOpViewChangeHandler()
}

class RootView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ChangeHandlerFrameLayout(context, attrs, defStyleAttr) {

    private lateinit var bottomNavRouter: Router

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val router = getRouterOrThrow()

        bottomNavRouter = router.getChildRouter(getKeyOrThrow()).apply {
            if (!hasRoot()) setRoot(SplitCounterKey())
            setStateChanger(ViewStateChanger(context, this@RootView))
        }
    }
}