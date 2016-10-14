/**
 * Germinate Mobile is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group
 * at JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p/>
 * Copyright (c) 2012-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.knodel.util;

import android.graphics.*;
import android.support.annotation.*;
import android.support.design.widget.*;
import android.view.*;
import android.widget.*;

/**
 * @author Sebastian Raubach
 */
public class SnackbarUtils
{
	public static void show(View parent, int message, @ColorInt int textColor, @ColorInt int backgroundColor, int length)
	{
		create(parent, message, textColor, backgroundColor, length).show();
	}

	public static void show(View parent, String message, @ColorInt int textColor, @ColorInt int backgroundColor, int length)
	{
		create(parent, message, textColor, backgroundColor, length).show();
	}

	public static void show(View parent, int message, int length)
	{
		show(parent, message, Color.WHITE, Color.BLACK, length);
	}

	public static void show(View parent, String message, int length)
	{
		show(parent, message, Color.WHITE, Color.BLACK, length);
	}

	public static Snackbar create(View parent, int message, @ColorInt int textColor, @ColorInt int backgroundColor, int length)
	{
		Snackbar snackbar = Snackbar.make(parent, message, length);
		customizeSnackbar(snackbar, textColor, backgroundColor);

		return snackbar;
	}

	public static Snackbar create(View parent, String message, @ColorInt int textColor, @ColorInt int backgroundColor, int length)
	{
		Snackbar snackbar = Snackbar.make(parent, message, length);
		customizeSnackbar(snackbar, textColor, backgroundColor);

		return snackbar;
	}

	public static Snackbar create(View parent, int message, int length)
	{
		return create(parent, message, Color.WHITE, Color.BLACK, length);
	}

	public static Snackbar create(View parent, String message, int length)
	{
		return create(parent, message, Color.WHITE, Color.BLACK, length);
	}

	private static void customizeSnackbar(Snackbar snackbar, @ColorInt int textColor, @ColorInt int backgroundColor)
	{
		View view = snackbar.getView();
		TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
		tv.setTextColor(textColor);
		view.setBackgroundColor(backgroundColor);
	}
}
