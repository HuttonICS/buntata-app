package uk.ac.hutton.ics.knodel.database.entity;

import java.util.*;

import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */

public class KnodelNodeAdvanced extends KnodelNode
{
	private List<KnodelMediaAdvanced> media = new ArrayList<>();

	public KnodelNodeAdvanced()
	{
	}

	public KnodelNodeAdvanced(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public KnodelNodeAdvanced(int id, Date createdOn, Date updatedOn, Integer datasourceId, String name, String description, List<KnodelMediaAdvanced> media)
	{
		super(id, createdOn, updatedOn, datasourceId, name, description);
		this.media = media;
	}

	public List<KnodelMediaAdvanced> getMedia()
	{
		return media;
	}

	public KnodelNodeAdvanced setMedia(List<KnodelMediaAdvanced> media)
	{
		this.media = media;
		return this;
	}

	public KnodelNodeAdvanced addMedia(KnodelMediaAdvanced medium)
	{
		this.media.add(medium);
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelNodeAdvanced{" +
				"media=" + media +
				"} " + super.toString();
	}
}
