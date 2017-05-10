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

package uk.ac.hutton.ics.buntata.util;

import android.content.*;
import android.preference.*;

import com.google.android.gms.analytics.*;

/**
 * @author Sebastian Raubach
 */

public class PreferenceUtils
{
	public static final String PREFS_KNODEL_SERVER_URL              = "prefs.general.restlet.api.url";
	public static final String PREFS_AT_LEAST_ONE_DATASOURCE        = "prefs.at.least.one.datasource";
	public static final String PREFS_SHOW_DATASOURCE_SELECTION_HELP = "prefs.show.datasource.selection.help";
	public static final String PREFS_EULA_ACCEPTED                  = "prefs.eula.accepted";
	public static final String PREFS_EULA_TYPE                      = "prefs.eula.type";
	public static final String PREFS_SELECTED_DATASOURCE_ID         = "prefs.selected.datasource.id";
	public static final String PREFS_GA_OPT_OUT                     = "prefs.google.analytics.opt.out";
	public static final String PREFS_LAST_VERSION                   = "prefs.last.version.code";
	public static final String PREFS_SHOW_CHANGELOG                 = "prefs.show.changelog";
	public static final String PREFS_COLUMNS_PORTRAIT               = "prefs.columns.portrait";
	public static final String PREFS_COLUMNS_LANDSCAPE              = "prefs.columns.landscape";

	private static final String DEFAULT_PREF_SERVER_URL = "https://ics.hutton.ac.uk/buntata/v1.1/";

	/**
	 * Sets the defaults for all preferences that don't have a value yet
	 *
	 * @param context The reference {@link android.content.Context}
	 */
	public static void setDefaults(final Context context)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();

		GoogleAnalytics.getInstance(context).setAppOptOut(!getPreferenceAsBoolean(context, PREFS_GA_OPT_OUT, true));

		if (!preferences.contains(PREFS_GA_OPT_OUT))
			editor.putBoolean(PREFS_GA_OPT_OUT, true);
		if (!preferences.contains(PREFS_AT_LEAST_ONE_DATASOURCE))
			editor.putBoolean(PREFS_AT_LEAST_ONE_DATASOURCE, false);
		if (!preferences.contains(PREFS_COLUMNS_PORTRAIT))
			editor.putInt(PREFS_COLUMNS_PORTRAIT, 2);
		if (!preferences.contains(PREFS_COLUMNS_LANDSCAPE))
			editor.putInt(PREFS_COLUMNS_LANDSCAPE, 3);
		if (!preferences.contains(PREFS_SHOW_DATASOURCE_SELECTION_HELP))
			editor.putBoolean(PREFS_SHOW_DATASOURCE_SELECTION_HELP, true);

		editor.putString(PREFS_KNODEL_SERVER_URL, DEFAULT_PREF_SERVER_URL);

		editor.apply();
	}

	public static void removePreference(Context context, String pref)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = preferences.edit();

		edit.remove(pref);
		edit.apply();
	}

	/**
	 * Returns the value of the given preference
	 *
	 * @param context The reference {@link android.content.Context}
	 * @param pref    The preference key as specified by one of the class constants
	 * @return The value of the preference
	 */
	public static String getPreference(Context context, String pref)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString(pref, "");
	}

	/**
	 * Returns the value of the given preference as an integer
	 *
	 * @param context  The reference {@link android.content.Context}
	 * @param pref     The preference key as specified by one of the class constants as an integer
	 * @param fallback The fallback value if the property isn't set
	 * @return The value of the preference
	 */
	public static int getPreferenceAsInt(Context context, String pref, int fallback)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getInt(pref, fallback);
	}

	/**
	 * Returns the value of the given preference as a boolean
	 *
	 * @param context  The reference {@link android.content.Context}
	 * @param pref     The preference key as specified by one of the class constants as a boolean
	 * @param fallback The fallback value if the property isn't set
	 * @return The value of the preference
	 */
	public static boolean getPreferenceAsBoolean(Context context, String pref, boolean fallback)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(pref, fallback);
	}

	/**
	 * Sets the given value to the given preference
	 *
	 * @param context The reference {@link android.content.Context}
	 * @param pref    The preference key
	 * @param value   The preference value
	 */
	public static void setPreference(Context context, String pref, String value)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = preferences.edit();

		edit.putString(pref, value);
		edit.apply();
	}

	/**
	 * Sets the given value to the given preference as an int
	 *
	 * @param context The reference {@link android.content.Context}
	 * @param pref    The preference key
	 * @param value   The preference value (int)
	 */
	public static void setPreferenceAsInt(Context context, String pref, int value)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = preferences.edit();

		edit.putInt(pref, value);
		edit.apply();
	}

	/**
	 * Sets the given value to the given preference as a boolean
	 *
	 * @param context The reference {@link android.content.Context}
	 * @param pref    The preference key
	 * @param value   The preference value (boolean)
	 */
	public static void setPreferenceAsBoolean(Context context, String pref, boolean value)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = preferences.edit();

		edit.putBoolean(pref, value);
		edit.apply();
	}
}
