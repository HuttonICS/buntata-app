/*
 * Copyright (c) 2016 Information & Computational Sciences, The James Hutton Institute
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

package uk.ac.hutton.ics.knodel.util;

import android.app.*;
import android.content.*;

/**
 * {@link uk.ac.hutton.ics.knodel.util.DialogUtils} contains methods to easily create {@link AlertDialog}s.
 *
 * @author Sebastian Raubach
 */
public class DialogUtils
{
	/**
	 * Ceates an {@link AlertDialog} with the given title, message and positive/negative button handling
	 *
	 * @param context          The current {@link Activity}
	 * @param title            The title resource to use
	 * @param message          The message resource to use
	 * @param positiveText     The positive button resource to use
	 * @param negativeText     The negative button resource to use
	 * @param positiveListener The positive button {@link android.content.DialogInterface.OnClickListener}
	 * @param negagiveListener The negative button {@link android.content.DialogInterface.OnClickListener}
	 */
	public static void showDialog(Context context, int title, int message, int positiveText, int negativeText, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negagiveListener)
	{
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(positiveText, positiveListener)
				.setNegativeButton(negativeText, negagiveListener)
				.show();
	}

	/**
	 * Creates an {@link AlertDialog} with the given title and message.
	 *
	 * @param context The current {@link Activity}
	 * @param title   The title resource to use
	 * @param error   The error resource to use
	 * @param finish  Finish the {@link Activity} after showing the {@link AlertDialog}?
	 */
	public static void showDialog(Activity context, int title, int error, boolean finish)
	{
		showDialog(context, title, context.getString(error), finish);
	}

	/**
	 * Creates an {@link AlertDialog} with the given title and message.
	 *
	 * @param context  The current {@link Activity}
	 * @param title    The title resource to use
	 * @param error    The error resource to use (prepared for formatting via String.format())
	 * @param finish   Finish the {@link Activity} after showing the {@link AlertDialog}?
	 * @param addition The Strings to use within String.format() on the dialog message
	 */
	public static void showDialog(Activity context, int title, int error, boolean finish, String... addition)
	{
		String resource = context.getString(error);
		resource = String.format(resource, (Object[]) addition);

		showDialog(context, title, resource, finish);
	}

	/**
	 * Creates an {@link AlertDialog} with the given title and message.
	 *
	 * @param context The current {@link Activity}
	 * @param title   The title resource to use
	 * @param message The message String to use
	 * @param finish  Finish the {@link Activity} after showing the {@link AlertDialog}?
	 */
	public static void showDialog(final Activity context, int title, String message, final boolean finish)
	{
		new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (finish)
				{
					context.finish();
				}
			}
		}).setCancelable(!finish).show();
	}
}
