package uk.ac.hutton.ics.knodel.util;


import android.graphics.*;
import android.support.v7.graphics.*;

import com.squareup.picasso.*;

import java.util.*;

public final class PaletteTransformation implements Transformation
{
	private static final PaletteTransformation INSTANCE = new PaletteTransformation();
	private static final Map<Bitmap, Palette>  CACHE    = new WeakHashMap<>();

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
		Palette palette = Palette.from(source).generate();
		CACHE.put(source, palette);
		return source;
	}

	@Override
	public String key()
	{
		return ""; // Stable key for all requests. An unfortunate requirement.
	}
}