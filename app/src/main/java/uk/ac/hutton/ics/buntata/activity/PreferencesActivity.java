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

package uk.ac.hutton.ics.buntata.activity;

import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.content.*;
import android.view.*;
import android.widget.*;

import com.google.android.gms.analytics.*;

import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.util.*;

public class PreferencesActivity extends BaseActivity
{
	private PrefsFragment prefsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		prefsFragment = new PrefsFragment();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(R.string.title_activity_settings);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		/* Show the fragment */
		getFragmentManager().beginTransaction()
							.replace(R.id.preferences_layout, prefsFragment)
							.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_preferences;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		prefsFragment.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View v = super.onCreateView(inflater, container, savedInstanceState);
			if (v != null)
			{
				ListView lv = v.findViewById(android.R.id.list);
				/* Remove the padding */
				lv.setPadding(0, 0, 0, 0);
			}
			return v;
		}

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			/* Load the preferences from an XML resource */
			addPreferencesFromResource(R.xml.prefs);

			/* Add listeners and set summaries */
			findPreference(PreferenceUtils.PREFS_SHOW_CHANGELOG).setOnPreferenceClickListener(this);
			findPreference(PreferenceUtils.PREFS_SHOW_EULA).setOnPreferenceClickListener(this);
			findPreference(PreferenceUtils.PREFS_COLUMNS_PORTRAIT).setSummary(getString(R.string.preferences_columns_portrait_summary, PreferenceUtils.getPreferenceAsInt(getActivity(), PreferenceUtils.PREFS_COLUMNS_PORTRAIT, 2)));
			findPreference(PreferenceUtils.PREFS_COLUMNS_LANDSCAPE).setSummary(getString(R.string.preferences_columns_landscape_summary, PreferenceUtils.getPreferenceAsInt(getActivity(), PreferenceUtils.PREFS_COLUMNS_LANDSCAPE, 3)));
		}

		@Override
		public void onResume()
		{
			super.onResume();
			/* Listen to changes to the preferences */
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause()
		{
			/* Stop listening to changes to the preferences */
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences pref, String key)
		{
			/* If the theme was changed */
			switch (key)
			{
				case PreferenceUtils.PREFS_GA_OPT_OUT:
					/* Disable GA tracking */
					GoogleAnalytics.getInstance(getActivity().getApplicationContext()).setAppOptOut(!PreferenceUtils.getPreferenceAsBoolean(getActivity(), PreferenceUtils.PREFS_GA_OPT_OUT, true));
					break;
				case PreferenceUtils.PREFS_COLUMNS_PORTRAIT:
					/* Update the summary and let the main view know that it needs to update */
					findPreference(key).setSummary(getString(R.string.preferences_columns_portrait_summary, PreferenceUtils.getPreferenceAsInt(getActivity(), key, 2)));
					MainActivity.override = true;
					break;
				case PreferenceUtils.PREFS_COLUMNS_LANDSCAPE:
					/* Update the summary and let the main view know that it needs to update */
					findPreference(key).setSummary(getString(R.string.preferences_columns_landscape_summary, PreferenceUtils.getPreferenceAsInt(getActivity(), key, 3)));
					MainActivity.override = true;
					break;
			}
		}

		@Override
		public boolean onPreferenceClick(Preference preference)
		{
			String key = preference.getKey();

			switch (key)
			{
				case PreferenceUtils.PREFS_SHOW_CHANGELOG:
					startActivity(new Intent(getActivity(), ChangelogActivity.class));

					if (getActivity() instanceof BaseActivity)
						GoogleAnalyticsUtils.trackEvent(getActivity(), ((BaseActivity) getActivity()).getTracker(TrackerName.APP_TRACKER), getString(R.string.ga_event_category_preferences), getString(R.string.ga_event_action_show_changelog));

					return true;

				case PreferenceUtils.PREFS_SHOW_EULA:

					startActivity(new Intent(getActivity(), EulaActivity.class));

					GoogleAnalyticsUtils.trackEvent(getActivity(), ((BaseActivity) getActivity()).getTracker(TrackerName.APP_TRACKER),  getString(R.string.ga_event_category_preferences), getString(R.string.ga_event_action_show_eula));
					return true;
			}

			return false;
		}
	}
}