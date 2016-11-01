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

import android.animation.*;
import android.annotation.*;
import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link IntroNetworkFragment} ensures that a network connection is available.
 *
 * @author Sebastian Raubach
 */
public class IntroNetworkFragment extends Fragment
{
	private static final int REQUEST_CODE_NETWORK = 3000;

	@BindView(R.id.network_status_image)
	ImageView networkStatusImage;
	@BindView(R.id.network_status_message)
	TextView  networkStatusMessage;
	@BindView(R.id.network_status_settings)
	Button    networkStatusSettings;
	@BindView(R.id.network_status_refresh)
	Button    networkStatusRefresh;

	private Animator anim;

	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_intro_network, container, false);

		unbinder = ButterKnife.bind(this, view);

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		unbinder.unbind();
	}

	@OnClick(R.id.network_status_settings)
	public void onSettingsClicked()
	{
		/* When 'settings' is clicked, open the wireless settings */
		startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), REQUEST_CODE_NETWORK);
	}

	@OnClick(R.id.network_status_refresh)
	public void onRefreshClicked()
	{
		/* When 'refresh' is clicked, refresh the state */
		updateStatus();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case REQUEST_CODE_NETWORK:
				updateStatus();
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Updates the status by checking the network connection and updating the icons and text
	 */
	private void updateStatus()
	{
		final int imageResource;
		final int message;
		final int buttonVisibility;
		if (!NetworkUtils.hasNetworkConnection(getActivity()))
		{
			imageResource = R.drawable.status_network_unavailable;
			message = R.string.network_intro_status_unavailable;
			buttonVisibility = View.VISIBLE;
		}
		else
		{
			imageResource = R.drawable.status_network_available;
			message = R.string.network_intro_status_available;
			buttonVisibility = View.GONE;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			/* Show a circular hide/reveal animation on lollipop and up */
			circularHideView(networkStatusImage, new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					networkStatusImage.setImageResource(imageResource);
					networkStatusMessage.setText(message);
					networkStatusSettings.setVisibility(buttonVisibility);
					circularShowView(networkStatusImage);
				}
			});
		}
		else
		{
			/* Just change the drawable on pre-lollipop */
			networkStatusImage.setImageResource(imageResource);
			networkStatusMessage.setText(message);
			networkStatusSettings.setVisibility(buttonVisibility);
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void circularShowView(final View view)
	{
		if (anim != null && anim.isRunning())
			anim.end();

		/* set the center for the clipping circle */
		int cx = view.getWidth() / 2;
		int cy = view.getHeight();

		/* get the final radius for the clipping circle */
		float finalRadius = (float) Math.hypot(cx, cy);

		/* create the animator for this view (the start radius is zero) */
		anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

		/* make the view visible and start the animation */
		view.setVisibility(View.VISIBLE);

		anim.start();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void circularHideView(final View view, final AnimatorListenerAdapter listener)
	{
		if (anim != null && anim.isRunning())
			anim.end();

		/* get the center for the clipping circle */
		int cx = view.getWidth() / 2;
		int cy = view.getHeight();

		/* get the initial radius for the clipping circle */
		float initialRadius = (float) Math.hypot(cx, cy);

		/* create the animation (the final radius is zero) */
		anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);

		/* make the view invisible when the animation is done */
		anim.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				super.onAnimationEnd(animation);
				view.setVisibility(View.INVISIBLE);

				if (listener != null)
				{
					listener.onAnimationEnd(animation);
				}
			}
		});

		anim.start();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser)
		{
			updateStatus();
		}
	}
}
