package uk.ac.hutton.ics.knodel.database.manager;

import android.content.*;
import android.database.*;

import java.text.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.database.*;

/**
 * @author Sebastian Raubach
 */
public class MediaTypeManager extends AbstractManager<KnodelMediaType>
{
	private static final String[] ALL_FIELDS = {KnodelMediaType.FIELD_ID, KnodelMediaType.FIELD_NAME, KnodelMediaType.FIELD_CREATED_ON, KnodelMediaType.FIELD_UPDATED_ON};

	public MediaTypeManager(Context context, int datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<KnodelMediaType> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return KnodelMediaType.TABLE_NAME;
	}

	@Override
	protected String[] getAllFields()
	{
		return ALL_FIELDS;
	}

	private static class Parser extends DatabaseObjectParser<KnodelMediaType>
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
				private static final MediaTypeManager.Parser INSTANCE = new MediaTypeManager.Parser();
			}

			public static MediaTypeManager.Parser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		@Override
		public KnodelMediaType parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			return new KnodelMediaType(cursor.getInt(KnodelMediaType.FIELD_ID), new Date(cursor.getLong(KnodelMediaType.FIELD_CREATED_ON)), new Date(cursor.getLong(KnodelMediaType.FIELD_UPDATED_ON)))
					.setName(cursor.getString(KnodelMediaType.FIELD_NAME));
		}
	}
}
