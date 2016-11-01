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

package uk.ac.hutton.ics.buntata.util;


import android.graphics.*;
import android.support.v7.graphics.*;
import android.util.LruCache;

import com.squareup.picasso.*;


public final class PaletteTransformation implements Transformation
{
	private static final PaletteTransformation INSTANCE = new PaletteTransformation();

	/**
	 * We're using a {@link LruCache} here to restrict the number of Bitmaps/Palettes we store. The {@link LruCache} will keep frequently used items
	 * while removing less frequently used items using the Least Recently Used strategy.
	 */
	private static final LruCache<Bitmap, Palette> CACHE = new LruCache<Bitmap, Palette>(20) // Limit to 20 Bitmaps/Palettes
	{
		@Override
		protected Palette create(Bitmap key)
		{
			return Palette.from(key).generate();
		}
	};

	public static PaletteTransformation instance()
	{
		return INSTANCE;
	}

	public static Palette getPalette(Bitmap bitmap)
	{
		return CACHE.get(bitmap);
	}

	private PaletteTransformation()
	{
	}

	@Override
	public Bitmap transform(Bitmap source)
	{
		/* Call get here, it it's in the CACHE already, this is a NO-OP, otherwise it will be created in the LruCache#create method */
		CACHE.get(source);
		return source;
	}

	@Override
	public String key()
	{
		return ""; // Stable key for all requests. An unfortunate requirement.
	}
}