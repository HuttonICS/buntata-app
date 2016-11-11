package uk.ac.hutton.ics.buntata.activity;

import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.content.*;
import android.view.*;
import android.widget.*;

import com.google.android.gms.analytics.*;

import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.util.*;

public class PreferencesActivity extends BaseActivity
{
	private PrefsFragment prefsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		prefsFragment = new PrefsFragment();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
		}

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(R.string.title_activity_datasource);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			{
				getSupportActionBar().setHomeButtonEnabled(true);
			}
		}

        /* Show the fragment */
		getFragmentManager().beginTransaction()
							.replace(R.id.preferences_layout, prefsFragment)
							.commit();
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
		return R.layout.activity_preferences;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		prefsFragment.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View v = super.onCreateView(inflater, container, savedInstanceState);
			if (v != null)
			{
				ListView lv = (ListView) v.findViewById(android.R.id.list);
				lv.setPadding(0, 0, 0, 0);
			}
			return v;
		}

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

            /* Load the preferences from an XML resource */
			addPreferencesFromResource(R.xml.prefs);
		}

		@Override
		public void onResume()
		{
			super.onResume();
			/* Listen to changes to the preferences */
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause()
		{
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences pref, String key)
		{
			/* If the theme was changed */
			switch (key)
			{
				case PreferenceUtils.PREFS_GA_OPT_OUT:
					/* Disable GA tracking */
					GoogleAnalytics.getInstance(getActivity().getApplicationContext()).setAppOptOut(!PreferenceUtils.getPreferenceAsBoolean(getActivity(), PreferenceUtils.PREFS_GA_OPT_OUT, true));
					break;
			}
		}
	}
}