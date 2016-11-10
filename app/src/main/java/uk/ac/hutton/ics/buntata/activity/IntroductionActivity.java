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

import android.*;
import android.os.*;

import com.heinrichreimersoftware.materialintro.app.*;
import com.heinrichreimersoftware.materialintro.slide.*;

import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.fragment.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link IntroductionActivity} is shown on first start. It guides the user though the initial data source selection and will show the EULA.
 *
 * @author Sebastian Raubach
 */
public class IntroductionActivity extends IntroActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		/* Set some preferences that are used for the navigation here initially to false */
		PreferenceUtils.setPreferenceAsBoolean(this, PreferenceUtils.PREFS_EULA_ACCEPTED, false);
		PreferenceUtils.setPreferenceAsBoolean(this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false);

		setButtonBackVisible(false);

		/* Welcome slide */
		addSlide(new SimpleSlide.Builder()
				.title(R.string.introduction_welcome_title)
				.description(R.string.introduction_welcome_text)
				.image(R.drawable.ic_launcher_2x)
				.background(R.color.colorPrimary)
				.backgroundDark(R.color.colorPrimaryDark)
				.permission(Manifest.permission.INTERNET)
				.build());

		addSlide(new FragmentSlide.Builder()
				.background(R.color.colorPrimary)
				.backgroundDark(R.color.colorPrimaryDark)
				.fragment(new EulaFragment())
				.build());

		addSlide(new FragmentSlide.Builder()
				.background(R.color.colorPrimary)
				.backgroundDark(R.color.colorPrimaryDark)
				.fragment(new IntroNetworkFragment())
				.build());

		/* Data source selection slide */
		addSlide(new FragmentSlide.Builder()
				.background(R.color.colorPrimary)
				.backgroundDark(R.color.colorPrimaryDark)
				.fragment(new DatasourceFragment())
				.build());

		setNavigationPolicy(new NavigationPolicy()
		{
			@Override
			public boolean canGoForward(int position)
			{
				if (position == getCount() - 1)
				{
					return PreferenceUtils.getPreferenceAsBoolean(IntroductionActivity.this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false);
				}
				else if (position == 1)
				{
					return PreferenceUtils.getPreferenceAsBoolean(IntroductionActivity.this, PreferenceUtils.PREFS_EULA_ACCEPTED, false);
				}
				else if (position == 2)
				{
					return NetworkUtils.hasNetworkConnection(IntroductionActivity.this);
				}
				else
				{
					return true;
				}
			}

			@Override
			public boolean canGoBackward(int position)
			{
				if (position == getCount() - 1)
				{
					return false;
//					return PreferenceUtils.getPreferenceAsBoolean(IntroductionActivity.this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false);
				}
				else
				{
					return true;
				}
			}
		});
	}


//	@Override
//	public void onConfigurationChanged(Configuration newConfig)
//	{
//		super.onConfigurationChanged(newConfig);
//
//		/* Force portrait */
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//	}
}