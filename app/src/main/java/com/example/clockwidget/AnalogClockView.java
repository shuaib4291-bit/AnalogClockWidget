package com.example.clockwidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.util.AttributeSet;
import java.util.Calendar;

public class AnalogClockView extends View {

    private final Paint p = new Paint(3);

    public AnalogClockView(Context c, AttributeSet a) {
        super(c, a);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        float w = getWidth();
        float h = getHeight();
        float r = Math.min(w, h) * 0.46f;
        float cx = w / 2f;
        float cy = h / 2f;

        p.setColor(Color.rgb(18, 61, 44));
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(cx, cy, r, p);

        p.setColor(Color.rgb(100, 220, 150));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        c.drawCircle(cx, cy, r, p);

        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(r * 0.17f);

        for (int i = 1; i <= 12; i++) {
            double a = Math.PI * i / 6 - Math.PI / 2;
            float x = cx + (float)Math.cos(a) * r * 0.78f;
            float y = cy + (float)Math.sin(a) * r * 0.78f
                    + p.getTextSize() * 0.35f;
            p.setColor(Color.WHITE);
            c.drawText(String.valueOf(i), x, y, p);
        }

        Calendar t = Calendar.getInstance();
        float sec = t.get(Calendar.SECOND);
        float min = t.get(Calendar.MINUTE) + sec / 60f;
        float hr = (t.get(Calendar.HOUR) % 12) + min / 60f;

        hand(c, cx, cy, r * 0.48f, hr * 30, 6,
                Color.WHITE);
        hand(c, cx, cy, r * 0.66f, min * 6, 4,
                Color.rgb(100, 220, 150));
        hand(c, cx, cy, r * 0.75f, sec * 6, 2,
                Color.RED);

        p.setColor(Color.RED);
        c.drawCircle(cx, cy, 5, p);
    }

    private void hand(Canvas c, float x, float y,
                      float len, float degrees,
                      float width, int color) {

        double a = Math.toRadians(degrees - 90);
        float ex = x + (float)Math.cos(a) * len;
        float ey = y + (float)Math.sin(a) * len;

        p.setColor(color);
        p.setStrokeWidth(width);
        p.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x, y, ex, ey, p);
    }
}