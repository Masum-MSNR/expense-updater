package com.msnr.expense_updater.repo;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.msnr.expense_updater.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Objects;

public class SheetsRepo {
    public static SheetsRepo instance;

    Sheets sheets;
    String spreadSheetId, currentSheetTitle;


    public SheetsRepo(Context context) {
        this.sheets = getSheetService(context);
    }

    public static SheetsRepo getInstance(Context context) {
        if (instance == null)
            instance = new SheetsRepo(context);
        return instance;
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
