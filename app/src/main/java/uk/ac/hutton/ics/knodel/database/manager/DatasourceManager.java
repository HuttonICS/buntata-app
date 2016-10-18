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

package uk.ac.hutton.ics.knodel.database.manager;

import android.content.*;

import java.io.*;
import java.text.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.database.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * The {@link DatasourceManager} extends {@link AbstractManager} and can be used to obtain {@link KnodelDatasource}s from the database.
 *
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

	/**
	 * This method deviates from the implementation of the {@link AbstractManager}. It doesn't actually query any internal database, but rather just
	 * checks which local Sqlite files are available. It'll then connect to them and get the {@link KnodelDatasource} from each.
	 *
	 * @return The {@link List} of {@link KnodelDatasource}s that are installed locally.
	 */
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

	/**
	 * Removes this datasource from the device
	 *
	 * @throws IOException Thrown if the file deletion fails
	 */
	public void remove() throws IOException
	{
		File dataFolder = new File(new File(context.getFilesDir(), "data"), Integer.toString(datasourceId));

		if (dataFolder.exists() && dataFolder.isDirectory())
		{
			FileUtils.deleteDirectoryRecursively(dataFolder);
		}
	}

	/**
	 * Checks if the given {@link KnodelDatasource} given as the first argument is newer than the one given as the second argument.
	 *
	 * @param ds  The {@link KnodelDatasource} to check
	 * @param old The {@link KnodelDatasource} to use as a reference
	 * @return <code>true</code> if the given {@link KnodelDatasource} given as the first argument is newer than the one given as the second argument
	 */
	public static boolean isNewer(KnodelDatasource ds, KnodelDatasource old)
	{
		Date newDsCreated = ds.getCreatedOn();
		Date oldDsCreated = old.getCreatedOn();
		Date newDsUpdated = ds.getUpdatedOn();
		Date oldDsUpdated = old.getUpdatedOn();

		/* If both don't have an updated date, check the creation date */
		if (oldDsUpdated == null && newDsUpdated == null)
		{
			return compare(newDsCreated, oldDsCreated);
		}
		/* If the old data source doesn't have a updated date, but the new one does, then the new one is newer */
		else if (oldDsUpdated == null)
		{
			return true;
		}
		/* If the new data source doesn't have a updated date, but the old one does, then the old one is newer */
		else if (newDsUpdated == null)
		{
			return false;
		}
		/* If both have an updated date, then just compare them */
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
