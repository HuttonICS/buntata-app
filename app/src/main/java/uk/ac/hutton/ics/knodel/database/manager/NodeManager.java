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
 * The {@link NodeManager} extends {@link AbstractManager} and can be used to obtain {@link KnodelNodeAdvanced}s from the database.
 *
 * @author Sebastian Raubach
 */
public class NodeManager extends AbstractManager<KnodelNodeAdvanced>
{
	private static final String[] ALL_FIELDS = {KnodelNode.FIELD_ID, KnodelNode.FIELD_NAME, KnodelNode.FIELD_DESCRIPTION, KnodelNode.FIELD_DATASOURCE_ID, KnodelNode.FIELD_CREATED_ON, KnodelNode.FIELD_UPDATED_ON};

	public NodeManager(Context context, int datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<KnodelNodeAdvanced> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return KnodelNode.TABLE_NAME;
	}

	@Override
	protected String[] getAllFields()
	{
		return ALL_FIELDS;
	}

	public List<KnodelNodeAdvanced> getAllRoots()
	{
		List<KnodelNodeAdvanced> result = new ArrayList<>();

		try
		{
			open();

			Cursor cursor = database.rawQuery("SELECT * FROM nodes WHERE NOT EXISTS (SELECT 1 FROM relationships WHERE relationships.child = nodes.id)", null);
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

	public boolean hasChildren(int nodeId)
	{
		boolean result = false;

		try
		{
			open();

			Cursor cursor = database.rawQuery("SELECT 1 FROM nodes WHERE EXISTS (SELECT 1 FROM relationships WHERE relationships.parent = ?)", new String[]{Integer.toString(nodeId)});
			cursor.moveToFirst();

			if (!cursor.isAfterLast())
			{
				result = true;
			}

			cursor.close();
		}
		finally
		{
			close();
		}

		return result;
	}

	public List<KnodelNodeAdvanced> getForParent(int parentId)
	{
		List<KnodelNodeAdvanced> result = new ArrayList<>();

		try
		{
			open();

			Cursor cursor = database.rawQuery("SELECT * FROM nodes WHERE EXISTS (SELECT 1 FROM relationships WHERE relationships.child = nodes.id AND relationships.parent = ?)", new String[]{Integer.toString(parentId)});
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

	private static class Parser extends DatabaseObjectParser<KnodelNodeAdvanced>
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
				private static final NodeManager.Parser INSTANCE = new NodeManager.Parser();
			}

			public static NodeManager.Parser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		@Override
		public KnodelNodeAdvanced parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			KnodelNodeAdvanced item = new KnodelNodeAdvanced(cursor.getInt(KnodelNode.FIELD_ID), new Date(cursor.getLong(KnodelNode.FIELD_CREATED_ON)), new Date(cursor.getLong(KnodelNode.FIELD_UPDATED_ON)));
			item.setName(cursor.getString(KnodelNode.FIELD_NAME))
				.setDescription(cursor.getString(KnodelNode.FIELD_DESCRIPTION))
				.setDatasourceId(cursor.getInt(KnodelNode.FIELD_DATASOURCE_ID));

			item.setMedia(new MediaManager(context, datasourceId).getForNode(null, item.getId()));

			return item;
		}
	}
}
