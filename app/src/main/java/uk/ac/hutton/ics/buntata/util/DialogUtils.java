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
import android.support.v7.app.*;

/**
 * {@link uk.ac.hutton.ics.buntata.util.DialogUtils} contains methods to easily create {@link AlertDialog}s.
 *
 * @author Sebastian Raubach
 */
public class DialogUtils
{
	/**
	 * Ceates an {@link AlertDialog} with the given title, message and positive/negative button handling
	 *
	 * @param context          The current {@link Context}
	 * @param title            The title resource to use
	 * @param message          The message resource to use
	 * @param positiveText     The positive button resource to use
	 * @param negativeText     The negative button resource to use
	 * @param positiveListener The positive button {@link android.content.DialogInterface.OnClickListener}
	 * @param negagiveListener The negative button {@link android.content.DialogInterface.OnClickListener}
	 */
	public static void showDialog(Context context, int title, int message, int positiveText, int negativeText, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negagiveListener)
	{
		showDialog(context, context.getString(title), context.getString(message), context.getString(positiveText), context.getString(negativeText), positiveListener, negagiveListener);
	}

	/**
	 * Ceates an {@link AlertDialog} with the given title, message and positive/negative button handling
	 *
	 * @param context          The current {@link Context}
	 * @param title            The title text to use
	 * @param message          The message text to use
	 * @param positiveText     The positive button text to use
	 * @param negativeText     The negative button text to use
	 * @param positiveListener The positive button {@link android.content.DialogInterface.OnClickListener}
	 * @param negagiveListener The negative button {@link android.content.DialogInterface.OnClickListener}
	 */
	public static void showDialog(Context context, String title, String message, String positiveText, String negativeText, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negagiveListener)
	{
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(positiveText, positiveListener)
				.setNegativeButton(negativeText, negagiveListener)
				.show();
	}
}
