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

package uk.ac.hutton.ics.knodel.adapter;

import android.graphics.*;
import android.support.v7.widget.*;
import android.view.*;

/**
 * {@link GridSpacingItemDecoration} handles item spacing for {@link RecyclerView} items.
 *
 * @author Sebastian Raubach
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration
{
	private int left;
	private int right;
	private int top;
	private int bottom;
	private int spanCount;
	private int spacing;

	/**
	 * Creates a new instance with the given column count, horizontal and vertical margin and spacing between items.
	 *
	 * @param spanCount        The number of columns
	 * @param horizontalMargin The horizontal margin for edge items (left, right)
	 * @param verticalMargin   The vertical margin for edge items (top, bottom)
	 * @param spacing          The spacing between items
	 */
	public GridSpacingItemDecoration(int spanCount, int horizontalMargin, int verticalMargin, int spacing)
	{
		this.left = horizontalMargin;
		this.right = horizontalMargin;
		this.top = verticalMargin;
		this.bottom = verticalMargin;

		this.spanCount = spanCount;
		this.spacing = spacing;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
	{
		int position = parent.getChildAdapterPosition(view); // item position
		int column = position % spanCount; // item column
		int row = position / spanCount; // item row
		int count = state.getItemCount(); // total count
		int maxRow = count / spanCount; // index of last row

		/* If this is the first column, add the left margin */
		if (column == 0)
		{
			outRect.left = left;
		}
		/* Else, there's an item to the left, add spacing */
		else
		{
			outRect.left = spacing / 2;
		}
		/* If this is the last column, add the right margin */
		if (column == spanCount - 1)
		{
			outRect.right = right;
		}
		/* Else, there's an item to the right, add spacing*/
		else
		{
			outRect.right = spacing / 2;
		}
		/* If this is the first row, add the top margin */
		if (position < spanCount)
		{
			outRect.top = top;
		}
		/* Else, add top spacing */
		else
		{
			outRect.top = spacing;
		}
		/* If this is the last row, add bottom margin */
		if (row == maxRow)
		{
			outRect.bottom = bottom;
		}
	}
}