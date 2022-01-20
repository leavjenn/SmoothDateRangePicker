package com.leavjenn.smoothdaterangepickerexample;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;

import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;

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
        final TextView tvDateRange = (TextView) findViewById(R.id.tv_date_range);
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
                                                       int yearStart, int monthStart,
                                                       int dayStart, int yearEnd,
                                                       int monthEnd, int dayEnd)
                            {
                                String date = "You picked the following date range: \n"
                                        + "From " + dayStart + "/" + (++monthStart)
                                        + "/" + yearStart + " To " + dayEnd + "/"
                                        + (++monthEnd) + "/" + yearEnd;
                                tvDateRange.setText(date);
                            }
                        });

                boolean isThemeDark = ((Switch) findViewById(R.id.switch_dark_theme)).isChecked();
                smoothDateRangePickerFragment.setThemeDark(isThemeDark);

                boolean showDuration = ((Switch) findViewById(R.id.switch_show_duration)).isChecked();
                smoothDateRangePickerFragment.setShowDuration(showDuration);

                smoothDateRangePickerFragment.show(getFragmentManager(), "Datepickerdialog");
            }
        });
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

