package uk.ac.hutton.ics.knodel.util;

import android.content.*;

import java.io.*;
import java.util.zip.*;

/**
 * @author Sebastian Raubach
 */

public class FileUtils
{
	public static File getFileForDatasource(Context context, int datasourceId, String filename)
	{
		return new File(new File(new File(context.getFilesDir(), "data"), Integer.toString(datasourceId)), filename);
	}

	public static void unzip(File zipFile, File targetDirectory) throws IOException
	{
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
		try
		{
			ZipEntry ze;
			int count;
			byte[] buffer = new byte[8192];
			while ((ze = zis.getNextEntry()) != null)
			{
				File file = new File(targetDirectory, ze.getName());
				File dir = ze.isDirectory() ? file : file.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs())
					throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
				if (ze.isDirectory())
					continue;
				FileOutputStream fout = new FileOutputStream(file);
				try
				{
					while ((count = zis.read(buffer)) != -1)
						fout.write(buffer, 0, count);
				}
				finally
				{
					fout.close();
				}
				/* if time should be restored as well */
				long time = ze.getTime();
				if (time > 0)
					file.setLastModified(time);
			}
		}
		finally
		{
			zis.close();
		}
	}
}
