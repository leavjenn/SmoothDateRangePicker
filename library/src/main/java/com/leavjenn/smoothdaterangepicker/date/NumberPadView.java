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
    Context mContext;
    DateRangePickerController mController;
    Button btnNum0, btnNum1, btnNum2, btnNum3, btnNum4, btnNum5,
            btnNum6, btnNum7, btnNum8, btnNum9, btnDel;

    public NumberPadView(Context context, DateRangePickerController controller) {
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.sdrp_number_pad, this);
//        int[] layoutIds = {R.id.btn_zero, R.id.btn_one, R.id.btn_two, R.id.btn_three,
//                R.id.btn_four, R.id.btn_five, R.id.btn_six, R.id.btn_seven,
//                R.id.btn_eight, R.id.btn_nine, R.id.btn_delete};
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
//        initMultiBtns(buttons, layoutIds, view);
        setMultiBtnsOnClickListener(buttons);
        btnDel.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mController.onDurationChanged(-2);
                return true;
            }
        });
    }

//    private void initMultiBtns(ArrayList<Button> buttons, int[] layoutIds, View view) {
//        for (int i = 0; i < buttons.size(); i++) {
//            buttons.get(i) = (Button) view.findViewById(layoutIds[i]);
//        }
//    }

    private void setMultiBtnsOnClickListener(ArrayList<Button> buttons) {
        for (Button btn : buttons) {
            btn.setOnClickListener(this);
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