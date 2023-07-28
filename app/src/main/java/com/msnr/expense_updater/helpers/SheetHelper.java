package com.msnr.expense_updater.helpers;

import static com.msnr.expense_updater.utils.Consts.PREFS_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.msnr.expense_updater.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SheetHelper {

    private final Context context;
    private String spreadSheetId, spreadSheetTitle, currentSheetTitle;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;
    private MutableLiveData<ArrayList<String>> sheetTitles;

    public Sheets sheets;

    public SheetHelper(Context context) {
        this.context = context;
        this.sheets = getSheetService(context);
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        sheetTitles = new MutableLiveData<>(new ArrayList<>());
        loadSheetDetails();
    }

    public Task<Boolean> loadSpreadSheet(String sheetId) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        ExecutorService service = Executors.newFixedThreadPool(1);
        AtomicBoolean result = new AtomicBoolean(false);
        ArrayList<String> tempTitles = new ArrayList<>();
        service.execute(() -> {
            try {
                Spreadsheet spreadsheet = sheets.spreadsheets().get(sheetId).execute();
                spreadSheetId = spreadsheet.getSpreadsheetId();
                spreadSheetTitle = spreadsheet.getProperties().getTitle();
                for (Sheet sheet : spreadsheet.getSheets()) {
                    tempTitles.add(sheet.getProperties().getTitle());
                }
                currentSheetTitle = tempTitles.get(0);
                sheetTitles.getValue().clear();
                sheetTitles.postValue(tempTitles);
                result.set(true);
            } catch (IOException e) {
                e.printStackTrace();
                result.set(false);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                saveSpreadSheetDetails();
                tcs.setResult(result.get());
            });
        });
        return tcs.getTask();
    }

    public Task<Integer> loadLastEmptyIndex() {
        TaskCompletionSource<Integer> tcs = new TaskCompletionSource<>();
        ExecutorService service = Executors.newFixedThreadPool(1);
        AtomicInteger result = new AtomicInteger(-1);
        service.execute(() -> {
            try {
                ValueRange response = sheets.spreadsheets().values()
                        .get(spreadSheetId, currentSheetTitle + "!A8:A")
                        .execute();
                List<List<Object>> values = response.getValues();
                int lastNotEmptyRowIndex = 0;
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        for (Object cell : row) {
                            if (!cell.toString().equals("")) {
                                lastNotEmptyRowIndex = i;
                            }
                        }
                    }
                    result.set(lastNotEmptyRowIndex + 9);
                } else {
                    result.set(8);
                }
            } catch (IOException e) {
                result.set(-1);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                tcs.setResult(result.get());
            });
        });
        return tcs.getTask();
    }

    public Task<List<List<Object>>> getSpecificRange(String range, int[] filter) {
        TaskCompletionSource<List<List<Object>>> tcs = new TaskCompletionSource<>();
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.execute(() -> {
            List<List<Object>> result;
            List<List<Object>> filteredResult = new ArrayList<>();

            try {
                ValueRange response = sheets.spreadsheets().values()
                        .get(spreadSheetId, currentSheetTitle + "!" + range)
                        .execute();
                result = response.getValues();
                for (int i = 0; i < result.size(); i++) {
                    boolean read = false;
                    for (int k : filter) {
                        if (i == k) {
                            read = true;
                            break;
                        }
                    }
                    if (read) {
                        filteredResult.add(result.get(i));
                    }
                }
            } catch (IOException e) {
                filteredResult = null;
            }
            List<List<Object>> finalResult = filteredResult;
            new Handler(Looper.getMainLooper()).post(() -> tcs.setResult(finalResult));
        });
        return tcs.getTask();
    }

    public Task<Boolean> refreshSheetTitles() {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        ExecutorService service = Executors.newFixedThreadPool(1);
        AtomicBoolean result = new AtomicBoolean(false);
        ArrayList<String> tempTitles = new ArrayList<>();
        service.execute(() -> {
            try {
                Spreadsheet spreadsheet = sheets.spreadsheets().get(spreadSheetId).execute();
                for (Sheet sheet : spreadsheet.getSheets()) {
                    tempTitles.add(sheet.getProperties().getTitle());
                }
                sheetTitles.getValue().clear();
                sheetTitles.postValue(tempTitles);
                result.set(true);
            } catch (IOException e) {
                e.printStackTrace();
                result.set(false);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                saveSpreadSheetDetails();
                tcs.setResult(result.get());
            });
        });
        return tcs.getTask();
    }

//    public Task<Boolean> appendWithSheet(ValueRange valueRange) {
//        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
//        ExecutorService service = Executors.newFixedThreadPool(1);
//        AtomicBoolean result = new AtomicBoolean(false);
//        service.execute(() -> {
//            try {
//                sheets.spreadsheets().values().append(spreadSheetId, currentSheetTitle + "!A:F", valueRange)
//                        .setValueInputOption("RAW")
//                        .execute();
//                result.set(true);
//            } catch (IOException ioException) {
//                result.set(false);
//            }
//            new Handler(Looper.getMainLooper()).post(() -> tcs.setResult(result.get()));
//        });
//        return tcs.getTask();
//    }

    public Task<Boolean> updateSpecificRow(int row,ValueRange valueRange) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        ExecutorService service = Executors.newFixedThreadPool(1);
        AtomicBoolean result = new AtomicBoolean(false);
        service.execute(() -> {
            try {
                sheets.spreadsheets().values().update(spreadSheetId, currentSheetTitle + "!A"+row+":M"+row, valueRange)
                        .setValueInputOption("USER_ENTERED")
                        .execute();
                result.set(true);
            } catch (IOException ioException) {
                result.set(false);
                ioException.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(() -> tcs.setResult(result.get()));
        });
        return tcs.getTask();
    }

    public void updateSheetService() {
        sheets = getSheetService(context);
    }

    private Sheets getSheetService(Context context) {
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            context, Collections.singleton(SheetsScopes.SPREADSHEETS));
            credential.setSelectedAccount(Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(context)).getAccount());
            try {
                JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
                NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(context.getString(R.string.app_name))
                        .build();
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    public void saveSpreadSheetDetails() {
        editor.putString("spread_sheet_id", spreadSheetId);
        editor.putString("spread_sheet_title", spreadSheetTitle);
        editor.putString("current_sheet_title", currentSheetTitle);
        Set<String> sheetSet = new TreeSet<>(sheetTitles.getValue());
        editor.putStringSet("sheet_titles", sheetSet);
        editor.apply();
    }

    public void loadSheetDetails() {
        spreadSheetId = preferences.getString("spread_sheet_id", "");
        spreadSheetTitle = preferences.getString("spread_sheet_title", "");
        currentSheetTitle = preferences.getString("current_sheet_title", "");
        Set<String> sheetSet = preferences.getStringSet("sheet_titles", new TreeSet<>());
        sheetTitles = new MutableLiveData<>(new ArrayList<>(sheetSet));
    }

    public void disconnect() {
        editor.putString("spread_sheet_id", "");
        editor.putString("spread_sheet_title", "");
        editor.putString("current_sheet_title", "");
        editor.putStringSet("sheet_titles", new TreeSet<>());
        editor.apply();
    }

    public void updateCurrentSheetTitle(String title) {
        currentSheetTitle = title;
        editor.putString("current_sheet_title", currentSheetTitle);
        editor.apply();
    }

    public MutableLiveData<ArrayList<String>> getSheetTitles() {
        return sheetTitles;
    }

    public String getCurrentSheetTitle() {
        return currentSheetTitle;
    }

    public String getSpreadSheetId() {
        return spreadSheetId;
    }

    public String getSpreadSheetTitle() {
        return spreadSheetTitle;
    }
}
