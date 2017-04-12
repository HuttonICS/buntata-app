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
import android.net.*;
import android.support.annotation.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.R;

/**
 * The {@link AcknowledgementsAdapter} takes care of all the {@link Acknowledgements}.
 *
 * @author Sebastian Raubach
 */
public class AcknowledgementsAdapter extends RecyclerView.Adapter<AcknowledgementsAdapter.ViewHolder>
{
	private Activity context;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View view;
		@BindView(R.id.ack_view_image)
		ImageView logo;
		@BindView(R.id.ack_view_text)
		TextView  text;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			ButterKnife.bind(this, v);
		}
	}

	public AcknowledgementsAdapter(Activity context)
	{
		this.context = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.acknowledgements_view, parent, false);

		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		final Acknowledgements item = Acknowledgements.values()[position];

		/* Set the content */
		holder.logo.setImageResource(item.image);
		holder.text.setText(item.text);

		/* Add a click listener */
		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(item.url))));
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return Acknowledgements.values().length;
	}

	/**
	 * {@link Acknowledgements} is an enum holding all the acknowledgements items for this app
	 *
	 * @author Sebastian Raubach
	 */
	private enum Acknowledgements
	{
		JHI(R.drawable.jhi, R.string.acknowledgements_jhi_text, R.string.acknowledgements_jhi_url),
		ST_ANDREWS(R.drawable.st_andrews_with_name, R.string.acknowledgements_st_andrews_text, R.string.acknowledgements_st_andrews_url),
		BBSRC(R.drawable.bbsrc, R.string.acknowledgements_bbsrc_text, R.string.acknowledgements_bbsrc_url),
		AHDB(R.drawable.ahdb, R.string.acknowledgements_ahdb_text, R.string.acknowledgements_ahdb_url),
		SCOTTISH_GOVERNMENT(R.drawable.scottish_government, R.string.acknowledgements_scottish_government_text, R.string.acknowledgements_scottish_government_url);

		@DrawableRes
		int image;
		@StringRes
		int text;
		@StringRes
		int url;


		Acknowledgements(@DrawableRes int image, @StringRes int text, @StringRes int url)
		{
			this.image = image;
			this.text = text;
			this.url = url;
		}
	}
}