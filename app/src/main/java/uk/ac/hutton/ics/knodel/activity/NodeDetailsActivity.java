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
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.util.*;

import jhi.knodel.resource.*;
import me.relex.circleindicator.*;
import uk.ac.hutton.ics.knodel.R;
import uk.ac.hutton.ics.knodel.adapter.*;
import uk.ac.hutton.ics.knodel.database.entity.*;
import uk.ac.hutton.ics.knodel.database.manager.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * The {@link NodeDetailsActivity} shows detailed information about the node. This contains images, attributes and videos.
 *
 * @author Sebastian Raubach
 */
public class NodeDetailsActivity extends BaseActivity
{
	public static final String PARAM_DATASOURCE_ID = "datasourceId";
	public static final String PARAM_NODE_ID       = "nodeId";

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
		ViewPager pager = (ViewPager) findViewById(R.id.node_details_image_pager);
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
		List<KnodelMediaAdvanced> media = mediaManager.getForNode("Image", nodeId);

		if (media.size() > 0)
		{
			/* Set to the pager */
			final ImagePagerAdapter adapter = new ImagePagerAdapter(getSupportFragmentManager(), datasourceId, media);
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
		recyclerView.setAdapter(new AttributeValueAdapter(recyclerView, attributeValues));

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

	/**
	 * The {@link ImagePagerAdapter} handles the image media fragments.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter
	{
		private final List<KnodelMediaAdvanced> dataset;
		private final int                       datasourceId;


		ImagePagerAdapter(FragmentManager fm, int datasourceId, List<KnodelMediaAdvanced> dataset)
		{
			super(fm);
			this.datasourceId = datasourceId;
			this.dataset = dataset;
		}

		@Override
		public int getCount()
		{
			return dataset.size();
		}

		@Override
		public Fragment getItem(final int position)
		{
			return ImageFragment.newInstance(datasourceId, dataset.get(position).getId());
		}
	}

	/**
	 * The {@link ImageFragment} displays the information about an image medium.
	 */
	public static class ImageFragment extends Fragment
	{
		private static final String PARAM_DATASOURCE_ID = "datasourceId";
		private static final String PARAM_MEDIUM_ID     = "mediumId";

		private MediaManager mediaManager;

		private int       datasourceId;
		private int       mediumId;
		private ImageView imageView;
		private TextView  copyright;

		static ImageFragment newInstance(int datasourceId, int mediumId)
		{
			final ImageFragment f = new ImageFragment();

			/* Pass parameters */
			Bundle args = new Bundle();
			args.putInt(PARAM_DATASOURCE_ID, datasourceId);
			args.putInt(PARAM_MEDIUM_ID, mediumId);
			f.setArguments(args);

			return f;
		}

		public ImageFragment()
		{
		}

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			/* Get parameters */
			datasourceId = getArguments() != null ? getArguments().getInt(PARAM_DATASOURCE_ID) : -1;
			mediumId = getArguments() != null ? getArguments().getInt(PARAM_MEDIUM_ID) : -1;
			mediaManager = new MediaManager(getActivity(), datasourceId);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View v = inflater.inflate(R.layout.fragment_node_image, container, false);
			imageView = (ImageView) v.findViewById(R.id.node_image_view);
			copyright = (TextView) v.findViewById(R.id.node_image_copyright);

			return v;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);

			/* Get the medium object */
			KnodelMediaAdvanced medium = mediaManager.getById(mediumId);

			/* Show copyright information if available */
			if (medium.getCopyright() != null)
			{
				copyright.setText(medium.getCopyright());
				copyright.setVisibility(View.VISIBLE);
				copyright.setAlpha(0.5f);
			}
			else
			{
				copyright.setVisibility(View.GONE);
			}

			/* Load the image */
			Picasso.with(getActivity())
				   .load(FileUtils.getFileForDatasource(getActivity(), datasourceId, medium.getInternalLink()))
				   .fit()
				   .centerCrop()
				   .into(imageView);
		}
	}
}
