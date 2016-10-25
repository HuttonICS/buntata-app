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

import android.annotation.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import java.io.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.activity.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * The {@link EulaFragment} shows information about all the libraries that are used by this app and their licenses.
 *
 * @author Sebastian Raubach
 */
public class EulaFragment extends Fragment
{
	private ListView          list;
	private WebView           webView;
	private Button            accept;
	private Button            cancel;
	private CoordinatorLayout buttonBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_eula, container, false);

		list = (ListView) view.findViewById(R.id.eula_list);
		webView = (WebView) view.findViewById(R.id.eula_webview);
		accept = (Button) view.findViewById(R.id.eula_button_accept);
		cancel = (Button) view.findViewById(R.id.eula_button_cancel);
		buttonBar = (CoordinatorLayout) view.findViewById(R.id.eula_button_bar);

		String[] licenseTypes = {getActivity().getString(EulaUtils.EulaType.CONSUMER.getTextResource()), getActivity().getString(EulaUtils.EulaType.COMMERCIAL.getTextResource())};

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.helper_list_view_text_color, licenseTypes);

		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position)
				{
					case 0:
						/* Consumer */
						showEulaForType(EulaUtils.EulaType.CONSUMER);
						break;
					case 1:
						/* Commercial */
						showEulaForType(EulaUtils.EulaType.COMMERCIAL);
						break;
				}
			}
		});

		accept.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				PreferenceUtils.setPreferenceAsBoolean(getActivity(), PreferenceUtils.PREFS_EULA_ACCEPTED, true);
				((IntroductionActivity) getActivity()).nextSlide();
			}
		});
		cancel.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getActivity().finish();
			}
		});

		return view;
	}

	private void showEulaForType(EulaUtils.EulaType type)
	{
		webView.setVisibility(View.VISIBLE);

		int resource = -1;
		switch (type)
		{
			case COMMERCIAL:
				resource = R.raw.eula_commercial;
				break;
			case CONSUMER:
				resource = R.raw.eula_consumer;
				break;
		}

		String prompt = "";
		try
		{
			InputStream inputStream = getResources().openRawResource(resource);
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			prompt = new String(buffer);
			inputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		webView.loadDataWithBaseURL(null, prompt, "text/html", "utf-8", null);

		webView.setWebViewClient(new WebViewClient()
		{
			public void onPageFinished(WebView view, String url)
			{
				buttonBar.setVisibility(View.VISIBLE);
			}

			@SuppressWarnings("deprecation")
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				return handleUri(url);
			}

			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
			{
				return handleUri(request.getUrl().toString());
			}

			private boolean handleUri(String url)
			{
				if (url.startsWith("http:") || url.startsWith("https:"))
				{
					return false;
				}

				try
				{
					// Otherwise allow the OS to handle things like tel, mailto, etc.
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					getActivity().startActivity(intent);
					return true;
				}
				catch (ActivityNotFoundException e)
				{
					return false;
				}
			}
		});
	}
}
