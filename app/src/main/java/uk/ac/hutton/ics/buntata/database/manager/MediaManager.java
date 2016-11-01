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
 * The {@link MediaManager} extends {@link AbstractManager} and can be used to obtain {@link BuntataMediaAdvanced}s from the database.
 *
 * @author Sebastian Raubach
 */
public class MediaManager extends AbstractManager<BuntataMediaAdvanced>
{
	private static final String[] ALL_FIELDS = {BuntataMedia.FIELD_ID, BuntataMedia.FIELD_NAME, BuntataMedia.FIELD_DESCRIPTION, BuntataMedia.FIELD_MEDIATYPE_ID, BuntataMedia.FIELD_INTERNAL_LINK, BuntataMedia.FIELD_EXTERNAL_LINK, BuntataMedia.FIELD_EXTERNAL_LINK_DESCRIPTION, BuntataMedia.FIELD_CREATED_ON, BuntataMedia.FIELD_UPDATED_ON, BuntataMedia.FIELD_COPYRIGHT};

	public MediaManager(Context context, int datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<BuntataMediaAdvanced> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return BuntataMedia.TABLE_NAME;
	}

	@Override
	protected String[] getAllFields()
	{
		return ALL_FIELDS;
	}

	public List<BuntataMediaAdvanced> getForNode(String type, int nodeId)
	{
		List<BuntataMediaAdvanced> result = new ArrayList<>();

		try
		{
			open();

			Cursor cursor;

			if (type != null)
				cursor = database.rawQuery("SELECT * FROM media WHERE EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id = ?) AND EXISTS (SELECT 1 FROM mediatypes WHERE mediatypes.id = media.mediatype_id AND mediatypes.name = ?)", new String[]{Integer.toString(nodeId), type});
			else
				cursor = database.rawQuery("SELECT * FROM media WHERE EXISTS (SELECT 1 FROM nodemedia WHERE nodemedia.media_id = media.id AND nodemedia.node_id = ?)", new String[]{Integer.toString(nodeId)});
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

	public Map<String, List<BuntataMediaAdvanced>> splitByType(List<BuntataMediaAdvanced> media)
	{
		Map<String, List<BuntataMediaAdvanced>> result = new HashMap<>();

		result.put(BuntataMediaType.TYPE_IMAGE, new ArrayList<BuntataMediaAdvanced>());
		result.put(BuntataMediaType.TYPE_VIDEO, new ArrayList<BuntataMediaAdvanced>());

		for (BuntataMediaAdvanced medium : media)
		{
			String type = medium.getMediaType().getName();

			result.get(type).add(medium);
		}

		return result;
	}

	private static class Parser extends DatabaseObjectParser<BuntataMediaAdvanced>
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
				private static final MediaManager.Parser INSTANCE = new MediaManager.Parser();
			}

			public static MediaManager.Parser get()
			{
				return InstanceHolder.INSTANCE;
			}
		}

		@Override
		public BuntataMediaAdvanced parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			BuntataMediaAdvanced result = new BuntataMediaAdvanced(cursor.getInt(BuntataMedia.FIELD_ID), new Date(cursor.getLong(BuntataMedia.FIELD_CREATED_ON)), new Date(cursor.getLong(BuntataMedia.FIELD_UPDATED_ON)));

			result.setName(cursor.getString(BuntataMedia.FIELD_NAME))
				  .setDescription(cursor.getString(BuntataMedia.FIELD_DESCRIPTION))
				  .setMediaTypeId(cursor.getInt(BuntataMedia.FIELD_MEDIATYPE_ID))
				  .setInternalLink(cursor.getString(BuntataMedia.FIELD_INTERNAL_LINK))
				  .setExternalLink(cursor.getString(BuntataMedia.FIELD_EXTERNAL_LINK))
				  .setExternalLinkDescription(cursor.getString(BuntataMedia.FIELD_EXTERNAL_LINK_DESCRIPTION))
				  .setCopyright(cursor.getString(BuntataMedia.FIELD_COPYRIGHT));

			result.setMediaType(new MediaTypeManager(context, datasourceId).getById(result.getMediaTypeId()));

			return result;
		}
	}
}
