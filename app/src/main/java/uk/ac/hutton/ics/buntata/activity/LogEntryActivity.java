/*
 * Copyright 2017 Information & Computational Sciences, The James Hutton Institute
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
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.io.*;
import java.util.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.adapter.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.thread.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link LogEntryActivity} just shows the Apache License, Version 2.0 for compliance reasons. That's it. Nothing to see here.
 *
 * @author Sebastian Raubach
 */
public class LogEntryActivity extends BaseActivity implements OnMapReadyCallback
{
	private static final int REQUEST_CODE_DETAILS                      = 1000;
	private static final int REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS = 1001;
	private static final int REQUEST_CODE_SHARE                        = 1002;

	@BindView(R.id.logentries_recycler_view)
	EmptyRecyclerView recyclerView;
	@BindView(R.id.list_empty)
	RelativeLayout    emptyList;

	private GoogleMap            mapView;
	private List<LogEntry>       dataset;
	private LogEntryManager      logManager;
	private LogEntryImageManager imageManager;
	private LogEntryAdapter      adapter;
	private List<File> createdFiles = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setTitle(R.string.title_activity_log_entries);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

		imageManager = new LogEntryImageManager(this);
		logManager = new LogEntryManager(this);
		dataset = logManager.getAll();

		/* Set the data to the adapter */
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		int horizontalMargin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
		int verticalMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);

		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;
		recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, horizontalMargin, verticalMargin, valueInPixels));
		recyclerView.setEmptyView(emptyList);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.logentries_map_view);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		mapView = googleMap;

		update(null);
	}

	public void start(LogEntry entry)
	{
		Intent intent = new Intent(getApplicationContext(), LogDetailsActivity.class);

		/* Pass parameters */
		Bundle args = new Bundle();
		args.putInt(LogDetailsActivity.PARAM_DATASOURCE_ID, entry.getDatasourceId());
		args.putInt(LogDetailsActivity.PARAM_NODE_ID, entry.getNodeId());
		args.putInt(LogDetailsActivity.PARAM_LOG_ID, entry.getId());
		intent.putExtras(args);

		startActivityForResult(intent, REQUEST_CODE_DETAILS);
	}

	public void update(LogEntry toRemove)
	{
		if (toRemove != null)
		{
			dataset.remove(toRemove);
			imageManager.delete(toRemove);
			logManager.delete(toRemove);
			adapter.notifyItemRemoved(dataset.indexOf(toRemove));
		}
		else
		{
			dataset = logManager.getAll();
			adapter = new LogEntryAdapter(this, dataset);
			recyclerView.setAdapter(adapter);
		}

		Collections.sort(dataset, new Comparator<LogEntry>()
		{
			@Override
			public int compare(LogEntry first, LogEntry second)
			{
				return -first.getCreatedOn().compareTo(second.getCreatedOn());
			}
		});

		mapView.clear();

		/* Set up a builder to keep track of the bounds */
		final LatLngBounds.Builder builder = new LatLngBounds.Builder();
		Bitmap markerImage = bitmapDescriptorFromVector(R.drawable.marker);
		int padding = markerImage.getHeight();
		BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(markerImage);

		mapView.setPadding(0, padding, 0, 0);

		int counter = 0;
		Marker marker = null;
		for (LogEntry entry : dataset)
		{
			if (entry.getLatitute() != null && entry.getLongitude() != null)
			{
				LatLng position = new LatLng(entry.getLatitute(), entry.getLongitude());

				marker = mapView.addMarker(new MarkerOptions()
						.position(position)
						.icon(markerIcon)
						.title(entry.getNodeName())
						.snippet(entry.getNote()));

				marker.setTag(entry);

				builder.include(position);
				counter++;
			}
		}

		if (counter > 0)
		{
			CameraUpdate cu;

			/* If there's just one marker, make sure to use this method as it won't zoom in all the way */
			if (counter == 1)
			{
				cu = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 10f);
			}
			/* If there's more than one marker, then let the map do the calculation */
			else
			{
				LatLngBounds bounds = builder.build();
				cu = CameraUpdateFactory.newLatLngBounds(bounds, 25 + padding);
			}

			/* Zoom in, animating the camera */
			mapView.animateCamera(cu);
		}

		invalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		/* Inflate the menu */
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.log_menu, menu);

		MenuItem item = menu.findItem(R.id.action_export);

		if (item != null)
			item.setVisible(dataset.size() > 0);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE_DETAILS)
		{
			update(null);
		}
		else if (requestCode == REQUEST_CODE_SHARE)
		{
			for (File file : createdFiles)
				file.delete();

			createdFiles.clear();
		}
		else
		{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private Bitmap bitmapDescriptorFromVector(int vectorResId)
	{
		Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
		vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
		Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		vectorDrawable.draw(canvas);
		return bitmap;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.action_export:
				exportPrePermission();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void exportPrePermission()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
					/* Request the permission */
			if (!deniedPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS);

			return;
		}

		export();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			case REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS:
			{
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					/* Permission granted */
					export();
				}
				else
				{
					int foreground = ContextCompat.getColor(this, android.R.color.white);
					int background = ContextCompat.getColor(this, R.color.snackbar_red);
					SnackbarUtils.show(getSnackbarParentView(), getString(R.string.snackbar_permission_missing_external_storage), foreground, background, Snackbar.LENGTH_LONG);
				}

				break;
			}

			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private void export()
	{
		new ZipExportTask(this, dataset)
		{
			@Override
			protected void processData(File result)
			{
				share(result);
			}
		}.execute();
	}

	private void share(File zip)
	{
		createdFiles.add(zip);

		/* Ask Android to share it for us */
		ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this)
																	 .setSubject(getString(R.string.app_name));
		/* Use the provider to make the file available to the other app (Android requirement) */
		String providerName = getPackageName() + ".fileprovider";
		Uri uri = FileProvider.getUriForFile(this, providerName, zip);

		builder.setStream(uri)
			   .setType("text/plain");

		try
		{
			startActivityForResult(builder.getIntent(), REQUEST_CODE_SHARE);
		}
		catch (ActivityNotFoundException e)
		{
			/* No app can handle the request, there's nothing we can do now... */
			SnackbarUtils.create(getSnackbarParentView(), R.string.snackbar_no_app_found, ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, R.color.snackbar_red), Snackbar.LENGTH_LONG)
						 .show();
		}
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_logentries;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}
}
