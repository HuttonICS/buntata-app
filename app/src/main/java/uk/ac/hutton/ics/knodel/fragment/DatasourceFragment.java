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
 * The {@link DatasourceFragment} shows all the {@link KnodelDatasource}s that are available locally and the ones available online (if connection
 * available).
 *
 * @author Sebastian Raubach
 */
public class DatasourceFragment extends Fragment
{
	private RecyclerView recyclerView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_datasource, container, false);

		recyclerView = (RecyclerView) view.findViewById(R.id.datasource_recycler_view);

		recyclerView.setHasFixedSize(false);

		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		/* Get the local data sources */
		final List<KnodelDatasource> localList = new DatasourceManager(getActivity(), -1).getAll();
		/* Keep track of their status (installed no update, installed update, not installed) */
		final Map<KnodelDatasource, DatasourceAdapter.InstallState> hasUpdate = new LinkedHashMap<>();

		/* Then try to get the online resources */
		DatasourceService.getAll(getActivity(), new RestletCallback<KnodelDatasourceList>(getActivity())
		{
			@Override
			public void onFailure(Exception caught)
			{
				caught.printStackTrace();

				/* If the request fails, just show the local ones as having no updates */
				for (KnodelDatasource ds : localList)
					hasUpdate.put(ds, DatasourceAdapter.InstallState.INSTALLED_NO_UPDATE);

				recyclerView.setAdapter(new DatasourceAdapter(getActivity(), hasUpdate));
			}

			@Override
			public void onSuccess(KnodelDatasourceList result)
			{
				/* If the request succeeds, try to figure out if it's already installed locally and then check if there's an update */
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
					/* Is not installed */
					else
					{
						hasUpdate.put(ds, DatasourceAdapter.InstallState.NOT_INSTALLED);
					}
				}

				/* Set whatever we got now to the adapter */
				recyclerView.setAdapter(new DatasourceAdapter(getActivity(), hasUpdate));
			}
		});

		return view;
	}
}
