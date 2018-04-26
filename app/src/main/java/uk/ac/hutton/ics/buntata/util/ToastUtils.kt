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

package uk.ac.hutton.ics.buntata.util

import android.content.Context
import android.widget.Toast

/**
 * [uk.ac.hutton.ics.buntata.util.ToastUtils] contains utility functions for showing [Toast]s. Calling one of these functions will cancel
 * the currently shown [Toast] (if any) and show the new one.

 * @author Sebastian Raubach
 */
object ToastUtils {

    private var toast: Toast? = null

    /**
     * Creates a new toast message while canceling all old ones
     * @param context  The context to use. Usually your Application or Activity object.
     * *
     * @param text     The text to show. Can be formatted text.
     * *
     * @param duration How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
     */
    fun createToast(context: Context, text: CharSequence, duration: Int = Toast.LENGTH_LONG) {
        /* If there's already a toast, cancel it */
        toast?.cancel()

        /* Create and show the toast */
        toast = Toast.makeText(context, text, duration)
        toast?.show()
    }

    fun createToast(context: Context, text: Int, duration: Int = Toast.LENGTH_LONG) {
        createToast(context, context.getString(text), duration)
    }
}
