package com.msnr.expense_updater.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;

import com.msnr.expense_updater.receiver.TaskReceiver;
import com.msnr.expense_updater.widgets.DashboardWidget;

public class Methods {
    public static void setAlarm(Context context) {
        Log.v("Methods", "setAlarm");
        long currentTime = System.currentTimeMillis();
        currentTime += 60000;

        Intent intent = new Intent(context, TaskReceiver.class);
        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(currentTime, pi), pi);
    }

    public static void cancelAlarm(Context context) {
        Log.v("Methods", "cancelAlarm");
        Intent intent = new Intent(context, TaskReceiver.class);
        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static void updateWidget(Context context) {
        setAlarm(context);
        Log.v("Methods", "updateWidget");
        Intent intent = new Intent(context, DashboardWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, DashboardWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    public static Spanned getHtml(Object object) {
        String temp = object.toString().trim();
        temp = temp.substring(0, temp.length() - 1);
        if (temp.contains("-")) {
            return Html.fromHtml("<font color='#FFA1A1'>" + temp.substring(1) + "</font>");
        } else {
            return Html.fromHtml("<font color='#000000'>" + temp + "</font>");
        }
    }

    public static void hideView(View view) {
        view.setVisibility(View.GONE);
    }
}
