package com.msnr.expense_updater.serviceHelpers;

import static com.msnr.expense_updater.utils.C.PREFS_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.msnr.expense_updater.R;
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
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BGSheetsServiceHelper {

    Context context;
    Sheets sheets;
    String spreadSheetId, currentSheetTitle;

    public BGSheetsServiceHelper(Context context) {
        this.context = context;
        this.sheets = getSheetService(context);
        loadSpreadSheetDetails();
    }

    private void loadSpreadSheetDetails() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        spreadSheetId = preferences.getString("spread_sheet_id", null);
        currentSheetTitle = preferences.getString("current_sheet_title", null);
    }


    public Task<Boolean> appendWithSheet(ValueRange valueRange) {
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
}
