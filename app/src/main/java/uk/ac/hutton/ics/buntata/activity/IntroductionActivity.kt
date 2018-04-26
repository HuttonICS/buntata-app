/*
 * Copyright 2016 Information & Computational Sciences, The James Hutton Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.hutton.ics.buntata.activity

import android.Manifest
import android.os.Bundle
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.app.NavigationPolicy
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import uk.ac.hutton.ics.buntata.R
import uk.ac.hutton.ics.buntata.fragment.DatasourceFragment
import uk.ac.hutton.ics.buntata.fragment.EulaFragment
import uk.ac.hutton.ics.buntata.fragment.IntroNetworkFragment
import uk.ac.hutton.ics.buntata.util.NetworkUtils
import uk.ac.hutton.ics.buntata.util.PreferenceUtils

/**
 * The [IntroductionActivity] is shown on first start. It guides the user though the initial data source selection and will show the EULA.

 * @author Sebastian Raubach
 */
class IntroductionActivity : IntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Set some preferences that are used for the navigation here initially to false */
        PreferenceUtils.setPreferenceAsBoolean(this, PreferenceUtils.PREFS_EULA_ACCEPTED, false)
        PreferenceUtils.setPreferenceAsBoolean(this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false)

        isButtonBackVisible = false

        /* Welcome slide */
        addSlide(SimpleSlide.Builder()
                .title(R.string.introduction_welcome_title)
                .description(R.string.introduction_welcome_text)
                .image(R.drawable.ic_launcher_2x)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .permission(Manifest.permission.INTERNET)
                .build())

        addSlide(FragmentSlide.Builder()
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .fragment(EulaFragment())
                .build())

        addSlide(FragmentSlide.Builder()
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .fragment(IntroNetworkFragment())
                .build())

        /* Data source selection slide */
        addSlide(FragmentSlide.Builder()
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .fragment(DatasourceFragment())
                .build())

        setNavigationPolicy(object : NavigationPolicy {
            override fun canGoForward(position: Int): Boolean {
                when (position) {
                    count - 1 -> return PreferenceUtils.getPreferenceAsBoolean(this@IntroductionActivity, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false)
                    1 -> return PreferenceUtils.getPreferenceAsBoolean(this@IntroductionActivity, PreferenceUtils.PREFS_EULA_ACCEPTED, false)
                    2 -> return NetworkUtils.hasNetworkConnection(this@IntroductionActivity)
                    else -> return true
                }
            }

            override fun canGoBackward(position: Int): Boolean = position != count - 1
        })
    }
}