package uk.ac.hutton.ics.knodel.activity;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.*;
import android.view.*;

import org.restlet.engine.*;
import org.restlet.ext.jackson.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.database.manager.*;
import uk.ac.hutton.ics.knodel.fragment.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * @author Sebastian Raubach
 */
public class MainActivity extends DrawerActivity implements OnFragmentChangeListener
{
	public static final int REQUEST_CODE_INTRO             = 1;
	public static final int REQUEST_CODE_SELECT_DATASOURCE = 2;
	public static final int REQUEST_CODE_DETAILS           = 3;

	static
	{
		/* Initialize this statically, so it's only done once */
		Engine.getInstance().getRegisteredConverters().add(new JacksonConverter());
	}

	private int datasourceId = -1;

	private boolean addNewFragment = true;
	private boolean override       = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		/* Make sure the default preferences are set */
		PreferenceUtils.setDefaults(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		init();
	}

	private void init()
	{
		boolean showIntro = !PreferenceUtils.getPreferenceAsBoolean(this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false);

		if (showIntro)
		{
			startActivityForResult(new Intent(getApplicationContext(), IntroductionActivity.class), REQUEST_CODE_INTRO);
		}
		else
		{
			int datasourceId = PreferenceUtils.getPreferenceAsInt(this, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, -1);

			if (datasourceId == -1)
				startActivityForResult(new Intent(getApplicationContext(), DatasourceActivity.class), REQUEST_CODE_SELECT_DATASOURCE);
			else
			{
				if (override)
				{
					Intent intent = getIntent();
					finish();
					startActivity(intent);
				}
				else if (datasourceId != this.datasourceId)
					updateContent(null, datasourceId, -1);
			}

			this.datasourceId = datasourceId;
			this.override = false;
		}
	}

	private void updateContent(View transitionRoot, int datasourceId, int parentId)
	{
		if (!addNewFragment)
		{
			addNewFragment = true;
			return;
		}

		boolean hasChildren = parentId == -1 || new NodeManager(this, datasourceId).hasChildren(parentId);

		if (hasChildren)
		{
			Fragment fragment = new NodeFragment();

			Bundle args = new Bundle();
			args.putInt(NodeFragment.PARAM_PARENT_ID, parentId);
			args.putInt(NodeFragment.PARAM_DATASOURCE_ID, datasourceId);
			fragment.setArguments(args);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
			ft.addToBackStack(fragment.toString());
			ft.replace(R.id.fragment_holder, fragment, fragment.toString()).commit();
			// TODO: animation
		}
		else
		{
			Intent intent = new Intent(getApplicationContext(), NodeDetailsActivity.class);
			Bundle args = new Bundle();
			args.putInt(NodeDetailsActivity.PARAM_NODE_ID, parentId);
			args.putInt(NodeDetailsActivity.PARAM_DATASOURCE_ID, datasourceId);
			intent.putExtras(args);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			{
				ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
						Pair.create(transitionRoot, getString(R.string.transition_node_view)),
						Pair.create(transitionRoot, getString(R.string.transition_node_details_view)));

				startActivityForResult(intent, REQUEST_CODE_DETAILS, options.toBundle());
			}
			else
			{
				startActivityForResult(intent, REQUEST_CODE_DETAILS);
				overridePendingTransition(R.anim.enter, R.anim.exit);
			}
		}
	}

	@Override
	public void onBackPressed()
	{
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
					PreferenceUtils.setPreferenceAsBoolean(this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, true);
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
					override = true;
				}
				break;
			case REQUEST_CODE_DETAILS:
				/* We're coming back from the details view, so don't add anything */
				addNewFragment = false;
				break;
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
	public void onFragmentChange(View transitionRoot, int datasourceId, int parentId)
	{
		updateContent(transitionRoot, datasourceId, parentId);
	}

	@Override
	public void onReset()
	{
		/* Clear all fragments from the stack */
		override = true;
		getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}
}
