package com.leavjenn.smoothdaterangepickerexample;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class MainActivity extends AppCompatActivity {
    private TextView tvDateRange, tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDateRange = (TextView) findViewById(R.id.tv_date_range);
        tvDate = (TextView) findViewById(R.id.tv_date);
        final GregorianCalendar today         = new GregorianCalendar();
        final GregorianCalendar tomorrow      = new GregorianCalendar();
        tomorrow.add(Calendar.DATE,1);
        Button btnDateRange = (Button) findViewById(R.id.btn_date_range_picker);
        btnDateRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmoothDateRangePickerFragment smoothDateRangePickerFragment =
                        SmoothDateRangePickerFragment
                                .newInstance(new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
                                    @Override
                                    public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                                               int yearStart, int monthStart,
                                                               int dayStart, int yearEnd,
                                                               int monthEnd, int dayEnd) {
                                        String date = "You picked the following date range: \n"
                                                + "From " + dayStart + "/" + (++monthStart)
                                                + "/" + yearStart + " To " + dayEnd + "/"
                                                + (++monthEnd) + "/" + yearEnd;
                                        tvDateRange.setText(date);
                                    }
                                },today,tomorrow);
                smoothDateRangePickerFragment.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        Button btnDate = (Button) findViewById(R.id.btn_date_picker);
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new android.app.DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
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

