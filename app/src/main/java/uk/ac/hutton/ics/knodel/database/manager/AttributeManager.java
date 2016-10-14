package uk.ac.hutton.ics.knodel.database.manager;

import android.content.*;

import java.text.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.database.*;

/**
 * @author Sebastian Raubach
 */
public class AttributeManager extends AbstractManager<KnodelAttribute>
{
	private static final String[] ALL_FIELDS = {KnodelAttribute.FIELD_ID, KnodelAttribute.FIELD_NAME, KnodelAttribute.FIELD_CREATED_ON, KnodelAttribute.FIELD_UPDATED_ON};

	public AttributeManager(Context context, int datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<KnodelAttribute> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return KnodelAttribute.TABLE_NAME;
	}

	@Override
	protected String[] getAllFields()
	{
		return ALL_FIELDS;
	}

	private static class Parser extends DatabaseObjectParser<KnodelAttribute>
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
				private static final AttributeManager.Parser INSTANCE = new AttributeManager.Parser();
			}

			public static AttributeManager.Parser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		@Override
		public KnodelAttribute parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			return new KnodelAttribute(cursor.getInt(KnodelAttribute.FIELD_ID), new Date(cursor.getLong(KnodelAttribute.FIELD_CREATED_ON)), new Date(cursor.getLong(KnodelAttribute.FIELD_UPDATED_ON)))
					.setName(cursor.getString(KnodelAttribute.FIELD_NAME));
		}
	}
}
