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
import android.database.*;
import android.database.sqlite.*;

import java.text.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.database.*;

/**
 * The {@link AbstractManager} handles interactions with the internal Sqlite database. It will handle opening and closing of the database as well as
 * very basic queries like {@link #getAll()} and {@link #getById(int)}.
 *
 * @author Sebastian Raubach
 */
public abstract class AbstractManager<T extends DatabaseObject>
{
	private   DatabaseInternal databaseHelper;
	SQLiteDatabase   database;
	Context          context;
	int              datasourceId;

	AbstractManager(Context context, int datasourceId)
	{
		this.databaseHelper = new DatabaseInternal(context, datasourceId);
		this.context = context;
		this.datasourceId = datasourceId;
	}

	void open() throws SQLException
	{
		database = databaseHelper.openFromFile();
	}

	void close()
	{
		database.close();
		databaseHelper.close();
	}

	/**
	 * Returns all the {@link DatabaseObject}s for this type of {@link AbstractManager}. Uses {@link #getDefaultParser()}, {@link #getTableName()} and
	 * {@link #getAllFields()} to get the data from the database into the Java classes.
	 *
	 * @return The {@link List} of {@link DatabaseObject}s.
	 */
	public List<T> getAll()
	{
		try
		{
			open();

			List<T> result = new ArrayList<>();

			Cursor cursor = database.query(getTableName(), getAllFields(), null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				try
				{
					T item = getDefaultParser().parse(context, datasourceId, new DatabaseInternal.AdvancedCursor(cursor));
					result.add(item);
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}

				cursor.moveToNext();
			}

			cursor.close();

			return result;
		}
		finally
		{
			close();
		}
	}

	/**
	 * Returns the {@link DatabaseObject} for this type of {@link AbstractManager}. Uses {@link #getDefaultParser()}, {@link #getTableName()} and
	 * {@link #getAllFields()} to get the data from the database into the Java classes.
	 *
	 * @param id The id of the {@link DatabaseObject}
	 * @return The {@link DatabaseObject}.
	 */
	public T getById(int id)
	{
		try
		{
			open();

			T result = null;

			Cursor cursor = database.query(getTableName(), getAllFields(), DatabaseObject.FIELD_ID + " = ?", new String[]{Integer.toString(id)}, null, null, null);
			cursor.moveToFirst();
			if (!cursor.isAfterLast())
			{
				try
				{
					result = getDefaultParser().parse(context, datasourceId, new DatabaseInternal.AdvancedCursor(cursor));
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}

			cursor.close();

			return result;
		}
		finally
		{
			close();
		}
	}

	/**
	 * Returns the default {@link DatabaseObjectParser} for this type of {@link DatabaseObject}.
	 *
	 * @return The default {@link DatabaseObjectParser} for this type of {@link DatabaseObject}.
	 */
	protected abstract DatabaseObjectParser<T> getDefaultParser();

	/**
	 * Returns the database table for this type of {@link DatabaseObject}.
	 *
	 * @return The database table for this type of {@link DatabaseObject}.
	 */
	protected abstract String getTableName();

	/**
	 * Returns the fields of the database table for this type of {@link DatabaseObject}.
	 *
	 * @return The fields of the database table for this type of {@link DatabaseObject}.
	 */
	protected abstract String[] getAllFields();
}
