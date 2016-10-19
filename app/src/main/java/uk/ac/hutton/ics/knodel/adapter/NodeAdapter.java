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
import uk.ac.hutton.ics.knodel.database.manager.*;
import uk.ac.hutton.ics.knodel.util.*;


/**
 * The {@link NodeAdapter} takes care of the {@link KnodelNodeAdvanced} objects.
 *
 * @author Sebastian Raubach
 */
public abstract class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.ViewHolder> implements Filterable
{
	private Context                  context;
	private int                      datasourceId;
	private List<KnodelNodeAdvanced> dataset;
	private UserFilter               userFilter;
	private NodeManager              nodeManager;

	private int defaultBackgroundColor;
	private int textColorLight;
	private int textColorDark;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
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
	}

	public NodeAdapter(Context context, int datasourceId, List<KnodelNodeAdvanced> dataset)
	{
		this.context = context;
		this.datasourceId = datasourceId;
		this.dataset = dataset;
		this.nodeManager = new NodeManager(context, datasourceId);

		/* Get some default color */
		this.defaultBackgroundColor = ContextCompat.getColor(context, R.color.cardview_light_background);
		this.textColorLight = ContextCompat.getColor(context, android.R.color.primary_text_dark);
		this.textColorDark = ContextCompat.getColor(context, android.R.color.primary_text_light);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.node_view, parent, false));
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		KnodelNodeAdvanced item = dataset.get(position);

		boolean foundImage = false;

		/* Listen to click events */
		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onNodeClicked(holder.image, dataset.get(holder.getAdapterPosition()));
			}
		});

		/* Try to find an image */
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

		/* If no image is found */
		if (!foundImage)
		{
			holder.image.setImageResource(R.drawable.missing_image);
			holder.image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

			/* Wait for the image to be loaded to get the width */
			holder.view.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					/* Get the width of the view */
					int viewWidth = holder.image.getWidth();
					holder.image.setMinimumHeight(viewWidth);
					holder.view.forceLayout();
				}
			}, 1);
		}
		/* If an image is found */
		else
		{
			final File f = imagePath;

			/* Wait for the image to be loaded to get the width */
			holder.view.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
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
						   .into(holder.image, new Callback.EmptyCallback() // When done, use the palette
						   {
							   @Override
							   public void onError()
							   {
								   /* Set the placeholder */
								   holder.image.setImageResource(R.drawable.missing_image);
								   holder.image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
							   }

							   @Override
							   public void onSuccess()
							   {
								   /* Get back the bitmap */
								   Bitmap bitmap = ((BitmapDrawable) holder.image.getDrawable()).getBitmap(); // Ew!
								   /* Get the generated palette */
								   Palette palette = PaletteTransformation.getPalette(bitmap);

								   /* Get the vibrant color and a high-contrast text color */
								   int vibrantColor = palette.getVibrantColor(defaultBackgroundColor);
								   int textColor = ColorUtils.isColorDark(vibrantColor) ? textColorLight : textColorDark;

								   holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
								   holder.title.setBackgroundColor(vibrantColor);
								   holder.title.setTextColor(textColor);
							   }
						   });
				}
			}, 1);
		}

		holder.title.setText(item.getName());
	}

	@Override
	public int getItemCount()
	{
		return dataset.size();
	}

	/**
	 * Called when a node is clicked.
	 *
	 * @param transitionRoot The {@link View} showing the image. It's used to start the scene transition
	 * @param node           The actual node that has been clicked.
	 */
	public abstract void onNodeClicked(View transitionRoot, KnodelNodeAdvanced node);

	@Override
	public Filter getFilter()
	{
		if (userFilter == null)
			userFilter = new UserFilter(this, dataset);

		return userFilter;
	}

	private class UserFilter extends Filter
	{
		private final NodeAdapter adapter;

		private final List<KnodelNodeAdvanced> originalList;

		private final List<KnodelNodeAdvanced> filteredList;

		private UserFilter(NodeAdapter adapter, List<KnodelNodeAdvanced> originalList)
		{
			super();
			this.adapter = adapter;
			this.originalList = new LinkedList<>(originalList);
			this.filteredList = new ArrayList<>();
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint)
		{
			filteredList.clear();
			final FilterResults results = new FilterResults();

			final String filterPattern = constraint.toString();

			if (constraint.length() == 0)
			{
				filteredList.addAll(originalList);
			}
			else
			{
				for (final KnodelNodeAdvanced item : originalList)
				{
					if (nodeManager.hasChildWithContent(item, filterPattern))
					{
						filteredList.add(item);
					}
				}
			}
			results.values = filteredList;
			results.count = filteredList.size();
			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults results)
		{
			/* Clear everything */
			adapter.dataset.clear();

			/* Add the new data. The two separate notify events are necessary to force a full re-layout of all items */
			adapter.dataset.addAll((ArrayList<KnodelNodeAdvanced>) results.values);
			adapter.notifyDataSetChanged();
		}
	}
}