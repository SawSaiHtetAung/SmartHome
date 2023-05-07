package com.safeseasonmyanmar.smarthome.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;


public class DimDial extends View {

    private Paint touchBarPaint;
    private Paint refBarPaint;
    int height, width, padding = 30;
    int NUMBER_OF_BLOCK = 40;
    int switchPercent = 30;

    public DimDial(Context context) {
        super(context);
        init(null);
    }

    public DimDial(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DimDial(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet set){
        touchBarPaint = new Paint();
        touchBarPaint.setAntiAlias(true);
        touchBarPaint.setColor(Color.parseColor("#FF0d64b0"));
        touchBarPaint.setStyle(Paint.Style.FILL);

        refBarPaint = new Paint();
        refBarPaint.setAntiAlias(true);
        refBarPaint.setColor(Color.parseColor("#FF0d64b0"));
        refBarPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int usableWidth = width - (2 * padding);
        int diffX = usableWidth / NUMBER_OF_BLOCK;

        for (int i=0; i<NUMBER_OF_BLOCK; i++){
            drawRefRect(canvas,(diffX*i)+padding,height/2);
        }

        drawTouchRect(canvas,padding,height/2,switchPercent);
    }

    private void drawTouchRect(Canvas canvas,int startX,int startY,int percent) {
        Rect rect = new Rect();
        int usableWidth = width - (2 * padding);
        int diffX = usableWidth / NUMBER_OF_BLOCK;
        if (percent > 100)
            percent = 100;
        int actualPercent = (int) (NUMBER_OF_BLOCK * ((float)percent/100));

        for (int i=0; i<actualPercent;i++) {
            rect.left = startX + (diffX * i) ;
            rect.top  = startY ;
            rect.right = rect.left + 5;
            rect.bottom = rect.top + 30;
            canvas.drawRect(rect, touchBarPaint);
        }
    }

    private void drawRefRect(Canvas canvas,int centerX,int centerY) {
        Rect rect = new Rect();
        rect.left = centerX ;
        rect.top  = centerY ;
        rect.right = rect.left + 5;
        rect.bottom = rect.top + 30;

        canvas.drawRect(rect,refBarPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean value = super.onTouchEvent(event);

        int tempStep = (int) (NUMBER_OF_BLOCK * ((float)switchPercent/100));
        int changeStep = 0;
        int boundaryStart = padding;
        int boundaryEnd = width - (2 * padding);
        int boundaryTop = 20 + height/2;
        int boundaryButton = boundaryTop + 50;
        int usableWidth = width - (2 * padding);
        int diffX = usableWidth / NUMBER_OF_BLOCK;

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();

                if ((x > boundaryStart && x < boundaryEnd) && (y > boundaryTop && y < boundaryButton)){
                    switchPercent = (int) ((x-padding)/diffX);
                    switchPercent = (int) (((float) switchPercent/NUMBER_OF_BLOCK) * 100);
                    changeStep = (int) (NUMBER_OF_BLOCK * ((float)switchPercent/100));
                    if (tempStep != changeStep) {
                        postInvalidate();
                    }
                }

                return true;
        }

        return value;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
    }
}
