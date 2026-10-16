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

    // বাংলা সংখ্যা (০-৯)
    private static final String[] BN_DIGITS = {
            "০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯"
    };

    // ইংরেজি সংখ্যাকে বাংলায় রূপান্তর (যেকোনো সংখ্যার জন্য)
    private static String toBangla(int number) {
        StringBuilder sb = new StringBuilder();
        String s = String.valueOf(number);
        for (char c : s.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append(BN_DIGITS[c - '0']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String toBangla(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append(BN_DIGITS[c - '0']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

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

            // প্রতি সেকেন্ডে আপডেট হলে বারবার schedule করা লাগবে না,
            // AlarmManager নিজেই repeat করছে
            if (Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                    || Intent.ACTION_DATE_CHANGED.equals(action)) {
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
            // প্রতি সেকেন্ডে আপডেট
            long firstUpdate = System.currentTimeMillis() + 1_000L;
            long interval = 1_000L;

            // Android 6.0+ এ সঠিক সময়ে আপডেট পেতে setExact ব্যবহার করা ভালো,
            // তবে Doze mode এ সমস্যা হতে পারে। এখানে setRepeating ব্যবহার করছি।
            alarmManager.setRepeating(
                    AlarmManager.RTC,
                    firstUpdate,
                    interval,
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

            // ★ ব্যাকগ্রাউন্ড সম্পূর্ণ স্বচ্ছ করা হচ্ছে ★
            views.setInt(R.id.widget_root,
                    "setBackgroundColor",
                    Color.TRANSPARENT);

            views.setImageViewBitmap(
                    R.id.widget_clock_image,
                    drawClock());

            // বাংলা তারিখ
            String dateEnglish = new SimpleDateFormat(
                    "EEEE, d MMMM yyyy",
                    new Locale("bn", "BD"))
                    .format(Calendar.getInstance().getTime());

            String dateBangla = toBangla(dateEnglish);

            views.setTextViewText(R.id.widget_date, dateBangla);

            // তারিখের টেক্সট কালার সাদা
            views.setTextColor(R.id.widget_date, Color.WHITE);

            manager.updateAppWidget(id, views);
        }
    }

    private Bitmap drawClock() {
        final int size = 520;
        final float center = size / 2f;
        final float radius = 240f;

        Bitmap bitmap = Bitmap.createBitmap(
                size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        // ★ সম্পূর্ণ স্বচ্ছ ব্যাকগ্রাউন্ড ★
        canvas.drawColor(Color.TRANSPARENT);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // ============ বাইরের বৃত্ত ============
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        paint.setColor(Color.rgb(255, 215, 90));
        canvas.drawCircle(center, center, radius, paint);

        // ভেতরের হালকা বর্ডার
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb(120, 255, 255, 255));
        canvas.drawCircle(center, center, radius - 8, paint);

        // ============ ৬০টি ছোট কাটা ============
        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6 - 90);

            float outer = radius - 12;
            float inner;
            float strokeWidth;
            int color;

            if (i % 5 == 0) {
                inner = radius - 32;
                strokeWidth = 5;
                color = Color.rgb(255, 215, 90);
            } else {
                inner = radius - 22;
                strokeWidth = 2;
                color = Color.argb(180, 255, 255, 255);
            }

            float x1 = center + (float) Math.cos(angle) * inner;
            float y1 = center + (float) Math.sin(angle) * inner;
            float x2 = center + (float) Math.cos(angle) * outer;
            float y2 = center + (float) Math.sin(angle) * outer;

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            paint.setStrokeWidth(strokeWidth);
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        // ============ বাংলা সংখ্যা (১-১২) ============
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(
                "sans-serif", Typeface.BOLD));
        paint.setTextSize(38);

        for (int i = 1; i <= 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);

            float x = center
                    + (float) Math.cos(angle) * (radius - 70);

            float y = center
                    + (float) Math.sin(angle) * (radius - 70)
                    + 13;

            // ★ সঠিক বাংলা সংখ্যা: ১ থেকে ১২ ★
            canvas.drawText(toBangla(i), x, y, paint);
        }

        Calendar time = Calendar.getInstance();

        int hour = time.get(Calendar.HOUR);          // 0-11
        int minute = time.get(Calendar.MINUTE);
        int second = time.get(Calendar.SECOND);

        // ============ ঘণ্টার কাঁটা ============
        float hourAngle = hour * 30f + minute * 0.5f;
        drawHand(canvas, center, radius * 0.48f,
                hourAngle, 14, Color.WHITE);

        // ============ মিনিটের কাঁটা ============
        float minuteAngle = minute * 6f + second * 0.1f;
        drawHand(canvas, center, radius * 0.68f,
                minuteAngle, 9, Color.rgb(255, 215, 90));

        // ============ সেকেন্ডের কাঁটা ============
        drawHand(canvas, center, radius * 0.78f,
                second * 6f, 4, Color.RED);

        // ============ কেন্দ্রের বিন্দু ============
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(center, center, 9, paint);

        paint.setColor(Color.rgb(255, 215, 90));
        canvas.drawCircle(center, center, 4, paint);

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