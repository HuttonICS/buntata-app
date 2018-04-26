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

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import uk.ac.hutton.ics.buntata.R
import uk.ac.hutton.ics.buntata.util.PreferenceUtils

/**
 * @author Sebastian Raubach
 */
class ChangelogActivity : BaseActivity() {
    override fun getLayoutId(): Int? = R.layout.activity_changelog

    override fun getToolbarId(): Int? = R.id.toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Set the toolbar as the action bar */
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        // Get the versionCode of the Package, which must be different (incremented) in each release on the market in the AndroidManifest.xml
        val packageInfo: PackageInfo
        try {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            PreferenceUtils.setPreferenceAsInt(this, PreferenceUtils.PREFS_LAST_VERSION, packageInfo.versionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
