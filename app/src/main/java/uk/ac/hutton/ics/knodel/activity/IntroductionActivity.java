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

import android.*;
import android.content.pm.*;
import android.content.res.*;
import android.os.*;

import com.heinrichreimersoftware.materialintro.app.*;
import com.heinrichreimersoftware.materialintro.slide.*;

import uk.ac.hutton.ics.knodel.R;
import uk.ac.hutton.ics.knodel.fragment.*;
import uk.ac.hutton.ics.knodel.util.*;

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

		setButtonBackVisible(false);

		setNavigationPolicy(new NavigationPolicy()
		{
			@Override
			public boolean canGoForward(int position)
			{
				return position != getCount() - 1 || PreferenceUtils.getPreferenceAsBoolean(IntroductionActivity.this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false);
			}

			@Override
			public boolean canGoBackward(int position)
			{
				return true;
			}
		});

		/* Welcome slide */
		addSlide(new SimpleSlide.Builder()
				.title("Welcome to Knödel") // TODO: i18n
				.description("Lorem Ipsum and so on...") // TODO: i18n
				.image(R.drawable.ic_logo)
				.background(R.color.colorPrimary)
				.backgroundDark(R.color.colorPrimaryDark)
				.permission(Manifest.permission.INTERNET)
				.build());

		/* Data source selection slide */
		addSlide(new FragmentSlide.Builder()
				.background(R.color.colorPrimary)
				.backgroundDark(R.color.colorPrimaryDark)
				.fragment(new DatasourceFragment())
				.build());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		/* Force portrait */
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}