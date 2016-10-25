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

import android.content.res.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;

import java.util.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.activity.*;
import uk.ac.hutton.ics.knodel.adapter.*;
import uk.ac.hutton.ics.knodel.database.entity.*;
import uk.ac.hutton.ics.knodel.database.manager.*;

/**
 * The {@link NodeFragment} shows the nodes in a grid.
 *
 * @author Sebastian Raubach
 */
public class NodeFragment extends Fragment
{
	public static final String PARAM_DATASOURCE_ID = "datasourceId";
	public static final String PARAM_PARENT_ID     = "parentId";

	private int datasourceId;

	private NodeAdapter               adapter;
	private List<KnodelNodeAdvanced>  originalList;
	private RecyclerView              recyclerView;
	private GridSpacingItemDecoration decoration;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		/* Get parameters */
		Bundle args = getArguments();
		datasourceId = args.getInt(PARAM_DATASOURCE_ID, -1);
		int parentId = args.getInt(PARAM_PARENT_ID, -1);

		NodeManager nodeManager = new NodeManager(getActivity(), datasourceId);

		/* Get the parent node */
		KnodelNodeAdvanced parent = nodeManager.getById(parentId);

		/* Inflate the layout */
		View view = inflater.inflate(R.layout.fragment_node, container, false);

		recyclerView = (RecyclerView) view.findViewById(R.id.node_recycler_view);
		recyclerView.setHasFixedSize(true);

		updateItemDecorator();

		String title = getString(R.string.app_name);

		/* If we don't have a parent, get all roots */
		if (parent == null)
		{
			originalList = nodeManager.getAllRoots();
		}
		/* Else get all the children of the parent */
		else
		{
			originalList = nodeManager.getForParent(parentId);

			title = parent.getName();
		}

		/* Set the name of the parent (if available) to the tool bar */
		ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (toolbar != null)
		{
			toolbar.setTitle(title);
		}

		/* Set the data to the adapter */
		adapter = new NodeAdapter(getActivity(), datasourceId, originalList)
		{
			@Override
			public void onNodeClicked(View animationRoot, KnodelNodeAdvanced node)
			{
				((MainActivity) getActivity()).onFragmentChange(animationRoot, datasourceId, node.getId());
			}
		};
		recyclerView.setAdapter(adapter);

		return view;
	}

	private void updateItemDecorator()
	{
		int columns = getResources().getInteger(R.integer.node_recyclerview_columns);
		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;
		recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));

		if (decoration != null)
			recyclerView.removeItemDecoration(decoration);

		decoration = new GridSpacingItemDecoration(columns, valueInPixels, valueInPixels, valueInPixels);
		recyclerView.addItemDecoration(decoration);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		updateItemDecorator();
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		String filter = ((MainActivity) getActivity()).getFilter();

		if(filter == null)
			filter = "";


		filter(filter);
	}

	public void filter(String query)
	{
		adapter.getFilter().filter(query);
	}
}
