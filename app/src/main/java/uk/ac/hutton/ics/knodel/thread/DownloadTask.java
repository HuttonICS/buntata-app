package uk.ac.hutton.ics.knodel.thread;

import android.content.*;
import android.os.*;
import android.widget.*;

import java.io.*;
import java.lang.ref.*;
import java.net.*;

import jhi.knodel.resource.*;

public abstract class DownloadTask extends AsyncTask<String, Integer, File>
{
	private Context                    context;
	private Exception                  exception;
	private KnodelDatasource           ds;
	private File                       target;
	private WeakReference<ProgressBar> progressBar;

	public DownloadTask(Context context, ProgressBar progressBar, KnodelDatasource ds, File target)
	{
		this.context = context;
		this.ds = ds;
		this.target = target;
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
			int fileLength = (int) ds.getSize();

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

//		try
//		{
//			URL website = new URL(params[0]);
//
//			target.getParentFile().mkdirs();
//
//			if (target.exists())
//				target.delete();
//
//			FileUtils.copyURLToFile(website, target);
//
//			return target;
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//			exception = e;
//			return null;
//		}
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