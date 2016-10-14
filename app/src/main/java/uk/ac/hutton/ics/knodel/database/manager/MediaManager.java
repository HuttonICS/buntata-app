package uk.ac.hutton.ics.knodel.database.manager;

import android.content.*;
import android.database.*;

import java.text.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.database.*;
import uk.ac.hutton.ics.knodel.database.entity.*;

/**
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

			if(type != null)
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

	private static class Parser extends DatabaseObjectParser<KnodelMediaAdvanced>
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
