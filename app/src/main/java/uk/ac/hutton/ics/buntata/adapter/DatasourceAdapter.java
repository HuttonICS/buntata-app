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

package uk.ac.hutton.ics.buntata.adapter;

import android.app.*;
import android.content.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.afollestad.sectionedrecyclerview.*;
import com.squareup.picasso.*;
import com.transitionseverywhere.*;

import java.io.*;
import java.util.*;

import butterknife.*;
import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.service.*;
import uk.ac.hutton.ics.buntata.thread.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link DatasourceAdapter} handles the {@link BuntataDatasource}s.
 *
 * @author Sebastian Raubach
 */
public class DatasourceAdapter extends SectionedRecyclerViewAdapter<DatasourceAdapter.AbstractViewHolder>
{
	private static final int LOCAL  = 0;
	private static final int REMOTE = 1;

	private int expandedPosition = -1;

	private Activity                        context;
	private RecyclerView                    parent;
	private List<BuntataDatasourceAdvanced> dataset;
	private List<BuntataDatasourceAdvanced> local  = new ArrayList<>();
	private List<BuntataDatasourceAdvanced> remote = new ArrayList<>();
	private DownloadTask downloadTask;

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
		@BindView(R.id.datasource_header_title)
		TextView header;
		@BindView(R.id.datasource_header_count)
		TextView count;

		HeaderViewHolder(View v)
		{
			super(v);

			ButterKnife.bind(this, v);
		}
	}

	static class ItemViewHolder extends AbstractViewHolder
	{
		@BindView(R.id.datasource_name_view)
		TextView     nameView;
		@BindView(R.id.datasource_description_view)
		TextView     descriptionView;
		@BindView(R.id.datasource_size_view)
		TextView     sizeView;
		@BindView(R.id.datasource_image_view)
		ImageView    imageView;
		@BindView(R.id.datasource_download_progress)
		ProgressBar  progressBar;
		@BindView(R.id.datasource_download_indicator)
		Button       downloadStatus;
		@BindView(R.id.datasource_details_view)
		LinearLayout detailsView;
		@BindView(R.id.datasource_provider_view)
		TextView     providerView;
		@BindView(R.id.datasource_version_view)
		TextView     versionView;
		@BindView(R.id.datasource_contact_view)
		TextView     contactView;

		ItemViewHolder(View v)
		{
			super(v);

			ButterKnife.bind(this, v);
		}
	}

	public DatasourceAdapter(Activity context, RecyclerView parent, List<BuntataDatasourceAdvanced> dataset)
	{
		this.context = context;
		this.parent = parent;
		this.dataset = dataset;

//		shouldShowHeadersForEmptySections(true);

		onDatasetChanged();
	}

	private synchronized void onDatasetChanged()
	{
		expandedPosition = -1;

		local.clear();
		remote.clear();

		for (BuntataDatasourceAdvanced ds : dataset)
		{
			if (ds.getState() == BuntataDatasourceAdvanced.InstallState.NOT_INSTALLED)
				remote.add(ds);
			else
				local.add(ds);
		}

		if (dataset.size() > 0)
			notifyDataSetChanged();
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
//		final int color = ContextCompat.getColor(context, R.color.colorAccent);
//
//		final ValueAnimator colorAnim = ObjectAnimator.ofFloat(0f, 1f);
//		colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
//		{
//			@Override
//			public void onAnimationUpdate(ValueAnimator animation)
//			{
//				float mul = (Float) animation.getAnimatedValue();
//				int alphaOrange = adjustAlpha(color, mul);
//				itemViewHolder.downloadStatus.setColorFilter(alphaOrange, PorterDuff.Mode.SRC_ATOP);
//				if (mul == 0.0)
//				{
//					itemViewHolder.downloadStatus.setColorFilter(null);
//				}
//			}
//		});
//
//		colorAnim.setDuration(500);
//		colorAnim.start();
	}
//
//	private int adjustAlpha(int color, float factor)
//	{
//		int alpha = Math.round(Color.alpha(color) * factor);
//		int red = Color.red(color);
//		int green = Color.green(color);
//		int blue = Color.blue(color);
//		return Color.argb(alpha, red, green, blue);
//	}

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
				if (local != null)
					holder.count.setText(Integer.toString(local.size()));
				else
					holder.count.setText(Integer.toString(0));
				break;
			case REMOTE:
				holder.header.setText(context.getString(R.string.datasource_list_header_remote));
				if (remote != null)
					holder.count.setText(Integer.toString(remote.size()));
				else
					holder.count.setText(Integer.toString(0));
				break;
		}

		if (context instanceof IntroductionActivity)
		{
			holder.header.setTextColor(ContextCompat.getColor(context, android.R.color.white));
			holder.count.setTextColor(ContextCompat.getColor(context, android.R.color.white));
		}
	}

	private BuntataDatasourceAdvanced get(int section, int relativePosition)
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

	private int getAbsolutePosition(int position, int section)
	{
		switch (section)
		{
			case 0:
				return position + 1;
			case 1:
				if (local.size() > 0)
					return position + 2;
				else
					return position + 3;
		}

		return -1;
	}

	@Override
	public void onBindViewHolder(final AbstractViewHolder h, final int section, final int relativePosition, final int absolutePosition)
	{
		final BuntataDatasource item;

		final boolean isExpanded = getAbsolutePosition(absolutePosition, section) == expandedPosition;

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
		holder.sizeView.setText(context.getString(R.string.datasource_size, (item.getSizeNoVideo() / 1024f / 1024f), (item.getSizeTotal() / 1024f / 1024f)));
		holder.contactView.setText(item.getContact());
		holder.contactView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ShareCompat.IntentBuilder.from(context)
										 .setType("message/rfc822")
										 .addEmailTo(item.getContact())
										 .setSubject(context.getString(R.string.contact_email_subject))
										 .setChooserTitle(R.string.contact_email_dialog_title)
										 .startChooser();
			}
		});

		holder.providerView.setText(item.getDataProvider());
		holder.versionView.setText(Integer.toString(item.getVersionNumber()));

		final BuntataDatasourceAdvanced ds = get(section, relativePosition);
		holder.progressBar.setVisibility(ds.isDownloading() ? View.VISIBLE : View.GONE);

		setState(ds, holder);
//		holder.downloadStatus.setImageResource(resource);
//		holder.downloadStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));

		/* If there is an icon, set it */
		String iconPath = DatasourceService.getIcon(context, item);
		if (!StringUtils.isEmpty(iconPath))
		{
			holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

			RequestCreator r;

			File f = new File(iconPath);
			if (f.exists())
				r = Picasso.with(context).load(f);
			else
				r = Picasso.with(context).load(iconPath);

			r.noPlaceholder()
			 .into(holder.imageView);
		}
		/* Else set a default icon */
		else
		{
			holder.imageView.setImageResource(R.drawable.drawer_data_source);
			holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			holder.imageView.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
		}

		holder.detailsView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				expandedPosition = isExpanded ? -1 : getAbsolutePosition(absolutePosition, section);

				/* Set a new transition */
				ChangeBounds transition = new ChangeBounds();
				/* For 150 ms */
				transition.setDuration(150);
				/* And start it */
				TransitionManager.beginDelayedTransition(parent, transition);

				/* Let the parent view know that something changed and that it needs to re-layout */
				notifyDataSetChanged();
			}
		});

		/* Add a long click handler */
		holder.view.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				/* If it's not installed or if it's currently downloading, do nothing */
				if ((ds.getState() == BuntataDatasourceAdvanced.InstallState.NOT_INSTALLED) || ds.isDownloading())
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
						ds.setState(BuntataDatasourceAdvanced.InstallState.NOT_INSTALLED);

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

						if (installedDatasources < 1)
							PreferenceUtils.removePreference(context, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE);

						onDatasetChanged();
						animate(holder);

						GoogleAnalyticsUtils.trackEvent(context, BaseActivity.getTracker(context, BaseActivity.TrackerName.APP_TRACKER), context.getString(R.string.ga_event_category_datasource_deleted), "Datasource: " + ds.getId());
					}
				}, null);
				return true;
			}
		});

		/* Add a click handler */
		holder.downloadStatus.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (ds.isDownloading())
				{

					DialogUtils.showDialog(context, R.string.dialog_download_cancel_title, R.string.dialog_download_cancel_text, R.string.generic_yes, R.string.generic_no, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							if (downloadTask != null)
							{
								downloadTask.cancel(true);
								holder.progressBar.setVisibility(View.GONE);
								ds.setDownloading(false);
								downloadTask = null;
								setState(ds, holder);
							}
						}
					}, null);
					return;
				}
				else
				{
					for (BuntataDatasourceAdvanced dss : dataset)
					{
						if (dss.isDownloading())
						{
							SnackbarUtils.show(v, R.string.snackbar_only_one_download, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);
							return;
						}
					}
				}

				switch (ds.getState())
				{
					case INSTALLED_NO_UPDATE:
						/* Just remember the selected id and close the activity to return to wherever we came from */
						PreferenceUtils.setPreferenceAsInt(context, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, ds.getId());
						context.setResult(Activity.RESULT_OK);
						context.finish();
						return;

					case INSTALLED_HAS_UPDATE:
					case NOT_INSTALLED:

						DialogUtils.showDialog(context, R.string.dialog_download_title, R.string.dialog_download_message, R.string.generic_yes, R.string.generic_no, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								initDownload(true, holder, ds);
							}
						}, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								initDownload(false, holder, ds);
							}
						});
						break;
				}
			}
		});
	}

	private void setState(BuntataDatasourceAdvanced ds, ItemViewHolder holder)
	{
		/* Set the state icon */
		int resource;
		switch (ds.getState())
		{
			case INSTALLED_NO_UPDATE:
				resource = R.string.datasource_status_select;
				break;

			case INSTALLED_HAS_UPDATE:
				resource = R.string.datasource_status_update;
				break;

			case NOT_INSTALLED:
			default:
				resource = R.string.datasource_status_download;
				break;
		}

		if (ds.isDownloading())
			resource = R.string.datasource_status_cancel;

		holder.downloadStatus.setText(resource);
	}

	private void initDownload(boolean includeVideos, final ItemViewHolder holder, final BuntataDatasourceAdvanced ds)
	{
		holder.progressBar.setVisibility(View.VISIBLE);
		holder.progressBar.setIndeterminate(true);
		holder.downloadStatus.setText(R.string.datasource_status_cancel);
		ds.setDownloading(true);

		/* Start the download */
		downloadTask = DatasourceService.download(context, includeVideos, holder.progressBar, ds, new RemoteCallback<File>(context)
		{
			@Override
			public void onFailure(Throwable caught)
			{
				super.onFailure(caught);

				GoogleAnalyticsUtils.trackException(context, BaseActivity.getTracker(context, BaseActivity.TrackerName.APP_TRACKER), caught);

				holder.progressBar.setVisibility(View.GONE);
				ds.setDownloading(false);
				downloadTask = null;
				setState(ds, holder);

				SnackbarUtils.show(holder.view, R.string.snackbar_download_unsuccessful, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);
			}

			@Override
			public void onSuccess(File result)
			{
				GoogleAnalyticsUtils.trackEvent(context, BaseActivity.getTracker(context, BaseActivity.TrackerName.APP_TRACKER), context.getString(R.string.ga_event_category_datasource_download), "Datasource: " + ds.getId());

				holder.progressBar.setVisibility(View.GONE);
				ds.setState(BuntataDatasourceAdvanced.InstallState.INSTALLED_NO_UPDATE);
				ds.setDownloading(false);
				setState(ds, holder);

				PreferenceUtils.setPreferenceAsInt(context, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, ds.getId());
				SnackbarUtils.show(holder.view, R.string.snackbar_download_successful, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);

				PreferenceUtils.setPreferenceAsBoolean(context, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, true);

				onDatasetChanged();
				animate(holder);
				downloadTask = null;
			}
		});
	}
}