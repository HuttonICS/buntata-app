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

import jhi.buntata.resource.*;

/**
 * {@link BuntataNodeAdvanced} is an advanced version of a {@link BuntataNode}. In addition to foreign keys it holds a list of references to actual
 * {@link BuntataMediaAdvanced} objects that are associated with it.
 *
 * @author Sebastian Raubach
 */
public class BuntataNodeAdvanced extends BuntataNode
{
	private List<BuntataMediaAdvanced> media       = new ArrayList<>();
	private boolean                    hasChildren = false;

	public BuntataNodeAdvanced()
	{
	}

	public BuntataNodeAdvanced(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public List<BuntataMediaAdvanced> getMediaAdvanced()
	{
		return media;
	}

	public BuntataNodeAdvanced setMedia(List<BuntataMediaAdvanced> media)
	{
		this.media = media;
		return this;
	}

	public BuntataNodeAdvanced addMedia(BuntataMediaAdvanced medium)
	{
		this.media.add(medium);
		return this;
	}

	public BuntataMediaAdvanced getFirstImage()
	{
		for (BuntataMediaAdvanced m : getMediaAdvanced())
		{
			if (m.getMediaType() != null && "Image".equals(m.getMediaType().getName()) && m.getInternalLink() != null)
			{
				return m;
			}
		}

		return null;
	}

	public boolean hasChildren()
	{
		return hasChildren;
	}

	public BuntataNodeAdvanced setHasChildren(boolean hasChildren)
	{
		this.hasChildren = hasChildren;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataNodeAdvanced{" +
				"media=" + media +
				"} " + super.toString();
	}
}
