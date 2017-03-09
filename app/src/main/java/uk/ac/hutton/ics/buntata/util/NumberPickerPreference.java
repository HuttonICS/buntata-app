/*
 * Copyright 2017 Information & Computational Sciences, The James Hutton Institute
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

package uk.ac.hutton.ics.buntata.util;

import android.content.*;
import android.content.res.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.widget.*;

/**
 * A {@link android.preference.Preference} that displays a number picker as a dialog.
 */
public class NumberPickerPreference extends DialogPreference
{

	// allowed range
	public int maxValue = 100;
	public int minValue = 0;

	private NumberPicker picker;
	private int          value;

	public NumberPickerPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		processAttributeSet(attrs);
	}

	public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		processAttributeSet(attrs);
	}

	private void processAttributeSet(AttributeSet attrs)
	{
		//This method reads the parameters given in the xml file and sets the properties according to it
		this.minValue = attrs.getAttributeIntValue(null, "min", 0);
		this.maxValue = attrs.getAttributeIntValue(null, "max", 0);
	}

	@Override
	protected View onCreateDialogView()
	{
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;

		picker = new NumberPicker(getContext());
		picker.setLayoutParams(layoutParams);

		FrameLayout dialogView = new FrameLayout(getContext());
		dialogView.addView(picker);

		return dialogView;
	}

	@Override
	protected void onBindDialogView(View view)
	{
		super.onBindDialogView(view);
		picker.setMinValue(minValue);
		picker.setMaxValue(maxValue);
		picker.setValue(getValue());
		picker.setWrapSelectorWheel(false);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		if (positiveResult)
		{
			picker.clearFocus();
			int newValue = picker.getValue();
			if (callChangeListener(newValue))
			{
				setValue(newValue);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return a.getInt(index, minValue);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
	{
		setValue(restorePersistedValue ? getPersistedInt(minValue) : (Integer) defaultValue);
	}

	public void setValue(int value)
	{
		this.value = value;
		persistInt(this.value);
	}

	public int getValue()
	{
		return this.value;
	}
}