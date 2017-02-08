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

package uk.ac.hutton.ics.buntata.activity;

import android.os.*;
import android.support.v4.view.*;
import android.view.*;

import java.util.*;

import butterknife.*;
import jhi.buntata.resource.*;
import me.relex.circleindicator.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.adapter.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;

/**
 * @author Sebastian Raubach
 */

public class ImageViewPagerActivity extends BaseActivity
{
	@BindView(R.id.node_details_image_pager)
	ViewPager       pager;
	@BindView(R.id.node_details_image_indicator)
	CircleIndicator circleIndicator;
	private ImagePagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		/* Get parameters */
		Bundle args = getIntent().getExtras();

		int datasourceId = -1;
		int nodeId = -1;
		int preferedMediumId = -1;

		/* If this Activity has been called based on deep linking, then get the parameters from the request */
		if (args != null)
		{
			datasourceId = args.getInt(NodeDetailsActivity.PARAM_DATASOURCE_ID, -1);
			nodeId = args.getInt(NodeDetailsActivity.PARAM_NODE_ID, -1);
			preferedMediumId = args.getInt(NodeDetailsActivity.PARAM_PREFERED_FIRST_MEDIUM, -1);
		}

		/* Initialize the media manager */
		MediaManager mediaManager = new MediaManager(this, datasourceId);

		/* Get all the media */
		List<BuntataMediaAdvanced> media = mediaManager.getForNode(null, nodeId);
		Map<String, List<BuntataMediaAdvanced>> splitByType = mediaManager.splitByType(media);

		int imageCount = splitByType.get(BuntataMediaType.TYPE_IMAGE).size();
			/* Set to the pager */
		adapter = new ImagePagerAdapter(getSupportFragmentManager(), datasourceId, nodeId, true, splitByType.get(BuntataMediaType.TYPE_IMAGE), preferedMediumId);
		pager.setAdapter(adapter);
		circleIndicator.setViewPager(pager);
		circleIndicator.setVisibility(imageCount > 1 ? View.VISIBLE : View.GONE);

		/* Hide the status bar */
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_node_images;
	}

	@Override
	protected Integer getToolbarId()
	{
		return null;
	}

	@Override
	public void finishAfterTransition()
	{
		super.finishAfterTransition();

		adapter.cleanup();
	}
}
