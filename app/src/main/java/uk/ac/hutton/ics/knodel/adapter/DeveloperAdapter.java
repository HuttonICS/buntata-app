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

import android.app.*;
import android.support.v4.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import uk.ac.hutton.ics.knodel.*;

/**
 * @author Sebastian Raubach
 */
public class DeveloperAdapter extends RecyclerView.Adapter<DeveloperAdapter.ViewHolder>
{
	private Activity context;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View     view;
		TextView name;
		TextView title;
		TextView group;
		TextView institute;
		CardView email;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			name = (TextView) v.findViewById(R.id.developer_view_name);
			title = (TextView) v.findViewById(R.id.developer_view_title);
			group = (TextView) v.findViewById(R.id.developer_view_group);
			institute = (TextView) v.findViewById(R.id.developer_view_institute);
			email = (CardView) v.findViewById(R.id.developer_view_email);
		}
	}

	public DeveloperAdapter(Activity context)
	{
		this.context = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.developer_view, parent, false);

		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		final Developer item = Developer.values()[position];

		holder.name.setText(item.name);
		holder.title.setText(item.title);
		holder.group.setText(item.group);
		holder.institute.setText(item.institute);

		holder.email.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ShareCompat.IntentBuilder.from(context)
										 .setType("message/rfc822")
										 .addEmailTo(item.email)
										 .setSubject(context.getString(R.string.contact_email_subject))
										 .setChooserTitle(R.string.contact_email_dialog_title)
										 .startChooser();
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return Developer.values().length;
	}

	private enum Developer
	{
		SEBASTIAN_RAUBACH("Sebastian Raubach", "Bioinformatics Software Developer", "Information & Computational Sciences", "The James Hutton Institute", "germinate@hutton.ac.uk"),
		PAUL_SHAW("Paul Shaw", "Bioinformatician", "Information & Computational Sciences", "The James Hutton Institute", "germinate@hutton.ac.uk");

		String name;
		String title;
		String group;
		String institute;
		String email;

		Developer(String name, String title, String group, String institute, String email)
		{
			this.name = name;
			this.title = title;
			this.group = group;
			this.institute = institute;
			this.email = email;
		}
	}
}