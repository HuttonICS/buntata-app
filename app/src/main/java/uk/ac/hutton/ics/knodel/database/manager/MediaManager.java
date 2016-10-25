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
 * The {@link MediaManager} extends {@link AbstractManager} and can be used to obtain {@link KnodelMediaAdvanced}s from the database.
 *
 * @author Sebastian Raubach
 */
public class MediaManager extends AbstractManager<KnodelMediaAdvanced>
{
	private static final String[] ALL_FIELDS = {KnodelMedia.FIELD_ID, KnodelMedia.FIELD_NAME, KnodelMedia.FIELD_DESCRIPTION, KnodelMedia.FIELD_MEDIATYPE_ID, KnodelMedia.FIELD_INTERNAL_LINK, KnodelMedia.FIELD_EXTERNAL_LINK, KnodelMedia.FIELD_EXTERNAL_LINK_DESCRIPTION, KnodelMedia.FIELD_CREATED_ON, KnodelMedia.FIELD_UPDATED_ON, KnodelMedia.FIELD_COPYRIGHT};

	public MediaManager(Context context, int datasourceId)
	{
		super(context, datasourceId);
	}

	@Override
	protected DatabaseObjectParser<KnodelMediaAdvanced> getDefaultParser()
	{
		return Parser.Inst.get();
	}

	@Override
	protected String getTableName()
	{
		return KnodelMedia.TABLE_NAME;
	}

	@Override
	protected String[] getAllFields()
	{
		return ALL_FIELDS;
	}

	public List<KnodelMediaAdvanced> getForNode(String type, int nodeId)
	{
		List<KnodelMediaAdvanced> result = new ArrayList<>();

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

	public Map<String, List<KnodelMediaAdvanced>> splitByType(List<KnodelMediaAdvanced> media)
	{
		Map<String, List<KnodelMediaAdvanced>> result = new HashMap<>();

		result.put(KnodelMediaType.TYPE_IMAGE, new ArrayList<KnodelMediaAdvanced>());
		result.put(KnodelMediaType.TYPE_VIDEO, new ArrayList<KnodelMediaAdvanced>());

		for (KnodelMediaAdvanced medium : media)
		{
			String type = medium.getMediaType().getName();

			result.get(type).add(medium);
		}

		return result;
	}

	private static class Parser extends DatabaseObjectParser<KnodelMediaAdvanced>
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
		public KnodelMediaAdvanced parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException
		{
			KnodelMediaAdvanced result = new KnodelMediaAdvanced(cursor.getInt(KnodelMedia.FIELD_ID), new Date(cursor.getLong(KnodelMedia.FIELD_CREATED_ON)), new Date(cursor.getLong(KnodelMedia.FIELD_UPDATED_ON)));

			result.setName(cursor.getString(KnodelMedia.FIELD_NAME))
				  .setDescription(cursor.getString(KnodelMedia.FIELD_DESCRIPTION))
				  .setMediaTypeId(cursor.getInt(KnodelMedia.FIELD_MEDIATYPE_ID))
				  .setInternalLink(cursor.getString(KnodelMedia.FIELD_INTERNAL_LINK))
				  .setExternalLink(cursor.getString(KnodelMedia.FIELD_EXTERNAL_LINK))
				  .setExternalLinkDescription(cursor.getString(KnodelMedia.FIELD_EXTERNAL_LINK_DESCRIPTION))
				  .setCopyright(cursor.getString(KnodelMedia.FIELD_COPYRIGHT));

			result.setMediaType(new MediaTypeManager(context, datasourceId).getById(result.getMediaTypeId()));

			return result;
		}
	}
}
