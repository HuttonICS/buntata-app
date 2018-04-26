/*
 * Copyright 2018 Information & Computational Sciences, The James Hutton Institute
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
import android.content.*;
import android.content.pm.*;
import android.location.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.widget.Toolbar;
import android.text.*;
import android.view.*;
import android.widget.*;

import com.esafirm.imagepicker.features.*;
import com.esafirm.imagepicker.model.*;

import java.io.*;
import java.text.*;
import java.util.*;

import butterknife.*;
import me.relex.circleindicator.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.adapter.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link LogDetailsActivity} shows detailed information about the node. This contains images, attributes and videos.
 *
 * @author Sebastian Raubach
 */
public class LogDetailsActivity extends BaseActivity implements LocationUtils.LocationChangeListener
{
	private static final int REQUEST_CODE_LOCATION_PERMISSIONS = 32141;
	private static final int REQUEST_CODE_PICKER               = 31245;

	public static final  String        PARAM_LOG_ID        = "logId";
	public static final  String        PARAM_NODE_ID       = "nodeId";
	public static final  String        PARAM_DATASOURCE_ID = "datasourceId";
	private static final DecimalFormat DECIMAL_FORMAT      = new DecimalFormat("0.00");

	@BindView(R.id.node_details_image_pager)
	ViewPager               pager;
	@BindView(R.id.node_details_image_indicator)
	CircleIndicator         circleIndicator;
	@BindView(R.id.log_details_app_bar)
	AppBarLayout            appBarLayout;
	@BindView(R.id.log_details_collapsing)
	CollapsingToolbarLayout collapsingToolbarLayout;
	@BindView(R.id.toolbar)
	Toolbar                 toolbar;
	@BindView(R.id.log_details_nested_scroll)
	NestedScrollView        scrollView;

	@BindView(R.id.log_details_layout)
	LinearLayout      layout;
	@BindView(R.id.log_details_datasource)
	TextInputEditText datasource;
	@BindView(R.id.log_details_node_name)
	TextInputEditText nodeName;
	@BindView(R.id.log_details_latitude)
	TextInputEditText latitude;
	@BindView(R.id.log_details_longitude)
	TextInputEditText longitude;
	@BindView(R.id.log_details_gps_button)
	ImageButton       gpsButton;
	@BindView(R.id.log_details_note)
	TextInputEditText note;

	private int datasourceId = -1;
	private int nodeId       = -1;
	private int logId        = -1;
	private Snackbar snackbar;
	private LogEntry log;
	private boolean unsavedChanges = false;
	private LogEntryImageManager imageManager;
	private List<LogEntryImage> newlyCreatedImages = new ArrayList<>();
	private List<LogEntryImage>  images;
	private LogEntryManager      logManager;
	private LogImagePagerAdapter imagePagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		Bundle args = getIntent().getExtras();

		if (args != null)
		{
			datasourceId = args.getInt(PARAM_DATASOURCE_ID, -1);
			nodeId = args.getInt(PARAM_NODE_ID, -1);
			logId = args.getInt(PARAM_LOG_ID, -1);
		}

		setSupportActionBar(toolbar);

		DatasourceManager datasourceManager = new DatasourceManager(this, datasourceId);
		NodeManager nodeManager = new NodeManager(this, datasourceId);
		imageManager = new LogEntryImageManager(this);

		logManager = new LogEntryManager(this);
		log = logManager.getById(logId);

		if (log == null)
		{
			BuntataNodeAdvanced node = nodeManager.getById(nodeId);
			log = new LogEntry();
			log.setDatasourceId(datasourceId);
			log.setNodeId(node.getId());
			log.setNodeName(node.getName());

			/* If this is a new entry, then definitely ask to save */
			unsavedChanges = true;
		}

		datasource.setText(datasourceManager.getById(log.getDatasourceId()).getName());
		nodeName.setText(nodeManager.getById(log.getNodeId()).getName());
		latitude.setFilters(new InputFilter[]{new InputFilterMinMax(-90, 90)});
		longitude.setFilters(new InputFilter[]{new InputFilterMinMax(-180, 180)});
		note.setText(log.getNote());
		if (log.getLatitute() != null)
			latitude.setText(DECIMAL_FORMAT.format(log.getLatitute()));
		if (log.getLongitude() != null)
			longitude.setText(DECIMAL_FORMAT.format(log.getLongitude()));

		datasource.setInputType(InputType.TYPE_NULL);
		nodeName.setInputType(InputType.TYPE_NULL);

		gpsButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startLocationTracking();
			}
		});

		latitude.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean b)
			{
				if (!b)
				{
					try
					{
						double newValue = Double.parseDouble(latitude.getText().toString());

						if (log.getLatitute() == null || log.getLatitute() != newValue)
						{
							log.setLatitute(newValue);
							unsavedChanges = true;
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		});
		longitude.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean b)
			{
				if (!b)
				{
					try
					{
						double newValue = Double.parseDouble(longitude.getText().toString());

						if (log.getLongitude() == null || log.getLongitude() != newValue)
						{
							log.setLongitude(newValue);
							unsavedChanges = true;
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		});

		note.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean b)
			{
				if (!b)
				{
					String newText = note.getText().toString();

					if (!StringUtils.areEqual(newText, log.getNote()))
					{
						log.setNote(newText);
						unsavedChanges = true;
					}
				}
			}
		});

		GoogleAnalyticsUtils.trackEvent(this, getTracker(TrackerName.APP_TRACKER), getString(R.string.ga_event_category_log), getString(R.string.ga_event_action_node_view), log.getNodeName());

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(log == null ? getString(R.string.log_add_title) : log.getNodeName());
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		updateImageSection();
	}

	public void updateImageSection()
	{
		LogEntryImageManager imageManager = new LogEntryImageManager(this);

		/* Get all the images */
		images = imageManager.getForLogEntry(log.getId());

		if (images.size() > 0)
		{
			/* Set to the pager */
			imagePagerAdapter = new LogImagePagerAdapter(getSupportFragmentManager(), images);
			pager.setAdapter(imagePagerAdapter);
			pager.setVisibility(View.VISIBLE);
			circleIndicator.setViewPager(pager);
			circleIndicator.setVisibility(images.size() > 1 ? View.VISIBLE : View.GONE);

			float heightDp = getResources().getDisplayMetrics().heightPixels / 2f;
			CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
			lp.height = (int) heightDp;
			scrollView.setNestedScrollingEnabled(true);
			appBarLayout.setExpanded(true);
			appBarLayout.setFitsSystemWindows(true);
		}
		else
		{
			/* Hide the views */
			pager.setVisibility(View.GONE);
			circleIndicator.setVisibility(View.GONE);

			/* Collapse the nested view as best as possible */
			scrollView.setNestedScrollingEnabled(false);
			appBarLayout.setExpanded(false);
			appBarLayout.setFitsSystemWindows(false);
		}
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_log_details;
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
			case R.id.action_add_image:
				/* We don't want to show images that have already been selected, so exclude those */
				ArrayList<File> toExclude = new ArrayList<>();

				for (LogEntryImage image : images)
					toExclude.add(new File(image.getPath()));
				for (LogEntryImage image : newlyCreatedImages)
					toExclude.add(new File(image.getPath()));

				ImagePicker.create(this)
						   .folderMode(true)
						   .returnMode(ReturnMode.CAMERA_ONLY)
						   .excludeFiles(toExclude)
						   .theme(R.style.ImagePickerTheme)
						   .start(REQUEST_CODE_PICKER);
				return true;
			case R.id.action_share:
				String text = log.getNote() + " " + getString(R.string.social_media_postfix);
				ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this)
																			 .setText(text)
																			 .setSubject(log.getNodeName());

				if (images.size() > 0)
				{
					/* Use the provider to make the file available to the other app (Android requirement) */
					String providerName = getPackageName() + ".fileprovider";
					Uri uri = FileProvider.getUriForFile(this, providerName, new File(images.get(0).getPath()));

					builder.setStream(uri)
						   .setType("image/jpeg");
				}
				else
				{
					/* If there are no images, set the type to be plain text */
					builder.setType("text/plain");
				}

				try
				{
					startActivity(builder.getIntent());
				}
				catch (ActivityNotFoundException e)
				{
					/* No app can handle the request, there's nothing we can do now... */
					SnackbarUtils.create(getSnackbarParentView(), R.string.snackbar_no_app_found, ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, R.color.snackbar_red), Snackbar.LENGTH_LONG)
								 .show();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		/* Inflate the menu */
		getMenuInflater().inflate(R.menu.log_details_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed()
	{
		latitude.clearFocus();
		longitude.clearFocus();
		note.clearFocus();

		if (unsavedChanges)
		{
			DialogUtils.showDialog(this, R.string.dialog_save_title, R.string.dialog_save_message, R.string.generic_yes, R.string.generic_no, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialogInterface, int i)
				{
					/* It's a new item */
					if (log.getId() == -1)
						logManager.add(log);
					/* It's an existing item */
					else
						logManager.update(log);

					/* Update the images, i.e. set the log entry id */
					for (LogEntryImage image : newlyCreatedImages)
					{
						image.setLogEntry(log);
						imageManager.update(image);
					}

					close();
				}
			}, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialogInterface, int i)
				{
					/* Delete them again */
					for (LogEntryImage image : newlyCreatedImages)
						imageManager.delete(image);

					close();
				}
			});
		}
		else
		{
			close();
		}

		if (imagePagerAdapter != null)
			imagePagerAdapter.cleanup();
	}

	private void close()
	{
		if (pager.getVisibility() == View.VISIBLE)
		{
			if (pager.getCurrentItem() < 2)
				pager.setCurrentItem(0, true);
		}

		LogDetailsActivity.this.finish();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			case REQUEST_CODE_LOCATION_PERMISSIONS:
				for (String permission : permissions)
				{
					if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
						deniedPermissions.add(permission);
				}

				if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
				{
					/* Permission Granted */
					LocationUtils.load(this);
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}

		if (snackbar != null)
		{
			snackbar.dismiss();
			snackbar = null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE_PICKER && resultCode == RESULT_OK && data != null)
		{
			List<Image> newImages = ImagePicker.getImages(data);

			if (newImages != null)
			{
				outer:
				for (Image image : newImages)
				{
					/* Check all the existing images to make sure we don't create duplicates */
					for (LogEntryImage oldImage : images)
					{
						if (image.getPath().equals(oldImage.getPath()))
							continue outer;
					}

					/* Create a new image object and save it to the database */
					LogEntryImage i = new LogEntryImage()
							.setLogEntry(log)
							.setPath(image.getPath());

					newlyCreatedImages.add(i);
					unsavedChanges = true;
					imageManager.add(i);
				}

				updateImageSection();
			}
		}
		else
		{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void startLocationTracking()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			/* Request the permission */
			if (!deniedPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) || !deniedPermissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
			{
				snackbar = SnackbarUtils.create(getSnackbarParentView(), R.string.snackbar_permission_missing_gps, Snackbar.LENGTH_INDEFINITE);
				snackbar.show();
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSIONS);
			}

			return;
		}

		/* Make sure the location information is available */
		LocationUtils.load(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		/* Release resources */
		LocationUtils.unload(this);
	}

	@Override
	public void onLocationChanged(Location location)
	{
		log.setLatitute(location.getLatitude());
		log.setLongitude(location.getLongitude());
		log.setElevation(location.getAltitude());
		unsavedChanges = true;

		this.latitude.setText(DECIMAL_FORMAT.format(location.getLatitude()));
		this.longitude.setText(DECIMAL_FORMAT.format(location.getLongitude()));

		LocationUtils.unload(this);
	}
}
