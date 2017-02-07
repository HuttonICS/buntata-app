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
import android.provider.*;
import android.support.annotation.*;
import android.support.design.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;

import com.google.android.gms.analytics.*;

import java.util.*;
import java.util.concurrent.*;

import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * {@link BaseActivity} is the main {@link android.app.Activity} type of Buntàta. All activities should subclass this class or a child of this class.
 *
 * @author Sebastian Raubach
 */
public abstract class BaseActivity extends AppCompatActivity
{
	protected static Set<String> deniedPermissions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	/**
	 * The Google Analytics property id
	 */
	private static String PROPERTY_ID = null;
	protected Toolbar toolbar;

	public enum TrackerName
	{
		/* Tracker used only in this app */
		APP_TRACKER,
		/* Tracker used by all the apps from a company. eg: roll-up tracking */
		GLOBAL_TRACKER,
		/* Tracker used by all ecommerce transactions from a company */
		ECOMMERCE_TRACKER
	}

	/**
	 * The Trackers
	 */
	private static HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

	/**
	 * Returns the Tracker based on the tracker name
	 *
	 * @param trackerId The {@link uk.ac.hutton.ics.buntata.activity.BaseActivity.TrackerName}
	 * @return The Tracker
	 */
	public static synchronized Tracker getTracker(Context context, TrackerName trackerId)
	{
		if (!mTrackers.containsKey(trackerId))
		{
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker) : analytics.newTracker(PROPERTY_ID);
			mTrackers.put(trackerId, t);

		}
		return mTrackers.get(trackerId);
	}

	/**
	 * Returns the Tracker based on the tracker name
	 *
	 * @param trackerId The {@link uk.ac.hutton.ics.buntata.activity.BaseActivity.TrackerName}
	 * @return The Tracker
	 */
	public synchronized Tracker getTracker(TrackerName trackerId)
	{
		return getTracker(this, trackerId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		/* Don't forget to call the parent class */
		super.onCreate(savedInstanceState);

        /* Get the property id */
		PROPERTY_ID = getString(R.string.ga_tracking_id);

        /* Get the layout id from sub-class */
		Integer layoutId = getLayoutId();
		Integer toolbarId = getToolbarId();

		if (layoutId != null)
		{
			/* Set the content */
			setContentView(layoutId);
		}
		if (toolbarId != null)
		{
			toolbar = (Toolbar) findViewById(toolbarId);
			setSupportActionBar(toolbar);
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();

        /* Handle uncaught exceptions */
		if (!isDebugApplication())
		{
			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
			{
				@Override
				public void uncaughtException(Thread thread, Throwable ex)
				{
					ex.printStackTrace();

					GoogleAnalyticsUtils.trackEvent(BaseActivity.this, getTracker(TrackerName.APP_TRACKER), getString(R.string.ga_event_category_exception), ex.getLocalizedMessage());
					ToastUtils.createToast(BaseActivity.this, getString(R.string.toast_exception, ex.getLocalizedMessage()), ToastUtils.LENGTH_LONG);

					System.exit(1);
				}
			});

            /* Only log to Google Analytics, if this is the release version */
			GoogleAnalytics.getInstance(this).reportActivityStart(this);
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();

        /* Only log to Google Analytics, if this is the release version */
		if (!isDebugApplication())
		{
			GoogleAnalytics.getInstance(this).reportActivityStop(this);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	protected void showMissingPermissionsSnackbar(int message)
	{
		Snackbar snackbar = SnackbarUtils.create(getSnackbarParentView(), getString(message, getString(R.string.app_name)), Snackbar.LENGTH_LONG);
		snackbar.setAction(getString(R.string.generic_settings).toUpperCase(), new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", BaseActivity.this.getPackageName(), null);
				intent.setData(uri);
				BaseActivity.this.startActivity(intent);
			}
		});
		snackbar.show();
	}

	/**
	 * The {@link uk.ac.hutton.ics.buntata.activity.BaseActivity} will call {@link #setContentView(int)} with the result of this function.
	 * <p/>
	 * Child classes MUST NOT call {@link #setContentView(int)} themselves, but rather let {@link uk.ac.hutton.ics.buntata.activity.BaseActivity} take
	 * care of it.
	 *
	 * @return The layout id of the child class. <code>null</code> can be returned to indicate that the child doesn't use a layout.
	 */
	protected abstract Integer getLayoutId();

	/**
	 * The {@link uk.ac.hutton.ics.buntata.activity.BaseActivity} will call {@link #setSupportActionBar(Toolbar)} with the {@link Toolbar} associated
	 * with the returned id.
	 * <p/>
	 * If <code>null</code> is returned, not support action bar will be set up.
	 *
	 * @return The id of the {@link Toolbar} to be used as the support action bar
	 */
	protected abstract Integer getToolbarId();

	protected View getSnackbarParentView()
	{
		return findViewById(android.R.id.content);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	protected boolean isDebugApplication()
	{
		String packageName = this.getPackageName();
		return packageName != null && packageName.endsWith(".debug");
	}
}
