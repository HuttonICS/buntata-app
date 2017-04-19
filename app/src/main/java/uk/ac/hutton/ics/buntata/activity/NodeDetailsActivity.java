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

package uk.ac.hutton.ics.buntata.activity;


import android.content.*;
import android.net.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.view.*;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import java.util.*;

import butterknife.*;
import jhi.buntata.resource.*;
import me.relex.circleindicator.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.adapter.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link NodeDetailsActivity} shows detailed information about the node. This contains images, attributes and videos.
 *
 * @author Sebastian Raubach
 */
public class NodeDetailsActivity extends BaseActivity
{
	public static final String PARAM_DATASOURCE_ID         = "datasourceId";
	public static final String PARAM_PREFERED_FIRST_MEDIUM = "preferedMediumId";
	public static final String PARAM_NODE_ID               = "nodeId";

	private int datasourceId = -1;
	private int nodeId       = -1;

	@BindView(R.id.node_details_image_pager)
	ViewPager       pager;
	@BindView(R.id.node_details_image_indicator)
	CircleIndicator circleIndicator;
	@BindView(R.id.node_details_attributes)
	RecyclerView    attributeRecyclerView;
	@BindView(R.id.node_details_app_bar)
	AppBarLayout    appBarLayout;
	@BindView(R.id.toolbar)
	Toolbar         toolbar;
	@BindView(R.id.node_details_similar_nodes_header)
	TextView        similarNodesHeader;
	@BindView(R.id.node_details_similar_nodes)
	RecyclerView    similarNodes;
	@BindView(R.id.node_details_similar_nodes_layout)
	LinearLayout    similarNodesLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		/* Get parameters */
		Bundle args = getIntent().getExtras();
		Uri data = getIntent().getData();

		int preferedMediumId = -1;

		/* If this Activity has been called based on deep linking, then get the parameters from the request */
		if (data != null)
		{
			String paramDatasourceId = data.getQueryParameter("d");
			try
			{
				datasourceId = Integer.parseInt(paramDatasourceId);
				PreferenceUtils.setPreferenceAsInt(this, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, datasourceId);
			}
			catch (NullPointerException | NumberFormatException e)
			{
			}
			String paramNodeId = data.getQueryParameter("n");
			try
			{
				nodeId = Integer.parseInt(paramNodeId);
			}
			catch (NullPointerException | NumberFormatException e)
			{
			}

			List<BuntataDatasource> datasources = new DatasourceManager(this, -1).getAll();

			BuntataDatasource datasource = null;
			if (datasources != null)
			{
				for (BuntataDatasource ds : datasources)
				{
					if (ds.getId() == datasourceId)
						datasource = ds;
				}
			}

			if (datasource == null)
			{
				ToastUtils.createToast(this, R.string.toast_datasource_not_found, ToastUtils.LENGTH_LONG);
				this.finish();
			}
			else
			{
				BuntataNodeAdvanced node = new NodeManager(this, datasourceId).getById(nodeId);

				if (node == null)
				{
					ToastUtils.createToast(this, R.string.toast_node_not_found, ToastUtils.LENGTH_LONG);
					this.finish();
				}
			}
		}
		/* Otherwise get the parameters from the calling Activity */
		else if (args != null)
		{
			datasourceId = args.getInt(PARAM_DATASOURCE_ID, -1);
			nodeId = args.getInt(PARAM_NODE_ID, -1);
			preferedMediumId = args.getInt(PARAM_PREFERED_FIRST_MEDIUM, -1);
		}

		/* Initialize the media manager */
		MediaManager mediaManager = new MediaManager(this, datasourceId);
		NodeManager nodeManager = new NodeManager(this, datasourceId);

		setSupportActionBar(toolbar);

		/* Get the node */
		BuntataNode node = new NodeManager(this, datasourceId).getById(nodeId);

		GoogleAnalyticsUtils.trackEvent(this, getTracker(TrackerName.APP_TRACKER), getString(R.string.ga_event_category_node), getString(R.string.ga_event_action_node_view), node.getName());

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(node.getName());
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		/* Get all the media */
		List<BuntataMediaAdvanced> media = mediaManager.getForNode(null, nodeId);
		Map<String, List<BuntataMediaAdvanced>> splitByType = mediaManager.splitByType(media);

		int imageCount = splitByType.get(BuntataMediaType.TYPE_IMAGE).size();
		if (imageCount > 0)
		{
			/* Set to the pager */
			final ImagePagerAdapter adapter = new ImagePagerAdapter(getSupportFragmentManager(), datasourceId, nodeId, false, splitByType.get(BuntataMediaType.TYPE_IMAGE), preferedMediumId);
			pager.setAdapter(adapter);
			circleIndicator.setViewPager(pager);
			circleIndicator.setVisibility(imageCount > 1 ? View.VISIBLE : View.GONE);
		}
		else
		{
			/* Hide the views */
			pager.setVisibility(View.GONE);
			circleIndicator.setVisibility(View.GONE);

			/* Tell the coordinator to wrap its content */
			CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
			lp.height = CoordinatorLayout.LayoutParams.WRAP_CONTENT;
			appBarLayout.setFitsSystemWindows(false);
		}

		float heightDp = getResources().getDisplayMetrics().heightPixels / 1.5f;
		CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
		lp.height = (int) heightDp;

		/* Get all the attributes */
		List<BuntataAttributeValueAdvanced> attributeValues = new AttributeValueManager(this, datasourceId).getForNode(nodeId);

		/* Set them to the recycler view */
		attributeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		attributeRecyclerView.setAdapter(new AttributeValueVideoAdapter(this, datasourceId, attributeValues, splitByType.get(BuntataMediaType.TYPE_VIDEO)));

		/* Set the separator width */
		final int valueInPixels = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
		final int horizontalMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
		final int verticalMargin = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);

		/* Add the item decorator */
		attributeRecyclerView.addItemDecoration(new GridSpacingItemDecoration(1, horizontalMargin, verticalMargin, valueInPixels / 2));

		/* Get all the similar nodes */
		final List<BuntataNodeAdvanced> similarNodeList = nodeManager.getSimilarNodes(nodeId);

		if (similarNodeList.size() > 0)
		{
			similarNodesLayout.setVisibility(View.VISIBLE);

			/* Wait for the view to fully become visible */
			similarNodesLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
			{
				@Override
				public void onGlobalLayout()
				{
					similarNodesLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

					/* Then add the data */
					similarNodes.setLayoutManager(new LinearLayoutManager(NodeDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false));
					similarNodes.addItemDecoration(new GridSpacingItemDecoration(similarNodeList.size(), horizontalMargin, verticalMargin, valueInPixels / 2));
					similarNodes.setAdapter(new NodeAdapter(NodeDetailsActivity.this, similarNodes, datasourceId, -1, similarNodeList)
					{
						@Override
						public void onNodeClicked(View transitionRoot, View title, BuntataMediaAdvanced medium, BuntataNodeAdvanced node)
						{
							/* Open the details activity */
							Intent intent = new Intent(getApplicationContext(), NodeDetailsActivity.class);

							/* Pass parameters */
							Bundle args = new Bundle();
							args.putInt(NodeDetailsActivity.PARAM_NODE_ID, node.getId());
							args.putInt(NodeDetailsActivity.PARAM_DATASOURCE_ID, node.getDatasourceId());
							args.putInt(NodeDetailsActivity.PARAM_PREFERED_FIRST_MEDIUM, -1);
							intent.putExtras(args);

							startActivity(intent);
						}
					});

					/* Snap the recyclerview items */
					SnapHelper helper = new LinearSnapHelper();
					helper.attachToRecyclerView(similarNodes);
				}
			});
		}
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
//		Intent upIntent = NavUtils.getParentActivityIntent(this);
//		if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot())
//		{
//			TaskStackBuilder.create(this)
//							.addNextIntentWithParentStack(upIntent)
//							.startActivities();
//		}
//		else
//		{
		if (pager.getVisibility() == View.VISIBLE)
		{
			if (pager.getCurrentItem() < 2)
				pager.setCurrentItem(0, true);
		}

		super.onBackPressed();
//		}
	}
}
