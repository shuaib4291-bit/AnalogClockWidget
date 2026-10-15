package com.example.clockwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ClockWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_UPDATE =
            "com.example.clockwidget.UPDATE_ANALOG_CLOCK";

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager manager,
                         int[] appWidgetIds) {

        updateAll(context);
        scheduleUpdates(context);
    }

    @Override
    public void onEnabled(Context context) {
        scheduleUpdates(context);
        updateAll(context);
    }

    @Override
    public void onDisabled(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(getUpdateIntent(context));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (ACTION_UPDATE.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)) {

            updateAll(context);

            if (ACTION_UPDATE.equals(action)) {
                scheduleUpdates(context);
            }
        }
    }

    private PendingIntent getUpdateIntent(Context context) {
        Intent intent = new Intent(context, ClockWidgetProvider.class);
        intent.setAction(ACTION_UPDATE);

        return PendingIntent.getBroadcast(
                context,
                101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE);
    }

    private void scheduleUpdates(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE);

        if (alarmManager != null) {
            long firstUpdate =
                    System.currentTimeMillis() + 60_000L;

            alarmManager.setRepeating(
                    AlarmManager.RTC,
                    firstUpdate,
                    60_000L,
                    getUpdateIntent(context));
        }
    }

    private void updateAll(Context context) {
        AppWidgetManager manager =
                AppWidgetManager.getInstance(context);

        ComponentName component =
                new ComponentName(context, ClockWidgetProvider.class);

        int[] ids = manager.getAppWidgetIds(component);

        for (int id : ids) {
            RemoteViews views =
                    new RemoteViews(
                            context.getPackageName(),
                            R.layout.widget_clock);

            views.setImageViewBitmap(
                    R.id.widget_clock_image,
                    drawClock());

            String date = new SimpleDateFormat(
                    "EEEE, d MMMM yyyy",
                    new Locale("bn", "BD"))
                    .format(Calendar.getInstance().getTime());

            views.setTextViewText(R.id.widget_date, date);

            manager.updateAppWidget(id, views);
        }
    }

    private Bitmap drawClock() {
        final int size = 400;
        final float center = size / 2f;
        final float radius = 184f;

        Bitmap bitmap = Bitmap.createBitmap(
                size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // ঘড়ির বাইরের বৃত্ত
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(center, center, radius + 5, paint);


        // বাইরের সীমানা
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.rgb(255, 215, 90));
        canvas.drawCircle(center, center, radius, paint);
        paint.setStyle(Paint.Style.FILL);

        // ১ থেকে ১২ পর্যন্ত সংখ্যা ও মিনিটের দাগ
        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6 - 90);

            float outer = radius - 13;
            float inner = (i % 5 == 0)
                    ? radius - 35
                    : radius - 23;

            float x1 = center + (float) Math.cos(angle) * inner;
            float y1 = center + (float) Math.sin(angle) * inner;
            float x2 = center + (float) Math.cos(angle) * outer;
            float y2 = center + (float) Math.sin(angle) * outer;

            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(i % 5 == 0 ? 5 : 2);
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(
                "sans-serif", Typeface.BOLD));
        paint.setTextSize(29);

        for (int i = 1; i <= 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);

            float x = center
                    + (float) Math.cos(angle) * (radius - 57);

            float y = center
                    + (float) Math.sin(angle) * (radius - 57)
                    + 10;

            canvas.drawText(
                    String.valueOf(i), x, y, paint);
        }

        Calendar time = Calendar.getInstance();

        int hour = time.get(Calendar.HOUR);
        int minute = time.get(Calendar.MINUTE);
        int second = time.get(Calendar.SECOND);

        // ঘণ্টার কাঁটা
        float hourAngle = hour * 30f + minute * 0.5f;
        drawHand(canvas, center, radius * 0.48f,
                hourAngle, 12, Color.WHITE);

        // মিনিটের কাঁটা
        float minuteAngle = minute * 6f + second * 0.1f;
        drawHand(canvas, center, radius * 0.68f,
                minuteAngle, 8, Color.rgb(255, 215, 90));

        // সেকেন্ডের কাঁটা
        drawHand(canvas, center, radius * 0.76f,
                second * 6f, 3, Color.RED);

        // স্বচ্ছ ব্যাকগ্রাউন্ডে শুধু গোল বৃত্ত
paint.setColor(Color.WHITE);
paint.setStyle(Paint.Style.STROKE);
paint.setStrokeWidth(5);
canvas.drawCircle(center, center, radius, paint);
paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(center, center, 5, paint);

        return bitmap;
    }

    private void drawHand(
            Canvas canvas,
            float center,
            float length,
            float angle,
            float thickness,
            int color) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStrokeWidth(thickness);
        paint.setStrokeCap(Paint.Cap.ROUND);

        canvas.save();
        canvas.rotate(angle, center, center);

        canvas.drawLine(
                center,
                center,
                center,
                center - length,
                paint);

        canvas.restore();
    }
}