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
