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

package uk.ac.hutton.ics.knodel.fragment;

import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.database.entity.*;
import uk.ac.hutton.ics.knodel.database.manager.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * The {@link ImageFragment} displays the information about an image medium.
 */
public class ImageFragment extends Fragment
{
	private static final String PARAM_DATASOURCE_ID = "datasourceId";
	private static final String PARAM_MEDIUM_ID     = "mediumId";

	private MediaManager mediaManager;

	private int       datasourceId;
	private int       mediumId;
	private ImageView imageView;
	private TextView  copyright;

	public static ImageFragment newInstance(int datasourceId, int mediumId)
	{
		final ImageFragment f = new ImageFragment();

			/* Pass parameters */
		Bundle args = new Bundle();
		args.putInt(PARAM_DATASOURCE_ID, datasourceId);
		args.putInt(PARAM_MEDIUM_ID, mediumId);
		f.setArguments(args);

		return f;
	}

	public ImageFragment()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

			/* Get parameters */
		datasourceId = getArguments() != null ? getArguments().getInt(PARAM_DATASOURCE_ID) : -1;
		mediumId = getArguments() != null ? getArguments().getInt(PARAM_MEDIUM_ID) : -1;
		mediaManager = new MediaManager(getActivity(), datasourceId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_node_image, container, false);
		imageView = (ImageView) v.findViewById(R.id.node_image_view);
		copyright = (TextView) v.findViewById(R.id.node_image_copyright);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

			/* Get the medium object */
		KnodelMediaAdvanced medium = mediaManager.getById(mediumId);

			/* Show copyright information if available */
		if (medium.getCopyright() != null)
		{
			copyright.setText(medium.getCopyright());
			copyright.setVisibility(View.VISIBLE);
			copyright.setAlpha(0.5f);
		}
		else
		{
			copyright.setVisibility(View.GONE);
		}

		/* Load the image */
		Picasso.with(getActivity())
			   .load(FileUtils.getFileForDatasource(getActivity(), datasourceId, medium.getInternalLink()))
			   .fit()
			   .centerCrop()
			   .into(imageView);
	}
}