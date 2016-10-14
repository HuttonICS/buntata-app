package uk.ac.hutton.ics.knodel.database.entity;

import java.util.*;

import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */

public class KnodelMediaAdvanced extends KnodelMedia
{
	private KnodelMediaType mediaType;

	public KnodelMediaAdvanced()
	{
	}

	public KnodelMediaAdvanced(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public KnodelMediaAdvanced(int id, Date createdOn, Date updatedOn, Integer mediaTypeId, String name, String description, String internalLink, String externalLink, String externalLinkDescription, String copyright, KnodelMediaType mediaType)
	{
		super(id, createdOn, updatedOn, mediaTypeId, name, description, internalLink, externalLink, externalLinkDescription, copyright);
		this.mediaType = mediaType;
	}

	public KnodelMediaType getMediaType()
	{
		return mediaType;
	}

	public KnodelMediaAdvanced setMediaType(KnodelMediaType mediaType)
	{
		this.mediaType = mediaType;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelMediaAdvanced{" +
				"mediaType=" + mediaType +
				"} " + super.toString();
	}
}
