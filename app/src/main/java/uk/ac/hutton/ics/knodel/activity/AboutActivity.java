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

import android.content.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v7.widget.*;
import android.view.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.fragment.*;

/**
 * The {@link AboutActivity} shows the about {@link Fragment}s: {@link AboutInformationFragment}, {@link AboutDeveloperFragment} and {@link
 * AboutLicenseFragment}.
 *
 * @author Sebastian Raubach
 */
public class AboutActivity extends BaseActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(" ");
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			{
				getSupportActionBar().setHomeButtonEnabled(true);
			}
		}

		/* Get the view pager and set the fragment adapter */
		ViewPager viewPager = (ViewPager) findViewById(R.id.about_viewpager);
		TabLayout tabs = (TabLayout) findViewById(R.id.about_tabs);
		viewPager.setAdapter(new AboutFragmentPagerAdapter(getSupportFragmentManager(), this));
		tabs.setupWithViewPager(viewPager);

		/* Get the CollapsingToolbarLayout and listen for offset change events to show/hide the toolbar title, i.e. it'll only be shown when the toolbar is fully collapsed */
		final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.about_collapsingtoolbarlayout);
		AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.about_appbarlayout);
		appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener()
		{
			boolean isShow = false;
			int scrollRange = -1;

			@Override
			public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
			{
				if (scrollRange == -1)
				{
					scrollRange = appBarLayout.getTotalScrollRange();
				}
				if (scrollRange + verticalOffset == 0)
				{
					collapsingToolbarLayout.setTitle(getString(R.string.title_activity_about));
					isShow = true;
				}
				else if (isShow)
				{
					collapsingToolbarLayout.setTitle(" "); // careful there should a space between double quote otherwise it wont work
					isShow = false;
				}
			}
		});
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_about;
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
	 * The {@link AboutFragmentPagerAdapter} takes care of the actual {@link Fragment}s in the {@link ViewPager}.
	 */
	public class AboutFragmentPagerAdapter extends FragmentPagerAdapter
	{
		private String[] titles;

		AboutFragmentPagerAdapter(FragmentManager fm, Context context)
		{
			super(fm);

			this.titles = new String[]{context.getString(R.string.about_tab_information), context.getString(R.string.about_tab_developers), context.getString(R.string.about_tab_acknowledgements), context.getString(R.string.about_tab_license)};
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return titles[position];
		}

		@Override
		public int getCount()
		{
			return 4;
		}

		@Override
		public Fragment getItem(int position)
		{
			switch (position)
			{
				case 0:
					return new AboutInformationFragment();
				case 1:
					return new AboutDeveloperFragment();
				case 2:
					return new AboutAcknowledgementsFragment();
				case 3:
					return new AboutLicenseFragment();
			}

			return null;
		}
	}
}
