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

package uk.ac.hutton.ics.knodel.database;

import android.content.*;

import java.text.*;

import jhi.knodel.resource.*;

/**
 * The {@link DatabaseObjectParser} is an interface that defines how a {@link DatabaseObject} should be parsed
 *
 * @author Sebastian Raubach
 */
public abstract class DatabaseObjectParser<T extends DatabaseObject>
{
	/**
	 * Called when a {@link DatabaseObject} needs to be parsed from the {@link uk.ac.hutton.ics.knodel.database.DatabaseInternal.AdvancedCursor}
	 *
	 * @param context      The {@link Context}
	 * @param datasourceId The current datasource id
	 * @param cursor       The {@link uk.ac.hutton.ics.knodel.database.DatabaseInternal.AdvancedCursor}
	 * @return The parsed {@link DatabaseObject}
	 * @throws ParseException Thrown if the parsing fails
	 */
	public abstract T parse(Context context, int datasourceId, DatabaseInternal.AdvancedCursor cursor) throws ParseException;
}
