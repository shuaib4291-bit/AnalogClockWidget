package com.example.clockwidget;

import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.widget.RemoteViews;
import android.os.Handler;
import android.os.Looper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.view.View;
import android.content.Intent;

public class ClockWidgetProvider extends AppWidgetProvider {

    private static final Handler handler =
        new Handler(Looper.getMainLooper());

    @Override
    public void onUpdate(
        Context context,
        AppWidgetManager manager,
        int[] ids
    ) {
        updateAll(context, manager, ids);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (Intent.ACTION_TIME_TICK.equals(action) ||
            Intent.ACTION_TIME_CHANGED.equals(action) ||
            Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
            Intent.ACTION_DATE_CHANGED.equals(action)) {

            AppWidgetManager manager =
                AppWidgetManager.getInstance(context);

            updateAll(
                context, manager,
                manager.getAppWidgetIds(
                    new android.content.ComponentName(
                        context, ClockWidgetProvider.class
                    )
                )
            );
        }
    }

    private void updateAll(
        Context context,
        AppWidgetManager manager,
        int[] ids
    ) {
        for (int id : ids) {
            RemoteViews views = new RemoteViews(
                context.getPackageName(),
                R.layout.widget_clock
            );

            Date now = new Date();

            views.setTextViewText(
                R.id.widget_time,
                new SimpleDateFormat(
                    "hh:mm:ss", Locale.getDefault()
                ).format(now)
            );

            views.setTextViewText(
                R.id.widget_date,
                new SimpleDateFormat(
                    "EEE, d MMM", new Locale("bn", "BD")
                ).format(now)
            );

            manager.updateAppWidget(id, views);
        }
    }
}