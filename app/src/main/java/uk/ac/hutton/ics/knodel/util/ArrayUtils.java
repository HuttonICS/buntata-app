/**
 * Germinate Mobile is written and developed by Sebastian Raubach, Paul Shaw and David Marshall from the Information and Computational Sciences Group
 * at JHI Dundee. For further information contact us at germinate@hutton.ac.uk or visit our webpages at http://ics.hutton.ac.uk/germinate-scan/
 * <p/>
 * Copyright (c) 2012-2016, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package uk.ac.hutton.ics.knodel.util;

/**
 * @author Sebastian Raubach
 */
public class ArrayUtils
{
	public static <T> boolean isEmpty(T... array)
	{
		if (array == null || array.length < 1)
			return true;
		else
		{
			for (T anArray : array)
			{
				if (anArray != null)
					return false;
			}

			return true;
		}
	}
}
