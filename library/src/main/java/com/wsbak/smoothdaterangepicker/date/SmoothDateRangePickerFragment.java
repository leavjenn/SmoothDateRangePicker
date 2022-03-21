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

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.wsbak.smoothdaterangepicker.HapticFeedbackController;
import com.wsbak.smoothdaterangepicker.R;
import com.wsbak.smoothdaterangepicker.TypefaceHelper;
import com.wsbak.smoothdaterangepicker.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * Dialog allowing users to select a date.
 */
public class SmoothDateRangePickerFragment extends DialogFragment implements OnClickListener,
        SmoothDateRangePickerController {

    private static final String TAG = "SmoothDateRangePickerFragment";

    static class DateData implements OnClickListener
    {
        public DateData(SmoothDateRangePickerFragment fragment,
                        String bundleKeyPrefix,
                        int yearViewIndex,
                        int monthAndDayViewIndex)
        {
            mFragment = fragment;
            mBundleKeyPrefix = bundleKeyPrefix;
            mYearViewIndex = yearViewIndex;
            mMonthAndDayViewIndex = monthAndDayViewIndex;
        }

        @Override
        public void onClick(View view) {
            if (view == mYearView) {
                mFragment.setCurrentView(mYearViewIndex);
            }
            else if (view == mMonthAndDayView)
            {
                mFragment.setCurrentView(mMonthAndDayViewIndex);
            }
            else if (view == mDateEnabledView)
            {
                if (mDateEnabledView.isChecked())
                {
                    mYearView.setVisibility(View.VISIBLE);
                    mMonthAndDayView.setVisibility(View.VISIBLE);
                    mFragment.onDateEnabled(this);
                }
                else
                {
                    mYearView.setVisibility(View.INVISIBLE);
                    mMonthAndDayView.setVisibility(View.INVISIBLE);
                    mFragment.onDateDisabled(this);
                }
            }
        }

        public void onSaveInstanceState(@NonNull Bundle outState)
        {
            outState.putInt(mBundleKeyPrefix+KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
            outState.putInt(mBundleKeyPrefix+KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
            outState.putInt(mBundleKeyPrefix+KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
            outState.putBoolean(mBundleKeyPrefix+KEY_SHOW_DATE_ENABLE_DISABLE, mShowDateEnableDisable);
            outState.putBoolean(mBundleKeyPrefix+KEY_DATE_ENABLED, mDateEnabledView.isChecked());

            int listPosition = -1;
            if (mFragment.mCurrentView == MONTH_AND_DAY_VIEW) {
                listPosition = mDayPickerView.getMostVisiblePosition();
            } else if (mFragment.mCurrentView == YEAR_VIEW) {
                listPosition = mYearPickerView.getFirstVisiblePosition();
                outState.putInt(mBundleKeyPrefix+KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
            }
            outState.putInt(mBundleKeyPrefix+KEY_LIST_POSITION, listPosition);
        }

        public void loadInstanceState(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                mCalendar.set(Calendar.YEAR, savedInstanceState.getInt(mBundleKeyPrefix+KEY_SELECTED_YEAR));
                mCalendar.set(Calendar.MONTH, savedInstanceState.getInt(mBundleKeyPrefix+KEY_SELECTED_MONTH));
                mCalendar.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(mBundleKeyPrefix+KEY_SELECTED_DAY));
                mShowDateEnableDisable = savedInstanceState.getBoolean(mBundleKeyPrefix+KEY_SHOW_DATE_ENABLE_DISABLE);
                boolean dateEnabled = savedInstanceState.getBoolean(mBundleKeyPrefix+KEY_DATE_ENABLED);
                if (mDateEnabledView != null) {
                    mDateEnabledView.setChecked(dateEnabled);
                }
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            loadInstanceState(savedInstanceState);
        }

        public void onCreateView(ViewGroup view,
                                 Bundle savedInstanceState)
        {
            mMainView = view;

            mDayOfWeekView = view.findViewById(R.id.date_picker_header);
            mMonthAndDayView = view.findViewById(R.id.date_picker_month_and_day);
            mMonthAndDayView.setOnClickListener(this);

            mSelectedMonthTextView = view.findViewById(R.id.date_picker_month);
            mSelectedDayTextView = view.findViewById(R.id.date_picker_day);

            mYearView = view.findViewById(R.id.date_picker_year);
            mYearView.setOnClickListener(this);

            final Activity activity = mFragment.getActivity();
            mDayPickerView = new SimpleDayPickerView(activity, mFragment);
            mYearPickerView = new YearPickerView(activity, mFragment);

            mDateEnabledView = view.findViewById(R.id.date_picker_enable);
            mDateEnabledView.setChecked(mDateEnabledAtBeginning);
            mDateEnabledView.setOnClickListener(this);

            loadInstanceState(savedInstanceState);
        }

        public void onCreateViewApplyThemeDark()
        {
            Utils.setMultiTextColorList(mFragment.getResources()
                            .getColorStateList(R.color.sdrp_selector_dark),
                    mDayOfWeekView,
                    mSelectedMonthTextView,
                    mSelectedDayTextView,
                    mYearView);

            mDateEnabledView.setThumbTintList(mFragment.getResources()
                    .getColorStateList(R.color.mdtp_material_switch_thumb_dark));
        }

        public void onCreateViewSetListPosition(Bundle savedInstanceState)
        {
            if (savedInstanceState != null) {
                final int currentView = mFragment.mCurrentView;
                final int listPosition = savedInstanceState.getInt(mBundleKeyPrefix+KEY_LIST_POSITION);

                if (currentView == mMonthAndDayViewIndex) {
                    mDayPickerView.postSetSelection(listPosition);
                } else if (currentView == mYearViewIndex) {
                    final int listPositionOffset = savedInstanceState.getInt(mBundleKeyPrefix+KEY_LIST_POSITION_OFFSET);
                    mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
                }
            }
        }

        public void onCreateViewFinalize()
        {
            if (!mShowDateEnableDisable) {
                mDateEnabledView.setVisibility(View.GONE);
            }

            if (!mDateEnabledView.isChecked())
            {
                // Must perform click to trigger hide date, change date highlight ...
                mDateEnabledView.setChecked(true);
                mDateEnabledView.performClick();
            }
        }

        public void addViews(AccessibleDateAnimator animator) {
            animator.addView(mDayPickerView);
            animator.addView(mYearPickerView);
        }

        public void setAccentColor(int mAccentColor) {
            if (mAccentColor == -1)
            {
                return;
            }
            if (mDayOfWeekView != null)
                mDayOfWeekView.setBackgroundColor(mAccentColor);

            mMainView.setBackgroundColor(mAccentColor);
            mYearPickerView.setAccentColor(mAccentColor);
            mDayPickerView.setAccentColor(mAccentColor);
        }

        private void updateDisplay() {
            if (mDayOfWeekView != null) {
                mDayOfWeekView.setText(mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                        Locale.getDefault()).toUpperCase(Locale.getDefault()));
            }

            mSelectedMonthTextView.setText(mCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                    Locale.getDefault()).toUpperCase(Locale.getDefault()));
            mSelectedDayTextView.setText(DAY_FORMAT.format(mCalendar.getTime()));
            mYearView.setText(YEAR_FORMAT.format(mCalendar.getTime()));

            // Accessibility.
            long millis = mCalendar.getTimeInMillis();
            int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
            String monthAndDayText = DateUtils.formatDateTime(mFragment.getActivity(), millis, flags);
            mMonthAndDayView.setContentDescription(monthAndDayText);
        }

        public void onChange() {
            if (mDayPickerView != null) {
                mDayPickerView.onChange();
            }
        }

        public void onYearSelected(int year) {
            mFragment.adjustDayInMonthIfNeeded(mCalendar);
            mCalendar.set(Calendar.YEAR, year);

            //make sure date is always after min date and before max date
            if (mFragment.getMinDate() != null && mCalendar.before(mFragment.getMinDate())) {
                mCalendar.setTime(mFragment.getMinDate().getTime());
            } else if (mFragment.getMaxDate() != null && mCalendar.after(mFragment.getMaxDate())) {
                mCalendar.setTime(mFragment.getMaxDate().getTime());
            }
        }

        public boolean isEnabled() {
            return mDateEnabledView.isChecked();
        }
        public boolean isDisabled() {
            return !mDateEnabledView.isChecked();
        }

        SmoothDateRangePickerFragment mFragment;
        View mMainView;
        String mBundleKeyPrefix;
        int mYearViewIndex;
        int mMonthAndDayViewIndex;

        Calendar mCalendar = Calendar.getInstance();
        TextView mDayOfWeekView;
        LinearLayout mMonthAndDayView;
        TextView mSelectedMonthTextView;
        TextView mSelectedDayTextView;
        TextView mYearView;
        private DayPickerView mDayPickerView;
        private YearPickerView mYearPickerView;
        SwitchMaterial mDateEnabledView;
        private boolean mDateEnabledAtBeginning = true;
        private boolean mShowDateEnableDisable = false;

        private static final String KEY_SELECTED_YEAR = "selected_year";
        private static final String KEY_SELECTED_MONTH = "selected_month";
        private static final String KEY_SELECTED_DAY = "selected_day";
        private static final String KEY_SHOW_DATE_ENABLE_DISABLE = "show_date_enable_disable";
        private static final String KEY_DATE_ENABLED = "date_enabled";
        private static final String KEY_LIST_POSITION = "list_position";
        private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";
    };

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;
    private static final int MONTH_AND_DAY_VIEW_END = 2;
    private static final int YEAR_VIEW_END = 3;
    private static final int DURATION_VIEW = 4;

    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_YEAR_START = "year_start";
    private static final String KEY_YEAR_END = "year_end";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private static final String KEY_MIN_DATE = "min_date";
    private static final String KEY_MIN_DATE_SELECTABLE = "min_date_end";
    private static final String KEY_MAX_DATE = "max_date";
    private static final String KEY_HIGHLIGHTED_DAYS = "highlighted_days";
    private static final String KEY_SELECTABLE_DAYS = "selectable_days";
    private static final String KEY_THEME_DARK = "theme_dark";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_DISMISS = "dismiss";
    private static final String KEY_SHOW_DURATION = "show_duration";

    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;

    private static final int ANIMATION_DURATION = 300;
    private static final int ANIMATION_DELAY = 500;

    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());

    private OnDateRangeSetListener mCallBack;
    private final HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;

    private AccessibleDateAnimator mAnimator;

    private final DateData mDateStart = new DateData(this, "date_start_", YEAR_VIEW, MONTH_AND_DAY_VIEW);
    private final DateData mDateEnd = new DateData(this, "date_end_", YEAR_VIEW_END, MONTH_AND_DAY_VIEW_END);
    private View mDateHyphen;

    private List<View> viewList;

    private LinearLayout mDurationView;
    private TextView mDurationTextView;
    private EditText mDurationEditText;
    private TextView mDurationDayTextView;
    private TextView mDurationArrow;
    private TextView mDurationArrowEnd;
    private NumberPadView mNumberPadView;

    private int mCurrentView = UNINITIALIZED;

    private int mWeekStart = mDateStart.mCalendar.getFirstDayOfWeek();
    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private Calendar mMinDate;
    private Calendar mMinSelectableDate;
    private Calendar mMaxDate;
    private Calendar[] highlightedDays;
    private Calendar[] selectableDays;

    private int mDuration;

    private boolean mThemeDark = false;
    private int mAccentColor = -1;
    private boolean mVibrate = true;
    private boolean mDismissOnPause = false;

    private HapticFeedbackController mHapticFeedbackController;

    private boolean mDelayAnimation = true;

    private boolean mShowDuration = true;

    // Accessibility strings.
    private String mDayPickerDescription;
    private String mSelectDay;
    private String mYearPickerDescription;
    private String mSelectYear;


    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateRangeSetListener {

        /**
         * @param view             The view associated with this listener.
         * @param calendarStart    The start date that was set, null if user disable it
         * @param calendarEnd      The end date that was set, null if user disable it
         */
        void onDateRangeSet(SmoothDateRangePickerFragment view,
                            Calendar calendarStart,
                            Calendar calendarEnd);
    }

    /**
     * The callback used to notify other date picker components of a change in selected date.
     */
    public interface OnDateChangedListener {
        void onDateChanged();
    }

    public SmoothDateRangePickerFragment() {
        // Empty constructor required for dialog fragment.
    }

    /**
     * @param callBack How the parent is notified that the date is set.
     *                 the initial date is set to today
     */
    public static SmoothDateRangePickerFragment newInstance(OnDateRangeSetListener callBack) {
        SmoothDateRangePickerFragment ret = new SmoothDateRangePickerFragment();
        ret.mCallBack = callBack;
        Calendar todayCal = Calendar.getInstance();
        ret.setStartDate(todayCal);
        ret.setEndDate(todayCal);
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mDateStart.onCreate(savedInstanceState);
        mDateEnd.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mDateStart.onSaveInstanceState(outState);
        mDateEnd.onSaveInstanceState(outState);
        outState.putInt(KEY_YEAR_START, mMinYear);
        outState.putInt(KEY_YEAR_END, mMaxYear);

        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        outState.putSerializable(KEY_MIN_DATE, mMinDate);
        outState.putSerializable(KEY_MAX_DATE, mMaxDate);
        outState.putSerializable(KEY_MIN_DATE_SELECTABLE, mMinSelectableDate);
        outState.putSerializable(KEY_HIGHLIGHTED_DAYS, highlightedDays);
        outState.putSerializable(KEY_SELECTABLE_DAYS, selectableDays);
        outState.putBoolean(KEY_THEME_DARK, mThemeDark);
        outState.putInt(KEY_ACCENT, mAccentColor);
        outState.putBoolean(KEY_VIBRATE, mVibrate);
        outState.putBoolean(KEY_DISMISS, mDismissOnPause);
        outState.putBoolean(KEY_SHOW_DURATION, mShowDuration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        int currentView = MONTH_AND_DAY_VIEW;
        if (savedInstanceState != null) {
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
            mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
            mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
            mMinDate = (Calendar) savedInstanceState.getSerializable(KEY_MIN_DATE);
            mMaxDate = (Calendar) savedInstanceState.getSerializable(KEY_MAX_DATE);
            mMinSelectableDate = (Calendar) savedInstanceState.getSerializable(KEY_MIN_DATE_SELECTABLE);
            highlightedDays = (Calendar[]) savedInstanceState.getSerializable(KEY_HIGHLIGHTED_DAYS);
            selectableDays = (Calendar[]) savedInstanceState.getSerializable(KEY_SELECTABLE_DAYS);
            mThemeDark = savedInstanceState.getBoolean(KEY_THEME_DARK);
            mAccentColor = savedInstanceState.getInt(KEY_ACCENT);
            mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE);
            mDismissOnPause = savedInstanceState.getBoolean(KEY_DISMISS);
            mShowDuration = savedInstanceState.getBoolean(KEY_SHOW_DURATION);
        }

        View view = inflater.inflate(R.layout.sdrp_dialog, container);

        mDateStart.onCreateView(view.findViewById(R.id.sdrp_selected_date_start), savedInstanceState);
        mDateEnd.onCreateView(view.findViewById(R.id.sdrp_selected_date_end), savedInstanceState);
        mDateHyphen = view.findViewById(R.id.hyphen);

        mDurationView = view.findViewById(R.id.date_picker_duration_layout);
        if(!mShowDuration) {
            mDurationView.setVisibility(View.GONE);
        }
        mDurationView.setOnClickListener(this);
        mDurationTextView = view.findViewById(R.id.date_picker_duration_days);
        mDurationEditText = view.findViewById(R.id.date_picker_duration_days_et);
        // disable soft keyboard popup when edittext is selected
        mDurationEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        mDurationEditText.setTextIsSelectable(true);
        mDurationDayTextView = view.findViewById(R.id.tv_duration_day);
        mDurationArrow = view.findViewById(R.id.arrow_start);
        mDurationArrow.setOnClickListener(this);
        mDurationArrowEnd = view.findViewById(R.id.arrow_end);
        mDurationArrowEnd.setOnClickListener(this);

        viewList = new ArrayList<>();
        viewList.add(MONTH_AND_DAY_VIEW, mDateStart.mMonthAndDayView);
        viewList.add(YEAR_VIEW, mDateStart.mYearView);
        viewList.add(MONTH_AND_DAY_VIEW_END, mDateEnd.mMonthAndDayView);
        viewList.add(YEAR_VIEW_END, mDateEnd.mYearView);
        viewList.add(DURATION_VIEW, mDurationView);

        final Activity activity = getActivity();
        mNumberPadView = new NumberPadView(activity, this);


        Resources res = getResources();
        mDayPickerDescription = res.getString(R.string.mdtp_day_picker_description);
        mSelectDay = res.getString(R.string.mdtp_select_day);
        mYearPickerDescription = res.getString(R.string.mdtp_year_picker_description);
        mSelectYear = res.getString(R.string.mdtp_select_year);

        int bgColorResource = mThemeDark ? R.color.mdtp_date_picker_view_animator_dark_theme
                : R.color.mdtp_date_picker_view_animator;
        view.setBackgroundColor(activity.getResources().getColor(bgColorResource));

        if (mThemeDark) {
            mDateHyphen.setBackgroundColor(activity.getResources()
                    .getColor(R.color.date_picker_selector_unselected_dark_theme));
            Utils.setMultiTextColorList(activity.getResources()
                            .getColorStateList(R.color.sdrp_selector_dark),
                    mDurationTextView, mDurationDayTextView,
                    mDurationArrow, mDurationArrowEnd,
                    mDurationEditText, view.findViewById(R.id.tv_duration));
            mDateStart.onCreateViewApplyThemeDark();
            mDateEnd.onCreateViewApplyThemeDark();
        }

        mAnimator = view.findViewById(R.id.animator);
        mAnimator.setBackgroundColor(activity.getResources().getColor(bgColorResource));

        mDateStart.addViews(mAnimator);
        mDateEnd.addViews(mAnimator);
        mAnimator.addView(mNumberPadView);
        mAnimator.setDateMillis(mDateStart.mCalendar.getTimeInMillis());
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIMATION_DURATION);
        mAnimator.setInAnimation(animation);
        Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(ANIMATION_DURATION);
        mAnimator.setOutAnimation(animation2);

        Button okButton = (Button) view.findViewById(R.id.ok);
        okButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                tryVibrate();
                if (mCallBack != null) {
                    Calendar start = mDateStart.isEnabled() ? mDateStart.mCalendar : null;
                    Calendar end = mDateEnd.isEnabled() ? mDateEnd.mCalendar : null;
                    mCallBack.onDateRangeSet(SmoothDateRangePickerFragment.this, start, end);
                }
                dismiss();
            }
        });
        okButton.setTypeface(TypefaceHelper.get(activity, "Roboto-Medium"));

        Button cancelButton = (Button) view.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tryVibrate();
                if (getDialog() != null) getDialog().cancel();
            }
        });
        cancelButton.setTypeface(TypefaceHelper.get(activity, "Roboto-Medium"));
        cancelButton.setVisibility(isCancelable() ? View.VISIBLE : View.GONE);

        //If an accent color has not been set manually, try and get it from the context
        if (mAccentColor == -1) {
            int accentColor = Utils.getAccentColorFromThemeIfAvailable(getActivity());
            if (accentColor != -1) {
                mAccentColor = accentColor;
            }
        }
        if (mAccentColor != -1) {
            view.findViewById(R.id.layout_container).setBackgroundColor(mAccentColor);
            mDurationView.setBackgroundColor(mAccentColor);
            mDurationEditText.setHighlightColor(Utils.darkenColor(mAccentColor));
            mDurationEditText.getBackground().setColorFilter(Utils.darkenColor(mAccentColor), PorterDuff.Mode.SRC_ATOP);
            okButton.setTextColor(mAccentColor);
            cancelButton.setTextColor(mAccentColor);
            mDateStart.setAccentColor(mAccentColor);
            mDateEnd.setAccentColor(mAccentColor);
        }

        updateDisplay(false);
        setCurrentView(currentView);

        mDateStart.onCreateViewSetListPosition(savedInstanceState);
        mDateEnd.onCreateViewSetListPosition(savedInstanceState);

        mHapticFeedbackController = new HapticFeedbackController(activity);

        mDateStart.onCreateViewFinalize();
        mDateEnd.onCreateViewFinalize();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHapticFeedbackController.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHapticFeedbackController.stop();
        if (mDismissOnPause) dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mOnCancelListener != null) mOnCancelListener.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) mOnDismissListener.onDismiss(dialog);
    }

    private void onDateDisabled(DateData date) {
        final DateData otherDate = (date == mDateStart) ? mDateEnd : mDateStart;

        // If other date is disabled, enable it
        if (!otherDate.mDateEnabledView.isChecked())
        {
            otherDate.mDateEnabledView.performClick();
        }

        // Hide duration (if not GONE)
        if (mShowDuration)
        {
            mDurationView.setVisibility(View.INVISIBLE);
        }

        // Select other date
        setCurrentView(otherDate.mMonthAndDayViewIndex);

        updateHighlightDays();
        updateDisplay(true);
    }

    private void onDateEnabled(DateData date) {
        if (mDateStart.isEnabled() && mDateEnd.isEnabled()) {
            // Show duration (if not GONE)
            if (mShowDuration)
            {
                mDurationView.setVisibility(View.VISIBLE);
            }
        }

        setCurrentView(date.mMonthAndDayViewIndex);

        updateHighlightDays();
        updateDisplay(true);
    }

    private void setCurrentView(final int viewIndex) {
        final long millis = mDateStart.mCalendar.getTimeInMillis();
        final long millisEnd = mDateEnd.mCalendar.getTimeInMillis();

        if (viewIndex != DURATION_VIEW) {
            if (mCurrentView != viewIndex) {
                setViewSelected(viewList.get(viewIndex));
                mAnimator.setDisplayedChild(viewIndex);
                mDurationTextView.setVisibility(View.VISIBLE);
                mDurationEditText.setVisibility(View.GONE);
                mDurationArrow.setVisibility(View.GONE);
                mDurationArrowEnd.setVisibility(View.GONE);
            }
        }

        switch (viewIndex) {
            case MONTH_AND_DAY_VIEW:
                mMinSelectableDate = mMinDate;
                mDateStart.mDayPickerView.onDateChanged();

                int flags = DateUtils.FORMAT_SHOW_DATE;
                String dayString = DateUtils.formatDateTime(getActivity(), millis, flags);
                mAnimator.setContentDescription(mDayPickerDescription + ": " + dayString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case MONTH_AND_DAY_VIEW_END:
                mMinSelectableDate = mDateStart.isEnabled() ? mDateStart.mCalendar : mMinDate;
                mDateEnd.mDayPickerView.onDateChanged();

                flags = DateUtils.FORMAT_SHOW_DATE;
                String dayStringEnd = DateUtils.formatDateTime(getActivity(), millisEnd, flags);
                mAnimator.setContentDescription(mDayPickerDescription + ": " + dayStringEnd);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case YEAR_VIEW:
                mMinSelectableDate = mMinDate;
                mDateStart.mYearPickerView.onDateChanged();
                mDateStart.mYearPickerView.refreshYearAdapter();

                CharSequence yearString = YEAR_FORMAT.format(millis);
                mAnimator.setContentDescription(mYearPickerDescription + ": " + yearString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
            case YEAR_VIEW_END:
                mMinSelectableDate = mDateStart.isEnabled() ? mDateStart.mCalendar : mMinDate;
                mDateEnd.mYearPickerView.onDateChanged();
                mDateEnd.mYearPickerView.refreshYearAdapter();

                CharSequence yearStringEnd = YEAR_FORMAT.format(millisEnd);
                mAnimator.setContentDescription(mYearPickerDescription + ": " + yearStringEnd);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
            case DURATION_VIEW:
                if (mCurrentView == YEAR_VIEW || mCurrentView == MONTH_AND_DAY_VIEW
                        || mDurationArrow.getVisibility() == View.VISIBLE) {
                    setViewSelected(mDateStart.mMonthAndDayView, mDateStart.mYearView, mDurationView);
                    mDurationArrow.setVisibility(View.GONE);
                    mDurationArrowEnd.setVisibility(View.VISIBLE);
                } else if (mCurrentView == YEAR_VIEW_END || mCurrentView == MONTH_AND_DAY_VIEW_END
                        || mDurationArrowEnd.getVisibility() == View.VISIBLE) {
                    setViewSelected(mDateEnd.mMonthAndDayView, mDateEnd.mYearView, mDurationView);
                    mDurationArrow.setVisibility(View.VISIBLE);
                    mDurationArrowEnd.setVisibility(View.GONE);
                }
                mAnimator.setDisplayedChild(DURATION_VIEW);
                mDurationTextView.setVisibility(View.GONE);
                mDurationEditText.setVisibility(View.VISIBLE);
                mDurationEditText.requestFocus();
                mDurationEditText.setText(String.valueOf(Utils.daysBetween(mDateStart.mCalendar, mDateEnd.mCalendar)));
                mDurationEditText.selectAll();
                //TODO Accessibility
                break;
        }
        mCurrentView = viewIndex;
    }

    private void setViewSelected(View... views) {
        mDateStart.mMonthAndDayView.setSelected(false);
        mDateEnd.mMonthAndDayView.setSelected(false);
        mDateStart.mYearView.setSelected(false);
        mDateEnd.mYearView.setSelected(false);
        mDurationView.setSelected(false);
        for (View view : views) {
            view.setSelected(true);
            if (view != mDurationView) { // disable DurationView animation
                ObjectAnimator pulseAnimator = Utils.getPulseAnimator(view, 0.9f, 1.05f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                pulseAnimator.start();
            }
        }
    }

    private void updateDisplay(boolean announce) {
        mDateStart.updateDisplay();
        mDateEnd.updateDisplay();

        mDuration = Utils.daysBetween(mDateStart.mCalendar, mDateEnd.mCalendar);
        mDurationTextView.setText(String.valueOf(mDuration));
        mDurationDayTextView.setText(mDuration > 1 ? getString(R.string.days) : getString(R.string.day));

        // Accessibility.
        final DateData date = mDateStart.isEnabled() ? mDateStart : mDateEnd;
        final long millis = date.mCalendar.getTimeInMillis();
        mAnimator.setDateMillis(millis);

        if (announce) {
            int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
            String fullDateText = DateUtils.formatDateTime(getActivity(), millis, flags);
//            String fullDateTextEnd = DateUtils.formatDateTime(getActivity(), millisEnd, flags);
            Utils.tryAccessibilityAnnounce(mAnimator, fullDateText);
        }
    }

    /**
     * Set whether the device should vibrate when touching fields
     *
     * @param vibrate true if the device should vibrate when touching a field
     */
    public void vibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    /**
     * Set whether the picker should dismiss itself when being paused or whether it should try to survive an orientation change
     *
     * @param dismissOnPause true if the dialog should dismiss itself when it's pausing
     */
    public void dismissOnPause(boolean dismissOnPause) {
        mDismissOnPause = dismissOnPause;
    }

    /**
     * Set whether the dark theme should be used
     *
     * @param themeDark true if the dark theme should be used, false if the default theme should be used
     */
    public void setThemeDark(boolean themeDark) {
        mThemeDark = themeDark;
    }

    /**
     * Returns true when the dark theme should be used
     *
     * @return true if the dark theme should be used, false if the default theme should be used
     */
    @Override
    public boolean isThemeDark() {
        return mThemeDark;
    }

    /**
     * Set the accent color of this dialog
     *
     * @param accentColor the accent color you want
     */
    public void setAccentColor(int accentColor) {
        mAccentColor = accentColor;
    }

    /**
     * Get the accent color of this dialog
     *
     * @return accent color
     */
    public int getAccentColor() {
        return mAccentColor;
    }

    @SuppressWarnings("unused")
    public void setFirstDayOfWeek(int startOfWeek, int startWeekEnd) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " +
                    "Calendar.SATURDAY");
        }
        mWeekStart = startOfWeek;

        mDateStart.onChange();
        mDateEnd.onChange();
    }

    @SuppressWarnings("unused")
    public void setYearRange(int startYear, int endYear) {
        if (endYear < startYear) {
            throw new IllegalArgumentException("Year end must be larger than or equal to year start");
        }

        mMinYear = startYear;
        mMaxYear = endYear;
        mDateStart.onChange();
        mDateEnd.onChange();
    }

    /**
     * Sets the default selected start date
     * Must be coherent with MinDate & MaxDate
     *
     * @param calendar a Calendar object set to the year, month, day desired as the selected start date.
     */
    public void setStartDate(Calendar calendar) {
        if (calendar.before(mMinDate)) {
            throw new IllegalArgumentException("calendar must be >= mMinDate");
        }
        if (calendar.after(mMaxDate)) {
            throw new IllegalArgumentException("calendar must be <= mMaxDate");
        }

        mDateStart.mCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        mDateStart.mCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        mDateStart.mCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

        if (mDateEnd.mCalendar.before(mDateStart.mCalendar)) {
            setEndDate(mDateStart.mCalendar);
        }
    }

    /**
     * Sets the default selected end date
     * Must be coherent with MinDate & MaxDate
     *
     * @param calendar a Calendar object set to the year, month, day desired as the selected end date.
     */
    public void setEndDate(Calendar calendar) {
        if (calendar.before(mMinDate)) {
            throw new IllegalArgumentException("calendar must be >= mMinDate");
        }
        if (calendar.after(mMaxDate)) {
            throw new IllegalArgumentException("calendar must be <= mMaxDate");
        }

        mDateEnd.mCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        mDateEnd.mCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        mDateEnd.mCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

        if (mDateStart.mCalendar.after(mDateEnd.mCalendar)) {
            setStartDate(mDateEnd.mCalendar);
        }
    }

    /**
     * Sets the minimal date supported by this DatePicker. Dates before (but not including) the
     * specified date will be disallowed from being selected.
     *
     * @param calendar a Calendar object set to the year, month, day desired as the mindate.
     */
    @SuppressWarnings("unused")
    public void setMinDate(Calendar calendar) {
        mMinDate = calendar;
        mDateStart.onChange();
        mDateEnd.onChange();
    }

    /**
     * @return The minimal date supported by this DatePicker. Null if it has not been set.
     */
    @Override
    public Calendar getMinDate() {
        return mMinDate;
    }

    /**
     * @return The minimal date can be selected by this DatePicker. return mMinDate if
     * mMonthAndDayView is showing.
     */
    @Override
    public Calendar getMinSelectableDate() {
        return mMinSelectableDate;
    }

    /**
     * Sets the minimal date supported by this DatePicker. Dates after (but not including) the
     * specified date will be disallowed from being selected.
     *
     * @param calendar a Calendar object set to the year, month, day desired as the maxdate.
     */
    @SuppressWarnings("unused")
    public void setMaxDate(Calendar calendar) {
        mMaxDate = calendar;

        mDateStart.onChange();
        mDateEnd.onChange();
    }

    /**
     * @return The maximal date supported by this DatePicker. Null if it has not been set.
     */
    @Override
    public Calendar getMaxDate() {
        return mMaxDate;
    }

    public void setShowDuration(boolean showDuration) {
        this.mShowDuration = showDuration;
    }


    public void setShowDateEnableDisable(boolean show) {
        mDateStart.mShowDateEnableDisable = show;
        mDateEnd.mShowDateEnableDisable = show;
    }

    public void enableStartDate(boolean enable) {
        if (!enable && !mDateEnd.mDateEnabledAtBeginning) {
            throw new IllegalArgumentException("1 date must be enabled");
        }
        mDateStart.mDateEnabledAtBeginning = enable;
    }

    public void enableEndDate(boolean enable) {
        if (!enable && !mDateStart.mDateEnabledAtBeginning) {
            throw new IllegalArgumentException("1 date must be enabled");
        }
        mDateEnd.mDateEnabledAtBeginning = enable;
    }


    // update highlight days
    private void updateHighlightDays() {
        List<Calendar> highlightList = new ArrayList<>();
        if (mDateStart.isEnabled() && mDateEnd.isEnabled())
        {
            for (int i = 0; i < Utils.daysBetween(mDateStart.mCalendar, mDateEnd.mCalendar) + 1; i++)
            {
                Calendar c = Calendar.getInstance();
                c.setTime(mDateStart.mCalendar.getTime());
                c.add(Calendar.DAY_OF_YEAR, i);
                highlightList.add(c);
            }
        }
        else
        {
            DateData enabledDate = mDateStart.isEnabled() ? mDateStart : mDateEnd;
            Calendar c = Calendar.getInstance();
            c.setTime(enabledDate.mCalendar.getTime());
            highlightList.add(c);
        }
        Calendar[] calendars = highlightList.toArray(new Calendar[highlightList.size()]);
        setHighlightedDays(calendars);
    }

    /**
     * Sets an array of dates which should be highlighted when the picker is drawn
     *
     * @param highlightedDays an Array of Calendar objects containing the dates to be highlighted
     */

    public void setHighlightedDays(Calendar[] highlightedDays) {
        // Sort the array to optimize searching over it later on
        Arrays.sort(highlightedDays);
        this.highlightedDays = highlightedDays;
    }

    /**
     * @return The list of dates, as Calendar Objects, which should be highlighted. null is no dates should be highlighted
     */
    @Override
    public Calendar[] getHighlightedDays() {
        return highlightedDays;
    }

    /**
     * Set's a list of days which are the only valid selections.
     * Setting this value will take precedence over using setMinDate() and setMaxDate()
     *
     * @param selectableDays an Array of Calendar Objects containing the selectable dates
     */
    @SuppressWarnings("unused")
    public void setSelectableDays(Calendar[] selectableDays) {
        // Sort the array to optimize searching over it later on
        Arrays.sort(selectableDays);
        this.selectableDays = selectableDays;
    }

    /**
     * @return an Array of Calendar objects containing the list with selectable items. null if no restriction is set
     */
    @Override
    public Calendar[] getSelectableDays() {
        return selectableDays;
    }


    @SuppressWarnings("unused")
    public void setOnDateSetListener(OnDateRangeSetListener listener) {
        mCallBack = listener;
    }

    @SuppressWarnings("unused")
    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

    @SuppressWarnings("unused")
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    // If the newly selected month / year does not contain the currently selected day number,
    // change the selected day number to the last day of the selected month or year.
    //      e.g. Switching from Mar to Apr when Mar 31 is selected -> Apr 30
    //      e.g. Switching from 2012 to 2013 when Feb 29, 2012 is selected -> Feb 28, 2013
    private void adjustDayInMonthIfNeeded(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (day > daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
        }
    }

    @Override
    public void onClick(View v) {
        tryVibrate();
        int i = v.getId();
        if (i == R.id.date_picker_duration_layout || i == R.id.arrow_start || i == R.id.arrow_end) {
            setCurrentView(DURATION_VIEW);
        }
    }


    @Override
    public void onYearSelected(int year) {
        updatePickers();
        if (mCurrentView == YEAR_VIEW) {
            mDateStart.onYearSelected(year);
            if (mDateStart.mCalendar.after(mDateEnd.mCalendar)) {
                //make sure end date always after start date
                mDateEnd.mCalendar.setTime(mDateStart.mCalendar.getTime());
            }
            setCurrentView(MONTH_AND_DAY_VIEW);
        } else if (mCurrentView == YEAR_VIEW_END) {
            mDateEnd.onYearSelected(year);
            if (mDateStart.mCalendar.after(mDateEnd.mCalendar)) {
                //make sure end date always after start date
                mDateStart.mCalendar.setTime(mDateEnd.mCalendar.getTime());
            }
            setCurrentView(MONTH_AND_DAY_VIEW_END);
        }
        updateHighlightDays();
        updateDisplay(true);
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            mDateStart.mCalendar.set(Calendar.YEAR, year);
            mDateStart.mCalendar.set(Calendar.MONTH, month);
            mDateStart.mCalendar.set(Calendar.DAY_OF_MONTH, day);
            if (mDateStart.mCalendar.after(mDateEnd.mCalendar)) {
                mDateEnd.mCalendar.setTime(mDateStart.mCalendar.getTime());
            }
            if (mDateEnd.isEnabled())
            {
                // jump to end day selector
                setCurrentView(MONTH_AND_DAY_VIEW_END);
            }
        } else if (mCurrentView == MONTH_AND_DAY_VIEW_END) {
            mDateEnd.mCalendar.set(Calendar.YEAR, year);
            mDateEnd.mCalendar.set(Calendar.MONTH, month);
            mDateEnd.mCalendar.set(Calendar.DAY_OF_MONTH, day);
            if (mDateStart.mCalendar.after(mDateEnd.mCalendar)) {
                mDateStart.mCalendar.setTime(mDateEnd.mCalendar.getTime());
            }
        }
        updatePickers();
        updateHighlightDays();

        updateDisplay(true);
    }

    @Override
    public void onDurationChanged(int num) {
        if (num >= 0) {
            Calendar limitDay = Calendar.getInstance();
            int limitDuration;
            if (mDateStart.mMonthAndDayView.isSelected()) {
                limitDay.set(1900, 0, 1);
                limitDuration = Utils.daysBetween(limitDay, mDateEnd.mCalendar) + 1;
            } else {
                limitDay.set(2100, 11, 31);
                limitDuration = Utils.daysBetween(mDateStart.mCalendar, limitDay);
            }
            if (mDurationEditText.hasSelection()) {
                mDuration = num;
            } else {
                mDuration = mDuration * 10 + num > limitDuration ? limitDuration : mDuration * 10 + num;
            }
        } else if (num == -1) { //del
            mDuration = (mDuration > 0) ? mDuration / 10 : mDuration;
        } else if (num == -2) { // delete all
            mDuration = 0;
        }
        mDurationEditText.setText(String.valueOf(mDuration));
        mDurationEditText.setSelection(String.valueOf(mDuration).length());
        if (mDateStart.mMonthAndDayView.isSelected()) {
            mDateStart.mCalendar.setTime(mDateEnd.mCalendar.getTime());
            mDateStart.mCalendar.add(Calendar.DATE, -mDuration);
        } else {
            mDateEnd.mCalendar.setTime(mDateStart.mCalendar.getTime());
            mDateEnd.mCalendar.add(Calendar.DATE, mDuration);
        }

        updateHighlightDays();
        updateDisplay(true);
    }

    private void updatePickers() {
        for (OnDateChangedListener listener : mListeners) listener.onDateChanged();
    }

    @Override
    public MonthAdapter.CalendarDay getSelectedDay() {
        if (mDateStart.mYearView.isSelected() || mDateStart.mMonthAndDayView.isSelected()) {
            return new MonthAdapter.CalendarDay(mDateStart.mCalendar);
        } else {
            return new MonthAdapter.CalendarDay(mDateEnd.mCalendar);
        }
    }

    @Override
    public int getMinYear() {
        if (selectableDays != null) return selectableDays[0].get(Calendar.YEAR);
        // Ensure no years can be selected outside of the given minimum date
        return mMinDate != null && mMinDate.get(Calendar.YEAR) > mMinYear ?
                mMinDate.get(Calendar.YEAR) : mMinYear;
    }

    @Override
    public int getMinSelectableYear() {
        if (selectableDays != null) return selectableDays[0].get(Calendar.YEAR);
        // Ensure no years can be selected outside of the given minimum date
        return mMinSelectableDate != null && mMinSelectableDate.get(Calendar.YEAR) > mMinYear ?
                mMinSelectableDate.get(Calendar.YEAR) : mMinYear;
    }

    @Override
    public int getMaxYear() {
        if (selectableDays != null)
            return selectableDays[selectableDays.length - 1].get(Calendar.YEAR);
        // Ensure no years can be selected outside of the given maximum date
        return mMaxDate != null && mMaxDate.get(Calendar.YEAR) < mMaxYear ?
                mMaxDate.get(Calendar.YEAR) : mMaxYear;
    }

    @Override
    public int getFirstDayOfWeek() {
        return mWeekStart;
    }

    @Override
    public void registerOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void tryVibrate() {
        if (mVibrate) mHapticFeedbackController.tryVibrate();
    }
}
