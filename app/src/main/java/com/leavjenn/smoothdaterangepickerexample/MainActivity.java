package com.leavjenn.smoothdaterangepickerexample;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.leavjenn.smoothdaterangepicker.date.DateRangePickerDialog;

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements DateRangePickerDialog.OnDateSetListener {
    private TextView tvDate, tvDate1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvDate1 = (TextView) findViewById(R.id.tv_date1);
        Button btnDateRange = (Button) findViewById(R.id.btn_date_range_picker);
        btnDateRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateRangePickerDialog dateRangePickerDialog =
                        DateRangePickerDialog.newInstance(MainActivity.this);
                dateRangePickerDialog.show(getFragmentManager(), "Datepickerdialog");
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
                                tvDate1.setText((year + "/" + monthOfYear + "/" + dayOfMonth));
                            }
                        }, year, month, day);
                Calendar cc = Calendar.getInstance();
                cc.set(2014, 4, 22);
                datePickerDialog.getDatePicker().setMinDate(cc.getTimeInMillis());
                datePickerDialog.show();

            }
        });
    }

    @Override
    public void onDateSet(DateRangePickerDialog view, int yearStart, int monthOfYearStart, int dayOfMonthStart,
                          int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        String date = "You picked the following date: \n" +
                "From " + dayOfMonthStart + "/" + (++monthOfYearStart) + "/" + yearStart +
                " To " + dayOfMonthEnd + "/" + (++monthOfYearEnd) + "/" + yearEnd;
        tvDate.setText(date);
    }
}

