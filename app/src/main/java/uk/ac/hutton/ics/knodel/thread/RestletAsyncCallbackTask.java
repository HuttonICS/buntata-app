/**
 * Germinate Mobile is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group
 * at JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p/>
 * Copyright (c) 2012-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.knodel.thread;

import android.content.*;

import org.restlet.resource.*;

import uk.ac.hutton.ics.knodel.service.*;

/**
 * RestletAsyncCallbackTask is an extension of {@link RestletAsyncTask} that includes a default {@link #processData(Object)} implementation that
 * forwards the result data or the exception to the {@link uk.ac.hutton.ics.knodel.service.RestletCallback}
 *
 * @author Sebastian Raubach
 */
public abstract class RestletAsyncCallbackTask<U, T> extends RestletAsyncTask<U, T>
{
	protected RestletCallback<T> callback;

	public RestletAsyncCallbackTask(Context context, int progressStyle, boolean cancelable, RestletCallback<T> callback)
	{
		super(context, progressStyle, cancelable);

		this.callback = callback;
	}

	public RestletAsyncCallbackTask(Context context, int progressStyle, RestletCallback<T> callback)
	{
		super(context, progressStyle);

		this.callback = callback;
	}

	@Override
	protected void processData(T result)
	{
		/* This is a default implementation of the method. It will just forward the result/exception to the RestletCallback */
		ResourceException e = getException();

		if (e != null)
		{
			/* If there is an exception, call the onFailure method */
			callback.onFailure(e);
		}
		else
		{
			/* Otherwise we return the actual result */
			callback.onSuccess(result);
		}
	}
}
