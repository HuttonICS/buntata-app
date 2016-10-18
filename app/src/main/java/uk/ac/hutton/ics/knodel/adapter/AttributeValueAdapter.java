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
import android.support.v7.widget.*;
import android.text.method.*;
import android.view.*;
import android.widget.*;

import com.transitionseverywhere.*;

import java.util.*;

import uk.ac.hutton.ics.knodel.R;
import uk.ac.hutton.ics.knodel.database.entity.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * The {@link AttributeValueAdapter} takes care of the {@link KnodelAttributeValueAdvanced} objects.
 *
 * @author Sebastian Raubach
 */
public class AttributeValueAdapter extends RecyclerView.Adapter<AttributeValueAdapter.ViewHolder>
{
	private Context                            context;
	private int                                datasourceId;
	private RecyclerView                       parent;
	private List<KnodelAttributeValueAdvanced> dataset;

	private int     expandedPosition = -1;
	private boolean firstStart       = true;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View      view;
		TextView  title;
		TextView  content;
		ImageView expandIcon;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			title = (TextView) v.findViewById(R.id.attribute_view_title);
			content = (TextView) v.findViewById(R.id.attribute_view_content);
			expandIcon = (ImageView) v.findViewById(R.id.attribute_view_expand_icon);
		}
	}

	/**
	 * Creates a new instance of this adapter
	 * @param context The {@link Context}
	 * @param datasourceId The id of the current data source
	 * @param parent The {@link RecyclerView} that'll show the items
	 * @param dataset The {@link List} of {@link KnodelAttributeValueAdvanced} objects to show
	 */
	public AttributeValueAdapter(Context context, int datasourceId, RecyclerView parent, List<KnodelAttributeValueAdvanced> dataset)
	{
		this.context = context;
		this.datasourceId = datasourceId;
		this.parent = parent;
		this.dataset = dataset;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.attribute_view, parent, false));
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		KnodelAttributeValueAdvanced item = dataset.get(position);

		final boolean isExpanded;

		/* Expand the first item by default */
		if (position == 0 && firstStart)
		{
			isExpanded = true;
			firstStart = false;
		}
		/* Else, expand based on the last selected item */
		else
		{
			isExpanded = position == expandedPosition;
		}

		/* Show or hide the content */
		holder.content.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
		/* Activate/deactivate the item */
		holder.itemView.setActivated(isExpanded);
		/* On click change the state */
		holder.itemView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				expandedPosition = isExpanded ? -1 : holder.getAdapterPosition();

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

		/* Set the title and content */
		holder.title.setText(item.getAttribute().getName());
		holder.content.setText(StringUtils.fromHtml(item.getValue()));
		/* Make sure to respect hyperlinks in the content */
		holder.content.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public int getItemCount()
	{
		return dataset.size();
	}
}