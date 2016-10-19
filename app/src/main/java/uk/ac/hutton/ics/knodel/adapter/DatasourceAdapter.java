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

import com.afollestad.sectionedrecyclerview.*;
import com.squareup.picasso.*;

import java.io.*;
import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.R;
import uk.ac.hutton.ics.knodel.activity.*;
import uk.ac.hutton.ics.knodel.database.entity.*;
import uk.ac.hutton.ics.knodel.database.manager.*;
import uk.ac.hutton.ics.knodel.service.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * The {@link DatasourceAdapter} handles the {@link KnodelDatasource}s.
 *
 * @author Sebastian Raubach
 */
public class DatasourceAdapter extends SectionedRecyclerViewAdapter<DatasourceAdapter.AbstractViewHolder>
{
	private static final int LOCAL  = 0;
	private static final int REMOTE = 1;

	private Activity                       context;
	private List<KnodelDatasourceAdvanced> dataset;
	private List<KnodelDatasourceAdvanced> local  = new ArrayList<>();
	private List<KnodelDatasourceAdvanced> remote = new ArrayList<>();

	static abstract class AbstractViewHolder extends RecyclerView.ViewHolder
	{
		View view;

		AbstractViewHolder(View v)
		{
			super(v);

			view = v;
		}
	}

	static class HeaderViewHolder extends AbstractViewHolder
	{
		TextView header;

		public HeaderViewHolder(View v)
		{
			super(v);

			header = (TextView) v.findViewById(R.id.datasource_header_title);
		}
	}

	static class ItemViewHolder extends AbstractViewHolder
	{
		TextView    nameView;
		TextView    descriptionView;
		TextView    sizeView;
		ImageView   imageView;
		ProgressBar progressBar;
		ImageView   downloadStatus;

		/* Remember the state */
		boolean isDownloading = false;

		ItemViewHolder(View v)
		{
			super(v);

			nameView = (TextView) v.findViewById(R.id.datasource_name_view);
			descriptionView = (TextView) v.findViewById(R.id.datasource_description_view);
			sizeView = (TextView) v.findViewById(R.id.datasource_size_view);
			imageView = (ImageView) v.findViewById(R.id.datasource_image_view);
			progressBar = (ProgressBar) v.findViewById(R.id.datasource_download_progress);
			downloadStatus = (ImageView) v.findViewById(R.id.datasource_download_indicator);
		}
	}

	public DatasourceAdapter(Activity context, List<KnodelDatasourceAdvanced> dataset)
	{
		this.context = context;
		this.dataset = dataset;

//		shouldShowHeadersForEmptySections(true);

		onDatasetChanged();
	}

	private void onDatasetChanged()
	{
		local.clear();
		remote.clear();

		for (KnodelDatasourceAdvanced ds : dataset)
		{
			if (ds.getState() == KnodelDatasourceAdvanced.InstallState.NOT_INSTALLED)
				remote.add(ds);
			else
				local.add(ds);
		}
	}

	@Override
	public DatasourceAdapter.AbstractViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		switch (viewType)
		{
			case VIEW_TYPE_HEADER:
				return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.helper_datasource_header, parent, false));
			case VIEW_TYPE_ITEM:
			default:
				return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.datasource_view, parent, false));
		}
	}

	private void animate(final ItemViewHolder itemViewHolder)
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
				itemViewHolder.downloadStatus.setColorFilter(alphaOrange, PorterDuff.Mode.SRC_ATOP);
				if (mul == 0.0)
				{
					itemViewHolder.downloadStatus.setColorFilter(null);
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
	public int getSectionCount()
	{
		return 2;
	}

	@Override
	public int getItemCount(int section)
	{
		switch (section)
		{
			case LOCAL:
				return local.size();
			case REMOTE:
			default:
				return remote.size();
		}
	}

	@Override
	public void onBindHeaderViewHolder(AbstractViewHolder h, int section)
	{
		HeaderViewHolder holder = (HeaderViewHolder) h;
		switch (section)
		{
			case LOCAL:
				holder.header.setText(context.getString(R.string.datasource_list_header_local));
				break;
			case REMOTE:
				holder.header.setText(context.getString(R.string.datasource_list_header_remote));
				break;
		}
	}

	private KnodelDatasourceAdvanced get(int section, int relativePosition)
	{
		switch (section)
		{
			case LOCAL:
				return local.get(relativePosition);
			case REMOTE:
			default:
				return remote.get(relativePosition);
		}
	}

	@Override
	public void onBindViewHolder(final AbstractViewHolder h, final int section, final int relativePosition, int absolutePosition)
	{
		KnodelDatasource item;

		switch (section)
		{
			case LOCAL:
				item = local.get(relativePosition);
				break;
			case REMOTE:
			default:
				item = remote.get(relativePosition);
		}

		final ItemViewHolder holder = (ItemViewHolder) h;

		holder.nameView.setText(item.getName());
		holder.descriptionView.setText(item.getDescription());
		holder.sizeView.setText(context.getString(R.string.datasource_size, (item.getSize() / 1024 / 1024)));

		final KnodelDatasourceAdvanced ds = get(section, relativePosition);

		/* Set the state icon */
		int resource;
		switch (ds.getState())
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
				/* If it's not installed or if it's currently downloading, do nothing */
				if ((ds.getState() == KnodelDatasourceAdvanced.InstallState.NOT_INSTALLED) || holder.isDownloading)
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
						ds.setState(KnodelDatasourceAdvanced.InstallState.NOT_INSTALLED);

						try
						{
							/* Delete associated files */
							new DatasourceManager(context, ds.getId()).remove();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}

						int installedDatasources = new DatasourceManager(context, -1).getAll().size();

						if(installedDatasources < 1)
							PreferenceUtils.removePreference(context, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE);

						onDatasetChanged();
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
				if (holder.isDownloading)
				{
					SnackbarUtils.show(v, R.string.snackbar_currently_downloading, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);
					return;
				}

				switch (ds.getState())
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
								ds.setState(KnodelDatasourceAdvanced.InstallState.INSTALLED_NO_UPDATE);
								holder.isDownloading = false;

								PreferenceUtils.setPreferenceAsInt(context, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, ds.getId());
								SnackbarUtils.show(holder.view, R.string.snackbar_download_successful, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);

								PreferenceUtils.setPreferenceAsBoolean(context, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, true);

								onDatasetChanged();
								notifyDataSetChanged();
							}
						});
						break;
				}
			}
		});
	}
}