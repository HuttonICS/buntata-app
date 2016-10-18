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

package uk.ac.hutton.ics.knodel.service;

import android.app.*;
import android.content.*;
import android.widget.*;

import org.restlet.resource.*;

import java.io.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.thread.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * The {@link DatasourceService} is used to get the data sources from the server
 *
 * @author Sebastian Raubach
 */
public class DatasourceService extends RestletService
{
	public static final String DATASOURCE_BASE_URL     = "datasource";
	public static final String DATASOURCE_DOWNLOAD_URL = "/%s/download";

	/**
	 * Returns all the data sources from the server
	 *
	 * @param context  The current context
	 * @param callback The {@link RestletCallback} to call when the query returns
	 */
	public static void getAll(final Context context, final RestletCallback<KnodelDatasourceList> callback)
	{
		String url = getBaseUrl(context) + DATASOURCE_BASE_URL;

		new RestletAsyncCallbackTask<String, KnodelDatasourceList>(context, ProgressDialog.STYLE_SPINNER, false, callback)
		{
			@Override
			protected KnodelDatasourceList getData(String... params)
			{
				ClientResource clientResource = getResource();
				clientResource.setReference(params[0]);

				return clientResource.get(KnodelDatasourceList.class);
			}
		}.execute(url);
	}

	/**
	 * Download the zipped dataset from the server
	 *
	 * @param context     The current context
	 * @param progressBar The progress bar to update with the download status
	 * @param ds          The current data source
	 * @param callback    The {@link RestletCallback} to call when the query returns
	 */
	public static void download(final Context context, ProgressBar progressBar, final KnodelDatasource ds, final RestletCallback<File> callback)
	{
		String url = getBaseUrl(context) + String.format(DATASOURCE_BASE_URL + DATASOURCE_DOWNLOAD_URL, ds.getId());

		/* The target file */
		File file = FileUtils.getFileForDatasource(context, ds.getId(), ds.getId() + ".zip");

		/* Start the download */
		new DownloadTask(context, progressBar, ds, file)
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
