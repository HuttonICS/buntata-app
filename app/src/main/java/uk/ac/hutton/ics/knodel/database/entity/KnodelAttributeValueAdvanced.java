package uk.ac.hutton.ics.knodel.database.entity;

import java.util.*;

import jhi.knodel.resource.*;

/**
 * @author Sebastian Raubach
 */

public class KnodelAttributeValueAdvanced extends KnodelAttributeValue
{
	private KnodelAttribute attribute;

	public KnodelAttributeValueAdvanced()
	{
	}

	public KnodelAttributeValueAdvanced(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public KnodelAttributeValueAdvanced(int id, Date createdOn, Date updatedOn, Integer nodeId, Integer attributeId, String value, KnodelAttribute attribute)
	{
		super(id, createdOn, updatedOn, nodeId, attributeId, value);
		this.attribute = attribute;
	}

	public KnodelAttribute getAttribute()
	{
		return attribute;
	}

	public KnodelAttributeValueAdvanced setAttribute(KnodelAttribute attribute)
	{
		this.attribute = attribute;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelAttributeValueAdvanced{" +
				"attribute=" + attribute +
				"} " + super.toString();
	}
}
