package com.msnr.expense_updater.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class Preference {
    public static void setNames(List<Object> names, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Consts.PREFS_NAME, Context.MODE_PRIVATE).edit();
        for (int i = 0; i < names.size(); i++) {
            editor.putString("name" + i, names.get(i).toString());
        }
        editor.apply();
    }


    public static List<String> getNames(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Consts.PREFS_NAME, Context.MODE_PRIVATE);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            names.add(prefs.getString("name" + i, ""));
        }
        return names;
    }
}
