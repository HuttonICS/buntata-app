package uk.ac.hutton.ics.knodel.database.manager;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;

import java.text.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.database.*;

/**
 * @author Sebastian Raubach
 */
public abstract class AbstractManager<T extends DatabaseObject>
{
	private   DatabaseInternal databaseHelper;
	protected SQLiteDatabase   database;
	protected Context context;
	protected int datasourceId;

	public AbstractManager(Context context, int datasourceId)
	{
		this.databaseHelper = new DatabaseInternal(context, datasourceId);
		this.context = context;
		this.datasourceId = datasourceId;
	}

	protected void open() throws SQLException
	{
		database = databaseHelper.openFromFile();
	}

	protected void close()
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
