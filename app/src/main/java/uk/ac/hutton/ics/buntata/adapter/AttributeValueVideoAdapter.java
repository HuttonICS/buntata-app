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
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.support.v7.widget.*;
import android.text.method.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.transitionseverywhere.*;

import java.io.*;
import java.util.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link AttributeValueVideoAdapter} takes care of the {@link BuntataAttributeValueAdvanced} and {@link BuntataMediaAdvanced} objects.
 *
 * @author Sebastian Raubach
 */
public class AttributeValueVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private Activity                            context;
	private RecyclerView                        parent;
	private List<BuntataAttributeValueAdvanced> attributes;
	private List<BuntataMediaAdvanced>          videos;
	private int                                 datasourceId;

	private int     expandedPosition = -1;
	private boolean firstStart       = true;

	static class AttributeViewHolder extends RecyclerView.ViewHolder
	{
		View view;
		@BindView(R.id.attribute_view_title)
		TextView  title;
		@BindView(R.id.attribute_view_content)
		TextView  content;
		@BindView(R.id.attribute_view_expand_icon)
		ImageView expandIcon;

		AttributeViewHolder(View v)
		{
			super(v);

			view = v;
			ButterKnife.bind(this, v);
		}
	}

	static class VideoViewHolder extends RecyclerView.ViewHolder
	{
		View view;
		@BindView(R.id.video_view_title)
		TextView  title;
		@BindView(R.id.video_view_link)
		TextView  link;
		@BindView(R.id.video_view_preview)
		ImageView preview;

		VideoViewHolder(View v)
		{
			super(v);

			view = v;
			ButterKnife.bind(this, v);
		}
	}

	/**
	 * Creates a new instance of this adapter
	 *
	 * @param parent     The {@link RecyclerView} that'll show the items
	 * @param attributes The {@link List} of {@link BuntataAttributeValueAdvanced} objects to show
	 */
	public AttributeValueVideoAdapter(Activity context, RecyclerView parent, int datasourceId, List<BuntataAttributeValueAdvanced> attributes, List<BuntataMediaAdvanced> videos)
	{
		this.context = context;
		this.parent = parent;
		this.datasourceId = datasourceId;
		this.attributes = attributes;
		this.videos = videos;
	}

	@Override
	public int getItemViewType(int position)
	{
		return position < attributes.size() ? 0 : 1;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		switch (viewType)
		{
			case 1:
				return new VideoViewHolder(LayoutInflater.from(context).inflate(R.layout.video_view, parent, false));
			case 0:
			default:
				return new AttributeViewHolder(LayoutInflater.from(context).inflate(R.layout.attribute_view, parent, false));
		}

	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position)
	{
		switch (holder.getItemViewType())
		{
			case 0:
				onBindAttributeViewHolder((AttributeViewHolder) holder, position);
				break;
			case 1:
				onBindVideoViewHolder((VideoViewHolder) holder, position);
		}
	}

	private void onBindVideoViewHolder(final VideoViewHolder holder, int position)
	{
		final BuntataMediaAdvanced item = videos.get(position - attributes.size());

		/* Set the title */
		holder.title.setText(item.getName());
		holder.preview.setVisibility(View.VISIBLE);

		/* Show the external link */
		if (item.getExternalLink() != null)
		{
			holder.link.setVisibility(View.VISIBLE);
			String text = item.getExternalLinkDescription();

			if (StringUtils.isEmpty(text))
				text = item.getExternalLink();

			holder.link.setText(StringUtils.fromHtml("<a href='" + item.getExternalLink() + "'>" + text + "</a>"));
			holder.link.setMovementMethod(LinkMovementMethod.getInstance());
		}
		else
		{
			holder.link.setVisibility(View.GONE);
		}

		/* If the video is available offline as well, show it */
		if (item.getInternalLink() != null)
		{
			File path = FileUtils.getFileForDatasource(context, datasourceId, item.getInternalLink());

			/* Set the thumbnail of the video */
			Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);

			if (thumbnail != null)
			{
				holder.preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
				holder.preview.setImageBitmap(thumbnail);
			}
			else
			{
				holder.preview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				holder.preview.setImageResource(R.drawable.missing_image);
			}

			holder.preview.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(context, VideoActivity.class);

					Bundle args = new Bundle();
					args.putSerializable(VideoActivity.PARAM_MEDIA, item);
					args.putInt(VideoActivity.PARAM_DATASOURCE_ID, datasourceId);
					intent.putExtras(args);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
					{
						View view = holder.preview;
						ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context,
								Pair.create(view, context.getString(R.string.transition_video_view)),
								Pair.create(view, context.getString(R.string.transition_video_details_view)));

						context.startActivity(intent, options.toBundle());
					}
					else
					{
						context.startActivity(intent);
					}
				}
			});
		}
		else if (item.getExternalLink() != null)
		{
			/* Set the external link, because the video isn't available locally */
			holder.preview.setImageResource(R.drawable.action_view_youtube);
			holder.preview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

			holder.preview.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getExternalLink())));
				}
			});
		}
		else
		{
			holder.preview.setVisibility(View.GONE);
		}
	}

	private void onBindAttributeViewHolder(final AttributeViewHolder holder, int position)
	{
		BuntataAttributeValueAdvanced item = attributes.get(position);

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
		return attributes.size() + videos.size();
	}
}