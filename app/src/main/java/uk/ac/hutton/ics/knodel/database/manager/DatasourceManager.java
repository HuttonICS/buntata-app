package uk.ac.hutton.ics.knodel.database.manager;

import android.content.*;

import org.apache.commons.io.*;

import java.io.*;
import java.text.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.database.*;

/**
 * @author Sebastian Raubach
 */
public class DatasourceManager extends AbstractManager<KnodelDatasource>
{
	private static final String[] ALL_FIELDS = {KnodelDatasource.FIELD_ID, KnodelDatasource.FIELD_NAME, KnodelDatasource.FIELD_DESCRIPTION, KnodelDatasource.FIELD_VERSION_NUMBER, KnodelDatasource.FIELD_DATA_PROVIDER, KnodelDatasource.FIELD_CONTACT, KnodelDatasource.FIELD_ICON, KnodelDatasource.FiELD_SIZE, KnodelDatasource.FIELD_CREATED_ON, KnodelDatasource.FIELD_UPDATED_ON};

	public DatasourceManager(Context context, int datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<KnodelDatasource> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return KnodelDatasource.TABLE_NAME;
	}

	@Override
	protected String[] getAllFields()
	{
		return ALL_FIELDS;
	}

	@Override
	public List<KnodelDatasource> getAll()
	{
		List<KnodelDatasource> result = new ArrayList<>();
		File dataFolder = new File(context.getFilesDir(), "data");

		File[] folders = dataFolder.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return pathname.isDirectory();
			}
		});

		if (folders != null)
		{
			for (File folder : folders)
			{
				try
				{
					int id = Integer.parseInt(folder.getName());

					DatasourceManager m = new DatasourceManager(context, id);
					KnodelDatasource ds = m.getById(id);

					if (ds != null)
						result.add(ds);
				}
				catch (NumberFormatException e)
				{
				}
			}
		}

		return result;
	}

	public void remove() throws IOException
	{
		File dataFolder = new File(new File(context.getFilesDir(), "data"), Integer.toString(datasourceId));

		if (dataFolder.exists() && dataFolder.isDirectory())
			FileUtils.deleteDirectory(dataFolder);
	}

	public boolean isDownloaded(KnodelDatasource ds)
	{
		File dataFolder = new File(new File(context.getFilesDir(), "data"), Integer.toString(ds.getId()));

		return dataFolder.exists() && dataFolder.listFiles() != null;
	}

	public static boolean isNewer(KnodelDatasource ds, KnodelDatasource old)
	{
		Date newDsCreated = ds.getCreatedOn();
		Date oldDsCreated = old.getCreatedOn();
		Date newDsUpdated = ds.getUpdatedOn();
		Date oldDsUpdated = old.getUpdatedOn();

		if (oldDsUpdated == null && newDsUpdated == null)
		{
			return compare(newDsCreated, oldDsCreated);
		}
		else if (oldDsUpdated == null)
		{
			return true;
		}
		else if (newDsUpdated == null)
		{
			return false;
		}
		else
		{
			return newDsUpdated.getTime() > oldDsUpdated.getTime();
		}
	}

	private static boolean compare(Date oldDate, Date newDate)
	{
		if (oldDate == null && newDate == null)
		{
			return false;
		}
		else if (oldDate == null)
		{
			return true;
		}
		else if (newDate == null)
		{
			return false;
		}
		else
		{
			return oldDate.getTime() > newDate.getTime();
		}
	}

	private static class Parser extends DatabaseObjectParser<KnodelDatasource>
	{
		public static final class Inst
		{
			/**
			 * {@link InstanceHolder} is loaded on the first execution of {@link Inst#get()} or the first access to {@link InstanceHolder#INSTANCE},
			 * not before.
			 * <p/>
			 * This solution (<a href= "http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom" >Initialization-on-demand holder
			 * idiom</a>) is thread-safe without requiring special language constructs (i.e. <code>volatile</code> or <code>synchronized</code>).
			 *
			 * @author Sebastian Raubach
			 */
			private static final class InstanceHolder
			{
				private static final Parser INSTANCE = new Parser();
			}

			public static Parser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		@Override
		public KnodelDatasource parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			return new KnodelDatasource(cursor.getInt(KnodelDatasource.FIELD_ID), new Date(cursor.getLong(KnodelDatasource.FIELD_CREATED_ON)), new Date(cursor.getLong(KnodelDatasource.FIELD_UPDATED_ON)))
					.setName(cursor.getString(KnodelDatasource.FIELD_NAME))
					.setDescription(cursor.getString(KnodelDatasource.FIELD_DESCRIPTION))
					.setVersionNumber(cursor.getInt(KnodelDatasource.FIELD_VERSION_NUMBER))
					.setDataProvider(cursor.getString(KnodelDatasource.FIELD_DATA_PROVIDER))
					.setContact(cursor.getString(KnodelDatasource.FIELD_CONTACT))
					.setIcon(cursor.getString(KnodelDatasource.FIELD_ICON))
					.setSize(cursor.getInt(KnodelDatasource.FiELD_SIZE));
		}
	}
}
