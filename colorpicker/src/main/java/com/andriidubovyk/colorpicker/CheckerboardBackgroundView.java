package com.andriidubovyk.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;


class CheckerboardBackgroundView  extends View {

    private Paint checkBoardPaint;

    public CheckerboardBackgroundView(Context context) {
        super(context);
    }

    public CheckerboardBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckerboardBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(checkBoardPaint==null) {
            int smallestSize = getHeight() < getWidth() ? getHeight() : getWidth();
            checkBoardPaint = createCheckerBoardPaint(smallestSize/3);
        }
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), checkBoardPaint);
    }

    private static Paint createCheckerBoardPaint(int pixelSize)
    {
        Bitmap bitmap = Bitmap.createBitmap(pixelSize * 2, pixelSize * 2, Bitmap.Config.ARGB_8888);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(0x30000000);

        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, pixelSize, pixelSize);
        canvas.drawRect(rect, fill);
        rect.offset(pixelSize, pixelSize);
        canvas.drawRect(rect, fill);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT));
        return paint;
    }
}
