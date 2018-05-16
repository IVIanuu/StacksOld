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

package com.ivianuu.stacks.internal

import android.app.Activity
import android.app.Application
import android.app.Fragment
import android.os.Bundle
import com.ivianuu.stacks.Router

/**
 * Holds the top level [Router]'s and retains them trough config changes
 * Also delegates save instance methods to them
 * This class is internal and you should not worry about it
 */
class RouterHolder : Fragment(), Application.ActivityLifecycleCallbacks {

    private val routerMap = HashMap<String, Router>()

    private var act: Activity? = null
    private var hasRegisteredCallbacks = false

    init {
        retainInstance = true
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        if (this.act == activity) {
            routerMap.values.forEach { it.activityResumed() }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (this.act == activity) {
            routerMap.values.forEach { it.activityPaused() }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (this.act == activity) {
            routerMap.forEach { (tag, router) ->
                val bundle = Bundle()
                router.saveInstanceState(bundle)
                outState.putBundle(KEY_ROUTER_PREFIX + tag, bundle)
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (this.act == activity) {
            routerMap.values.forEach { it.activityDestroyed() }

            activity.application.unregisterActivityLifecycleCallbacks(this)
            this.hasRegisteredCallbacks = false

            this.act = null
        }

        activeBackstackHolders.remove(activity)
    }

    internal fun getRouter(savedInstanceState: Bundle?,
                           tag: String) : Router {
        return routerMap.getOrPut(tag) {
            Router().apply {
                setTag(tag)

                // restore
                if (savedInstanceState != null) {
                    val bundle = savedInstanceState.getBundle(KEY_ROUTER_PREFIX + tag)
                    if (bundle != null) {
                        restoreInstanceState(bundle)
                    }
                }
            }
        }
    }

    private fun registerActivityListener(activity: Activity) {
        this.act = activity

        if (!hasRegisteredCallbacks) {
            hasRegisteredCallbacks = true
            activity.application.registerActivityLifecycleCallbacks(this)
            activeBackstackHolders[activity] = this
        }
    }

    companion object {
        private const val FRAGMENT_TAG = "RouterHolder"
        private const val KEY_ROUTER_PREFIX = "Backstack.routerState"

        private val activeBackstackHolders = HashMap<Activity, RouterHolder>()

        internal fun install(activity: Activity): RouterHolder {
            var backstackHolder =
                findInActivity(activity)
            if (backstackHolder == null) {
                backstackHolder = RouterHolder()
                activity.fragmentManager.beginTransaction()
                    .add(backstackHolder, FRAGMENT_TAG).commit()
            }

            backstackHolder.registerActivityListener(activity)

            return backstackHolder
        }

        private fun findInActivity(activity: Activity): RouterHolder? {
            var backstackHolder = activeBackstackHolders[activity]
            if (backstackHolder == null) {
                backstackHolder = activity.fragmentManager
                    .findFragmentByTag(FRAGMENT_TAG) as RouterHolder?
            }

            return backstackHolder
        }
    }

}