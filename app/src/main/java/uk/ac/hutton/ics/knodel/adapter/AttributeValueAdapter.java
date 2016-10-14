package uk.ac.hutton.ics.knodel.adapter;

import android.content.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import com.transitionseverywhere.*;

import java.util.*;

import uk.ac.hutton.ics.knodel.R;
import uk.ac.hutton.ics.knodel.database.entity.*;

public class AttributeValueAdapter extends RecyclerView.Adapter<AttributeValueAdapter.ViewHolder>
{
	private Context                            context;
	private int                                datasourceId;
	private RecyclerView                       parent;
	private List<KnodelAttributeValueAdvanced> dataset;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		// each data item is just a string in this case
		View      view;
		TextView  title;
		TextView  content;
		ImageView expandIcon;

		ViewHolder(View v)
		{
			super(v);

			view = v;
			title = (TextView) v.findViewById(R.id.attribute_view_title);
			content = (TextView) v.findViewById(R.id.attribute_view_content);
			expandIcon = (ImageView) v.findViewById(R.id.attribute_view_expand_icon);
		}
	}

	public AttributeValueAdapter(Context context, int datasourceId, RecyclerView parent, List<KnodelAttributeValueAdvanced> dataset)
	{
		this.context = context;
		this.datasourceId = datasourceId;
		this.parent = parent;
		this.dataset = dataset;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		/* Create a new view from the layout file */
		final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attribute_view, parent, false);

		final ViewHolder viewHolder = new ViewHolder(v);

		return viewHolder;
	}

	private int mExpandedPosition = -1;

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position)
	{
		KnodelAttributeValueAdvanced item = dataset.get(position);

		final boolean isExpanded = position == mExpandedPosition;
		holder.content.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
		holder.itemView.setActivated(isExpanded);
		holder.itemView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mExpandedPosition = isExpanded ? -1 : position;
				TransitionManager.beginDelayedTransition(parent);
				notifyDataSetChanged();
			}
		});

		holder.title.setText(item.getAttribute().getName());
		holder.content.setText(item.getValue());
	}

	@Override
	public int getItemCount()
	{
		return dataset.size();
	}
}