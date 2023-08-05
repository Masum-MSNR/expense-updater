package com.msnr.expense_updater;

import android.app.Application;

import com.msnr.expense_updater.helpers.SheetHelper;
import com.msnr.expense_updater.utils.Methods;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Methods.updateWidget(getApplicationContext());
    }
}
