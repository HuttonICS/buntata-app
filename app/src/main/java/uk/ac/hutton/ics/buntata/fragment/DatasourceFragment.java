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

import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.heinrichreimersoftware.materialintro.app.*;

import java.util.*;

import butterknife.*;
import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.R;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.adapter.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.service.*;

/**
 * The {@link DatasourceFragment} shows all the {@link BuntataDatasource}s that are available locally and the ones available online (if connection
 * available).
 *
 * @author Sebastian Raubach
 */
public class DatasourceFragment extends Fragment
{
	@BindView(R.id.datasource_text)
	TextView     text;
	@BindView(R.id.datasource_recycler_view)
	RecyclerView recyclerView;
	@BindView(R.id.datasource_network_warning)
	TextView     networkWarning;
	private DatasourceAdapter adapter;

	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_datasource, container, false);

		unbinder = ButterKnife.bind(this, view);

		setRetainInstance(true);

		if (getActivity() instanceof IntroActivity)
			text.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.white));

		recyclerView.setHasFixedSize(false);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setItemAnimator(null);

		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;
		recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, valueInPixels, valueInPixels, valueInPixels));

		/* If  this is part of the DatasourceActivity, then load the content here */
		if (getActivity() instanceof DatasourceActivity)
			updateStatus();

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		unbinder.unbind();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

		/* If this is part of the IntroActivity, then only load the content once this is visible. Otherwise it'll load this fragment already on the previous page. */
		if (isVisibleToUser && getActivity() instanceof IntroActivity)
			updateStatus();
	}

	private void updateStatus()
	{
//		if (!NetworkUtils.hasNetworkConnection(getActivity()))
//		{
//			networkWarning.setVisibility(View.VISIBLE);
//		}
//		else
//		{
		networkWarning.setVisibility(View.GONE);
		requestData();
//		}
	}

	private void requestData()
	{
		final boolean cancelable = getActivity() instanceof DatasourceActivity;

		final List<BuntataDatasourceAdvanced> datasources = new ArrayList<>();

		/* Set it initially */
		adapter = new DatasourceAdapter(getActivity(), recyclerView, datasources);
		recyclerView.setAdapter(adapter);

		DatasourceService.getAllAdvanced(getActivity(), cancelable, true, new RemoteCallback<List<BuntataDatasourceAdvanced>>(getActivity())
		{
			@Override
			public void onSuccess(List<BuntataDatasourceAdvanced> result)
			{
				if (result.size() < 1)
				{
					networkWarning.setVisibility(View.VISIBLE);
				}
				else
				{
					networkWarning.setVisibility(View.GONE);

					adapter = new DatasourceAdapter(getActivity(), recyclerView, result);
					recyclerView.setAdapter(adapter);
				}
			}
		});
	}
}
