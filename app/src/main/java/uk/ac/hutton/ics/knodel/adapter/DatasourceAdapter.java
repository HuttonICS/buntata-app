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

public class DatasourceAdapter extends RecyclerView.Adapter<DatasourceAdapter.ViewHolder>
{
	private Activity                                              context;
	private Map<KnodelDatasource, DatasourceAdapter.InstallState> mapping;
	private List<KnodelDatasource>                                dataset;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	static class ViewHolder extends RecyclerView.ViewHolder
	{
		// each data item is just a string in this case
		View        view;
		TextView    nameView;
		TextView    descriptionView;
		TextView    sizeView;
		ImageView   imageView;
		ProgressBar progressBar;
		ImageView   downloadStatus;

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

		void setViewHolderClickListener(final ViewHolderClickListener listener)
		{
			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					listener.onViewHolderClicked(getAdapterPosition());
				}
			});
		}

		void setViewHolderLongClickListener(final ViewHolderClickListener listener)
		{
			view.setOnLongClickListener(new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					listener.onViewHolderClicked(getAdapterPosition());
					return true;
				}
			});
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public DatasourceAdapter(Activity context, Map<KnodelDatasource, DatasourceAdapter.InstallState> mapping)
	{
		this.context = context;
		this.mapping = mapping;
		this.dataset = new ArrayList<>(mapping.keySet());
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.datasource_view, parent, false);

		final ViewHolder viewHolder = new ViewHolder(v);

		viewHolder.setViewHolderLongClickListener(new ViewHolderClickListener()
		{
			@Override
			public void onViewHolderClicked(int adapterPosition)
			{
				final KnodelDatasource ds = dataset.get(adapterPosition);

				if ((viewHolder.state == InstallState.NOT_INSTALLED) || viewHolder.isDownloading)
					return;

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
						viewHolder.state = InstallState.NOT_INSTALLED;

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
			}
		});

		viewHolder.setViewHolderClickListener(new ViewHolderClickListener()
		{
			@Override
			public void onViewHolderClicked(int adapterPosition)
			{
				final KnodelDatasource ds = dataset.get(adapterPosition);

				if (viewHolder.isDownloading)
				{
					SnackbarUtils.show(v, R.string.snackbar_currently_downloading, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);
					return;
				}

				switch (viewHolder.state)
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
						viewHolder.progressBar.setVisibility(View.VISIBLE);
						viewHolder.isDownloading = true;

						DatasourceService.download(context, viewHolder.progressBar, ds, new RestletCallback<File>(context)
						{
							@Override
							public void onFailure(Exception caught)
							{
								super.onFailure(caught);

								viewHolder.progressBar.setVisibility(View.INVISIBLE);
								viewHolder.isDownloading = false;
							}

							@Override
							public void onSuccess(File result)
							{
								viewHolder.progressBar.setVisibility(View.INVISIBLE);
								viewHolder.state = InstallState.INSTALLED_NO_UPDATE;
								viewHolder.isDownloading = false;

								PreferenceUtils.setPreferenceAsInt(context, PreferenceUtils.PREFS_SELECTED_DATASOURCE_ID, ds.getId());
								SnackbarUtils.show(v, R.string.snackbar_download_successful, ContextCompat.getColor(context, android.R.color.primary_text_dark), ContextCompat.getColor(context, R.color.colorPrimaryDark), Snackbar.LENGTH_LONG);

								PreferenceUtils.setPreferenceAsBoolean(context, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, true);

								notifyDataSetChanged();
							}
						});
						break;
				}
			}
		});

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
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		KnodelDatasource item = dataset.get(position);
		holder.nameView.setText(item.getName());
		holder.descriptionView.setText(item.getDescription());
		holder.sizeView.setText(context.getString(R.string.datasource_size, (item.getSize() / 1024 / 1024)));

		if (holder.state == null)
		{
			holder.state = mapping.get(item);
		}
		/* If it's already downloaded, mark it as such */
		switch (holder.state)
		{
			case INSTALLED_NO_UPDATE:
				holder.downloadStatus.setImageResource(R.drawable.action_ok);
				holder.downloadStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
				break;

			case INSTALLED_HAS_UPDATE:
				holder.downloadStatus.setImageResource(R.drawable.action_update);
				holder.downloadStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
				break;

			case NOT_INSTALLED:
				holder.downloadStatus.setImageResource(R.drawable.action_download);
				holder.downloadStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
				break;
		}

		animate(holder);


		if (!StringUtils.isEmpty(item.getIcon()))
		{
			Picasso.with(context)
				   .load(item.getIcon())
				   .noPlaceholder()
				   .into(holder.imageView);
		}
	}

	@Override
	public int getItemCount()
	{
		return dataset.size();
	}

	public enum InstallState
	{
		NOT_INSTALLED,
		INSTALLED_HAS_UPDATE,
		INSTALLED_NO_UPDATE
	}
}