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

package uk.ac.hutton.ics.buntata.util;

import android.content.*;
import android.support.annotation.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;

public class EmptyRecyclerView extends RecyclerView
{
	@Nullable
	View emptyView;

	public EmptyRecyclerView(Context context)
	{
		super(context);
	}

	public EmptyRecyclerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public EmptyRecyclerView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	void checkIfEmpty()
	{
		if (emptyView != null)
		{
			boolean visible = getAdapter() == null || getAdapter().getItemCount() < 1;
			emptyView.setVisibility(visible ? VISIBLE : GONE);
		}
	}

	final AdapterDataObserver observer = new AdapterDataObserver()
	{
		@Override
		public void onChanged()
		{
			super.onChanged();
			checkIfEmpty();
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount)
		{
			super.onItemRangeInserted(positionStart, itemCount);
			checkIfEmpty();
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount)
		{
			super.onItemRangeRemoved(positionStart, itemCount);
			checkIfEmpty();
		}
	};

	@Override
	public void setAdapter(@Nullable Adapter adapter)
	{
		final Adapter oldAdapter = getAdapter();
		if (oldAdapter != null)
		{
			oldAdapter.unregisterAdapterDataObserver(observer);
		}
		super.setAdapter(adapter);
		if (adapter != null)
		{
			adapter.registerAdapterDataObserver(observer);
			observer.onChanged();
		}
	}

	public void setEmptyView(@Nullable View emptyView)
	{
		this.emptyView = emptyView;
		checkIfEmpty();
	}
}