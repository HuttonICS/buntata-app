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

package uk.ac.hutton.ics.buntata.thread;

import android.os.*;
import android.widget.*;

import java.io.*;
import java.lang.ref.*;
import java.net.*;

import jhi.knodel.resource.*;

/**
 * The {@link DownloadTask} will download a data source zip file.
 *
 * @author Sebastian Raubach
 */
public abstract class DownloadTask extends AsyncTask<String, Integer, File>
{
	private Exception                  exception;
	private KnodelDatasource           ds;
	private File                       target;
	private WeakReference<ProgressBar> progressBar;
	private boolean                    includeVideos;

	/**
	 * Creates a new instance of the download task
	 *
	 * @param progressBar The progressbar that should be updated on download progress
	 * @param ds          The data source
	 * @param target      The target file (the local zip file)
	 */
	public DownloadTask(ProgressBar progressBar, boolean includeVideos, KnodelDatasource ds, File target)
	{
		this.ds = ds;
		this.target = target;
		this.includeVideos = includeVideos;
		progressBar.setProgress(0);
		this.progressBar = new WeakReference<>(progressBar);
	}

	@Override
	protected File doInBackground(String... params)
	{
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try
		{
			target.getParentFile().mkdirs();

			if (target.exists())
				target.delete();

			URL url = new URL(params[0]);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
			{
				exception = new Exception(connection.getResponseMessage());
				exception.printStackTrace();
				return null;
			}

			// this will be useful to display download percentage
			// might be -1: server did not report the length
			long fileLength;

			if (includeVideos)
				fileLength = ds.getSizeTotal();
			else
				fileLength = ds.getSizeNoVideo();

			// download the file
			input = connection.getInputStream();
			output = new FileOutputStream(target);

			byte data[] = new byte[4096];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1)
			{
				// allow canceling with back button
				if (isCancelled())
				{
					input.close();
					return null;
				}
				total += count;
				// publishing the progress....
				if (fileLength > 0) // only if total length is known
					publishProgress((int) (total * 100 / fileLength));
				output.write(data, 0, count);
			}
		}
		catch (Exception e)
		{
			exception = e;
			e.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			}
			catch (IOException ignored)
			{
			}

			if (connection != null)
				connection.disconnect();
		}

		return target;
	}

	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		super.onProgressUpdate(progress);
		// if we get here, length is known, now set indeterminate to false
		ProgressBar p = progressBar.get();

		if (p != null)
		{
			p.setIndeterminate(false);
			p.setMax(100);
			p.setProgress(progress[0]);
		}
	}

	@Override
	protected void onPostExecute(File file)
	{
		super.onPostExecute(file);

		processData(file);
	}

	/**
	 * This is where you actually process your data. Return it to the caller/callback or handle it straight away.
	 *
	 * @param result The result
	 */
	protected abstract void processData(File result);

	/**
	 * Returns the Exception that occurred (if any)
	 *
	 * @return The Exception that occurred (if any) or <code>null</code>
	 */
	public Exception getException()
	{
		return exception;
	}
}