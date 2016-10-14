package uk.ac.hutton.ics.knodel.fragment;

import android.os.*;
import android.support.v4.app.*;
import android.support.v7.widget.*;
import android.view.*;

import java.util.*;

import jhi.knodel.resource.*;
import uk.ac.hutton.ics.knodel.*;
import uk.ac.hutton.ics.knodel.adapter.*;
import uk.ac.hutton.ics.knodel.database.manager.*;
import uk.ac.hutton.ics.knodel.service.*;

/**
 * @author Sebastian Raubach
 */
public class DatasourceFragment extends Fragment
{
	private RecyclerView recyclerView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_datasource, container, false);

		recyclerView = (RecyclerView) view.findViewById(R.id.datasource_recycler_view);

		recyclerView.setHasFixedSize(false);

		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		// TODO: Don't just show the online resources, this should show the offline resources and online (if connection), then compare dates to see if it can be updated

		final List<KnodelDatasource> localList = new DatasourceManager(getActivity(), -1).getAll();
		final Map<KnodelDatasource, DatasourceAdapter.InstallState> hasUpdate = new LinkedHashMap<>();

		DatasourceService.getAll(getActivity(), new RestletCallback<KnodelDatasourceList>(getActivity())
		{
			@Override
			public void onFailure(Exception caught)
			{
				caught.printStackTrace();

				for (KnodelDatasource ds : localList)
					hasUpdate.put(ds, DatasourceAdapter.InstallState.INSTALLED_NO_UPDATE);

				recyclerView.setAdapter(new DatasourceAdapter(getActivity(), hasUpdate));
			}

			@Override
			public void onSuccess(KnodelDatasourceList result)
			{
				for (KnodelDatasource ds : result.getList())
				{
					int index = localList.indexOf(ds);

					/* Is installed */
					if (index != -1)
					{
						KnodelDatasource old = localList.get(index);

						boolean isNewer = DatasourceManager.isNewer(ds, old);

						if (isNewer)
							hasUpdate.put(ds, DatasourceAdapter.InstallState.INSTALLED_HAS_UPDATE);
						else
							hasUpdate.put(ds, DatasourceAdapter.InstallState.INSTALLED_NO_UPDATE);
					}
					else
					{
						hasUpdate.put(ds, DatasourceAdapter.InstallState.NOT_INSTALLED);
					}
				}

				recyclerView.setAdapter(new DatasourceAdapter(getActivity(), hasUpdate));
			}
		});

		return view;
	}
}
