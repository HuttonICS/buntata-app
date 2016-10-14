/**
 * Germinate Mobile is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group
 * at JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p/>
 * Copyright (c) 2012-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.knodel.util;

import android.content.*;
import android.widget.*;

/**
 * {@link uk.ac.hutton.ics.knodel.util.ToastUtils} contains utility functions for showing {@link Toast}s. Calling one of
 * these functions will cancel the currently shown {@link Toast} (if any) and show the new one.
 *
 * @author Sebastian Raubach
 */
public class ToastUtils
{
	public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
	public static final int LENGTH_LONG  = Toast.LENGTH_LONG;

	private static Toast toast;

	/**
	 * Creates a new toast message while canceling all old ones
	 *
	 * @param context  The context to use. Usually your Application or Activity object.
	 * @param text     The text to show. Can be formatted text.
	 * @param duration How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 */
	public static void createToast(Context context, CharSequence text, int duration)
	{
		/* If there's already a toast, cancel it */
		if (toast != null)
		{
			toast.cancel();
		}

        /* Create and show the toast */
		toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	/**
	 * Creates a new toast message while canceling all old ones
	 *
	 * @param context  The context to use. Usually your Application or Activity object.
	 * @param text     The resource id of the string resource to use. Can be formatted text.
	 * @param duration How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 */
	public static void createToast(Context context, int text, int duration)
	{
		/* If there's already a toast, cancel it */
		if (toast != null)
		{
			toast.cancel();
		}

        /* Create and show the toast */
		toast = Toast.makeText(context, text, duration);
		toast.show();
	}
}
