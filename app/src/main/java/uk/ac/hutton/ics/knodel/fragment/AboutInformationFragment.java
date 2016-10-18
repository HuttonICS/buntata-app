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

import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v7.widget.*;
import android.view.*;

import uk.ac.hutton.ics.knodel.*;

/**
 * The {@link AboutInformationFragment} shows information about the app in general.
 *
 * @author Sebastian Raubach
 */
public class AboutInformationFragment extends Fragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_about_information, container, false);

		CardView email = (CardView) view.findViewById(R.id.about_information_email);
		CardView share = (CardView) view.findViewById(R.id.about_information_share);
		CardView play = (CardView) view.findViewById(R.id.about_information_google_play);

		email.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ShareCompat.IntentBuilder.from(getActivity())
										 .setType("message/rfc822")
										 .addEmailTo(getString(R.string.contact_email_address))
										 .setSubject(getString(R.string.contact_email_subject))
										 .setChooserTitle(R.string.contact_email_dialog_title)
										 .startChooser();
			}
		});
		share.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ShareCompat.IntentBuilder.from(getActivity())
										 .setText("text/plain")
										 .setSubject(getString(R.string.contact_email_subject))
										 .setText(getString(R.string.google_play_url))
										 .setChooserTitle(R.string.share_chooser_title)
										 .startChooser();
			}
		});
		play.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_url))));
			}
		});

		return view;
	}
}
