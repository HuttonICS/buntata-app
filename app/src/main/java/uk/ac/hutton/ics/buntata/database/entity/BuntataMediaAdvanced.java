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
 * {@link BuntataMediaAdvanced} is an advanced version of a {@link BuntataMedia}. In addition to foreign keys it holds a reference to the actual
 * {@link BuntataMediaType} it's associated with.
 *
 * @author Sebastian Raubach
 */
public class BuntataMediaAdvanced extends BuntataMedia
{
	private BuntataMediaType mediaType;

	public BuntataMediaAdvanced()
	{
	}

	public BuntataMediaAdvanced(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public BuntataMediaType getMediaType()
	{
		return mediaType;
	}

	public BuntataMediaAdvanced setMediaType(BuntataMediaType mediaType)
	{
		this.mediaType = mediaType;
		return this;
	}

	@Override
	public String toString()
	{
		return "BuntataMediaAdvanced{" +
				"mediaType=" + mediaType +
				"} " + super.toString();
	}
}
