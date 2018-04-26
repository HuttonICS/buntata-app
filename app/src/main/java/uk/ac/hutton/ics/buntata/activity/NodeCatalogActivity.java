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

package uk.ac.hutton.ics.buntata.activity;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v4.util.Pair;
import android.support.v4.view.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;

import java.util.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.adapter.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * @author Sebastian Raubach
 */

public class NodeCatalogActivity extends BaseActivity
{
	@BindView(R.id.node_catalog_recyclerview)
	RecyclerView recyclerView;
	private int                       datasourceId;
	private List<BuntataNodeAdvanced> nodes;
	private NodeCatalogAdapter        adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(R.string.title_activity_catalog);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

		datasourceId = PreferenceUtils.getPreferenceAsInt(this, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, -1);

		nodes = new NodeManager(this, datasourceId).getAllLeaves();

		int horizontalMargin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
		int verticalMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);

		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;
		recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, horizontalMargin, verticalMargin, valueInPixels));

		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		adapter = new NodeCatalogAdapter(this, recyclerView, datasourceId, nodes)
		{
			@Override
			public void onNodeClicked(View transitionRoot, BuntataMediaAdvanced medium, BuntataNodeAdvanced node)
			{
				/* If it's a leaf node, open the details activity */
				Intent intent = new Intent(getApplicationContext(), NodeDetailsActivity.class);

				/* Pass parameters */
				Bundle args = new Bundle();
				args.putInt(NodeDetailsActivity.PARAM_NODE_ID, node.getId());
				args.putInt(NodeDetailsActivity.PARAM_DATASOURCE_ID, datasourceId);
				if (medium != null)
					args.putInt(NodeDetailsActivity.PARAM_PREFERED_FIRST_MEDIUM, medium.getId());
				intent.putExtras(args);

				/* Depending on the android version, transition views or just slide */
				List<Pair<View, String>> pairs = new ArrayList<>();
				pairs.add(Pair.create(transitionRoot, getString(R.string.transition_node_view)));
				pairs.add(Pair.create(transitionRoot, getString(R.string.transition_node_details_view)));
				pairs.add(Pair.create((View) toolbar, "t_toolbar"));

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
					View decor = getWindow().getDecorView();

					View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
					if (navigationBar != null)
						pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));

					View statusBar = decor.findViewById(android.R.id.statusBarBackground);
					if (statusBar != null)
						pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
				}

				ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(NodeCatalogActivity.this, pairs.toArray(new Pair[pairs.size()]));

				startActivity(intent, options.toBundle());
			}
		};
		recyclerView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		/* Inflate the menu */
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.node_list_menu, menu);

		/* Find the search menu item */
		final MenuItem searchItem = menu.findItem(R.id.action_search);

		/* Get the search manager */
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

		/* Get the actual search view */
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

		if (searchView != null)
		{
			DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			searchView.setMaxWidth(displayMetrics.widthPixels / 2);
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			searchView.setQueryHint(getString(R.string.search_node_name_hint));
			/* Listen to submit events */
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
			{
				@Override
				public boolean onQueryTextSubmit(String query)
				{
					/* Trim leading and trailing spaces that some keyboards will add */
					query = query.trim();

					filter(query);

					searchView.clearFocus();

					return false;
				}

				@Override
				public boolean onQueryTextChange(String s)
				{
					/* Close the search field when the search string is empty */
					if (StringUtils.isEmpty(s))
					{
						searchView.setIconified(true);
						searchView.clearFocus();
					}
					return false;
				}
			});
			searchView.setOnCloseListener(new SearchView.OnCloseListener()
			{
				@Override
				public boolean onClose()
				{
					filter("");

					return false;
				}
			});
		}

		return super.onCreateOptionsMenu(menu);
	}

	private void filter(String filter)
	{
		if (StringUtils.isEmpty(filter))
		{
			adapter.update(nodes);
		}
		else
		{
			filter = filter.toLowerCase();
			List<BuntataNodeAdvanced> list = new ArrayList<>();

			for (BuntataNodeAdvanced node : nodes)
			{
				if (node.getName().toLowerCase().contains(filter))
					list.add(node);
			}

			adapter.update(list);
		}
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
	protected Integer getLayoutId()
	{
		return R.layout.activity_node_catalog;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}
}
