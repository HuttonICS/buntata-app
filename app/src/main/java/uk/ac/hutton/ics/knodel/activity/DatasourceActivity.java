package uk.ac.hutton.ics.knodel.activity;

import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.content.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * @author Sebastian Raubach
 */
public class DatasourceActivity extends BaseActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.datasource_fragment);
		fragment.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

		if (!PreferenceUtils.getPreferenceAsBoolean(this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false))
		{
			SnackbarUtils.show(findViewById(android.R.id.content), R.string.snackbar_select_datasource, Snackbar.LENGTH_LONG);
		}
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_datasource;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	public void onBackPressed()
	{
		int newDatasourceId = PreferenceUtils.getPreferenceAsInt(this, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, -1);

		if (newDatasourceId != -1)
		{
//			if(previousDatasourceId != newDatasourceId)
				setResult(RESULT_OK);
//			else
//				setResult(RESULT_CANCELED);
		}

		super.onBackPressed();
	}
}
