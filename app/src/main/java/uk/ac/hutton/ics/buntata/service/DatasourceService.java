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
import android.database.sqlite.*;
import android.widget.*;

import java.io.*;
import java.util.*;

import jhi.buntata.resource.*;
import retrofit2.*;
import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
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
	private static final String DATASOURCE_ICON_URL     = "/%s/icon";

	private static DatasourceProvider PROVIDER;

	public static void init(Context context)
	{
		PROVIDER = new Retrofit.Builder()
				.baseUrl(PreferenceUtils.getPreference(context, PreferenceUtils.PREFS_KNODEL_SERVER_URL))
				.addConverterFactory(JacksonConverterFactory.create())
				.build()
				.create(DatasourceProvider.class);
	}

	/**
	 * Returns the base URL of the REST API instance. Will <b>ALWAYS</b> end with a tailing slash
	 *
	 * @return The base URL of the REST API instance. Will <b>ALWAYS</b> end with a tailing slash
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
	public static void getAll(final Context context, boolean cancelable, boolean showDialog, final RemoteCallback<List<BuntataDatasource>> callback)
	{
		final Call<List<BuntataDatasource>> result = PROVIDER.getAll();

		final ProgressDialog dialog = showDialog ? prepareProgressBar(context, result, cancelable) : null;

		if (dialog != null)
			dialog.show();

		result.enqueue(new Callback<List<BuntataDatasource>>()
		{
			@Override
			public void onResponse(Response<List<BuntataDatasource>> response)
			{
				if (dialog != null)
					dialog.dismiss();
				callback.onSuccess(response.body());
			}

			@Override
			public void onFailure(Throwable t)
			{
				if (dialog != null)
					dialog.dismiss();
				callback.onFailure(t);
			}
		});
	}

	public static void getAllAdvanced(final Context context, boolean cancelable, boolean showDialog, final RemoteCallback<List<BuntataDatasourceAdvanced>> callback)
	{
		/* Get the local data sources */
		List<BuntataDatasource> local;

		try
		{
			local = new DatasourceManager(context, -1).getAll();
		}
		catch (SQLiteException e)
		{
			local = new ArrayList<>();
		}

		final List<BuntataDatasource> localList = local;
		/* Keep track of their status (installed no update, installed update, not installed) */
		final List<BuntataDatasourceAdvanced> datasources = new ArrayList<>();

		/* Then try to get the online resources */
		getAll(context, cancelable, showDialog, new RemoteCallback<List<BuntataDatasource>>(context)
		{
			@Override
			public void onFailure(Throwable caught)
			{
				caught.printStackTrace();

				/* If the request fails, just show the local ones as having no updates */
				for (BuntataDatasource ds : localList)
				{
					BuntataDatasourceAdvanced adv = BuntataDatasourceAdvanced.create(ds);
					adv.setState(BuntataDatasourceAdvanced.InstallState.INSTALLED_NO_UPDATE);
					datasources.add(adv);
				}

				callback.onSuccess(datasources);
			}

			@Override
			public void onSuccess(List<BuntataDatasource> result)
			{
				/* If the request succeeds, try to figure out if it's already installed locally and then check if there's an update */
				for (BuntataDatasource ds : result)
				{
					int index = localList.indexOf(ds);

					BuntataDatasourceAdvanced adv = BuntataDatasourceAdvanced.create(ds);

					/* Is installed */
					if (index != -1)
					{
						BuntataDatasource old = localList.get(index);

						if (DatasourceManager.isNewer(ds, old))
							adv.setState(BuntataDatasourceAdvanced.InstallState.INSTALLED_HAS_UPDATE);
						else
							adv.setState(BuntataDatasourceAdvanced.InstallState.INSTALLED_NO_UPDATE);

						localList.remove(index);
					}
					/* Is not installed */
					else
					{
						adv.setState(BuntataDatasourceAdvanced.InstallState.NOT_INSTALLED);
					}

					datasources.add(adv);
				}

				for (BuntataDatasource ds : localList)
				{
					BuntataDatasourceAdvanced adv = BuntataDatasourceAdvanced.create(ds);
					adv.setState(BuntataDatasourceAdvanced.InstallState.INSTALLED_NO_UPDATE);
					datasources.add(adv);
				}

				callback.onSuccess(datasources);
			}
		});
	}

	public static String getIcon(Context context, BuntataDatasource ds)
	{
		String path = null;

		if (ds.getIcon() != null)
		{
			File localImage = FileUtils.getFileForDatasource(context, ds.getId(), ds.getIcon());

			if (localImage.exists() && localImage.isFile())
				path = localImage.getAbsolutePath();
			else
				path = getBaseUrl(context) + String.format(DATASOURCE_BASE_URL + DATASOURCE_ICON_URL, ds.getId());
		}

		return path;
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
	public static DownloadTask download(final Context context, boolean includeVideos, ProgressBar progressBar, final BuntataDatasource ds, final RemoteCallback<File> callback)
	{
		String url = getBaseUrl(context) + String.format(DATASOURCE_BASE_URL + DATASOURCE_DOWNLOAD_URL, ds.getId(), Boolean.toString(includeVideos));

		/* The target file */
		File file = FileUtils.getFileForDatasource(context, ds.getId(), ds.getId() + ".zip");

		/* Start the download */
		DownloadTask task = new DownloadTask(progressBar, includeVideos, ds, file)
		{
			@Override
			protected void processData(File file)
			{
				Exception e = getException();

				if (e != null)
					callback.onFailure(e);

				try
				{
					if (file == null)
					{
						callback.onFailure(new IOException("File not found"));
						return;
					}

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
		};
		task.execute(url);

		return task;
	}
}
