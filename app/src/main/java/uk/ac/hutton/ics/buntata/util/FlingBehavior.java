/*
 * Copyright 2017 Information & Computational Sciences, The James Hutton Institute
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
import android.support.design.widget.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;

public final class FlingBehavior extends AppBarLayout.Behavior
{

	private static final int TOP_CHILD_FLING_THRESHOLD = 3;
	private boolean isPositive;

	public FlingBehavior()
	{
	}

	public FlingBehavior(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed)
	{
		if (velocityY > 0 && !isPositive || velocityY < 0 && isPositive)
		{
			velocityY = velocityY * -1;
		}
		if (target instanceof RecyclerView && velocityY < 0)
		{
			final RecyclerView recyclerView = (RecyclerView) target;
			final View firstChild = recyclerView.getChildAt(0);
			final int childAdapterPosition = recyclerView.getChildAdapterPosition(firstChild);
			consumed = childAdapterPosition > TOP_CHILD_FLING_THRESHOLD;
		}
		return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
	}

	@Override
	public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed)
	{
		super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
		isPositive = dy > 0;
	}
}