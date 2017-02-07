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

package uk.ac.hutton.ics.buntata.adapter;

import android.support.v4.app.*;

import java.util.*;

import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.fragment.*;

/**
 * The {@link ImagePagerAdapter} handles the image media fragments.
 */
public class ImagePagerAdapter extends FragmentStatePagerAdapter
{
	private final List<BuntataMediaAdvanced> dataset = new ArrayList<>();
	private final int     datasourceId;
	private final int     nodeId;
	private final boolean isFullscreen;

	public ImagePagerAdapter(FragmentManager fm, int datasourceId, int nodeId, boolean isFullscreen, List<BuntataMediaAdvanced> dataset, int preferedMediumId)
	{
		super(fm);
		this.datasourceId = datasourceId;
		this.nodeId = nodeId;
		this.isFullscreen = isFullscreen;

		for (BuntataMediaAdvanced medium : dataset)
		{
			if (medium.getId() == preferedMediumId)
				this.dataset.add(0, medium);
			else
				this.dataset.add(medium);
		}
	}

	@Override
	public int getCount()
	{
		return dataset.size();
	}

	@Override
	public Fragment getItem(final int position)
	{
		return ImageFragment.newInstance(datasourceId, nodeId, position == 0, dataset.get(position).getId(), isFullscreen);
	}
}