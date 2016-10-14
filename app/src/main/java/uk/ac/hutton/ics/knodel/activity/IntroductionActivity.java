package uk.ac.hutton.ics.knodel.activity;

import android.*;
import android.app.*;
import android.content.pm.*;
import android.content.res.*;
import android.os.*;

import com.heinrichreimersoftware.materialintro.app.*;
import com.heinrichreimersoftware.materialintro.slide.*;

import uk.ac.hutton.ics.knodel.R;
import uk.ac.hutton.ics.knodel.fragment.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
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

		/**
		 * Standard slide (like Google's intros)
		 */
		addSlide(new SimpleSlide.Builder()
				.title("Welcome to Knödel")
				.description("Lorem Ipsum and so on...")
				.image(R.drawable.ic_logo)
				.background(R.color.colorPrimary)
				.backgroundDark(R.color.colorPrimaryDark)
				.permission(Manifest.permission.INTERNET)
				.build());

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
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}