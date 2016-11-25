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

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.*;
import android.support.v4.view.*;
import android.support.v7.widget.*;
import android.view.*;

import java.util.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.fragment.*;
import uk.ac.hutton.ics.buntata.service.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link MainActivity} is the main view of the app. It shows the categories and let's the user navigate to the leaf nodes which then show
 * information about the item.
 *
 * @author Sebastian Raubach
 */
public class MainActivity extends DrawerActivity implements OnFragmentChangeListener
{
	private static final int REQUEST_CODE_INTRO             = 1;
	private static final int REQUEST_CODE_SELECT_DATASOURCE = 2;
	private static final int REQUEST_CODE_DETAILS           = 3;

	private int datasourceId = -1;

	private boolean override = false;
	private String  query    = null;

	@BindView(R.id.main_view_fab)
	FloatingActionButton fab;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		/* Make sure the default preferences are set */
		PreferenceUtils.setDefaults(this);
		NodeManager.clearCaches();
		DatasourceService.init(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		init();
	}

	private void init()
	{
		boolean showIntro = !PreferenceUtils.getPreferenceAsBoolean(this, PreferenceUtils.PREFS_EULA_ACCEPTED, false);

		if (showIntro)
		{
			/* Show the intro */
			startActivityForResult(new Intent(getApplicationContext(), IntroductionActivity.class), REQUEST_CODE_INTRO);
		}
		else
		{
			/* Get the selected data source */
			int datasourceId = PreferenceUtils.getPreferenceAsInt(this, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, -1);

			/* If no valid source is selected, open the data source activity */
			if (datasourceId == -1)
			{
				startActivityForResult(new Intent(getApplicationContext(), DatasourceActivity.class), REQUEST_CODE_SELECT_DATASOURCE);
			}
			else
			{
				/* Else check if this is an override call. If so, recreate the activity */
				if (override)
				{
					Intent intent = getIntent();
					finish();
					startActivity(intent);
				}
				/* Else if the data source changed, update the content */
				else if (datasourceId != this.datasourceId)
				{
					updateContent(null, null, datasourceId, -1, -1);
				}
			}

			/* Remember the new data source and disable override */
			this.datasourceId = datasourceId;
			this.override = false;
		}

		showWhatsNew();
	}

	protected void showWhatsNew()
	{
		try
		{
			/* Get the versionCode of the Package, which must be different (incremented) in each release on the market in the AndroidManifest.xml */
			final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);

			int lastVersionCode = PreferenceUtils.getPreferenceAsInt(this, PreferenceUtils.PREFS_LAST_VERSION, -1);

			/* If this is the first start, show nothing just remember the version number */
			if (lastVersionCode == -1)
			{
				PreferenceUtils.setPreferenceAsInt(this, PreferenceUtils.PREFS_LAST_VERSION, packageInfo.versionCode);
			}
			/* Else show what's new if version numbers differ */
			else
			{
				if (packageInfo.versionCode > lastVersionCode)
				{
					startActivity(new Intent(getApplicationContext(), ChangelogActivity.class));
				}
			}

		}
		catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Updates the main view's content
	 *
	 * @param transitionRoot The view to use as the transition root in case we're navigating to a leaf node
	 * @param datasourceId   The current data source id
	 * @param parentId       The id of the parent node
	 */
	private void updateContent(View transitionRoot, final View title, int datasourceId, int parentId, int mediumId)
	{
		if (getSupportFragmentManager().getBackStackEntryCount() == 0)
			fab.hide();
		else
			fab.show();

		/* Check if this node has children */
		List<BuntataNodeAdvanced> children = new NodeManager(this, datasourceId).getForParent(parentId);
		boolean hasChildren = parentId == -1 || children.size() > 0;

		/* If it does */
		if (!hasChildren || children.size() == 1)
		{
			/* If it's only got one child, jump straight to it */
			if (children.size() == 1)
				parentId = children.get(0).getId();

			/* If it's a leaf node, open the details activity */
			Intent intent = new Intent(getApplicationContext(), NodeDetailsActivity.class);

			/* Pass parameters */
			Bundle args = new Bundle();
			args.putInt(NodeDetailsActivity.PARAM_NODE_ID, parentId);
			args.putInt(NodeDetailsActivity.PARAM_DATASOURCE_ID, datasourceId);
			args.putInt(NodeDetailsActivity.PARAM_PREFERED_FIRST_MEDIUM, mediumId);
			intent.putExtras(args);

			/* Depending on the android version, transition views or just slide */
			List<Pair<View, String>> pairs = new ArrayList<>();
			pairs.add(Pair.create(transitionRoot, getString(R.string.transition_node_view)));
			pairs.add(Pair.create(transitionRoot, getString(R.string.transition_node_details_view)));

			if (title.getVisibility() == View.VISIBLE)
				pairs.add(Pair.create(title, "t_node_title"));

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

			ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pairs.toArray(new Pair[pairs.size()]));

			startActivityForResult(intent, REQUEST_CODE_DETAILS, options.toBundle());
		}
		else
		{
			/* Add a new fragment */
			Fragment fragment = new NodeFragment();

			/* Pass parameters */
			Bundle args = new Bundle();
			args.putInt(NodeFragment.PARAM_PARENT_ID, parentId);
			args.putInt(NodeFragment.PARAM_DATASOURCE_ID, datasourceId);
			fragment.setArguments(args);

			/* Add a slide transition */
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
			ft.addToBackStack(fragment.toString());
			ft.replace(R.id.fragment_holder, fragment, fragment.toString()).commit();
		}
	}

	@OnClick(R.id.main_view_fab)
	public void onFabClicked()
	{
		while (getSupportFragmentManager().getBackStackEntryCount() > 1)
			onBackPressed();
	}

	@Override
	public void onBackPressed()
	{
		if (getSupportFragmentManager().getBackStackEntryCount() > 2)
			fab.show();
		else
			fab.hide();

		/* If there's only one item left on the stack, finish as there's nothing to go back to */
		if (getSupportFragmentManager().getBackStackEntryCount() == 1)
			finish();
		/* Else, just let the parent handle things */
		else
			super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode)
		{
			case REQUEST_CODE_INTRO:
				if (resultCode == RESULT_OK)
				{
//					PreferenceUtils.setPreferenceAsBoolean(this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, true);
				}
				else
				{
					PreferenceUtils.setPreferenceAsBoolean(this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false);
					/* User cancelled the intro so we'll finish this activity too. */
					finish();
				}
				break;
			case REQUEST_CODE_SELECT_DATASOURCE:
			case REQUEST_DATA_SOURCE:
				if (resultCode != RESULT_OK)
				{
					startActivityForResult(new Intent(getApplicationContext(), DatasourceActivity.class), REQUEST_CODE_SELECT_DATASOURCE);
				}
				else
				{
					override = true;
				}
				break;
			case REQUEST_CODE_DETAILS:
				/* We're coming back from the details view, so don't add anything */
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		/* Inflate the menu */
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, menu);

		/* Find the search menu item */
		final MenuItem searchItem = menu.findItem(R.id.action_search);

		/* Get the search manager */
		SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

		/* Get the actual search view */
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

		if (searchView != null)
		{
			searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
			searchView.setQueryHint(getString(R.string.search_query_hint));
			/* Listen to submit events */
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
			{
				@Override
				public boolean onQueryTextSubmit(String query)
				{
					/* Trim leading and trailing spaces that some keyboards will add */
					query = query.trim();

					searchView.clearFocus();

					MainActivity.this.query = query;
					filter(query);

					return false;
				}

				@Override
				public boolean onQueryTextChange(String s)
				{
					return false;
				}
			});
			searchView.setOnCloseListener(new SearchView.OnCloseListener()
			{
				@Override
				public boolean onClose()
				{
					MainActivity.this.query = null;
					filter("");

					return false;
				}
			});
		}

		return super.onCreateOptionsMenu(menu);
	}

	public String getFilter()
	{
		return query;
	}

	private void filter(String query)
	{
		/* Get the current fragment with this horrible construct */
		String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
		NodeFragment fragment = (NodeFragment) getSupportFragmentManager().findFragmentByTag(tag);

		/* Then filter it */
		fragment.filter(query);

		if (!StringUtils.isEmpty(query))
		{
			GoogleAnalyticsUtils.trackEvent(this, getTracker(BaseActivity.TrackerName.APP_TRACKER), getString(R.string.ga_event_category_node_search), "Datasource: " + datasourceId, query);
		}
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_main;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	public void onFragmentChange(View transitionRoot, View title, int datasourceId, int parentId, int mediumId)
	{
		updateContent(transitionRoot, title, datasourceId, parentId, mediumId);
	}
}
