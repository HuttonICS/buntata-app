package uk.ac.hutton.ics.knodel.database;

import android.content.*;

import java.text.*;

import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */

public abstract class DatabaseObjectParser<T extends DatabaseObject>
{
	public abstract T parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException;
}
