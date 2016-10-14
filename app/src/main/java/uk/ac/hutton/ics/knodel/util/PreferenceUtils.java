package uk.ac.hutton.ics.knodel.util;

import android.content.*;
import android.preference.*;

import com.google.android.gms.analytics.*;

/**
 * @author Sebastian Raubach
 */

public class PreferenceUtils
{
	public static final String PREFS_KNODEL_SERVER_URL       = "prefs.general.restlet.api.url";
	public static final String PREFS_AT_LEAST_ONE_DATASOURCE = "prefs.at.least.one.datasource";
	public static final String PREFS_SELECTED_DATASOURCE_ID  = "prefs.selected.datasource.id";
	public static final String PREFS_GA_OPT_OUT              = "prefs.google.analytics.opt.out";

	public static final String DEFAULT_PREF_KNODEL_SERVER_URL = "https://ics.hutton.ac.uk/knodel/v1/";
//public static final String DEFAULT_PREF_KNODEL_SERVER_URL = "http://wildcat.hutton.ac.uk:8080/knodel/v1/";

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
		{
			editor.putBoolean(PREFS_GA_OPT_OUT, true);
		}

		if (!preferences.contains(PREFS_AT_LEAST_ONE_DATASOURCE))
		{
			editor.putBoolean(PREFS_AT_LEAST_ONE_DATASOURCE, false);
		}

//		if (!preferences.contains(PREFS_KNODEL_SERVER_URL))
//		{
		editor.putString(PREFS_KNODEL_SERVER_URL, DEFAULT_PREF_KNODEL_SERVER_URL);
//		}

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
