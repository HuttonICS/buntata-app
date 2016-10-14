/**
 * Germinate Mobile is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group
 * at JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p/>
 * Copyright (c) 2012-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.knodel.util;

import android.content.*;

import com.google.android.gms.analytics.*;

import uk.ac.hutton.ics.knodel.activity.*;

/**
 * Utility class for Google Analytics. Contains methods to track events.
 *
 * @author Sebastian Raubach
 */
public class GoogleAnalyticsUtils
{
	/**
	 * Sends an event to the Google Analytics server
	 *
	 * @param context  The calling {@link Context}
	 * @param tracker  The {@link Tracker}
	 * @param category The category
	 * @param action   The action
	 */
	public static void trackEvent(Context context, Tracker tracker, String category, String action)
	{
		trackEvent(context, tracker, category, action, null);
	}

	/**
	 * Sends an event to the Google Analytics server
	 *
	 * @param context  The calling {@link Context}
	 * @param tracker  The {@link Tracker}
	 * @param category The category
	 * @param action   The action
	 * @param label    The label
	 */
	public static void trackEvent(Context context, Tracker tracker, String category, String action, String label)
	{
		trackEvent(context, tracker, category, action, label, null);
	}

	/**
	 * Sends an event to the Google Analytics server
	 *
	 * @param context  The calling {@link Context}
	 * @param tracker  The {@link Tracker}
	 * @param category The category
	 * @param action   The action
	 * @param label    The label
	 * @param value    The value
	 */
	public static void trackEvent(Context context, Tracker tracker, String category, String action, String label, Long value)
	{
		if (tracker == null)
		{
			return;
		}

        /* If we're using the debug version, don't track to Google Analytics */
		String packageName = BaseActivity.INSTANCE.getPackageName();
		/* Also, if the user disabled tracking, we don't track to Google Analytics */
		if (packageName != null && packageName.endsWith(".debug") || !PreferenceUtils.getPreferenceAsBoolean(context, PreferenceUtils.PREFS_GA_OPT_OUT, true))
		{
			return;
		}

		HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder().setCategory(category).setAction(action);

		if (!StringUtils.isEmpty(label))
		{
			builder.setLabel(label);
		}

		if (value != null)
		{
			builder.setValue(value);
		}

        /* Build and send an Event */
		tracker.send(builder.build());
	}
}
