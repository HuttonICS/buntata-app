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

package uk.ac.hutton.ics.buntata.activity;

import android.os.*;
import android.widget.*;

import java.io.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.R;

/**
 * The {@link ApacheLicenseActivity} just shows the Apache License, Version 2.0 for compliance reasons. That's it. Nothing to see here.
 *
 * @author Sebastian Raubach
 */
public class ApacheLicenseActivity extends BaseActivity
{
	@BindView(R.id.license_apache)
	TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		/* Read the file and set to the text view with line breaks */
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(getAssets().open("LICENSE-2.0.txt"), "UTF-8"));

			StringBuilder builder = new StringBuilder();
			String mLine;
			while ((mLine = reader.readLine()) != null)
			{
				builder.append(mLine)
					   .append("\n");
			}

			textView.setText(builder.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_apachelicense;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}
}
