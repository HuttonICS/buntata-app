package uk.ac.hutton.ics.knodel.fragment;

import android.view.*;

public interface OnFragmentChangeListener
{
	void onFragmentChange(View transitionRoot, int datasourceId, int parentId);

	void onReset();
}