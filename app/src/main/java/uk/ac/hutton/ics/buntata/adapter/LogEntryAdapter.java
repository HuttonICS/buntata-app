/*
 * Copyright 2017 Information & Computational Sciences, The James Hutton Institute
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
import android.support.v7.widget.*;
import android.text.format.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.io.*;
import java.util.*;

import butterknife.*;
import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link LogEntryAdapter} handles the {@link BuntataDatasource}s.
 *
 * @author Sebastian Raubach
 */
public class LogEntryAdapter extends RecyclerView.Adapter<LogEntryAdapter.ViewHolder>
{
	private Activity       context;
	private List<LogEntry> dataset;

	private LogEntryImageManager imageManager;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View view;

		@BindView(R.id.logentry_image_view)
		ImageView   imageView;
		@BindView(R.id.logentry_name_view)
		TextView    nameView;
		@BindView(R.id.logentry_date_view)
		TextView    dateView;
		@BindView(R.id.logentry_note_view)
		TextView    noteView;
		@BindView(R.id.logentry_delete_button)
		ImageButton deleteButton;

		ViewHolder(View v)
		{
			super(v);
			this.view = v;

			ButterKnife.bind(this, v);
		}
	}

	public LogEntryAdapter(Activity context, List<LogEntry> dataset)
	{
		this.context = context;
		this.dataset = dataset;
		this.imageManager = new LogEntryImageManager(context);
	}


	@Override
	public LogEntryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.logentry_view, parent, false);

		return new LogEntryAdapter.ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final LogEntryAdapter.ViewHolder holder, int position)
	{
		final LogEntry entry = dataset.get(position);

		List<LogEntryImage> images = imageManager.getForLogEntry(entry.getId());

		File imagePath = null;

		/* Get the first image the user took of this disease */
		if (images.size() > 0)
			imagePath = new File(images.get(0).getPath());

		/* As a fallback try to get the first image of the node */
		if (imagePath == null)
		{
			NodeManager nodeManager = new NodeManager(context, entry.getDatasourceId());
			BuntataNodeAdvanced node = nodeManager.getById(entry.getNodeId());

			if (node != null)
			{
				MediaManager mediaManager = new MediaManager(context, entry.getDatasourceId());
				List<BuntataMediaAdvanced> media = mediaManager.getForNode(BuntataMediaType.TYPE_IMAGE, node.getId());

				if (media.size() > 0)
					imagePath = FileUtils.getFileForDatasource(context, entry.getDatasourceId(), media.get(0).getInternalLink());
			}
		}

		if (imagePath != null)
		{
			holder.view.measure(
					View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
					View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

			Picasso.get()
				   .load(imagePath)
				   .error(R.drawable.missing_image)
				   .resize(holder.imageView.getWidth(), holder.view.getMeasuredHeight())
				   .into(holder.imageView);
		}
		else
		{
			holder.imageView.setImageResource(R.drawable.missing_image);
		}

		holder.nameView.setText(entry.getNodeName());
		holder.dateView.setText(DateUtils.formatDateTime(context.getApplicationContext(), entry.getCreatedOn().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME));
		holder.noteView.setText(entry.getNote());

		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (context instanceof LogEntryActivity)
					((LogEntryActivity) context).start(entry);
			}
		});
		holder.deleteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (context instanceof LogEntryActivity)
				{
					DialogUtils.showDialog(context, R.string.dialog_delete_log_entry_title, R.string.dialog_delete_log_entry_message, R.string.generic_yes, R.string.generic_no, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialogInterface, int i)
						{
							((LogEntryActivity) context).update(entry);
						}
					}, null);
				}
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return dataset.size();
	}
}