package com.msnr.expense_updater.activities.connection;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.msnr.expense_updater.helpers.SheetHelper;
import com.msnr.expense_updater.repo.StateRepo;
import com.msnr.expense_updater.utils.Methods;
import com.msnr.expense_updater.utils.Preference;

import java.util.ArrayList;

public class ConnectionViewModel extends AndroidViewModel {

    private SheetHelper sheetHelper;
    private StateRepo stateRepo;

    public ConnectionViewModel(@NonNull Application application) {
        super(application);
        init(application.getApplicationContext());
    }

    private void init(Context context) {
        stateRepo = StateRepo.getInstance();
        sheetHelper = new SheetHelper(context);

        stateRepo.setState(1);
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            stateRepo.setState(2);
            if (!sheetHelper.getSpreadSheetId().equals("")) {
                stateRepo.setState(3);
            }
        }
    }


    public GoogleSignInClient getGoogleSignInOption(Context context) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestScopes(new Scope(SheetsScopes.SPREADSHEETS)).requestEmail().build();
        return GoogleSignIn.getClient(context, signInOptions);
    }


    public void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result).addOnSuccessListener(googleAccount -> {
            stateRepo.setState(2);
            sheetHelper.updateSheetService();
        }).addOnFailureListener(exception -> {
            stateRepo.setState(1);
        });
    }

    public void connectSheet(String sheetId) {
        sheetHelper.loadSpreadSheet(sheetId).addOnSuccessListener(aBoolean -> {
            if (aBoolean) {
                stateRepo.setState(3);
                Methods.setAlarm(getApplication().getApplicationContext());
                sheetHelper.getSpecificRange("E1:H1", new int[]{0}).addOnSuccessListener(result -> {
                    Preference.setNames(result.get(0), getApplication().getApplicationContext());
                });
            }
        });
    }

    public void disconnectSheet() {
        sheetHelper.disconnect();
        stateRepo.setState(2);
        Methods.cancelAlarm(getApplication().getApplicationContext());
    }


    public SheetHelper getSheetHelper() {
        return sheetHelper;
    }

    public MutableLiveData<Integer> getState() {
        return stateRepo.getState();
    }

    public MutableLiveData<ArrayList<String>> getSheetTitles() {
        return sheetHelper.getSheetTitles();
    }
}
