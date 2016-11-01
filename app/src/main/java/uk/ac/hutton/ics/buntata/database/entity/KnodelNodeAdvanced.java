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

package uk.ac.hutton.ics.buntata.database.entity;

import java.util.*;

import jhi.knodel.resource.*;

/**
 * {@link KnodelNodeAdvanced} is an advanced version of a {@link KnodelNode}. In addition to foreign keys it holds a list of references to actual
 * {@link KnodelMediaAdvanced} objects that are associated with it.
 *
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
