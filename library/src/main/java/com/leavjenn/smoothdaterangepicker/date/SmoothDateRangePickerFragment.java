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

package com.leavjenn.smoothdaterangepicker.date;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.leavjenn.smoothdaterangepicker.HapticFeedbackController;
import com.leavjenn.smoothdaterangepicker.R;
import com.leavjenn.smoothdaterangepicker.TypefaceHelper;
import com.leavjenn.smoothdaterangepicker.Utils;

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

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;
    private static final int MONTH_AND_DAY_VIEW_END = 2;
    private static final int YEAR_VIEW_END = 3;
    private static final int DURATION_VIEW = 4;

    private static final String KEY_SELECTED_YEAR = "selected_year";
    private static final String KEY_SELECTED_YEAR_END = "selected_year_end";
    private static final String KEY_SELECTED_MONTH = "selected_month";
    private static final String KEY_SELECTED_MONTH_END = "selected_month_end";
    private static final String KEY_SELECTED_DAY = "selected_day";
    private static final String KEY_SELECTED_DAY_END = "selected_day_end";
    private static final String KEY_DURATION_DAYS = "duration_days";
    private static final String KEY_LIST_POSITION = "list_position";
    private static final String KEY_LIST_POSITION_END = "list_position_end";
    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_YEAR_START = "year_start";
    private static final String KEY_YEAR_END = "year_end";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";
    private static final String KEY_LIST_POSITION_OFFSET_END = "list_position_offset_end";
    private static final String KEY_MIN_DATE = "min_date";
    private static final String KEY_MIN_DATE_SELECTABLE = "min_date_end";
    private static final String KEY_MAX_DATE = "max_date";
    private static final String KEY_HIGHLIGHTED_DAYS = "highlighted_days";
    private static final String KEY_SELECTABLE_DAYS = "selectable_days";
    private static final String KEY_THEME_DARK = "theme_dark";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_DISMISS = "dismiss";

    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;

    private static final int ANIMATION_DURATION = 300;
    private static final int ANIMATION_DELAY = 500;

    private static SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());
    private static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());

    private Calendar mCalendar = Calendar.getInstance();
    private Calendar mCalendarEnd = Calendar.getInstance();
    private OnDateRangeSetListener mCallBack;
    private HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;

    private AccessibleDateAnimator mAnimator;

    private TextView mDayOfWeekView;
    private LinearLayout mMonthAndDayView;
    private TextView mSelectedMonthTextView;
    private TextView mSelectedDayTextView;
    private TextView mYearView;
    private DayPickerView mDayPickerView;
    private YearPickerView mYearPickerView;

    private TextView mDayOfWeekViewEnd;
    private LinearLayout mMonthAndDayViewEnd;
    private TextView mSelectedMonthTextViewEnd;
    private TextView mSelectedDayTextViewEnd;
    private TextView mYearViewEnd;
    private SimpleDayPickerView mDayPickerViewEnd;
    private YearPickerView mYearPickerViewEnd;

    private List<View> viewList;

    private LinearLayout mDurationView;
    private TextView mDurationTextView;
    private EditText mDurationEditText;
    private TextView mDurationDayTextView;
    private TextView mDurationArrow;
    private TextView mDurationArrowEnd;
    private NumberPadView mNumberPadView;

    private int mCurrentView = UNINITIALIZED;

    private int mWeekStart = mCalendar.getFirstDayOfWeek();
    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private Calendar mMinDate;
    private Calendar mMinSelectableDate;
    private Calendar mMaxDate;
    private Calendar[] highlightedDays;
    private Calendar[] selectableDays;

    private int mDuration;

    private boolean mThemeDark;
    private int mAccentColor = -1;
    private boolean mVibrate;
    private boolean mDismissOnPause;

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
         * @param yearStart        The start year that was set.
         * @param monthStart The start month that was set (0-11) for compatibility
         *                         with {@link Calendar}.
         * @param dayStart  The start day of the month that was set.
         * @param yearEnd          The end year that was set.
         * @param monthEnd   The end month that was set (0-11) for compatibility
         *                         with {@link Calendar}.
         * @param dayEnd    The end day of the month that was set.
         */
        void onDateRangeSet(SmoothDateRangePickerFragment view, int yearStart, int monthStart,
                            int dayStart, int yearEnd, int monthEnd, int dayEnd);
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
     * @param callBack    How the parent is notified that the date is set.
     * @param year        The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth  The initial day of the dialog.
     */
    public static SmoothDateRangePickerFragment newInstance(OnDateRangeSetListener callBack, int year,
                                                            int monthOfYear,
                                                            int dayOfMonth,
                                                            boolean showDuration) {
        SmoothDateRangePickerFragment ret = new SmoothDateRangePickerFragment();
        ret.initialize(callBack, year, monthOfYear, dayOfMonth, showDuration);
        return ret;
    }

    /**
     * @param callBack    How the parent is notified that the date is set.
     * @param year        The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth  The initial day of the dialog.
     */
    public static SmoothDateRangePickerFragment newInstance(OnDateRangeSetListener callBack, int year,
                                                            int monthOfYear,
                                                            int dayOfMonth) {
        SmoothDateRangePickerFragment ret = new SmoothDateRangePickerFragment();
        ret.initialize(callBack, year, monthOfYear, dayOfMonth, true);
        return ret;
    }

    /**
     * @param callBack How the parent is notified that the date is set.
     *                 the initial date is set to today
     */
    public static SmoothDateRangePickerFragment newInstance(OnDateRangeSetListener callBack) {
        SmoothDateRangePickerFragment ret = new SmoothDateRangePickerFragment();
        Calendar todayCal = Calendar.getInstance();
        ret.initialize(callBack, todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH),
                todayCal.get(Calendar.DAY_OF_MONTH), true);
        return ret;
    }

    public void initialize(OnDateRangeSetListener callBack, int year, int monthOfYear, int dayOfMonth, boolean showDuration) {
        mCallBack = callBack;
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mCalendarEnd.set(Calendar.YEAR, year);
        mCalendarEnd.set(Calendar.MONTH, monthOfYear);
        mCalendarEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        mThemeDark = false;
        mAccentColor = -1;
        mVibrate = true;
        mDismissOnPause = false;
        mShowDuration = showDuration;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (savedInstanceState != null) {
            mCalendar.set(Calendar.YEAR, savedInstanceState.getInt(KEY_SELECTED_YEAR));
            mCalendar.set(Calendar.MONTH, savedInstanceState.getInt(KEY_SELECTED_MONTH));
            mCalendar.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(KEY_SELECTED_DAY));
            mCalendarEnd.set(Calendar.YEAR, savedInstanceState.getInt(KEY_SELECTED_YEAR_END));
            mCalendarEnd.set(Calendar.MONTH, savedInstanceState.getInt(KEY_SELECTED_MONTH_END));
            mCalendarEnd.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(KEY_SELECTED_DAY_END));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
        outState.putInt(KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
        outState.putInt(KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
        outState.putInt(KEY_SELECTED_YEAR_END, mCalendarEnd.get(Calendar.YEAR));
        outState.putInt(KEY_SELECTED_MONTH_END, mCalendarEnd.get(Calendar.MONTH));
        outState.putInt(KEY_SELECTED_DAY_END, mCalendarEnd.get(Calendar.DAY_OF_MONTH));
        outState.putInt(KEY_YEAR_START, mMinYear);
        outState.putInt(KEY_YEAR_END, mMaxYear);

        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        int listPosition = -1;
        int listPositionEnd = -1;
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            listPosition = mDayPickerView.getMostVisiblePosition();
        } else if (mCurrentView == YEAR_VIEW) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        } else if (mCurrentView == MONTH_AND_DAY_VIEW_END) {
            listPositionEnd = mDayPickerViewEnd.getMostVisiblePosition();
        } else if (mCurrentView == YEAR_VIEW_END) {
            listPositionEnd = mYearPickerViewEnd.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET_END, mYearPickerViewEnd.getFirstPositionOffset());
        }
        outState.putInt(KEY_LIST_POSITION, listPosition);
        outState.putInt(KEY_LIST_POSITION_END, listPositionEnd);
        outState.putSerializable(KEY_MIN_DATE, mMinDate);
        outState.putSerializable(KEY_MAX_DATE, mMaxDate);
        outState.putSerializable(KEY_MIN_DATE_SELECTABLE, mMinSelectableDate);
        outState.putSerializable(KEY_HIGHLIGHTED_DAYS, highlightedDays);
        outState.putSerializable(KEY_SELECTABLE_DAYS, selectableDays);
        outState.putBoolean(KEY_THEME_DARK, mThemeDark);
        outState.putInt(KEY_ACCENT, mAccentColor);
        outState.putBoolean(KEY_VIBRATE, mVibrate);
        outState.putBoolean(KEY_DISMISS, mDismissOnPause);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.sdrp_dialog, container);

        mDayOfWeekView = (TextView) view.findViewById(R.id.date_picker_header);
        mDayOfWeekViewEnd = (TextView) view.findViewById(R.id.date_picker_header_end);
        mMonthAndDayView = (LinearLayout) view.findViewById(R.id.date_picker_month_and_day);
        mMonthAndDayViewEnd = (LinearLayout) view.findViewById(R.id.date_picker_month_and_day_end);
        mMonthAndDayView.setOnClickListener(this);
        mMonthAndDayViewEnd.setOnClickListener(this);

        mSelectedMonthTextView = (TextView) view.findViewById(R.id.date_picker_month);
        mSelectedMonthTextViewEnd = (TextView) view.findViewById(R.id.date_picker_month_end);

        mSelectedDayTextView = (TextView) view.findViewById(R.id.date_picker_day);
        mSelectedDayTextViewEnd = (TextView) view.findViewById(R.id.date_picker_day_end);

        mYearView = (TextView) view.findViewById(R.id.date_picker_year);
        mYearViewEnd = (TextView) view.findViewById(R.id.date_picker_year_end);
        mYearView.setOnClickListener(this);
        mYearViewEnd.setOnClickListener(this);

        mDurationView = (LinearLayout) view.findViewById(R.id.date_picker_duration_layout);
        if(!mShowDuration) {
            mDurationView.setVisibility(View.GONE);
        }
        mDurationView.setOnClickListener(this);
        mDurationTextView = (TextView) view.findViewById(R.id.date_picker_duration_days);
        mDurationEditText = (EditText) view.findViewById(R.id.date_picker_duration_days_et);
        // disable soft keyboard popup when edittext is selected
        mDurationEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        mDurationEditText.setTextIsSelectable(true);
        mDurationDayTextView = (TextView) view.findViewById(R.id.tv_duration_day);
        mDurationArrow = (TextView) view.findViewById(R.id.arrow_start);
        mDurationArrow.setOnClickListener(this);
        mDurationArrowEnd = (TextView) view.findViewById(R.id.arrow_end);
        mDurationArrowEnd.setOnClickListener(this);

        viewList = new ArrayList<>();
        viewList.add(MONTH_AND_DAY_VIEW, mMonthAndDayView);
        viewList.add(YEAR_VIEW, mYearView);
        viewList.add(MONTH_AND_DAY_VIEW_END, mMonthAndDayViewEnd);
        viewList.add(YEAR_VIEW_END, mYearViewEnd);
        viewList.add(DURATION_VIEW, mDurationView);

        int listPosition = -1;
        int listPositionOffset = 0;
        int listPositionEnd = -1;
        int listPositionOffsetEnd = 0;
        int currentView = MONTH_AND_DAY_VIEW;
        if (savedInstanceState != null) {
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
            mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
            mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
            listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
            listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
            listPositionEnd = savedInstanceState.getInt(KEY_LIST_POSITION_END);
            listPositionOffsetEnd = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET_END);
            mMinDate = (Calendar) savedInstanceState.getSerializable(KEY_MIN_DATE);
            mMaxDate = (Calendar) savedInstanceState.getSerializable(KEY_MAX_DATE);
            mMinSelectableDate = (Calendar) savedInstanceState.getSerializable(KEY_MIN_DATE_SELECTABLE);
            highlightedDays = (Calendar[]) savedInstanceState.getSerializable(KEY_HIGHLIGHTED_DAYS);
            selectableDays = (Calendar[]) savedInstanceState.getSerializable(KEY_SELECTABLE_DAYS);
            mThemeDark = savedInstanceState.getBoolean(KEY_THEME_DARK);
            mAccentColor = savedInstanceState.getInt(KEY_ACCENT);
            mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE);
            mDismissOnPause = savedInstanceState.getBoolean(KEY_DISMISS);
        }

        final Activity activity = getActivity();
        mDayPickerView = new SimpleDayPickerView(activity, this);
        mYearPickerView = new YearPickerView(activity, this);
        mDayPickerViewEnd = new SimpleDayPickerView(activity, this);
        mYearPickerViewEnd = new YearPickerView(activity, this);
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
            view.findViewById(R.id.hyphen).setBackgroundColor(activity.getResources()
                    .getColor(R.color.date_picker_selector_unselected_dark_theme));
            Utils.setMultiTextColorList(activity.getResources()
                            .getColorStateList(R.color.sdrp_selector_dark),
                    mDayOfWeekView, mDayOfWeekViewEnd,
                    mSelectedMonthTextView, mSelectedMonthTextViewEnd,
                    mSelectedDayTextView, mSelectedDayTextViewEnd,
                    mYearView, mYearViewEnd, mDurationTextView,
                    mDurationDayTextView, mDurationArrow, mDurationArrowEnd,
                    mDurationEditText, (TextView) view.findViewById(R.id.tv_duration));
        }

        mAnimator = (AccessibleDateAnimator) view.findViewById(R.id.animator);

        mAnimator.addView(mDayPickerView);
        mAnimator.addView(mYearPickerView);
        mAnimator.addView(mDayPickerViewEnd);
        mAnimator.addView(mYearPickerViewEnd);
        mAnimator.addView(mNumberPadView);
        mAnimator.setDateMillis(mCalendar.getTimeInMillis());
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
                    mCallBack.onDateRangeSet(SmoothDateRangePickerFragment.this,
                            mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                            mCalendar.get(Calendar.DAY_OF_MONTH), mCalendarEnd.get(Calendar.YEAR),
                            mCalendarEnd.get(Calendar.MONTH), mCalendarEnd.get(Calendar.DAY_OF_MONTH));
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
            if (mDayOfWeekView != null)
                mDayOfWeekView.setBackgroundColor(mAccentColor);
            if (mDayOfWeekViewEnd != null)
                mDayOfWeekViewEnd.setBackgroundColor(mAccentColor);

            view.findViewById(R.id.layout_container).setBackgroundColor(mAccentColor);
            view.findViewById(R.id.day_picker_selected_date_layout).setBackgroundColor(mAccentColor);
            view.findViewById(R.id.day_picker_selected_date_layout_end).setBackgroundColor(mAccentColor);
            mDurationView.setBackgroundColor(mAccentColor);
            mDurationEditText.setHighlightColor(Utils.darkenColor(mAccentColor));
            mDurationEditText.getBackground().setColorFilter(Utils.darkenColor(mAccentColor), PorterDuff.Mode.SRC_ATOP);
            okButton.setTextColor(mAccentColor);
            cancelButton.setTextColor(mAccentColor);
            mYearPickerView.setAccentColor(mAccentColor);
            mDayPickerView.setAccentColor(mAccentColor);
            mYearPickerViewEnd.setAccentColor(mAccentColor);
            mDayPickerViewEnd.setAccentColor(mAccentColor);
        }

        updateDisplay(false);
        setCurrentView(currentView);

        if (listPosition != -1) {
            if (currentView == MONTH_AND_DAY_VIEW) {
                mDayPickerView.postSetSelection(listPosition);
            } else if (currentView == YEAR_VIEW) {
                mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
            }
        }

        if (listPositionEnd != -1) {
            if (currentView == MONTH_AND_DAY_VIEW_END) {
                mDayPickerViewEnd.postSetSelection(listPositionEnd);
            } else if (currentView == YEAR_VIEW_END) {
                mYearPickerViewEnd.postSetSelectionFromTop(listPositionEnd, listPositionOffsetEnd);
            }
        }

        mHapticFeedbackController = new HapticFeedbackController(activity);

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

    private void setCurrentView(final int viewIndex) {
        long millis = mCalendar.getTimeInMillis();
        long millisEnd = mCalendarEnd.getTimeInMillis();

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
                mDayPickerView.onDateChanged();

                int flags = DateUtils.FORMAT_SHOW_DATE;
                String dayString = DateUtils.formatDateTime(getActivity(), millis, flags);
                mAnimator.setContentDescription(mDayPickerDescription + ": " + dayString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case MONTH_AND_DAY_VIEW_END:
                mMinSelectableDate = mCalendar;
                mDayPickerViewEnd.onDateChanged();

                flags = DateUtils.FORMAT_SHOW_DATE;
                String dayStringEnd = DateUtils.formatDateTime(getActivity(), millisEnd, flags);
                mAnimator.setContentDescription(mDayPickerDescription + ": " + dayStringEnd);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case YEAR_VIEW:
                mMinSelectableDate = mMinDate;
                mYearPickerView.onDateChanged();
                mYearPickerView.refreshYearAdapter();

                CharSequence yearString = YEAR_FORMAT.format(millis);
                mAnimator.setContentDescription(mYearPickerDescription + ": " + yearString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
            case YEAR_VIEW_END:
                mMinSelectableDate = mCalendar;
                mYearPickerViewEnd.onDateChanged();
                mYearPickerViewEnd.refreshYearAdapter();

                CharSequence yearStringEnd = YEAR_FORMAT.format(millisEnd);
                mAnimator.setContentDescription(mYearPickerDescription + ": " + yearStringEnd);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
            case DURATION_VIEW:
                if (mCurrentView == YEAR_VIEW || mCurrentView == MONTH_AND_DAY_VIEW
                        || mDurationArrow.getVisibility() == View.VISIBLE) {
                    setViewSelected(mMonthAndDayView, mYearView, mDurationView);
                    mDurationArrow.setVisibility(View.GONE);
                    mDurationArrowEnd.setVisibility(View.VISIBLE);
                } else if (mCurrentView == YEAR_VIEW_END || mCurrentView == MONTH_AND_DAY_VIEW_END
                        || mDurationArrowEnd.getVisibility() == View.VISIBLE) {
                    setViewSelected(mMonthAndDayViewEnd, mYearViewEnd, mDurationView);
                    mDurationArrow.setVisibility(View.VISIBLE);
                    mDurationArrowEnd.setVisibility(View.GONE);
                }
                mAnimator.setDisplayedChild(DURATION_VIEW);
                mDurationTextView.setVisibility(View.GONE);
                mDurationEditText.setVisibility(View.VISIBLE);
                mDurationEditText.requestFocus();
                mDurationEditText.setText(String.valueOf(Utils.daysBetween(mCalendar, mCalendarEnd)));
                mDurationEditText.selectAll();
                //TODO Accessibility
                break;
        }
        mCurrentView = viewIndex;
    }

    private void setViewSelected(View... views) {
        mMonthAndDayView.setSelected(false);
        mMonthAndDayViewEnd.setSelected(false);
        mYearView.setSelected(false);
        mYearViewEnd.setSelected(false);
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
        if (mDayOfWeekView != null && mDayOfWeekViewEnd != null) {
            mDayOfWeekView.setText(mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                    Locale.getDefault()).toUpperCase(Locale.getDefault()));
            mDayOfWeekViewEnd.setText(mCalendarEnd.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                    Locale.getDefault()).toUpperCase(Locale.getDefault()));
        }

        mSelectedMonthTextView.setText(mCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                Locale.getDefault()).toUpperCase(Locale.getDefault()));
        mSelectedMonthTextViewEnd.setText(mCalendarEnd.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                Locale.getDefault()).toUpperCase(Locale.getDefault()));
        mSelectedDayTextView.setText(DAY_FORMAT.format(mCalendar.getTime()));
        mSelectedDayTextViewEnd.setText(DAY_FORMAT.format(mCalendarEnd.getTime()));
        mYearView.setText(YEAR_FORMAT.format(mCalendar.getTime()));
        mYearViewEnd.setText(YEAR_FORMAT.format(mCalendarEnd.getTime()));
        mDuration = Utils.daysBetween(mCalendar, mCalendarEnd);
        mDurationTextView.setText(String.valueOf(mDuration));
        mDurationDayTextView.setText(mDuration > 1 ? getString(R.string.days) : getString(R.string.day));

        // Accessibility.
        long millis = mCalendar.getTimeInMillis();
        long millisEnd = mCalendarEnd.getTimeInMillis();
        mAnimator.setDateMillis(millis);
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
        String monthAndDayText = DateUtils.formatDateTime(getActivity(), millis, flags);
        String monthAndDayTextEnd = DateUtils.formatDateTime(getActivity(), millisEnd, flags);
        mMonthAndDayView.setContentDescription(monthAndDayText);
        mMonthAndDayViewEnd.setContentDescription(monthAndDayTextEnd);

        if (announce) {
            flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
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

        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }

        if (mDayPickerViewEnd != null) {
            mDayPickerViewEnd.onChange();
        }
    }

    @SuppressWarnings("unused")
    public void setYearRange(int startYear, int endYear) {
        if (endYear < startYear) {
            throw new IllegalArgumentException("Year end must be larger than or equal to year start");
        }

        mMinYear = startYear;
        mMaxYear = endYear;
        if (mDayPickerView != null && mDayPickerViewEnd != null) {
            mDayPickerView.onChange();
            mDayPickerViewEnd.onChange();
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
        if (mDayPickerView != null && mDayPickerViewEnd != null) {
            mDayPickerView.onChange();
            mDayPickerViewEnd.onChange();
        }
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

        if (mDayPickerView != null && mDayPickerViewEnd != null) {
            mDayPickerView.onChange();
            mDayPickerViewEnd.onChange();
        }
    }

    public void setShowDuration(boolean showDuration) {
        this.mShowDuration = showDuration;
    }

    /**
     * @return The maximal date supported by this DatePicker. Null if it has not been set.
     */
    @Override
    public Calendar getMaxDate() {
        return mMaxDate;
    }


    // update highlight days
    private void updateHighlightDays() {
        List<Calendar> highlightList = new ArrayList<>();
        for (int i = 0; i < Utils.daysBetween(mCalendar, mCalendarEnd) + 1; i++) {
            Calendar c = Calendar.getInstance();
            c.setTime(mCalendar.getTime());
            c.add(Calendar.DAY_OF_YEAR, i);
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
        if (i == R.id.date_picker_year) {
            setCurrentView(YEAR_VIEW);

        } else if (i == R.id.date_picker_year_end) {
            setCurrentView(YEAR_VIEW_END);

        } else if (i == R.id.date_picker_month_and_day) {
            setCurrentView(MONTH_AND_DAY_VIEW);

        } else if (i == R.id.date_picker_month_and_day_end) {
            setCurrentView(MONTH_AND_DAY_VIEW_END);

        } else if (i == R.id.date_picker_duration_layout || i == R.id.arrow_start || i == R.id.arrow_end) {
            setCurrentView(DURATION_VIEW);

        }
    }

    @Override
    public void onYearSelected(int year) {
        updatePickers();
        if (mCurrentView == YEAR_VIEW) {
            adjustDayInMonthIfNeeded(mCalendar);
            mCalendar.set(Calendar.YEAR, year);
            //make sure start date always after min date and before max date
            if (getMinDate() != null && mCalendar.before(getMinDate())) {
                mCalendar.setTime(getMinDate().getTime());
            } else if (getMaxDate() != null && mCalendar.after(getMaxDate())) {
                mCalendar.setTime(getMaxDate().getTime());
            }
            if (mCalendar.after(mCalendarEnd)) {
                //make sure end date always after start date
                mCalendarEnd.setTime(mCalendar.getTime());
            }
            setCurrentView(MONTH_AND_DAY_VIEW);
        } else if (mCurrentView == YEAR_VIEW_END) {
            adjustDayInMonthIfNeeded(mCalendarEnd);
            mCalendarEnd.set(Calendar.YEAR, year);
            //make sure end date always after min date and before max date
            if (getMinDate() != null && mCalendarEnd.before(getMinDate())) {
                mCalendarEnd.setTime(getMinDate().getTime());
            } else if (getMaxDate() != null && mCalendarEnd.after(getMaxDate())) {
                mCalendarEnd.setTime(getMaxDate().getTime());
            }
            if (mCalendar.after(mCalendarEnd)) {
                //make sure end date always after start date
                mCalendarEnd.setTime(mCalendar.getTime());
            }
            setCurrentView(MONTH_AND_DAY_VIEW_END);
        }
        updateHighlightDays();
        updateDisplay(true);
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, day);
            if (mCalendar.after(mCalendarEnd)) {
                mCalendarEnd.setTime(mCalendar.getTime());
            }
            // jump to end day selector
            setCurrentView(MONTH_AND_DAY_VIEW_END);
        } else if (mCurrentView == MONTH_AND_DAY_VIEW_END) {
            mCalendarEnd.set(Calendar.YEAR, year);
            mCalendarEnd.set(Calendar.MONTH, month);
            mCalendarEnd.set(Calendar.DAY_OF_MONTH, day);
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
            if (mMonthAndDayView.isSelected()) {
                limitDay.set(1900, 0, 1);
                limitDuration = Utils.daysBetween(limitDay, mCalendarEnd) + 1;
            } else {
                limitDay.set(2100, 11, 31);
                limitDuration = Utils.daysBetween(mCalendar, limitDay);
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
        if (mMonthAndDayView.isSelected()) {
            mCalendar.setTime(mCalendarEnd.getTime());
            mCalendar.add(Calendar.DATE, -mDuration);
        } else {
            mCalendarEnd.setTime(mCalendar.getTime());
            mCalendarEnd.add(Calendar.DATE, mDuration);
        }

        updateHighlightDays();
        updateDisplay(true);
    }

    private void updatePickers() {
        for (OnDateChangedListener listener : mListeners) listener.onDateChanged();
    }

    @Override
    public MonthAdapter.CalendarDay getSelectedDay() {
        if (mYearView.isSelected() || mMonthAndDayView.isSelected()) {
            return new MonthAdapter.CalendarDay(mCalendar);
        } else {
            return new MonthAdapter.CalendarDay(mCalendarEnd);
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
