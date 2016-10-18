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

package uk.ac.hutton.ics.knodel.adapter;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.support.design.widget.*;
import android.support.v4.content.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.io.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.activity.*;
import uk.ac.hutton.ics.knodel.database.manager.*;
import uk.ac.hutton.ics.knodel.service.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * The {@link DatasourceAdapter} handles the {@link KnodelDatasource}s.
 *
 * @author Sebastian Raubach
 */
public class DatasourceAdapter extends RecyclerView.Adapter<DatasourceAdapter.ViewHolder>
{
	private Activity                                              context;
	private Map<KnodelDatasource, DatasourceAdapter.InstallState> mapping;
	private List<KnodelDatasource>                                dataset;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View        view;
		TextView    nameView;
		TextView    descriptionView;
		TextView    sizeView;
		ImageView   imageView;
		ProgressBar progressBar;
		ImageView   downloadStatus;

		/* Remember the state */
		InstallState state         = null;
		boolean      isDownloading = false;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			nameView = (TextView) v.findViewById(R.id.datasource_name_view);
			descriptionView = (TextView) v.findViewById(R.id.datasource_description_view);
			sizeView = (TextView) v.findViewById(R.id.datasource_size_view);
			imageView = (ImageView) v.findViewById(R.id.datasource_image_view);
			progressBar = (ProgressBar) v.findViewById(R.id.datasource_download_progress);
			downloadStatus = (ImageView) v.findViewById(R.id.datasource_download_indicator);
		}
	}

	public DatasourceAdapter(Activity context, Map<KnodelDatasource, DatasourceAdapter.InstallState> mapping)
	{
		this.context = context;
		this.mapping = mapping;
		this.dataset = new ArrayList<>(mapping.keySet());
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.datasource_view, parent, false);

		final ViewHolder viewHolder = new ViewHolder(v);

		return viewHolder;
	}

	private void animate(final ViewHolder viewHolder)
	{
		final int color = ContextCompat.getColor(context, R.color.colorAccent);

		final ValueAnimator colorAnim = ObjectAnimator.ofFloat(0f, 1f);
		colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				float mul = (Float) animation.getAnimatedValue();
				int alphaOrange = adjustAlpha(color, mul);
				viewHolder.downloadStatus.setColorFilter(alphaOrange, PorterDuff.Mode.SRC_ATOP);
				if (mul == 0.0)
				{
					viewHolder.downloadStatus.setColorFilter(null);
				}
			}
		});

		colorAnim.setDuration(500);
		colorAnim.start();
	}

	private int adjustAlpha(int color, float factor)
	{
		int alpha = Math.round(Color.alpha(color) * factor);
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		return Color.argb(alpha, red, green, blue);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		KnodelDatasource item = dataset.get(position);
		holder.nameView.setText(item.getName());
		holder.descriptionView.setText(item.getDescription());
		holder.sizeView.setText(context.getString(R.string.datasource_size, (item.getSize() / 1024 / 1024)));

		if (holder.state == null)
		{
			holder.state = mapping.get(item);
		}
		/* Set the state icon */
		int resource;
		switch (holder.state)
		{
			case INSTALLED_NO_UPDATE:
				resource = R.drawable.action_ok;
				break;

			case INSTALLED_HAS_UPDATE:
				resource = R.drawable.action_update;
				break;

			case NOT_INSTALLED:
			default:
				resource = R.drawable.action_download;
				break;
		}

		holder.downloadStatus.setImageResource(resource);
		holder.downloadStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));

		animate(holder);

		/* If there is an icon, set it */
		if (!StringUtils.isEmpty(item.getIcon()))
		{
			holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			Picasso.with(context)
				   .load(item.getIcon())
				   .noPlaceholder()
				   .into(holder.imageView);
		}
		/* Else set a default icon */
		else
		{
			holder.imageView.setImageResource(R.drawable.drawer_data_source);
			holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			holder.imageView.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
		}

		/* Add a long click handler */
		holder.view.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				final KnodelDatasource ds = dataset.get(holder.getAdapterPosition());

				/* If it's not installed or if it's currently downloading, do nothing */
				if ((holder.state == InstallState.NOT_INSTALLED) || holder.isDownloading)
					return true;

				/* Show the option do delete the data source */
				DialogUtils.showDialog(context, R.string.dialog_delete_datasource_title, R.string.dialog_delete_datasource_text, R.string.generic_yes, R.string.generic_no, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						/* Reset the currently selected data source, if this was the selected item */
						int selected = PreferenceUtils.getPreferenceAsInt(context, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, -1);
						if (selected == ds.getId())
							PreferenceUtils.removePreference(context, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID);

						/* Remember that this isn't downloaded anymore */
						holder.state = InstallState.NOT_INSTALLED;

						try
						{
							/* Delete associated files */
							new DatasourceManager(context, ds.getId()).remove();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}

						notifyDataSetChanged();
					}
				}, null);
				return true;
			}
		});

		/* Add a click handler */
		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final KnodelDatasource ds = dataset.get(holder.getAdapterPosition());

				if (holder.isDownloading)
				{
					SnackbarUtils.show(v, R.string.snackbar_currently_downloading, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);
					return;
				}

				switch (holder.state)
				{
					case INSTALLED_NO_UPDATE:
						if (context instanceof DatasourceActivity)
						{
							/* If we're coming from the DatasourceActivity and the user clicked on a data source that has already been downloaded
						 	 * then we just remember the selected id and close the activity to return to wherever we came from */
							PreferenceUtils.setPreferenceAsInt(context, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, ds.getId());
							context.setResult(Activity.RESULT_OK);
							context.finish();
							return;
						}
							/* Else, we're coming from the introduction activity and we don't want to return anywhere */
						else
						{
							SnackbarUtils.show(v, R.string.snackbar_already_downloaded, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);
							PreferenceUtils.setPreferenceAsInt(context, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, ds.getId());
							return;
						}

					case INSTALLED_HAS_UPDATE:
					case NOT_INSTALLED:
						holder.progressBar.setVisibility(View.VISIBLE);
						holder.isDownloading = true;

						/* Start the download */
						DatasourceService.download(context, holder.progressBar, ds, new RestletCallback<File>(context)
						{
							@Override
							public void onFailure(Exception caught)
							{
								super.onFailure(caught);

								holder.progressBar.setVisibility(View.INVISIBLE);
								holder.isDownloading = false;
							}

							@Override
							public void onSuccess(File result)
							{
								holder.progressBar.setVisibility(View.INVISIBLE);
								holder.state = InstallState.INSTALLED_NO_UPDATE;
								holder.isDownloading = false;

								PreferenceUtils.setPreferenceAsInt(context, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, ds.getId());
								SnackbarUtils.show(holder.view, R.string.snackbar_download_successful, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);

								PreferenceUtils.setPreferenceAsBoolean(context, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, true);

								notifyDataSetChanged();
							}
						});
						break;
				}
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return dataset.size();
	}

	/**
	 * {@link InstallState} represents the different states a {@link KnodelDatasource} can have locally.
	 */
	public enum InstallState
	{
		/** The data source isn't installed at all */
		NOT_INSTALLED,
		/** The data source is installed, but there's an update */
		INSTALLED_HAS_UPDATE,
		/** The data source is installed and there is no update */
		INSTALLED_NO_UPDATE
	}
}