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

package uk.ac.hutton.ics.buntata.database.manager;

import android.content.*;

import java.text.*;
import java.util.*;

import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.database.*;

/**
 * The {@link AttributeManager} extends {@link AbstractManager} and can be used to obtain {@link BuntataAttribute}s from the database.
 *
 * @author Sebastian Raubach
 */
public class AttributeManager extends AbstractManager<BuntataAttribute>
{
	public AttributeManager(Context context, int datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<BuntataAttribute> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return BuntataAttribute.TABLE_NAME;
	}

	private static class Parser extends DatabaseObjectParser<BuntataAttribute>
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
				private static final AttributeManager.Parser INSTANCE = new AttributeManager.Parser();
			}

			public static AttributeManager.Parser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		@Override
		public BuntataAttribute parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			return new BuntataAttribute(cursor.getInt(BuntataAttribute.FIELD_ID), new Date(cursor.getLong(BuntataAttribute.FIELD_CREATED_ON)), new Date(cursor.getLong(BuntataAttribute.FIELD_UPDATED_ON)))
					.setName(cursor.getString(BuntataAttribute.FIELD_NAME));
		}
	}
}
