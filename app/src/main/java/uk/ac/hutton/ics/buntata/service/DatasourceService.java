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

package uk.ac.hutton.ics.buntata.service;

import android.app.*;
import android.content.*;
import android.widget.*;

import java.io.*;

import jhi.knodel.resource.*;
import retrofit2.*;
import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.thread.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link DatasourceService} is used to get the data sources from the server
 *
 * @author Sebastian Raubach
 */
public class DatasourceService
{
	private static final String DATASOURCE_BASE_URL     = "datasource";
	private static final String DATASOURCE_DOWNLOAD_URL = "/%s/download?includevideos=%s";

	private static Retrofit           RETROFIT;
	private static DatasourceProvider PROVIDER;

	public static void init(Context context)
	{
		RETROFIT = new Retrofit.Builder()
				.baseUrl(PreferenceUtils.getPreference(context, PreferenceUtils.PREFS_KNODEL_SERVER_URL))
				.addConverterFactory(JacksonConverterFactory.create())
				.build();

		PROVIDER = RETROFIT.create(DatasourceProvider.class);
	}

	/**
	 * Returns the base URL of the BRAPI instance. Will <b>ALEAYS</b> end with a tailing slash
	 *
	 * @return The base URL of the BRAPI instance. Will <b>ALEAYS</b> end with a tailing slash
	 */
	private static String getBaseUrl(Context context)
	{
		String url = PreferenceUtils.getPreference(context, PreferenceUtils.PREFS_KNODEL_SERVER_URL);
		if (StringUtils.isEmpty(url) || !url.endsWith("/"))
			url += "/";

		return url;
	}

	private static ProgressDialog prepareProgressBar(Context context, final Call<?> call, boolean cancelable)
	{
		/* Check if the style is valid */
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getString(R.string.dialog_restlet_please_wait));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(cancelable);

		if (cancelable)
		{
			progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.generic_cancel), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					call.cancel();
				}
			});
		}

		return progressDialog;
	}

	/**
	 * Returns all the data sources from the server
	 *
	 * @param context  The current context
	 * @param callback The {@link RemoteCallback} to call when the query returns
	 */
	public static void getAll(final Context context, boolean cancelable, final RemoteCallback<KnodelDatasourceList> callback)
	{
		final Call<KnodelDatasourceList> result = PROVIDER.getAll();

		final ProgressDialog dialog = prepareProgressBar(context, result, cancelable);
		dialog.show();

		result.enqueue(new Callback<KnodelDatasourceList>()
		{
			@Override
			public void onResponse(Response<KnodelDatasourceList> response)
			{
				dialog.dismiss();
				callback.onSuccess(response.body());
			}

			@Override
			public void onFailure(Throwable t)
			{
				dialog.dismiss();
				callback.onFailure(t);
			}
		});
	}

	/**
	 * Download the zipped dataset from the server
	 *
	 * @param context       The current context
	 * @param includeVideos Should videos be downloaded as well?
	 * @param progressBar   The progress bar to update with the download status
	 * @param ds            The current data source
	 * @param callback      The {@link RemoteCallback} to call when the query returns
	 */
	public static void download(final Context context, boolean includeVideos, ProgressBar progressBar, final KnodelDatasource ds, final RemoteCallback<File> callback)
	{
		String url = getBaseUrl(context) + String.format(DATASOURCE_BASE_URL + DATASOURCE_DOWNLOAD_URL, ds.getId(), Boolean.toString(includeVideos));

		/* The target file */
		File file = FileUtils.getFileForDatasource(context, ds.getId(), ds.getId() + ".zip");

		/* Start the download */
		new DownloadTask(progressBar, includeVideos, ds, file)
		{
			@Override
			protected void processData(File file)
			{
				Exception e = getException();

				if (e != null)
					callback.onFailure(e);

				try
				{
					FileUtils.unzip(file, file.getParentFile());

					file.delete();

					File result = file.getParentFile();

					callback.onSuccess(result);
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					callback.onFailure(ex);
				}
			}
		}.execute(url);
	}
}
