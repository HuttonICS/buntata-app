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

package uk.ac.hutton.ics.buntata.adapter;

import android.content.*;
import android.os.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.io.*;
import java.util.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.util.*;


/**
 * The {@link NodeCatalogAdapter} takes care of the {@link BuntataNodeAdvanced} objects.
 *
 * @author Sebastian Raubach
 */
public abstract class NodeCatalogAdapter extends RecyclerView.Adapter<NodeCatalogAdapter.ViewHolder>
{
	private static final boolean USE_DIRTY_HACK_TO_FIX_SHARED_ELEMENT_TRANSITION = true;

	private Context                   context;
	private RecyclerView              parent;
	private int                       datasourceId;
	private List<BuntataNodeAdvanced> dataset;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View view;
		@BindView(R.id.node_catalog_image)
		ImageView image;
		@BindView(R.id.node_catalog_name)
		TextView  name;
//		@BindView(R.id.node_catalog_description)
//		TextView  description;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			ButterKnife.bind(this, v);
		}
	}

	public NodeCatalogAdapter(Context context, RecyclerView parent, int datasourceId, List<BuntataNodeAdvanced> dataset)
	{
		this.context = context;
		this.parent = parent;
		this.datasourceId = datasourceId;
		this.dataset = dataset;
	}

	public void update(List<BuntataNodeAdvanced> filtered)
	{
		this.dataset = filtered;
		notifyDataSetChanged();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.node_catalog_view, parent, false));
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		final BuntataNodeAdvanced item = dataset.get(position);

		boolean foundImage = false;
		/* Try to find an image */
		File imagePath = null;
		BuntataMediaAdvanced m = null;
		if (item.getMediaAdvanced().size() > 0)
		{
			m = item.getFirstImage();

			if (m != null)
			{
				foundImage = true;
				imagePath = FileUtils.getFileForDatasource(context, datasourceId, m.getInternalLink());
			}
		}

		if (context instanceof MainActivity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			holder.image.setTransitionName(context.getString(R.string.transition_node_view));
		}
		/* If no image is found */
		if (!foundImage)
		{
			Picasso.get()
				   .load(R.drawable.missing_image)
				   .error(R.drawable.missing_image)
				   .centerInside()
				   .into(holder.image);
		}
		/* If an image is found */
		else
		{
			holder.view.measure(
					View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
					View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

			Picasso.get()
				   .load(imagePath) /* Load from file */
				   .resize(holder.image.getWidth(), holder.view.getMeasuredHeight())
				   .centerCrop()
				   .into(holder.image);
		}

		holder.name.setText(item.getName());
//		holder.description.setText(item.getDescription());

		final BuntataMediaAdvanced medium = m;

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
								onNodeClicked(holder.image, medium, dataset.get(holder.getAdapterPosition()));
							}
						}
					});
				}
				else
				{
					onNodeClicked(holder.image, medium, dataset.get(holder.getAdapterPosition()));
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
	 * @param node The actual node that has been clicked.
	 */
	public abstract void onNodeClicked(View animationRoot, BuntataMediaAdvanced medium, BuntataNodeAdvanced node);
}