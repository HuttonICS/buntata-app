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
import android.content.*;
import android.net.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.activity.*;

/**
 * The {@link LicenseAdapter} takes care of all the {@link License}s.
 *
 * @author Sebastian Raubach
 */
public class LicenseAdapter extends RecyclerView.Adapter<LicenseAdapter.ViewHolder>
{
	private Activity context;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View     view;
		TextView name;
		TextView author;
		TextView description;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			name = (TextView) v.findViewById(R.id.license_view_name);
			author = (TextView) v.findViewById(R.id.license_view_author);
			description = (TextView) v.findViewById(R.id.license_view_description);
		}
	}

	public LicenseAdapter(Activity context)
	{
		this.context = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.license_view, parent, false);

		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		final License item = License.values()[position];

		/* Set the content */
		holder.name.setText(item.name);
		holder.author.setText(item.author);
		holder.description.setText(item.description);

		/* Add a click listener */
		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				/* If this is our app, give the option to show the license as well */
				if (item == License.KNODEL)
				{
					final CharSequence[] options = new CharSequence[]{"View license", "View on GitHub"}; // TODO: i18n

					new AlertDialog.Builder(context)
							.setTitle("Choose an option") // TODO: i18n
							.setItems(options, new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									switch (which)
									{
										case 0:
											context.startActivity(new Intent(context, ApacheLicenseActivity.class));
											break;
										case 1:
											context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.url)));
											break;
									}
								}
							})
							.show();
				}
				/* Else, just open the url */
				else
				{
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.url)));
				}
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return License.values().length;
	}

	/**
	 * {@link License} is an enum holding all the libraries we use in this app with their license information
	 *
	 * @author Sebastian Raubach
	 */
	private enum License
	{
		KNODEL("Knödel", "Information & Computational Sciences, The James Hutton Institute", "Apache License, Version 2.0", "https://github.com/knodel-app/knodel"),
		CIRCLE_INDICATOR("CircleIndicator", "relex", "Apache License, Version 2.0", "https://github.com/ongakuer/CircleIndicator"),
		JACKSON_ANNOTATIONS("FasterXML Jackson Annotations", "FasterXML", "Apache License, Version 2.0", "https://github.com/FasterXML/jackson-annotations"),
		JACKSON_CORE("FasterXML Jackson Core", "FasterXML", "Apache License, Version 2.0", "https://github.com/FasterXML/jackson-core"),
		JACKSON_DATABIND("FasterXML Jackson Databind", "FasterXML", "Apache License, Version 2.0", "https://github.com/FasterXML/jackson-databind"),
		MATERIAL_DESIGN_ICONS("Material design icons", "Material Design Authors", "Apache License, Version 2.0", "https://github.com/google/material-design-icons"),
		MATERIAL_INTRO("material-intro", "Heinrich Reimer", "Apache License, Version 2.0", "https://github.com/HeinrichReimer/material-intro"),
		PICASSO("Picasso", "Square", "Apache License, Version 2.0", "https://github.com/square/picasso"),
		RESTLET("Restlet", "Restlet", "Apache License, Version 2.0", "https://github.com/restlet/restlet-framework-java"),
		SLF4J("slf4j", "QOS.ch", "MIT license", "https://github.com/qos-ch/slf4j"),
		TRANSITIONS_EVERYWHERE("Transitions-Everywhere", "andkulikov", "Apache License, Version 2.0", "https://github.com/andkulikov/Transitions-Everywhere"),
		ZT_ZIP("zt-zip", "ZeroTurnaround LLC.", "Apache License, Version 2.0", "https://github.com/zeroturnaround/zt-zip");

		String name;
		String author;
		String description;
		String url;

		License(String name, String author, String description, String url)
		{
			this.name = name;
			this.author = author;
			this.description = description;
			this.url = url;
		}
	}
}