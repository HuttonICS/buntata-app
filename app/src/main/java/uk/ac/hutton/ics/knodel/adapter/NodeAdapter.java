package uk.ac.hutton.ics.knodel.adapter;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.support.v4.content.*;
import android.support.v7.graphics.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.io.*;
import java.util.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.database.entity.*;
import uk.ac.hutton.ics.knodel.util.*;

public abstract class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.ViewHolder>
{
	private Context                  context;
	private int                      datasourceId;
	private List<KnodelNodeAdvanced> dataset;

	private int defaultBackgroundColor;
	private int defaultForegroundColor;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		// each data item is just a string in this case
		View      view;
		ImageView image;
		TextView  title;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			image = (ImageView) v.findViewById(R.id.node_view_image);
			title = (TextView) v.findViewById(R.id.node_view_title);
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
	}

	public NodeAdapter(Context context, int datasourceId, List<KnodelNodeAdvanced> dataset)
	{
		this.context = context;
		this.datasourceId = datasourceId;
		this.dataset = dataset;

		this.defaultBackgroundColor = ContextCompat.getColor(context, R.color.cardview_light_background);
		this.defaultForegroundColor = ContextCompat.getColor(context, android.R.color.primary_text_light);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.node_view, parent, false);

		final ViewHolder viewHolder = new ViewHolder(v);

		viewHolder.setViewHolderClickListener(new ViewHolderClickListener()
		{
			@Override
			public void onViewHolderClicked(int adapterPosition)
			{
				onNodeClicked(viewHolder.image, dataset.get(adapterPosition));
			}
		});

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		KnodelNodeAdvanced item = dataset.get(position);

		boolean foundImage = false;

		File imagePath = null;
		if (item.getMedia().size() > 0)
		{
			for (KnodelMediaAdvanced m : item.getMedia())
			{
				if (m.getMediaType() != null && "Image".equals(m.getMediaType().getName()) && m.getInternalLink() != null)
				{
					imagePath = FileUtils.getFileForDatasource(context, datasourceId, m.getInternalLink());

					foundImage = true;

					break;
				}
			}
		}

		if (!foundImage)
		{
			holder.image.setImageResource(R.drawable.missing_image);
		}
		else
		{
			final File f = imagePath;

			/* Wait for the image to be loaded to get the width */
			ViewTreeObserver viewTreeObserver = holder.image.getViewTreeObserver();
			if (viewTreeObserver.isAlive())
			{
				viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
				{
					@Override
					public void onGlobalLayout()
					{
						/* Remove this listener */
						holder.view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

						/* Get the width of the view */
						int viewWidth = holder.image.getWidth();
						holder.image.setMinimumHeight(viewWidth);

						/* Load the image */
						final PaletteTransformation paletteTransformation = PaletteTransformation.instance();
						Picasso.with(context)
							   .load(f) // Load from file
							   .transform(paletteTransformation) // Generate the palette based on the image
							   .resize(viewWidth, viewWidth) // Resize to fit
							   .onlyScaleDown() // But only scale down
							   .centerCrop() // And respect the aspect ratio
							   .placeholder(R.drawable.missing_image) // Set a placeholder
							   .into(holder.image, new Callback.EmptyCallback() // When done, use the palette
							   {
								   @Override
								   public void onSuccess()
								   {
									   /* Get back the bitmap */
									   Bitmap bitmap = ((BitmapDrawable) holder.image.getDrawable()).getBitmap(); // Ew!
									   /* Get the generated palette */
									   Palette palette = PaletteTransformation.getPalette(bitmap);

									   int background = palette.getDarkMutedColor(defaultBackgroundColor);
									   int foreground = palette.getLightVibrantColor(defaultForegroundColor);

									   /* If it can't find a suitable color for both background and foreground, then use the defaults */
									   if (background == defaultBackgroundColor || foreground == defaultForegroundColor)
									   {
										   holder.title.setBackgroundColor(defaultBackgroundColor);
										   holder.title.setTextColor(defaultForegroundColor);
									   }
									   /* Otherwise use the palette values */
									   else
									   {
										   holder.title.setBackgroundColor(foreground);
										   holder.title.setTextColor(background);
									   }
								   }
							   });
					}
				});
			}
		}

		holder.title.setText(item.getName());
	}

	@Override
	public int getItemCount()
	{
		return dataset.size();
	}

	public abstract void onNodeClicked(View transitionRoot, KnodelNodeAdvanced node);
}