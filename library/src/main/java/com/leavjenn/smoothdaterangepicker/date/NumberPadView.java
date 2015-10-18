package com.leavjenn.smoothdaterangepicker.date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.leavjenn.smoothdaterangepicker.R;

public class NumberPadView extends View {
    private static int NUM_COLUMN = 3;
    private static int NUMBER_TEXT_SIZE;
    private static final int SELECTED_CIRCLE_ALPHA = 255;


    private Paint numPaint;
    private int mNumberColor;
    RangePickerController mController;

    public NumberPadView(Context context) {
        super(context);
    }

    public NumberPadView(Context context, RangePickerController controller) {
        super(context);
        mController = controller;
        Resources res = context.getResources();
        mNumberColor = res.getColor(R.color.mdtp_date_picker_text_normal);
        NUMBER_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_selected_date_year_size);
        initView();
    }

    public NumberPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberPadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void initView() {
        numPaint = new Paint();
        numPaint.setAntiAlias(true);
        numPaint.setTextSize(NUMBER_TEXT_SIZE);
        numPaint.setColor(mNumberColor);
        numPaint.setStyle(Paint.Style.FILL);
        numPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int num = 0; num < 9; num++) {
            canvas.drawText(String.valueOf(num + 1), 96 * (num % NUM_COLUMN + 1),
                    96 * (num / NUM_COLUMN + 1), numPaint);
        }
        canvas.drawText("0", 96, 96 * 4, numPaint);
        canvas.drawText("C", 96 * 3, 96 * 4, numPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }
}
