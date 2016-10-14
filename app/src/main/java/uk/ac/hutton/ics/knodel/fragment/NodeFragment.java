package uk.ac.hutton.ics.knodel.fragment;

import android.app.*;
import android.os.*;
import android.support.v4.app.Fragment;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;

import java.util.*;

import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.activity.*;
import uk.ac.hutton.ics.knodel.adapter.*;
import uk.ac.hutton.ics.knodel.database.entity.*;
import uk.ac.hutton.ics.knodel.database.manager.*;

/**
 * @author Sebastian Raubach
 */
public class NodeFragment extends Fragment
{
	public static final String PARAM_DATASOURCE_ID = "datasourceId";
	public static final String PARAM_PARENT_ID     = "parentId";

	private RecyclerView recyclerView;

	private int datasourceId;
	private int parentId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Bundle args = getArguments();
		datasourceId = args.getInt(PARAM_DATASOURCE_ID, -1);
		parentId = args.getInt(PARAM_PARENT_ID, -1);

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_node, container, false);

		recyclerView = (RecyclerView) view.findViewById(R.id.node_recycler_view);

		// use this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		recyclerView.setHasFixedSize(true);

		int columns = getResources().getInteger(R.integer.node_recyclerview_columns);
		recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));

		List<KnodelNodeAdvanced> nodes;

		if (parentId == -1)
			nodes = new NodeManager(getActivity(), datasourceId).getAllRoots();
		else
			nodes = new NodeManager(getActivity(), datasourceId).getForParent(parentId);

		recyclerView.setAdapter(new NodeAdapter(getActivity(), datasourceId, nodes)
		{
			@Override
			public void onNodeClicked(View animationRoot, KnodelNodeAdvanced node)
			{
				((MainActivity) getActivity()).onFragmentChange(animationRoot, datasourceId, node.getId());
			}
		});

		return view;
	}
}
