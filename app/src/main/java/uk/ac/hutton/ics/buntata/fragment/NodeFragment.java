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

import android.content.res.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;

import java.util.*;

import butterknife.*;
import jhi.buntata.resource.*;
import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.activity.*;
import uk.ac.hutton.ics.buntata.adapter.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.database.manager.*;

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
	private List<BuntataNodeAdvanced> originalList;

	@BindView(R.id.node_recycler_view)
	RecyclerView recyclerView;

	private GridSpacingItemDecoration decoration;

	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		/* Get parameters */
		Bundle args = getArguments();
		datasourceId = args.getInt(PARAM_DATASOURCE_ID, -1);
		int parentId = args.getInt(PARAM_PARENT_ID, -1);

		NodeManager nodeManager = new NodeManager(getActivity(), datasourceId);

		/* Get the parent node */
		BuntataNodeAdvanced parent = nodeManager.getById(parentId);
		BuntataDatasource datasource = new DatasourceManager(getActivity(), datasourceId).getById(datasourceId);

		/* Inflate the layout */
		View view = inflater.inflate(R.layout.fragment_node, container, false);

		unbinder = ButterKnife.bind(this, view);

		recyclerView.setHasFixedSize(true);

		String title = getString(R.string.app_name);
		int parentMediaId = -1;

		/* If we don't have a parent, get all roots */
		if (parent == null)
		{
			originalList = nodeManager.getAllRoots();
		}
		/* Else get all the children of the parent */
		else
		{
			BuntataMediaAdvanced m = parent.getFirstImage();

			if (m != null)
				parentMediaId = m.getId();

			originalList = nodeManager.getForParent(parentId);

			if (datasource.isShowKeyName())
				title = parent.getName();
		}

		/* Set the name of the parent (if available) to the tool bar */
		ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (toolbar != null)
			toolbar.setTitle(title);

		/* Set the data to the adapter */
		adapter = new NodeAdapter(getActivity(), recyclerView, datasourceId, parentMediaId, originalList)
		{
			@Override
			public void onNodeClicked(View animationRoot, View title, BuntataMediaAdvanced medium, BuntataNodeAdvanced node)
			{
				((MainActivity) getActivity()).onFragmentChange(animationRoot, title, datasourceId, node.getId(), medium.getId());
			}
		};
		recyclerView.setAdapter(adapter);

		updateItemDecorator();

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		unbinder.unbind();
	}

	private void updateItemDecorator()
	{
		int columns = getResources().getInteger(R.integer.node_recyclerview_columns);
		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;
		recyclerView.setLayoutManager(new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL));

		if (decoration != null)
			recyclerView.removeItemDecoration(decoration);

		decoration = new GridSpacingItemDecoration(columns, valueInPixels, valueInPixels, valueInPixels);
		recyclerView.addItemDecoration(decoration);

		adapter.updateDimensions(columns, valueInPixels, valueInPixels, valueInPixels);
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

		if (filter == null)
			filter = "";

		filter(filter);
	}

	public void filter(String query)
	{
		adapter.getFilter().filter(query);
	}
}
