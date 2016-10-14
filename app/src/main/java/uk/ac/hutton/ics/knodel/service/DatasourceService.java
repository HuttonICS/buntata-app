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
 * @author Sebastian Raubach
 */

public class DatasourceService extends RestletService
{
	public static final String DATASOURCE_BASE_URL     = "datasource";
	public static final String DATASOURCE_DOWNLOAD_URL = "/%s/download";

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

	public static void download(final Context context, ProgressBar progressBar, final KnodelDatasource ds, final RestletCallback<File> callback)
	{
		String url = getBaseUrl(context) + String.format(DATASOURCE_BASE_URL + DATASOURCE_DOWNLOAD_URL, ds.getId());

		File file = FileUtils.getFileForDatasource(context, ds.getId(), ds.getId() + ".zip");

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
		}
				.execute(url);
	}
}
