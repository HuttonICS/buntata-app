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

package uk.ac.hutton.ics.knodel.database.entity;

import java.util.*;

import jhi.knodel.resource.*;

/**
 * {@link KnodelAttributeValueAdvanced} is an advanced version of a {@link KnodelAttributeValue}. In addition to foreign keys it holds a reference to
 * the actual {@link KnodelAttribute} it's associated with.
 *
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
