/*
 * Copyright 2018 Information & Computational Sciences, The James Hutton Institute
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

import android.text.*;

public class InputFilterMinMax implements InputFilter
{
	private float min;
	private float max;

	public InputFilterMinMax(float min, float max)
	{
		this.min = min;
		this.max = max;
	}

	public InputFilterMinMax(String min, String max)
	{
		this.min = Float.parseFloat(min);
		this.max = Float.parseFloat(max);
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
	{
		String newValue = dest.toString().substring(0, dstart) + source.toString().substring(start, end) + dest.toString().substring(dend, dest.length());

		try
		{
			float input = Float.parseFloat(newValue);
			if (isInRange(min, max, input))
				return null;
		}
		catch (NumberFormatException nfe)
		{
			if ("-".equals(newValue))
				return null;
		}
		return "";
	}

	private boolean isInRange(float a, float b, float c)
	{
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}
}