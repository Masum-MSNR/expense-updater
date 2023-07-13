package com.msnr.expense_updater.viewModels;


import static com.msnr.expense_updater.utils.C.PREFS_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.msnr.expense_updater.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeActivityVm extends ViewModel {

    public MutableLiveData<Integer> state;
    public Sheets sheetService;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    public String spreadSheetId, currentSheetTitle, spreadSheetTitle;
    public MutableLiveData<ArrayList<String>> sheetTitles;


    public void init(Context context) {
        state = new MutableLiveData<>();
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        sheetTitles = new MutableLiveData<>(new ArrayList<>());
        loadSheetDetails();
    }


    public GoogleSignInClient getGoogleSignInOption(Context context) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(SheetsScopes.SPREADSHEETS))
                        .requestEmail()
                        .build();
        return GoogleSignIn.getClient(context, signInOptions);
    }


    public void handleSignInResult(Intent result, Context context) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(SheetsScopes.SPREADSHEETS));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    state.setValue(2);
                    initiateSheetService(credential, context);
                })
                .addOnFailureListener(exception -> {
                    state.setValue(1);
                });
    }

    public void initiateSheetService(GoogleAccountCredential credential, Context context) {
        try {
            JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(context.getString(R.string.app_name))
                    .build();
        } catch (GeneralSecurityException | IOException ignored) {
        }
    }

    public Task<Boolean> loadSpreadSheet(String sheetId) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        ExecutorService service = Executors.newFixedThreadPool(1);
        AtomicBoolean result = new AtomicBoolean(false);
        ArrayList<String> tempTitles = new ArrayList<>();
        service.execute(() -> {
            try {
                Spreadsheet spreadsheet = this.sheetService.spreadsheets().get(sheetId).execute();
                spreadSheetId = spreadsheet.getSpreadsheetId();
                spreadSheetTitle = spreadsheet.getProperties().getTitle();
                for (Sheet sheet : spreadsheet.getSheets()) {
                    tempTitles.add(sheet.getProperties().getTitle());
                }
                currentSheetTitle = tempTitles.get(0);
                sheetTitles.getValue().clear();
                sheetTitles.postValue(tempTitles);
                state.postValue(3);
                result.set(true);
            } catch (IOException e) {
                result.set(false);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                saveSpreadSheetDetails();
                tcs.setResult(result.get());
            });
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
                Spreadsheet spreadsheet = this.sheetService.spreadsheets().get(spreadSheetId).execute();
                for (Sheet sheet : spreadsheet.getSheets()) {
                    tempTitles.add(sheet.getProperties().getTitle());
                }
                sheetTitles.getValue().clear();
                sheetTitles.postValue(tempTitles);
                result.set(true);
            } catch (IOException e) {
                result.set(false);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                saveSpreadSheetDetails();
                tcs.setResult(result.get());
            });
        });
        return tcs.getTask();
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
        sheetTitles.getValue().addAll(sheetSet);
    }

    public void clearSpreadSheetDetails() {
        editor.putString("spread_sheet_id", "");
        editor.putString("spread_sheet_title", "");
        editor.putString("current_sheet_title", "");
        Set<String> sheetSet = new TreeSet<>();
        editor.putStringSet("sheet_titles", sheetSet);
        editor.apply();
    }


    public void updateCurrentSheetTitle(String title) {
        currentSheetTitle = title;
        editor.putString("current_sheet_title", currentSheetTitle);
        editor.apply();
    }
}
