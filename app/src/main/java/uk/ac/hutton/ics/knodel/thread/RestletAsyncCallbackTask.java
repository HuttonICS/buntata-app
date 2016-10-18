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
