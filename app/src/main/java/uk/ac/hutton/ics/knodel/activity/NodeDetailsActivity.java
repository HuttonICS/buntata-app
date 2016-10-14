package uk.ac.hutton.ics.knodel.activity;


import android.graphics.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.util.*;

import jhi.knodel.resource.*;
import me.relex.circleindicator.*;
import uk.ac.hutton.ics.knodel.R;
import uk.ac.hutton.ics.knodel.adapter.*;
import uk.ac.hutton.ics.knodel.database.entity.*;
import uk.ac.hutton.ics.knodel.database.manager.*;
import uk.ac.hutton.ics.knodel.util.*;

/**
 * @author Sebastian Raubach
 */
public class NodeDetailsActivity extends BaseActivity
{
	public static final String PARAM_DATASOURCE_ID = "datasourceId";
	public static final String PARAM_NODE_ID       = "nodeId";

	private static MediaManager MEDIA_MANAGER;

	private int datasourceId;
	private int nodeId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = getIntent().getExtras();

		datasourceId = args.getInt(PARAM_DATASOURCE_ID, -1);
		nodeId = args.getInt(PARAM_NODE_ID, -1);

		MEDIA_MANAGER = new MediaManager(this, datasourceId);

		ViewPager pager = (ViewPager) findViewById(R.id.node_details_image_pager);
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		CircleIndicator indicator = (CircleIndicator) findViewById(R.id.node_details_image_indicator);
		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.node_details_attributes);

		KnodelNode node = new NodeManager(this, datasourceId).getById(nodeId);

		setSupportActionBar(toolbar);

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			/* Set the title */
			getSupportActionBar().setTitle(node.getName());
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			{
				getSupportActionBar().setHomeButtonEnabled(true);
			}
		}

		/* Get all the media */
		List<KnodelMediaAdvanced> media = MEDIA_MANAGER.getForNode("Image", nodeId);
		media.add(media.get(0));

		/* Set to the pager */
		final ImagePagerAdapter adapter = new ImagePagerAdapter(getSupportFragmentManager(), datasourceId, media);
		pager.setAdapter(adapter);
		indicator.setViewPager(pager);

		/* Get all the attributes */
		List<KnodelAttributeValueAdvanced> attributeValues = new AttributeValueManager(this, datasourceId).getForNode(nodeId);

		/* Set them to the recycler view */
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new AttributeValueAdapter(this, datasourceId, recyclerView, attributeValues));

		/* Set the separator width */
		int valueInPixels = (int) getResources().getDimension(R.dimen.activity_vertical_margin) / 2;
		recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(valueInPixels));
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_node_details;
	}

	@Override
	protected Integer getToolbarId()
	{
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public class ImagePagerAdapter extends FragmentStatePagerAdapter
	{
		private final List<KnodelMediaAdvanced> dataset;
		private final int                       datasourceId;


		public ImagePagerAdapter(FragmentManager fm, int datasourceId, List<KnodelMediaAdvanced> dataset)
		{
			super(fm);
			this.datasourceId = datasourceId;
			this.dataset = dataset;
		}

		@Override
		public int getCount()
		{
			return dataset.size();
		}

		@Override
		public Fragment getItem(final int position)
		{
			return ImageFragment.newInstance(datasourceId, dataset.get(position).getId());
		}
	}

	public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration
	{
		private final int verticalSpaceHeight;

		public VerticalSpaceItemDecoration(int verticalSpaceHeight)
		{
			this.verticalSpaceHeight = verticalSpaceHeight;
		}

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
		{
			if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1)
			{
				outRect.bottom = verticalSpaceHeight;
			}
		}
	}

	public static class ImageFragment extends Fragment
	{
		private static final String PARAM_DATASOURCE_ID = "datasourceId";
		private static final String PARAM_MEDIUM_ID     = "mediumId";

		private int       datasourceId;
		private int       mediumId;
		private ImageView imageView;

		static ImageFragment newInstance(int datasourceId, int mediumId)
		{
			final ImageFragment f = new ImageFragment();
			final Bundle args = new Bundle();
			args.putInt(PARAM_DATASOURCE_ID, datasourceId);
			args.putInt(PARAM_MEDIUM_ID, mediumId);
			f.setArguments(args);
			return f;
		}

		public ImageFragment()
		{
		}

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			datasourceId = getArguments() != null ? getArguments().getInt(PARAM_DATASOURCE_ID) : -1;
			mediumId = getArguments() != null ? getArguments().getInt(PARAM_MEDIUM_ID) : -1;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState)
		{
			final View v = inflater.inflate(R.layout.fragment_node_image, container, false);
			imageView = (ImageView) v.findViewById(R.id.node_image_view);
			return v;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);

			Picasso.with(getActivity())
				   .load(FileUtils.getFileForDatasource(getActivity(), datasourceId, MEDIA_MANAGER.getById(mediumId).getInternalLink()))
				   .placeholder(R.drawable.missing_image)
				   .fit()
				   .centerCrop()
				   .into(imageView);
		}
	}
}
