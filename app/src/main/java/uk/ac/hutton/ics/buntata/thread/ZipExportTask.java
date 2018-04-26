/*
 * Copyright 2018 Information & Computational Sciences, The James Hutton Institute
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

import android.app.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.content.*;

import java.io.*;
import java.lang.ref.*;
import java.util.*;

import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link ZipExportTask} will download a data source zip file.
 *
 * @author Sebastian Raubach
 */
public abstract class ZipExportTask extends AsyncTask<Void, Integer, File>
{
	private WeakReference<BaseActivity> context;
	private List<LogEntry>              dataset;
	private Exception                   exception;
	private ProgressDialog              dialog;

	/**
	 * Creates a new instance of the zip export task
	 */
	public ZipExportTask(BaseActivity context, List<LogEntry> dataset)
	{
		this.context = new WeakReference<>(context);
		dialog = new ProgressDialog(context);
		dialog.setIndeterminate(true);
		this.dataset = dataset;
	}

	@Override
	protected void onPreExecute()
	{
		this.dialog.setMessage(context.get().getString(R.string.dialog_export_message));
		this.dialog.show();
	}

	@Override
	protected File doInBackground(Void... params)
	{
		BaseActivity c = context.get();

		if (c == null)
			throw new IllegalArgumentException();

		File zip = FileUtils.getExportFile(c, "zip");
		File file = FileUtils.getExportFile(c, "txt");

		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
			LogEntryManager.FileWriter writer = new LogEntryManager.FileWriter(c);

			writer.writeHeader(bw);

			for (LogEntry log : dataset)
			{
				writer.writeObject(bw, log);
			}
		}
		catch (IOException e)
		{
			exception = e;
			e.printStackTrace();
			return null;
		}
		finally
		{
			if (bw != null)
			{
				try
				{
					bw.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}


		if (file.exists())
		{
			List<File> files = new ArrayList<>();
			files.add(file);

			LogEntryImageManager imageManager = new LogEntryImageManager(c);
			for (LogEntry entry : dataset)
			{
				List<LogEntryImage> images = imageManager.getForLogEntry(entry.getId());

				for (LogEntryImage i : images)
					files.add(new File(i.getPath()));
			}


			try
			{
				FileUtils.zip(zip, files.toArray(new File[files.size()]));
			}
			catch (IOException e)
			{
				exception = e;
				e.printStackTrace();
				return null;
			}
			finally
			{
				file.delete();
			}
		}
		else
		{
			SnackbarUtils.create(c.getSnackbarParentView(), R.string.snackbar_export_failed, ContextCompat.getColor(c, android.R.color.white), ContextCompat.getColor(c, R.color.snackbar_red), Snackbar.LENGTH_LONG)
						 .show();
		}

		return zip;
	}

	@Override
	protected void onPostExecute(File file)
	{
		if (dialog.isShowing())
			dialog.dismiss();

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