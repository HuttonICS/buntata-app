/**
 * Germinate Mobile is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group
 * at JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p/>
 * Copyright (c) 2012-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.knodel.thread;

import android.app.*;
import android.content.*;
import android.os.*;

import org.restlet.resource.*;

import uk.ac.hutton.ics.knodel.util.*;

/**
 * RestletAsyncTask is an abstract version of an AsyncTask that requests data from the Restlet server.
 * <p/>
 * It will catch Restlet related exceptions and store them for the child class to examine.
 *
 * @author Sebastian Raubach
 */
public abstract class RestletAsyncTask<U, T> extends AsyncTask<U, Integer, T>
{
	private ResourceException exception = null;

	private ProgressDialog progressDialog;

	private Context context;

	private int progressStyle = -1;

	private int parameterCount = 1;
	private int progress       = 0;

	private boolean cancelable = true;

	public RestletAsyncTask(Context context, int progressStyle, boolean cancelable)
	{
		this(context, progressStyle);
		this.cancelable = cancelable;
	}

	public RestletAsyncTask(Context context, int progressStyle)
	{
		this.context = context;
		this.progressStyle = progressStyle;
	}

	@Override
	protected void onPreExecute()
	{
		/* Check if the style is valid */
		if (progressStyle == ProgressDialog.STYLE_SPINNER || progressStyle == ProgressDialog.STYLE_HORIZONTAL)
		{
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Please wait"); // TODO: context.getString(R.string.dialog_title_restlet_please_wait)
			progressDialog.setProgressStyle(progressStyle);
			progressDialog.setCancelable(cancelable);

			if(cancelable)
			{
				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() // TODO: context.getString(R.string.general_cancel)
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					/* Cancel the async task if the progress dialog is cancelled */
						RestletAsyncTask.this.cancel(true);
					}
				});
			}

			progressDialog.show();
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		if (progressDialog != null)
		{
			/* Set the progress */
			progress += values[0];
			int percent = Math.round(progress / (1f * parameterCount) * 100);

			progressDialog.setProgress(percent);
		}
	}

	@SafeVarargs
	@Override
	protected final T doInBackground(U... params)
	{
		/* Check if we have valid urls */
		if (ArrayUtils.isEmpty(params))
		{
			return null;
		}
		else
		{
			/* Remember the number of parameters */
			parameterCount = params.length;

			/* Wrap this in try/catch to handle all exceptions here */
			try
			{
				/* Let the child class get the data */
				return getData(params);
			}
			catch (ResourceException e)
			{
				/* Remember the exception */
				exception = e;

				return null;
			}
		}
	}

	@Override
	protected final void onPostExecute(T t)
	{
		super.onPostExecute(t);

		/* Close the progress dialog */
		if (progressDialog != null && progressDialog.isShowing())
		{
			progressDialog.dismiss();
		}

		processData(t);
	}

	/**
	 * This is where you do your Restlet call to get the data. Return it afterwards.
	 * <p/>
	 * <b>DO NOT</b> process the result, wait for the {@link #processData(Object)} method call
	 *
	 * @param params The URL of the request
	 * @return The returned data
	 */
	protected abstract T getData(U... params);

	/**
	 * This is where you actually process your data. Return it to the caller/callback or handle it straight away.
	 *
	 * @param result The result
	 */
	protected abstract void processData(T result);

	/**
	 * Returns the Exception that occurred (if any)
	 *
	 * @return The Exception that occurred (if any) or <code>null</code>
	 */
	public ResourceException getException()
	{
		return exception;
	}
}
