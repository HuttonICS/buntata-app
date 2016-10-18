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

import java.text.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.database.*;
import uk.ac.hutton.ics.knodel.database.entity.*;

/**
 * The {@link AttributeValueManager} extends {@link AbstractManager} and can be used to obtain {@link KnodelAttributeValueAdvanced}s from the database.
 *
 * @author Sebastian Raubach
 */
public class AttributeValueManager extends AbstractManager<KnodelAttributeValueAdvanced>
{
	private static final String[] ALL_FIELDS = {KnodelAttributeValue.FIELD_ID, KnodelAttributeValue.FIELD_ATTRIBUTE_ID, KnodelAttributeValue.FIELD_NODE_ID, KnodelAttributeValue.FIELD_VALUE, KnodelAttributeValue.FIELD_CREATED_ON, KnodelAttributeValue.FIELD_UPDATED_ON};

	public AttributeValueManager(Context context, int datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<KnodelAttributeValueAdvanced> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return KnodelAttributeValue.TABLE_NAME;
	}

	@Override
	protected String[] getAllFields()
	{
		return ALL_FIELDS;
	}

	public List<KnodelAttributeValueAdvanced> getForNode(int nodeId)
	{
		List<KnodelAttributeValueAdvanced> result = new ArrayList<>();

		try
		{
			open();

			Cursor cursor = database.rawQuery("SELECT * FROM attributevalues WHERE attributevalues.node_id = ?", new String[]{Integer.toString(nodeId)});
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

	private static class Parser extends DatabaseObjectParser<KnodelAttributeValueAdvanced>
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
				private static final AttributeValueManager.Parser INSTANCE = new AttributeValueManager.Parser();
			}

			public static AttributeValueManager.Parser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		@Override
		public KnodelAttributeValueAdvanced parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			KnodelAttributeValueAdvanced result = new KnodelAttributeValueAdvanced(cursor.getInt(KnodelAttributeValue.FIELD_ID), new Date(cursor.getLong(KnodelAttributeValue.FIELD_CREATED_ON)), new Date(cursor.getLong(KnodelAttributeValue.FIELD_UPDATED_ON)));

			result.setAttributeId(cursor.getInt(KnodelAttributeValue.FIELD_ATTRIBUTE_ID))
				  .setNodeId(cursor.getInt(KnodelAttributeValue.FIELD_NODE_ID))
				  .setValue(cursor.getString(KnodelAttributeValue.FIELD_VALUE));


			result.setAttribute(new AttributeManager(context, datasourceId).getById(result.getAttributeId()));

			return result;
		}
	}
}
