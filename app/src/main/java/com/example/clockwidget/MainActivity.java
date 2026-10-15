package com.example.clockwidget;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.Handler;

public class MainActivity extends Activity {

    LinearLayout root;
    TextView time, date;
    Handler handler = new Handler();

    Runnable clockRun = new Runnable() {
        @Override
        public void run() {
            Date now = new Date();

            time.setText(new SimpleDateFormat(
                "hh:mm:ss a", Locale.getDefault()
            ).format(now));

            date.setText(new SimpleDateFormat(
                "EEEE, d MMMM yyyy", new Locale("bn", "BD")
            ).format(now));

            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(20, 30, 20, 30);
        root.setBackgroundColor(Color.rgb(12, 45, 34));

        TextView title = new TextView(this);
        title.setText("আমার অ্যানালগ ঘড়ি");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);

        time = new TextView(this);
        time.setTextColor(Color.rgb(130, 255, 180));
        time.setTextSize(32);
        time.setGravity(Gravity.CENTER);

        date = new TextView(this);
        date.setTextColor(Color.WHITE);
        date.setTextSize(18);
        date.setGravity(Gravity.CENTER);

        Button widget = new Button(this);
        widget.setText("হোমস্ক্রিনে উইজেট যোগ করুন");
        widget.setOnClickListener(v -> {
            AppWidgetManager manager =
                AppWidgetManager.getInstance(this);

            ComponentName provider = new ComponentName(
                this, ClockWidgetProvider.class
            );

            if (android.os.Build.VERSION.SDK_INT >= 26 &&
                manager.isRequestPinAppWidgetSupported()) {

                manager.requestPinAppWidget(
                    provider, null, null
                );
            } else {
                date.setText(
                    "হোমস্ক্রিনে চাপ দিয়ে Widgets নির্বাচন করুন"
                );
            }
        });

        root.addView(title);
        root.addView(time);
        root.addView(date);
        root.addView(widget);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(clockRun);
        handler.post(clockRun);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(clockRun);
    }
}