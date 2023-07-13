package com.msnr.expense_updater.serviceHelpers;

import static com.msnr.expense_updater.utils.C.PREFS_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SheetsServiceHelper {

    Context context;
    Sheets sheets;
    String spreadSheetId, spreadSheetTitle, currentSheetTitle;
    SharedPreferences.Editor editor;

    public SheetsServiceHelper(Context context, Sheets sheets) {
        this.context = context;
        this.sheets = sheets;
        this.editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
    }

    private Task<Boolean> checkPermission(String sheetId) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        ExecutorService service = Executors.newFixedThreadPool(1);
        AtomicBoolean result = new AtomicBoolean(false);
        service.execute(() -> {
            try {
                sheets.spreadsheets().get(sheetId).execute();
                result.set(true);
            } catch (IOException e) {
                result.set(false);
            }
            new Handler(Looper.getMainLooper()).post(() -> tcs.setResult(result.get()));
        });
        return tcs.getTask();
    }

    private Task<String> retrieveSpreadSheetTitle() {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.execute(() -> {
            String result;
            try {
                result = sheets.spreadsheets().get(spreadSheetId).execute().getProperties().getTitle();
            } catch (IOException e) {
                result = "";
            }
            String finalResult = result;
            new Handler(Looper.getMainLooper()).post(() -> tcs.setResult(finalResult));
        });
        return tcs.getTask();
    }

    private Task<Boolean> appendWithSheet(ValueRange valueRange) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        ExecutorService service = Executors.newFixedThreadPool(1);
        AtomicBoolean result = new AtomicBoolean(false);
        service.execute(() -> {
            try {
                sheets.spreadsheets().values().append(spreadSheetId, currentSheetTitle + "!A:F", valueRange)
                        .setValueInputOption("RAW")
                        .execute();
                result.set(true);
            } catch (IOException ioException) {
                result.set(false);
            }
            new Handler(Looper.getMainLooper()).post(() -> tcs.setResult(result.get()));
        });
        return tcs.getTask();
    }

    public String getSpreadSheetId() {
        return spreadSheetId;
    }

    public void setSpreadSheetId(String spreadSheetId) {
        this.spreadSheetId = spreadSheetId;
        editor.putString("spread_sheet_id", spreadSheetId);
        editor.apply();
    }

    public String getSpreadSheetTitle() {
        return spreadSheetTitle;
    }

    public void setSpreadSheetTitle(String spreadSheetTitle) {
        this.spreadSheetTitle = spreadSheetTitle;
        editor.putString("spread_sheet_title", spreadSheetTitle);
        editor.apply();
    }

    public String getCurrentSheetTitle() {
        return currentSheetTitle;
    }

    public void setCurrentSheetTitle(String currentSheetTitle) {
        this.currentSheetTitle = currentSheetTitle;
        editor.putString("current_sheet_title", currentSheetTitle);
        editor.apply();
    }
}
