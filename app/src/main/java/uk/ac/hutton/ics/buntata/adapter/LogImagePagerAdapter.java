/*
 * Copyright 2018 Information & Computational Sciences, The James Hutton Institute
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
 * The {@link LogImagePagerAdapter} handles the image media fragments.
 */
public class LogImagePagerAdapter extends FragmentStatePagerAdapter
{
	private final List<LogEntryImage> dataset = new ArrayList<>();

	private WeakHashMap<Integer, LogImageFragment> fragments = new WeakHashMap<>();

	public LogImagePagerAdapter(FragmentManager fm, List<LogEntryImage> dataset)
	{
		super(fm);
		this.dataset.addAll(dataset);
	}

	@Override
	public int getCount()
	{
		return dataset.size();
	}

	@Override
	public Fragment getItem(final int position)
	{
		LogImageFragment f = LogImageFragment.newInstance(dataset.get(position).getId(), true, true);
		fragments.put(position, f);
		return f;
	}

	public void cleanup()
	{
		for (LogImageFragment f : fragments.values())
			f.cleanup();
	}
}