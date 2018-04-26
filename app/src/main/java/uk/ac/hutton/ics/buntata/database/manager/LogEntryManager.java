/*
 * Copyright 2017 Information & Computational Sciences, The James Hutton Institute
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

package uk.ac.hutton.ics.buntata.database.manager;

import android.content.*;

import java.io.*;
import java.text.*;
import java.util.*;

import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.database.*;
import uk.ac.hutton.ics.buntata.database.entity.*;

/**
 * The {@link LogEntryManager} extends {@link AbstractManager} and can be used to obtain {@link BuntataMediaAdvanced}s from the database.
 *
 * @author Sebastian Raubach
 */
public class LogEntryManager extends AbstractManager<LogEntry>
{
	public LogEntryManager(Context context)
	{
		super(context, -7);
	}

	@Override
	protected DatabaseObjectParser<LogEntry> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return LogEntry.TABLE_NAME;
	}

	public void update(LogEntry entry)
	{
		try
		{
			open();

			ContentValues insertValues = new ContentValues();
			insertValues.put(LogEntry.FIELD_DATASOURCE_ID, entry.getDatasourceId());
			insertValues.put(LogEntry.FIELD_NODE_ID, entry.getNodeId());
			insertValues.put(LogEntry.FIELD_NODE_NAME, entry.getNodeName());
			insertValues.put(LogEntry.FIELD_NOTE, entry.getNote());

			if (entry.getLatitute() == null)
				insertValues.putNull(LogEntry.FIELD_LATITUDE);
			else
				insertValues.put(LogEntry.FIELD_LATITUDE, entry.getLatitute());

			if (entry.getLongitude() == null)
				insertValues.putNull(LogEntry.FIELD_LONGITUDE);
			else
				insertValues.put(LogEntry.FIELD_LONGITUDE, entry.getLongitude());

			if (entry.getElevation() == null)
				insertValues.putNull(LogEntry.FIELD_ELEVATION);
			else
				insertValues.put(LogEntry.FIELD_ELEVATION, entry.getElevation());

			insertValues.put(LogEntry.FIELD_CREATED_ON, entry.getCreatedOn().getTime());
			insertValues.put(LogEntry.FIELD_UPDATED_ON, new Date().getTime());
			database.update(getTableName(), insertValues, LogEntry.FIELD_ID + " = ?", new String[]{Integer.toString(entry.getId())});
		}
		finally
		{
			close();
		}
	}

	public void delete(LogEntry entry)
	{
		try
		{
			open();

			database.delete(getTableName(), LogEntry.FIELD_ID + " = ?", new String[]{Integer.toString(entry.getId())});
		}
		finally
		{
			close();
		}
	}

	private void deleteNulls()
	{
		try
		{
			open();

			database.delete(getTableName(), LogEntry.FIELD_ID + " IS NULL", null);
			database.delete(getTableName(), LogEntry.FIELD_ID + " = ?", new String[]{Integer.toString(-1)});
			database.delete(LogEntryImage.TABLE_NAME, LogEntryImage.FIELD_LOGENTRY_ID + " IS NULL", null);
			database.delete(LogEntryImage.TABLE_NAME, LogEntryImage.FIELD_LOGENTRY_ID + " = ?", new String[]{Integer.toString(-1)});
		}
		finally
		{
			close();
		}
	}

	public void add(LogEntry entry)
	{
		try
		{
			open();

			ContentValues insertValues = new ContentValues();
			insertValues.put(LogEntry.FIELD_DATASOURCE_ID, entry.getDatasourceId());
			insertValues.put(LogEntry.FIELD_NODE_ID, entry.getNodeId());
			insertValues.put(LogEntry.FIELD_NODE_NAME, entry.getNodeName());
			insertValues.put(LogEntry.FIELD_NOTE, entry.getNote());

			if (entry.getLatitute() == null)
				insertValues.putNull(LogEntry.FIELD_LATITUDE);
			else
				insertValues.put(LogEntry.FIELD_LATITUDE, entry.getLatitute());

			if (entry.getLongitude() == null)
				insertValues.putNull(LogEntry.FIELD_LONGITUDE);
			else
				insertValues.put(LogEntry.FIELD_LONGITUDE, entry.getLongitude());

			if (entry.getElevation() == null)
				insertValues.putNull(LogEntry.FIELD_ELEVATION);
			else
				insertValues.put(LogEntry.FIELD_ELEVATION, entry.getElevation());

			insertValues.put(LogEntry.FIELD_CREATED_ON, new Date().getTime());
			insertValues.put(LogEntry.FIELD_UPDATED_ON, new Date().getTime());
			long id = database.insert(getTableName(), null, insertValues);
			entry.setId((int) id);
		}
		finally
		{
			close();
		}
	}

	public void initializeDatabase()
	{
		try
		{
			open();

   			/* Create a Table in the Database. */
			database.execSQL("CREATE TABLE IF NOT EXISTS `logentries` (\n" +
					"`id` integer NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
					"`datasource_id` integer NOT NULL,\n" +
					"`node_id` integer NOT NULL,\n" +
					"`node_name` varchar(255) NOT NULL,\n" +
					"`note` text DEFAULT NULL,\n" +
					"`latitude` real DEFAULT NULL,\n" +
					"`longitude` real DEFAULT NULL,\n" +
					"`elevation` real DEFAULT NULL,\n" +
					"`created_on` datetime DEFAULT NULL,\n" +
					"`updated_on` timestamp DEFAULT NULL\n" +
					");");

			database.execSQL("CREATE TABLE IF NOT EXISTS `logentryimages` (\n" +
					"`id` integer NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
					"`logentry_id` integer NOT NULL,\n" +
					"`path` text DEFAULT NULL,\n" +
					"`created_on` datetime DEFAULT NULL,\n" +
					"`updated_on` timestamp DEFAULT NULL,\n" +
					"CONSTRAINT `logentryimages_ibfk_1` FOREIGN KEY(`logentry_id`) REFERENCES `logentries`(`id`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
					");");

			deleteNulls();
		}
		finally
		{
			close();
		}
	}

	private static class Parser extends DatabaseObjectParser<LogEntry>
	{
		static final class Inst
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
		public LogEntry parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			LogEntry result = new LogEntry(cursor.getInt(BuntataMedia.FIELD_ID), new Date(cursor.getLong(BuntataMedia.FIELD_CREATED_ON)), new Date(cursor.getLong(BuntataMedia.FIELD_UPDATED_ON)));

			result.setDatasourceId(cursor.getInt(LogEntry.FIELD_DATASOURCE_ID))
				  .setNodeName(cursor.getString(LogEntry.FIELD_NODE_NAME))
				  .setNodeId(cursor.getInt(LogEntry.FIELD_NODE_ID))
				  .setNote(cursor.getString(LogEntry.FIELD_NOTE))
				  .setLatitute(cursor.getDouble(LogEntry.FIELD_LATITUDE))
				  .setLongitude(cursor.getDouble(LogEntry.FIELD_LONGITUDE))
				  .setElevation(cursor.getDouble(LogEntry.FIELD_ELEVATION))
				  .setCreatedOn(cursor.getDate(LogEntry.FIELD_CREATED_ON))
				  .setUpdatedOn(cursor.getDate(LogEntry.FIELD_UPDATED_ON));

			return result;
		}
	}

	public static class FileWriter implements DatabaseObjectFileWriter<LogEntry>
	{
		private Context              context;
		private DateFormat           dateFormat;
		private LogEntryImageManager imageManager;

		public FileWriter(Context context)
		{
			this.context = context;
			this.dateFormat = android.text.format.DateFormat.getDateFormat(context);
			this.imageManager = new LogEntryImageManager(context);
		}

		@Override
		public void writeHeader(BufferedWriter bw) throws IOException
		{
			bw.write("datasource_name");
			bw.write("\tdatasource_version");
			bw.write("\tnode_name");
			bw.write("\tlatitude");
			bw.write("\tlongitude");
			bw.write("\televation");
			bw.write("\tnote");
			bw.write("\tcreated_on");
			bw.write("\tupdated_on");
			bw.write("\timages");
			bw.newLine();
		}

		@Override
		public void writeObject(BufferedWriter bw, LogEntry object) throws IOException
		{
			BuntataDatasource ds = new DatasourceManager(context, object.getDatasourceId()).getById(object.getDatasourceId());

			bw.write(ds.getName());
			bw.write("\t" + ds.getVersionNumber());
			bw.write("\t" + object.getNodeName());
			bw.write("\t" + object.getLatitute());
			bw.write("\t" + object.getLongitude());
			bw.write("\t" + object.getElevation());
			bw.write("\t" + object.getNote());
			if (object.getCreatedOn() != null)
				bw.write("\t" + dateFormat.format(object.getCreatedOn()));
			else
				bw.write("\t");
			if (object.getUpdatedOn() != null)
				bw.write("\t" + dateFormat.format(object.getUpdatedOn()));
			else
				bw.write("\t");

			List<LogEntryImage> images = imageManager.getForLogEntry(object.getId());

			bw.write("\t[");
			if (images.size() > 0)
			{
				bw.write(new File(images.get(0).getPath()).getName());

				for (int i = 1; i < images.size(); i++)
					bw.write(", " + new File(images.get(i).getPath()).getName());
			}
			bw.write("]");

			bw.newLine();
		}
	}
}
