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

package uk.ac.hutton.ics.buntata.database;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;

import java.io.*;
import java.util.*;

import static android.content.Context.*;

/**
 * {@link DatabaseInternal} extends {@link SQLiteOpenHelper} and contains convenience methods for handling local sqlite files.
 *
 * @author Sebastian Raubach
 */
public class DatabaseInternal extends SQLiteOpenHelper
{
	public static final int DATABASE_INTERNAL = -7;

	private static final String DATABASE_NAME    = "buntata";
	private static final int    DATABASE_VERSION = 1;

	private Context context;
	private int     datasourceId;

	public DatabaseInternal(Context context, int datasourceId)
	{
		super(context.getApplicationContext(), DATABASE_NAME + datasourceId, null, DATABASE_VERSION);
		this.context = context;
		this.datasourceId = datasourceId;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}

	public SQLiteDatabase openFromFile() throws SQLException
	{
		if (datasourceId == DATABASE_INTERNAL)
			return context.openOrCreateDatabase("LogEntries", MODE_PRIVATE, null);
		else
			return SQLiteDatabase.openOrCreateDatabase(new File(new File(new File(context.getFilesDir(), "data"), Integer.toString(datasourceId)), datasourceId + ".sqlite"), null);
	}

	/**
	 * The {@link AdvancedCursor} wraps a {@link Cursor} and adds methods to get values based on column name rather than column index.
	 *
	 * @author Sebastian Raubach
	 */
	public static class AdvancedCursor
	{
		private Cursor cursor;

		public AdvancedCursor(Cursor cursor)
		{
			this.cursor = cursor;
		}

		public Double getDouble(String columnName)
		{
			int index = cursor.getColumnIndex(columnName);

			if (index == -1)
				return null;
			else if (cursor.isNull(index))
				return null;
			else
				return cursor.getDouble(index);
		}

		public String getString(String columnName)
		{
			int index = cursor.getColumnIndex(columnName);

			if (index == -1)
				return null;
			else if (cursor.isNull(index))
				return null;
			else
				return cursor.getString(index);
		}

		public int getInt(String columnName)
		{
			if (cursor.getColumnIndex(columnName) == -1)
				return -1;
			else
				return cursor.getInt(cursor.getColumnIndex(columnName));
		}

		public long getLong(String columnName)
		{
			if (cursor.getColumnIndex(columnName) == -1)
				return -1;
			else
				return cursor.getLong(cursor.getColumnIndex(columnName));
		}

		public Date getDate(String columnName)
		{
			if (cursor.getColumnIndex(columnName) == -1)
				return null;
			else
			{
				Long date = getLong(columnName);

				if (date != -1)
					return new Date(date);
				else
					return null;
			}
		}

		public boolean getBoolean(String columnName)
		{
			if (cursor.getColumnIndex(columnName) == -1)
				return false;
			else
				return cursor.getInt(cursor.getColumnIndex(columnName)) == 1;
		}
	}
}
