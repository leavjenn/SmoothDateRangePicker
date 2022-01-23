/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.wsbak.smoothdaterangepicker.date;

import java.util.Calendar;

/**
 * Controller class to communicate among the various components of the date picker dialog.
 */
interface SmoothDateRangePickerController {

    void onYearSelected(int year);

    void onDayOfMonthSelected(int year, int month, int day);

    void onDurationChanged(int duration);

    void registerOnDateChangedListener(SmoothDateRangePickerFragment.OnDateChangedListener listener);

    void unregisterOnDateChangedListener(SmoothDateRangePickerFragment.OnDateChangedListener listener);

    MonthAdapter.CalendarDay getSelectedDay();

    boolean isThemeDark();
    
    Calendar[] getHighlightedDays();

    Calendar[] getSelectableDays();

    int getFirstDayOfWeek();

    int getMinYear();

    int getMinSelectableYear();

    int getMaxYear();

    Calendar getMinDate();

    Calendar getMinSelectableDate();

    Calendar getMaxDate();

    void tryVibrate();
}
