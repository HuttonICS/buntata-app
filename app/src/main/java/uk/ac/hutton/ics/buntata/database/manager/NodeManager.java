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
import android.database.*;

import java.text.*;
import java.util.*;

import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.database.*;
import uk.ac.hutton.ics.buntata.database.entity.*;

/**
 * The {@link NodeManager} extends {@link AbstractManager} and can be used to obtain {@link BuntataNodeAdvanced}s from the database.
 *
 * @author Sebastian Raubach
 */
public class NodeManager extends AbstractManager<BuntataNodeAdvanced>
{
	private static final String[] ALL_FIELDS = {BuntataNode.FIELD_ID, BuntataNode.FIELD_NAME, BuntataNode.FIELD_DESCRIPTION, BuntataNode.FIELD_DATASOURCE_ID, BuntataNode.FIELD_CREATED_ON, BuntataNode.FIELD_UPDATED_ON};

	private static Map<String, Set<Integer>> CACHE_FILTER_POSITIVE = new LinkedHashMap<String, Set<Integer>>()
	{
		@Override
		protected boolean removeEldestEntry(Entry eldest)
		{
			/* Only cache 10 entries */
			return size() > 10;
		}
	};

	public static void clearCaches()
	{
		CACHE_FILTER_POSITIVE.clear();
	}

	public NodeManager(Context context, int datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<BuntataNodeAdvanced> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return BuntataNode.TABLE_NAME;
	}

	@Override
	protected String[] getAllFields()
	{
		return ALL_FIELDS;
	}

	public List<BuntataNodeAdvanced> getAllRoots()
	{
		List<BuntataNodeAdvanced> result = new ArrayList<>();

		try
		{
			open();

			Cursor cursor = database.rawQuery("SELECT * FROM nodes WHERE NOT EXISTS (SELECT 1 FROM relationships WHERE relationships.child = nodes.id)", null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				try
				{
					BuntataNodeAdvanced node = getDefaultParser().parse(context, datasourceId, new DatabaseInternal.AdvancedCursor(cursor));
					node.setHasChildren(hasChildren(node.getId()));
					result.add(node);
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

	public List<BuntataNodeAdvanced> getForParent(int parentId)
	{
		List<BuntataNodeAdvanced> result = new ArrayList<>();

		try
		{
			open();

			Cursor cursor = database.rawQuery("SELECT * FROM nodes WHERE EXISTS (SELECT 1 FROM relationships WHERE relationships.child = nodes.id AND relationships.parent = ?)", new String[]{Integer.toString(parentId)});
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				try
				{
					BuntataNodeAdvanced node = getDefaultParser().parse(context, datasourceId, new DatabaseInternal.AdvancedCursor(cursor));
					node.setHasChildren(hasChildren(node.getId()));
					result.add(node);
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

	private String getPlaceholder(int nrOfPlaceholders)
	{
		if (nrOfPlaceholders < 1)
			return "";

		StringBuilder builder = new StringBuilder();
		builder.append("?");

		for (int i = 1; i < nrOfPlaceholders; i++)
			builder.append(",?");

		return builder.toString();
	}

	private void fillSearchCache(String query)
	{
		String placeholder = "%" + query + "%";

		try
		{
			open();

			/* Keep track of the positive matches */
			Set<Integer> positiveIds = new HashSet<>();
			/* Keep track of the new ids (as in not known before) from each query */
			Set<Integer> newIds = new HashSet<>();

			/* First, check all leaf nodes */
			Cursor cursor = database.rawQuery("SELECT id FROM nodes WHERE NOT EXISTS (SELECT 1 FROM relationships WHERE relationships.parent = nodes.id) AND (nodes.name LIKE ? OR EXISTS (SELECT 1 FROM attributevalues WHERE attributevalues.node_id = nodes.id AND attributevalues.value LIKE ?))", new String[]{placeholder, placeholder});
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				positiveIds.add(cursor.getInt(0));

				cursor.moveToNext();
			}

			cursor.close();

			newIds.addAll(positiveIds);

			boolean goOn = true;
			while (goOn)
			{
				/* Don't continue unless new ids are discovered */
				goOn = false;

				/* Now check all the parents of the nodes from the previous step */
				String formatted = String.format("SELECT id FROM nodes WHERE nodes.id IN (SELECT parent FROM relationships WHERE child IN (%s)) OR ( nodes.name LIKE ? OR EXISTS ( SELECT 1 FROM attributevalues WHERE attributevalues.node_id = nodes.id AND attributevalues.value LIKE ?))", getPlaceholder(newIds.size()));
				String[] parameters = new String[2 + newIds.size()];
				int i = 0;
				for (Integer id : newIds)
					parameters[i++] = Integer.toString(id);
				parameters[i++] = parameters[i++] = placeholder;

				cursor = database.rawQuery(formatted, parameters);
				cursor.moveToFirst();

				/* Remember to clear this before we start */
				newIds.clear();

				while (!cursor.isAfterLast())
				{
					int id = cursor.getInt(0);
					boolean added = positiveIds.add(id);

					/* If it's a new id, we remember it */
					if (added)
						newIds.add(id);

					/* Should we continue with another loop? */
					goOn |= added;

					cursor.moveToNext();
				}

				cursor.close();
			}

			/* Now that we're finished, we've got all the ids that fulfill the query. Cache them. */
			CACHE_FILTER_POSITIVE.put(query, positiveIds);
		}
		finally
		{
			close();
		}
	}

	/**
	 * Check if the node itself or a child fulfills the query
	 *
	 * @param node  The node in question
	 * @param query The query
	 * @return <code>true</code> if the node itself or a child fulfills the query
	 */
	public boolean hasChildWithContent(BuntataNodeAdvanced node, String query)
	{
		/* Lower case everything */
		query = query.toLowerCase();

		/* Create the cache if it doesn't exist */
		if (!CACHE_FILTER_POSITIVE.containsKey(query))
			fillSearchCache(query);

		/* Check the cache */
		return CACHE_FILTER_POSITIVE.get(query).contains(node.getId());
	}

	private static class Parser extends DatabaseObjectParser<BuntataNodeAdvanced>
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
				private static final NodeManager.Parser INSTANCE = new NodeManager.Parser();
			}

			public static NodeManager.Parser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		@Override
		public BuntataNodeAdvanced parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			BuntataNodeAdvanced item = new BuntataNodeAdvanced(cursor.getInt(BuntataNode.FIELD_ID), new Date(cursor.getLong(BuntataNode.FIELD_CREATED_ON)), new Date(cursor.getLong(BuntataNode.FIELD_UPDATED_ON)));
			item.setName(cursor.getString(BuntataNode.FIELD_NAME))
				.setDescription(cursor.getString(BuntataNode.FIELD_DESCRIPTION))
				.setDatasourceId(cursor.getInt(BuntataNode.FIELD_DATASOURCE_ID));

			item.setMedia(new MediaManager(context, datasourceId).getForNode(null, item.getId()));

			return item;
		}
	}
}
