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

import butterknife.*;
import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.util.*;


/**
 * The {@link NodeAdapter} takes care of the {@link BuntataNodeAdvanced} objects.
 *
 * @author Sebastian Raubach
 */
public abstract class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.ViewHolder> implements Filterable
{
	private static final boolean USE_DIRTY_HACK_TO_FIX_SHARED_ELEMENT_TRANSITION = true;

	private Context                   context;
	private RecyclerView              parent;
	private BuntataDatasource         datasource;
	private int                       parentMediaId;
	private List<BuntataNodeAdvanced> dataset;
	private UserFilter                userFilter;
	private NodeManager               nodeManager;

	private int defaultBackgroundColor;
	private int textColorLight;
	private int textColorDark;

	private int left        = 0;
	private int right       = 0;
	private int padding     = 0;
	private int columnCount = 1;

	public void updateDimensions(int columnCount, int left, int right, int padding)
	{
		this.columnCount = columnCount;
		this.left = left;
		this.right = right;
		this.padding = padding;
	}

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View view;
		@BindView(R.id.node_view_image)
		ImageView image;
		@BindView(R.id.node_view_title)
		TextView  title;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			ButterKnife.bind(this, v);
		}
	}

	public NodeAdapter(Context context, RecyclerView parent, int datasourceId, int parentMediaId, List<BuntataNodeAdvanced> dataset)
	{
		this.context = context;
		this.parent = parent;
		this.dataset = dataset;
		this.nodeManager = new NodeManager(context, datasourceId);
		this.parentMediaId = parentMediaId;
		DatasourceManager manager = new DatasourceManager(context, datasourceId);
		this.datasource = manager.getById(datasourceId);

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
		final BuntataNodeAdvanced item = dataset.get(position);

		boolean foundImage = false;

		/* Try to find an image */
		File imagePath = null;
		BuntataMediaAdvanced medium = null;
		if (item.getMedia().size() > 0)
		{
			/* First, check if we've got the same image as our parent. If so, show this one preferably */
			for (BuntataMediaAdvanced m : item.getMedia())
			{
				if (m.getId() == parentMediaId)
				{
					medium = m;
					foundImage = true;
					imagePath = FileUtils.getFileForDatasource(context, datasource.getId(), m.getInternalLink());
					break;
				}
			}

			/* If we don't, check all our media and pick the first "Image" */
			if (!foundImage)
			{
				BuntataMediaAdvanced m = item.getFirstImage();

				if (m != null)
				{
					medium = m;
					foundImage = true;
					imagePath = FileUtils.getFileForDatasource(context, datasource.getId(), m.getInternalLink());
				}
			}
		}

		/* Get the width of the view */
		int viewWidth = (parent.getWidth() - left - right - (columnCount - 1) * padding) / columnCount;
		holder.image.setMinimumHeight(viewWidth);

		/* If no image is found */
		if (!foundImage)
		{
			Picasso.with(context)
				   .load(R.drawable.missing_image)
				   .error(R.drawable.missing_image)
				   .resize(viewWidth, viewWidth) // Resize to fit
				   .centerInside()
				   .into(holder.image);
		}
		/* If an image is found */
		else
		{
			/* Load the image */
			final PaletteTransformation paletteTransformation = PaletteTransformation.instance();
			Picasso.with(context)
				   .load(imagePath) // Load from file
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

		holder.title.setText(item.getName());

		if (datasource.isShowKeyName() || !item.hasChildren())
			holder.title.setVisibility(View.VISIBLE);
		else
			holder.title.setVisibility(View.GONE);

		final BuntataMediaAdvanced m = medium;

		/* Listen to click events */
		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int top = v.getTop();

				if (top < 0 && USE_DIRTY_HACK_TO_FIX_SHARED_ELEMENT_TRANSITION)
				{
					/*
					 * This is a very dirty hack to fix an issue where shared elements will overlap the toolbar and status bar
					 * when the node is hidden behind them on click. Solutions on Stackoverflow suggested to include the toolbar
					 * and status bar in the shared elements transition. This, however, didn't seem to work, thus this solution.
					 * The view is then scrolled "into view" and the shared elements transition is postponed until the scroll
					 * event finishes.
					 */
					parent.smoothScrollToPosition(holder.getAdapterPosition());
					parent.addOnScrollListener(new RecyclerView.OnScrollListener()
					{
						@Override
						public void onScrollStateChanged(RecyclerView recyclerView, int newState)
						{
							if (newState == RecyclerView.SCROLL_STATE_IDLE)
							{
								parent.removeOnScrollListener(this);
								onNodeClicked(holder.image, holder.title, m, dataset.get(holder.getAdapterPosition()));
							}
						}
					});
				}
				else
				{
					onNodeClicked(holder.image, holder.title, m, dataset.get(holder.getAdapterPosition()));
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
	 * Called when a node is clicked.
	 *
	 * @param transitionRoot The {@link View} showing the image. It's used to start the scene transition
	 * @param node           The actual node that has been clicked.
	 */
	public abstract void onNodeClicked(View transitionRoot, View title, BuntataMediaAdvanced medium, BuntataNodeAdvanced node);

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

		private final List<BuntataNodeAdvanced> originalList;

		private final List<BuntataNodeAdvanced> filteredList;

		private UserFilter(NodeAdapter adapter, List<BuntataNodeAdvanced> originalList)
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
				for (final BuntataNodeAdvanced item : originalList)
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
			/* Get the new items that match the search query */
			List<BuntataNodeAdvanced> newDataset = (ArrayList<BuntataNodeAdvanced>) results.values;

			/* If the new data is smaller (items have been removed), then tell the adapter */
			if (dataset.size() > newDataset.size())
			{
				/* Remove each item that is no longer in the dataset with the default transition */
				for (int i = dataset.size() - 1; i >= 0; i--)
				{
					if (!newDataset.contains(dataset.get(i)))
						adapter.notifyItemRemoved(i);
				}
			}
			/* Else, the new data is larger (items have been added) */
			else
			{
				/* Insert each item that's new to the dataset with the default transition */
				for (int i = 0; i < newDataset.size(); i++)
				{
					if (!adapter.dataset.contains(newDataset.get(i)))
						adapter.notifyItemInserted(i);
				}
			}

			/* Remember the new dataset */
			adapter.dataset = new ArrayList<>(newDataset);
		}
	}
}