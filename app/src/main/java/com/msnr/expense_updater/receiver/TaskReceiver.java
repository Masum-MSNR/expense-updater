package com.msnr.expense_updater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.msnr.expense_updater.utils.Methods;


public class TaskReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("TaskReceiver", "onReceive");
        Methods.updateWidget(context);
    }

}
