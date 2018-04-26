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
import android.database.*;

import java.text.*;
import java.util.*;

import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.database.*;
import uk.ac.hutton.ics.buntata.database.entity.*;

/**
 * The {@link LogEntryImageManager} extends {@link AbstractManager} and can be used to obtain {@link BuntataMediaAdvanced}s from the database.
 *
 * @author Sebastian Raubach
 */
public class LogEntryImageManager extends AbstractManager<LogEntryImage>
{
	public LogEntryImageManager(Context context)
	{
		super(context, -7);
	}

	@Override
	protected DatabaseObjectParser<LogEntryImage> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return LogEntryImage.TABLE_NAME;
	}

	public void delete(LogEntry entry)
	{
		try
		{
			open();

			database.delete(getTableName(), LogEntryImage.FIELD_LOGENTRY_ID + " = ?", new String[]{Integer.toString(entry.getId())});
		}
		finally
		{
			close();
		}
	}

	public void delete(LogEntryImage entry)
	{
		try
		{
			open();

			database.delete(getTableName(), LogEntryImage.FIELD_ID + " = ?", new String[]{Integer.toString(entry.getId())});
		}
		finally
		{
			close();
		}
	}

	public void add(LogEntryImage entry)
	{
		try
		{
			open();

			ContentValues insertValues = new ContentValues();
			insertValues.put(LogEntryImage.FIELD_LOGENTRY_ID, entry.getLogEntryId());
			insertValues.put(LogEntryImage.FIELD_PATH, entry.getPath());
			insertValues.put(LogEntryImage.FIELD_CREATED_ON, new Date().getTime());
			insertValues.put(LogEntryImage.FIELD_UPDATED_ON, new Date().getTime());
			long id = database.insert(getTableName(), null, insertValues);
			entry.setId((int) id);
		}
		finally
		{
			close();
		}
	}

	public List<LogEntryImage> getForLogEntry(int logEntryId)
	{
		List<LogEntryImage> result = new ArrayList<>();

		try
		{
			open();

			Cursor cursor = database.rawQuery("SELECT * FROM " + LogEntryImage.TABLE_NAME + " WHERE " + LogEntryImage.FIELD_LOGENTRY_ID + " = ?", new String[]{Integer.toString(logEntryId)});
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				try
				{
					result.add(getDefaultParser().parse(context, datasourceId, new DatabaseInternal.AdvancedCursor(cursor)));
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}

				cursor.moveToNext();
			}

			cursor.close();
		}
		finally
		{
			close();
		}

		return result;
	}

	public void update(LogEntryImage image)
	{
		try
		{
			open();

			ContentValues insertValues = new ContentValues();
			insertValues.put(LogEntryImage.FIELD_LOGENTRY_ID, image.getLogEntryId());
			insertValues.put(LogEntryImage.FIELD_PATH, image.getPath());
			insertValues.put(LogEntryImage.FIELD_CREATED_ON, image.getCreatedOn().getTime());
			insertValues.put(LogEntryImage.FIELD_UPDATED_ON, new Date().getTime());
			database.update(getTableName(), insertValues, LogEntryImage.FIELD_ID + " = ?", new String[]{Integer.toString(image.getId())});
		}
		finally
		{
			close();
		}
	}

	private static class Parser extends DatabaseObjectParser<LogEntryImage>
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
		public LogEntryImage parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			LogEntryImage result = new LogEntryImage(cursor.getInt(BuntataMedia.FIELD_ID), new Date(cursor.getLong(BuntataMedia.FIELD_CREATED_ON)), new Date(cursor.getLong(BuntataMedia.FIELD_UPDATED_ON)));

			result.setLogEntryId(cursor.getInt(LogEntryImage.FIELD_LOGENTRY_ID))
				  .setPath(cursor.getString(LogEntryImage.FIELD_PATH));

			result.setLogEntry(new LogEntryManager(context).getById(result.getLogEntryId()));

			return result;
		}
	}
}
