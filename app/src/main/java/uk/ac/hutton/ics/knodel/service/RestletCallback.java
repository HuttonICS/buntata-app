/**
 * Germinate Mobile is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group
 * at JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p/>
 * Copyright (c) 2012-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.knodel.service;

import android.content.*;
import android.widget.*;

import org.restlet.resource.*;

/**
 * @param <T> The type of the server result
 * @author Sebastian Raubach
 */
public abstract class RestletCallback<T>
{
	private Context context;

	public RestletCallback(Context context)
	{
		this.context = context;
	}

	/**
	 * Called when the Restlet request succeeds.
	 *
	 * @param result The result returned by the server.
	 */
	public abstract void onSuccess(T result);

	/**
	 * Called when the Restlet request fails.
	 *
	 * @param caught The {@link org.restlet.resource.ResourceException} returned from the server
	 */
	public void onFailure(Exception caught)
	{
		caught.printStackTrace();

		// TODO: error handling
		Toast.makeText(context, "ERROR: " + caught.getLocalizedMessage(), Toast.LENGTH_LONG).show();
	}
}
