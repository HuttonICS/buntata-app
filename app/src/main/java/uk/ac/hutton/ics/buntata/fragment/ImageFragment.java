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

package uk.ac.hutton.ics.buntata.fragment;

import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.util.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.util.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.util.*;
import uk.co.senab.photoview.*;

/**
 * The {@link ImageFragment} displays the information about an image medium.
 */
public class ImageFragment extends Fragment
{
	private static final String PARAM_DATASOURCE_ID = "datasourceId";
	private static final String PARAM_MEDIUM_ID     = "mediumId";
	private static final String PARAM_TRANSITION    = "transition";
	private static final String PARAM_NODE_ID       = "nodeId";
	private static final String PARAM_IS_FULLSCREEN = "isFullscreen";

	private MediaManager mediaManager;

	private int     datasourceId;
	private int     mediumId;
	private boolean transition;
	private int     nodeId;
	private boolean isFullscreen;

	@BindView(R.id.node_image_view)
	ImageView imageView;
	@BindView(R.id.node_image_copyright)
	TextView  copyright;

	private Unbinder          unbinder;
	private PhotoViewAttacher photoViewAttacher;

	public static ImageFragment newInstance(int datasourceId, int nodeId, boolean transition, int mediumId, boolean isFullscreen)
	{
		final ImageFragment f = new ImageFragment();

		/* Pass parameters */
		Bundle args = new Bundle();
		args.putInt(PARAM_DATASOURCE_ID, datasourceId);
		args.putInt(PARAM_MEDIUM_ID, mediumId);
		args.putBoolean(PARAM_TRANSITION, transition);
		args.putInt(PARAM_NODE_ID, nodeId);
		args.putBoolean(PARAM_IS_FULLSCREEN, isFullscreen);
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
		transition = getArguments() != null ? getArguments().getBoolean(PARAM_TRANSITION, false) : false;
		nodeId = getArguments() != null ? getArguments().getInt(PARAM_NODE_ID) : -1;
		isFullscreen = getArguments() != null ? getArguments().getBoolean(PARAM_IS_FULLSCREEN, false) : false;
		mediaManager = new MediaManager(getActivity(), datasourceId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_node_image, container, false);

		unbinder = ButterKnife.bind(this, view);

		if (transition && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			imageView.setTransitionName(getString(R.string.transition_node_image_view));
		}

		if (!isFullscreen)
		{
			imageView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					/* If it's a leaf node, open the details activity */
					Intent intent = new Intent(getActivity().getApplicationContext(), ImageViewPagerActivity.class);

					/* Depending on the android version, transition views or just slide */
					List<Pair<View, String>> pairs = new ArrayList<>();
					pairs.add(Pair.create(view, getString(R.string.transition_node_details_view)));
					pairs.add(Pair.create(view, getString(R.string.transition_node_image_view)));

					/* Pass parameters */
					Bundle args = new Bundle();
					args.putInt(NodeDetailsActivity.PARAM_DATASOURCE_ID, datasourceId);
					args.putInt(NodeDetailsActivity.PARAM_PREFERED_FIRST_MEDIUM, mediumId);
					args.putInt(NodeDetailsActivity.PARAM_NODE_ID, nodeId);
					intent.putExtras(args);

					ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pairs.toArray(new Pair[pairs.size()]));

					startActivity(intent, options.toBundle());
				}
			});
		}

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		unbinder.unbind();
	}

//	@Override
//	public void onStop()
//	{
//		super.onStop();
//
//		if(attacher != null)
//			attacher.cleanup();
//	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		/* Get the medium object */
		BuntataMediaAdvanced medium = mediaManager.getById(mediumId);

			/* Show copyright information if available */
		if (medium.getCopyright() != null)
		{
			copyright.setText(getString(R.string.datasource_details_image_copyright, medium.getCopyright()));
			copyright.setVisibility(View.VISIBLE);
			copyright.setAlpha(0.75f);
		}
		else
		{
			copyright.setVisibility(View.GONE);
		}

		/* Load the image */
		RequestCreator p = Picasso.get()
								  .load(FileUtils.getFileForDatasource(getActivity(), datasourceId, medium.getInternalLink()))
								  .fit();

		if (isFullscreen)
			p.centerInside();
		else
			p.centerCrop();

		p.into(imageView, new Callback()
		{
			@Override
			public void onSuccess()
			{
				if (isFullscreen && imageView != null)
					photoViewAttacher = new PhotoViewAttacher(imageView);
			}

			@Override
			public void onError(Exception e)
			{
			}
		});
	}

	public void cleanup()
	{
		if (photoViewAttacher != null)
			photoViewAttacher.cleanup();
	}
}