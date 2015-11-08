package com.leavjenn.smoothdaterangepicker.date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import com.leavjenn.smoothdaterangepicker.R;

import java.util.ArrayList;
import java.util.Arrays;

public class NumberPadView extends TableLayout implements View.OnClickListener {
    private Context mContext;
    private SmoothDateRangePickerController mController;
    private Button btnNum0, btnNum1, btnNum2, btnNum3, btnNum4, btnNum5,
            btnNum6, btnNum7, btnNum8, btnNum9, btnDel;
    private int mTextColor;

    public NumberPadView(Context context, SmoothDateRangePickerController controller) {
        super(context);
        mContext = context;
        mController = controller;
        init();
    }

    public NumberPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        boolean darkTheme = mController.isThemeDark();
        if (darkTheme) {
            mTextColor = mContext.getResources().getColor(R.color.mdtp_date_picker_text_normal_dark_theme);
        } else {
            mTextColor = mContext.getResources().getColor(R.color.mdtp_date_picker_text_normal);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.sdrp_number_pad, this);
        btnNum0 = (Button) view.findViewById(R.id.btn_zero);
        btnNum1 = (Button) view.findViewById(R.id.btn_one);
        btnNum2 = (Button) view.findViewById(R.id.btn_two);
        btnNum3 = (Button) view.findViewById(R.id.btn_three);
        btnNum4 = (Button) view.findViewById(R.id.btn_four);
        btnNum5 = (Button) view.findViewById(R.id.btn_five);
        btnNum6 = (Button) view.findViewById(R.id.btn_six);
        btnNum7 = (Button) view.findViewById(R.id.btn_seven);
        btnNum8 = (Button) view.findViewById(R.id.btn_eight);
        btnNum9 = (Button) view.findViewById(R.id.btn_nine);
        btnDel = (Button) view.findViewById(R.id.btn_delete);
        ArrayList<Button> buttons = new ArrayList<>(Arrays.asList(btnNum0, btnNum1, btnNum2,
                btnNum3, btnNum4, btnNum5, btnNum6, btnNum7, btnNum8, btnNum9, btnDel));
        setMultiButtonsTextColor(buttons);
        setMultiBtnsOnClickListener(buttons);
        btnDel.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mController.onDurationChanged(-2);
                return true;
            }
        });
    }

    private void setMultiBtnsOnClickListener(ArrayList<Button> buttons) {
        for (Button btn : buttons) {
            btn.setOnClickListener(this);
        }
    }

    private void setMultiButtonsTextColor(ArrayList<Button> buttons) {
        for (Button btn : buttons) {
            btn.setTextColor(mTextColor);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_zero) {
            mController.onDurationChanged(0);
        } else if (i == R.id.btn_one) {
            mController.onDurationChanged(1);
        } else if (i == R.id.btn_two) {
            mController.onDurationChanged(2);
        } else if (i == R.id.btn_three) {
            mController.onDurationChanged(3);
        } else if (i == R.id.btn_four) {
            mController.onDurationChanged(4);
        } else if (i == R.id.btn_five) {
            mController.onDurationChanged(5);
        } else if (i == R.id.btn_six) {
            mController.onDurationChanged(6);
        } else if (i == R.id.btn_seven) {
            mController.onDurationChanged(7);
        } else if (i == R.id.btn_eight) {
            mController.onDurationChanged(8);
        } else if (i == R.id.btn_nine) {
            mController.onDurationChanged(9);
        } else if (i == R.id.btn_delete) {
            mController.onDurationChanged(-1);
        }
    }
}