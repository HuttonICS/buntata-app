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

package uk.ac.hutton.ics.buntata.fragment;

import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.io.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;
import uk.ac.hutton.ics.buntata.util.*;
import uk.co.senab.photoview.*;

/**
 * The {@link LogImageFragment} displays the information about an image medium.
 */
public class LogImageFragment extends Fragment
{
	private static final String PARAM_IMAGE_ID      = "imageId";
	private static final String PARAM_TRANSITION    = "transition";
	private static final String PARAM_IS_FULLSCREEN = "isFullscreen";

	private int     imageId;
	private boolean transition;

	@BindView(R.id.node_image_view)
	ImageView imageView;
	@BindView(R.id.node_image_copyright)
	TextView  copyright;

	private LogEntryImageManager imageManager;

	private Unbinder          unbinder;
	private PhotoViewAttacher photoViewAttacher;

	public static LogImageFragment newInstance(int imageId, boolean transition, boolean isFullscreen)
	{
		final LogImageFragment f = new LogImageFragment();

		/* Pass parameters */
		Bundle args = new Bundle();
		args.putInt(PARAM_IMAGE_ID, imageId);
		args.putBoolean(PARAM_TRANSITION, transition);
		args.putBoolean(PARAM_IS_FULLSCREEN, isFullscreen);
		f.setArguments(args);

		return f;
	}

	public LogImageFragment()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		/* Get parameters */
		imageId = getArguments() != null ? getArguments().getInt(PARAM_IMAGE_ID) : -1;
		transition = getArguments() != null ? getArguments().getBoolean(PARAM_TRANSITION, false) : false;
		imageManager = new LogEntryImageManager(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_node_image, container, false);

		unbinder = ButterKnife.bind(this, view);

		if (transition && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			imageView.setTransitionName(getString(R.string.transition_node_image_view));

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		unbinder.unbind();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		/* Get the medium object */
		final LogEntryImage image = imageManager.getById(imageId);

		copyright.setVisibility(View.GONE);

		/* Load the image */
		Picasso.get()
			   .load(new File(image.getPath()))
			   .fit()
			   .centerCrop()
			   .into(imageView, new Callback()
			   {
				   @Override
				   public void onSuccess()
				   {
					   if (imageView != null)
					   {
						   photoViewAttacher = new PhotoViewAttacher(imageView);
						   photoViewAttacher.setOnLongClickListener(new View.OnLongClickListener()
						   {
							   @Override
							   public boolean onLongClick(View view)
							   {
								   if (getActivity() instanceof LogDetailsActivity)
								   {
									   DialogUtils.showDialog(getActivity(), R.string.dialog_delete_image_title, R.string.dialog_delete_image_message, R.string.generic_yes, R.string.generic_no, new DialogInterface.OnClickListener()
									   {
										   @Override
										   public void onClick(DialogInterface dialogInterface, int i)
										   {
											   imageManager.delete(image);
											   ((LogDetailsActivity) getActivity()).updateImageSection();
										   }
									   }, null);
								   }

								   return true;
							   }
						   });
					   }
				   }

				   @Override
				   public void onError(Exception e)
				   {
					   System.out.println("");
				   }
			   });
	}

	public void cleanup()
	{
		if (photoViewAttacher != null)
			photoViewAttacher.cleanup();
	}
}