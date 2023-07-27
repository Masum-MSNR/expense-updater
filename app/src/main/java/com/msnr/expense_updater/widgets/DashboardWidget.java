package com.msnr.expense_updater.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.msnr.expense_updater.R;
import com.msnr.expense_updater.helpers.SheetHelper;
import com.msnr.expense_updater.utils.Methods;

import java.util.List;

public class DashboardWidget extends AppWidgetProvider {


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dashboard_widget);

        SheetHelper sheetHelper = new SheetHelper(context);
        sheetHelper.getSpecificRange("E1:H6", new int[]{0, 3, 5}).addOnSuccessListener(result -> {

            if(result==null) return;
            //update name
            List<Object> list = result.get(0);
            views.setTextViewText(R.id.tv_name1, list.get(0).toString());
            views.setTextViewText(R.id.tv_name2, list.get(1).toString());
            views.setTextViewText(R.id.tv_name3, list.get(2).toString());
            views.setTextViewText(R.id.tv_name4, list.get(3).toString());

            //cost amount
            list = result.get(2);
            views.setTextViewText(R.id.tv_spent1, Methods.getHtml(list.get(0)));
            views.setTextViewText(R.id.tv_spent2, Methods.getHtml(list.get(1)));
            views.setTextViewText(R.id.tv_spent3, Methods.getHtml(list.get(2)));
            views.setTextViewText(R.id.tv_spent4, Methods.getHtml(list.get(3)));

            //balance amount
            list = result.get(1);
            views.setTextViewText(R.id.tv_balance1, Methods.getHtml(list.get(0)));
            views.setTextViewText(R.id.tv_balance2, Methods.getHtml(list.get(1)));
            views.setTextViewText(R.id.tv_balance3, Methods.getHtml(list.get(2)));
            views.setTextViewText(R.id.tv_balance4, Methods.getHtml(list.get(3)));

            appWidgetManager.updateAppWidget(appWidgetId, views);
        });

    }

//    @Override
//    public void onReceive(Context context, Intent intent) {
//        int[] appWidgetIdsToUpdate = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
//        for(int appWidgetId : appWidgetIdsToUpdate) {
//            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
//        }
//        super.onReceive(context, intent);
//    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v("DashboardWidget", "onUpdate");
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}