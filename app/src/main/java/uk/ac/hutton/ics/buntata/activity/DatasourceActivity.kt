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

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import butterknife.ButterKnife
import uk.ac.hutton.ics.buntata.R
import uk.ac.hutton.ics.buntata.util.PreferenceUtils
import uk.ac.hutton.ics.buntata.util.SnackbarUtils

/**
 * The [DatasourceActivity] contains the [uk.ac.hutton.ics.buntata.fragment.DatasourceFragment]. It's used to select the data source.

 * @author Sebastian Raubach
 */
class DatasourceActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ButterKnife.bind(this)

        /* Prompt the user to select at least one data source */
        if (!PreferenceUtils.getPreferenceAsBoolean(this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false))
            SnackbarUtils.show(findViewById(android.R.id.content), R.string.snackbar_select_datasource, Snackbar.LENGTH_LONG)

        setSupportActionBar(toolbar)

        /* Set the toolbar as the action bar */
        /* Set the title */
        supportActionBar?.setTitle(R.string.title_activity_datasource)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
    }

    override fun getLayoutId(): Int? = R.layout.activity_datasource

    override fun getToolbarId(): Int? = R.id.toolbar

    override fun onBackPressed() {
        val newDatasourceId = PreferenceUtils.getPreferenceAsInt(this, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, -1)

        if (newDatasourceId != -1)
            setResult(Activity.RESULT_OK)

        super.onBackPressed()
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
