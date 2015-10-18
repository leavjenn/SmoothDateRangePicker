package com.leavjenn.smoothdaterangepickerexample;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.leavjenn.smoothdaterangepicker.date.RangePickerDialog;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements RangePickerDialog.OnDateSetListener {
    private TextView tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDate = (TextView) findViewById(R.id.tv_date);
        Button btnDateRange = (Button) findViewById(R.id.btn_date_range_picker);
        btnDateRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
//                DatePickerDialog dpd = DatePickerDialog.newInstance(
                RangePickerDialog dpd = RangePickerDialog.newInstance(
                        MainActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
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

                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
    }

    @Override
    public void onDateSet(RangePickerDialog view, int year, int monthOfYear, int dayOfMonth,
                          int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        String date = "You picked the following date: From- "
                + dayOfMonth + "/" + (++monthOfYear) + "/" + year +
                " To " + dayOfMonthEnd + "/" + (++monthOfYearEnd) + "/" + yearEnd;
        tvDate.setText(date);
    }
}

