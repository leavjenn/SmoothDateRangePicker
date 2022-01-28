package com.wsbak.smoothdaterangepickerexample;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;

import com.wsbak.smoothdaterangepicker.date.SmoothDateRangePickerFragment;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onCreateDateRange();
        onCreateDate();
    }

    protected void onCreateDateRange()
    {
        final String pickerFragmentTag = "Datepickerdialog";

        final Button btnDateRange = (Button) findViewById(R.id.btn_date_range_picker);
        btnDateRange.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SmoothDateRangePickerFragment smoothDateRangePickerFragment =
                    SmoothDateRangePickerFragment
                        .newInstance(new SmoothDateRangePickerFragment.OnDateRangeSetListener()
                        {
                            @Override
                            public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                                       Calendar calendarStart,
                                                       Calendar calendarEnd)
                            {
                                onActivityDateRangeSet(view, calendarStart, calendarEnd);
                            }
                        });

                final Calendar cMin = Calendar.getInstance();
                cMin.set(1999, 0, 1);
                smoothDateRangePickerFragment.setMinDate(cMin);  // 1999/01/01

                final Calendar cMax = Calendar.getInstance();
                cMax.set(Calendar.MONTH, 11);
                cMax.set(Calendar.DAY_OF_MONTH, 31);
                smoothDateRangePickerFragment.setMaxDate(cMax);  // last day of current year

                boolean isThemeDark = ((Switch) findViewById(R.id.switch_dark_theme)).isChecked();
                smoothDateRangePickerFragment.setThemeDark(isThemeDark);

                boolean showDuration = ((Switch) findViewById(R.id.switch_show_duration)).isChecked();
                smoothDateRangePickerFragment.setShowDuration(showDuration);

                boolean showDateEnableDisable = ((Switch) findViewById(R.id.switch_show_date_enable_disable)).isChecked();
                smoothDateRangePickerFragment.setShowDateEnableDisable(showDateEnableDisable);

                smoothDateRangePickerFragment.show(getFragmentManager(), pickerFragmentTag);
            }
        });

        Fragment fragment = getFragmentManager().findFragmentByTag(pickerFragmentTag);
        if (fragment != null) {
            // Means that activity and picker have been re-created (following screen rotation)
            // So picker has loose its listener, so must set it again
            SmoothDateRangePickerFragment pickerFragment = (SmoothDateRangePickerFragment)fragment;
            pickerFragment.setOnDateSetListener(new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
                @Override
                public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                           Calendar calendarStart,
                                           Calendar calendarEnd)
                {
                    onActivityDateRangeSet(view, calendarStart, calendarEnd);
                }
            });
        }
    }

    protected void onActivityDateRangeSet(SmoothDateRangePickerFragment view,
                                          Calendar calendarStart,
                                          Calendar calendarEnd)
    {
        String date;
        if (calendarStart == null && calendarEnd == null) {
            date = "No date enabled !";  // impossible case
        } else {
            date = "You picked the following date range: \n"
                    + "From " + formatDate(calendarStart)
                    + " To " + formatDate(calendarEnd);
        }

        final TextView tvDateRange = findViewById(R.id.tv_date_range);
        tvDateRange.setText(date);
    }

    private static String formatDate(Calendar calendar) {
        if (calendar != null) {
            return calendar.get(Calendar.YEAR) + "/" +
                    (calendar.get(Calendar.MONTH) + 1) + "/" +
                    calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            return "...";
        }
    }

    protected void onCreateDate()
    {
        final TextView tvDate = (TextView) findViewById(R.id.tv_date);
        final Button btnDate = (Button) findViewById(R.id.btn_date_picker);
        btnDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Calendar c = Calendar.getInstance();
                final int year = c.get(Calendar.YEAR);
                final int month = c.get(Calendar.MONTH);
                final int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new android.app.DatePickerDialog.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                            {
                                tvDate.setText("You picked the following date: \n"
                                        + (year + "/" + monthOfYear + "/" + dayOfMonth));
                            }
                        }, year, month, day);
                Calendar cc = Calendar.getInstance();
                cc.set(2014, 4, 22);
                datePickerDialog.getDatePicker().setMinDate(cc.getTimeInMillis());
                datePickerDialog.show();
            }
        });
    }
}

