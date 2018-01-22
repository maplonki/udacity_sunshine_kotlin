/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.maplonki.sunshine

import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object SunshineDateUtils {
    val DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)

    fun getNormalizedUtcDateForToday(): Long {

        /*
         * This number represents the number of milliseconds that have elapsed since January
         * 1st, 1970 at midnight in the GMT time zone.
         */
        val utcNowMillis = System.currentTimeMillis()

        /*
         * This TimeZone represents the device's current time zone. It provides us with a means
         * of acquiring the offset for local time from a UTC time stamp.
         */
        val currentTimeZone = TimeZone.getDefault()

        /*
         * The getOffset method returns the number of milliseconds to add to UTC time to get the
         * elapsed time since the epoch for our current time zone. We pass the current UTC time
         * into this method so it can determine changes to account for daylight savings time.
         */
        val gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis).toLong()

        /*
         * UTC time is measured in milliseconds from January 1, 1970 at midnight from the GMT
         * time zone. Depending on your time zone, the time since January 1, 1970 at midnight (GMT)
         * will be greater or smaller. This variable represents the number of milliseconds since
         * January 1, 1970 (GMT) time.
         */
        val timeSinceEpochLocalTimeMillis = utcNowMillis + gmtOffsetMillis

        /* This method simply converts milliseconds to days, disregarding any fractional days */
        val daysSinceEpochLocal = TimeUnit.MILLISECONDS.toDays(timeSinceEpochLocalTimeMillis)

        /*
         * Finally, we convert back to milliseconds. This time stamp represents today's date at
         * midnight in GMT time. We will need to account for local time zone offsets when
         * extracting this information from the database.
         */

        return TimeUnit.DAYS.toMillis(daysSinceEpochLocal)
    }

    private fun elapsedDaysSinceEpoch(utcDate: Long) =
            TimeUnit.MILLISECONDS.toDays(utcDate)

    fun normalizeDate(date: Long) =
            elapsedDaysSinceEpoch(date) * DAY_IN_MILLIS

    fun isDateNormalized(millisSinceEpoch: Long) =
            millisSinceEpoch % DAY_IN_MILLIS == 0L


    private fun getLocalMidnightFromNormalizedUtcDate(normalizedUtcDate: Long): Long {
        /* The timeZone object will provide us the current user's time zone offset */
        val timeZone = TimeZone.getDefault()
        /*
         * This offset, in milliseconds, when added to a UTC date time, will produce the local
         * time.
         */
        val gmtOffset = timeZone.getOffset(normalizedUtcDate).toLong()
        return normalizedUtcDate - gmtOffset
    }

    fun getFriendlyDateString(context: Context, normalizedUtcMidnight: Long, showFullDate: Boolean): String {

        /*
         * NOTE: localDate should be localDateMidnightMillis and should be straight from the
         * database
         *
         * Since we normalized the date when we inserted it into the database, we need to take
         * that normalized date and produce a date (in UTC time) that represents the local time
         * zone at midnight.
         */
        val localDate = getLocalMidnightFromNormalizedUtcDate(normalizedUtcMidnight)

        /*
         * In order to determine which day of the week we are creating a date string for, we need
         * to compare the number of days that have passed since the epoch (January 1, 1970 at
         * 00:00 GMT)
         */
        val daysFromEpochToProvidedDate = elapsedDaysSinceEpoch(localDate)

        /*
         * As a basis for comparison, we use the number of days that have passed from the epoch
         * until today.
         */
        val daysFromEpochToToday = elapsedDaysSinceEpoch(System.currentTimeMillis())

        if (daysFromEpochToProvidedDate == daysFromEpochToToday || showFullDate) {
            /*
             * If the date we're building the String for is today's date, the format
             * is "Today, June 24"
             */
            val dayName = getDayName(context, localDate)
            val readableDate = getReadableDateString(context, localDate)
            if (daysFromEpochToProvidedDate - daysFromEpochToToday < 2) {
                /*
                 * Since there is no localized format that returns "Today" or "Tomorrow" in the API
                 * levels we have to support, we take the name of the day (from SimpleDateFormat)
                 * and use it to replace the date from DateUtils. This isn't guaranteed to work,
                 * but our testing so far has been conclusively positive.
                 *
                 * For information on a simpler API to use (on API > 18), please check out the
                 * documentation on DateFormat#getBestDateTimePattern(Locale, String)
                 * https://developer.android.com/reference/android/text/format/DateFormat.html#getBestDateTimePattern
                 */
                val localizedDayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(localDate)
                return readableDate.replace(localizedDayName, dayName)
            } else {
                return readableDate
            }
        } else if (daysFromEpochToProvidedDate < daysFromEpochToToday + 7) {
            /* If the input date is less than a week in the future, just return the day name. */
            return getDayName(context, localDate)
        } else {
            val flags = (DateUtils.FORMAT_SHOW_DATE
                    or DateUtils.FORMAT_NO_YEAR
                    or DateUtils.FORMAT_ABBREV_ALL
                    or DateUtils.FORMAT_SHOW_WEEKDAY)

            return DateUtils.formatDateTime(context, localDate, flags)
        }
    }

    private fun getReadableDateString(context: Context, timeInMillis: Long): String {
        val flags = (DateUtils.FORMAT_SHOW_DATE
                or DateUtils.FORMAT_NO_YEAR
                or DateUtils.FORMAT_SHOW_WEEKDAY)

        return DateUtils.formatDateTime(context, timeInMillis, flags)
    }

    private fun getDayName(context: Context, dateInMillis: Long): String {
        /*
         * If the date is today, return the localized version of "Today" instead of the actual
         * day name.
         */
        val daysFromEpochToProvidedDate = elapsedDaysSinceEpoch(dateInMillis)
        val daysFromEpochToToday = elapsedDaysSinceEpoch(System.currentTimeMillis())

        val daysAfterToday = (daysFromEpochToProvidedDate - daysFromEpochToToday).toInt()

        return when (daysAfterToday) {
            0 -> context.getString(R.string.today)
            1 -> context.getString(R.string.tomorrow)

            else -> {
                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                dayFormat.format(dateInMillis)
            }
        }
    }
}