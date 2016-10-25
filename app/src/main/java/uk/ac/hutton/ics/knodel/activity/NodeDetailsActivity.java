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

package uk.ac.hutton.ics.knodel.activity;


import android.os.*;
import android.support.design.widget.*;
import android.support.v4.view.*;
import android.support.v7.widget.*;
import android.view.*;

import java.util.*;

import jhi.knodel.resource.*;
import me.relex.circleindicator.*;
import uk.ac.hutton.ics.knodel.R;
import uk.ac.hutton.ics.knodel.adapter.*;
import uk.ac.hutton.ics.knodel.database.entity.*;
import uk.ac.hutton.ics.knodel.database.manager.*;

/**
 * The {@link NodeDetailsActivity} shows detailed information about the node. This contains images, attributes and videos.
 *
 * @author Sebastian Raubach
 */
public class NodeDetailsActivity extends BaseActivity
{
	public static final String PARAM_DATASOURCE_ID = "datasourceId";
	public static final String PARAM_NODE_ID       = "nodeId";
	private ViewPager pager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		/* Get parameters */
		Bundle args = getIntent().getExtras();
		int datasourceId = args.getInt(PARAM_DATASOURCE_ID, -1);
		int nodeId = args.getInt(PARAM_NODE_ID, -1);

		/* Initialize the media manager */
		MediaManager mediaManager = new MediaManager(this, datasourceId);

		/* Get the views */
		pager = (ViewPager) findViewById(R.id.node_details_image_pager);
		CircleIndicator indicator = (CircleIndicator) findViewById(R.id.node_details_image_indicator);
		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.node_details_attributes);
		AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.node_details_app_bar);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		/* Get the node */
		KnodelNode node = new NodeManager(this, datasourceId).getById(nodeId);

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(node.getName());
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			{
				getSupportActionBar().setHomeButtonEnabled(true);
			}
		}

		/* Get all the media */
		List<KnodelMediaAdvanced> media = mediaManager.getForNode(null, nodeId);
		Map<String, List<KnodelMediaAdvanced>> splitByType = mediaManager.splitByType(media);

		if (splitByType.get(KnodelMediaType.TYPE_IMAGE).size() > 0)
		{
			/* Set to the pager */
			final ImagePagerAdapter adapter = new ImagePagerAdapter(getSupportFragmentManager(), datasourceId, splitByType.get(KnodelMediaType.TYPE_IMAGE));
			pager.setAdapter(adapter);
			indicator.setViewPager(pager);
		}
		else
		{
			/* Hide the views */
			pager.setVisibility(View.GONE);
			indicator.setVisibility(View.GONE);

			/* Tell the coordinator to wrap its content */
			CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
			lp.height = CoordinatorLayout.LayoutParams.WRAP_CONTENT;
			appBarLayout.setFitsSystemWindows(false);
		}

		/* Get all the attributes */
		List<KnodelAttributeValueAdvanced> attributeValues = new AttributeValueManager(this, datasourceId).getForNode(nodeId);

		/* Set them to the recycler view */
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new AttributeValueVideoAdapter(this, recyclerView, datasourceId, attributeValues, splitByType.get(KnodelMediaType.TYPE_VIDEO)));

		/* Set the separator width */
		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;

		int horizontalMargin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
		int verticalMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);

		/* Add the item decorator */
		recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, horizontalMargin, verticalMargin, valueInPixels));
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_node_details;
	}

	@Override
	protected Integer getToolbarId()
	{
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed()
	{
		if (pager.getVisibility() == View.VISIBLE)
		{
			if (pager.getCurrentItem() < 2)
				pager.setCurrentItem(0, true);
		}

		super.onBackPressed();
	}
}
