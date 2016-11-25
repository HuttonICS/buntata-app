/**
 * Germinate Scan is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group at
 * JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p>
 * Copyright (c) 2013-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.buntata.activity;

import android.content.pm.*;
import android.os.*;
import android.support.v4.content.*;
import android.view.*;

import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * @author Sebastian Raubach
 */
public class ChangelogActivity extends BaseActivity
{
	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_changelog;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			{
				getSupportActionBar().setHomeButtonEnabled(true);
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
		}

		// Get the versionCode of the Package, which must be different (incremented) in each release on the market in the AndroidManifest.xml
		final PackageInfo packageInfo;
		try
		{
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
			PreferenceUtils.setPreferenceAsInt(ChangelogActivity.this, PreferenceUtils.PREFS_LAST_VERSION, packageInfo.versionCode);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
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
}
