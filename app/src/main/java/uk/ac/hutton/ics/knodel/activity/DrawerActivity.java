/**
 * Germinate Mobile is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group
 * at JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p/>
 * Copyright (c) 2012-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.knodel.activity;

import android.content.*;
import android.content.res.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.view.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * {@link uk.ac.hutton.ics.knodel.activity.DrawerActivity} extends {@link uk.ac.hutton.ics.knodel.activity.BaseActivity} and adds a {@link
 * DrawerLayout} to the {@link android.app.Activity}.
 * <p/>
 * This drawer is used as the main menu of Germinate Scan. All subclasses will have this drawer. Make sure to include the correct layout in the layout
 * .xml file. Refer to activity_main.xml to see an example.
 *
 * @author Sebastian Raubach
 */
public abstract class DrawerActivity extends BaseActivity
{
	protected static final int REQUEST_DATA_SOURCE = 1000;
	protected static final int REQUEST_PREFS       = 1001;

	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			{
				getSupportActionBar().setHomeButtonEnabled(true);
			}
		}

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		NavigationView navigationView = (NavigationView) findViewById(R.id.drawer_menu);

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
		{
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem)
			{
				return onNavigation(menuItem);
			}
		});

		        /* ActionBarDrawerToggle ties together the the proper interactions
		 * between the sliding drawer and the action bar app icon */
		drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
				drawerLayout, /* DrawerLayout object */
				R.string.drawer_open, /* "open drawer" description for accessibility */
				R.string.drawer_close /* "close drawer" description for accessibility */
		);
		drawerLayout.addDrawerListener(drawerToggle);
	}


	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		/* Sync the toggle state after onRestoreInstanceState has occurred. */
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		/* Pass any configuration change to the drawer toggles */
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (drawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data)
//	{
//		super.onActivityResult(requestCode, resultCode, data);
//
//		switch (requestCode)
//		{
//			case REQUEST_DATA_SOURCE:
//				if (resultCode == RESULT_OK && this instanceof MainActivity)
//					((MainActivity) this).onReset();
//
//				break;
//		}
//	}

	private boolean onNavigation(MenuItem item)
	{
		/* Depending on the Android version, handle things differently */
		switch (item.getItemId())
		{
			case R.id.drawer_menu_data_source:
				startActivityForResult(new Intent(getApplicationContext(), DatasourceActivity.class), REQUEST_DATA_SOURCE);
				break;
			case R.id.drawer_menu_visit_homepage:
//				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_homepage))));
				break;
			case R.id.drawer_menu_online_help:
//				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_online_help))));
				break;
			case R.id.drawer_menu_settings:
//				startActivityForResult(new Intent(getApplicationContext(), PreferencesActivity.class), REQUEST_PREFS);
				break;
			case R.id.drawer_menu_about:
//				startActivity(new Intent(getApplicationContext(), AboutActivity.class));
				break;
			default:
		}

		drawerLayout.closeDrawer(GravityCompat.START);
		return true;
	}
}
