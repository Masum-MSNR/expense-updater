package com.msnr.expense_updater.viewModels;


import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.msnr.expense_updater.activities.SettingsActivity;
import com.msnr.expense_updater.serviceHelpers.SheetHelper;
import com.msnr.expense_updater.utils.Methods;
import com.msnr.expense_updater.utils.Preference;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivityVm extends AndroidViewModel {

    public MutableLiveData<Integer> state;
    public SheetHelper sheetHelper;
    public MutableLiveData<ArrayList<String>> sheetTitles;

    private final Observer<ArrayList<String>> sheetTitlesObserver = new Observer<ArrayList<String>>() {
        @Override
        public void onChanged(ArrayList<String> strings) {
            sheetTitles.setValue(strings);
        }
    };

    public SettingsActivityVm(@NonNull Application application) {
        super(application);
        init(application.getApplicationContext());
    }

    public void init(Context context) {
        state = new MutableLiveData<>();
        sheetTitles = new MutableLiveData<>(new ArrayList<>());
        sheetHelper = new SheetHelper(context);
        sheetHelper.getSheetTitles().observeForever(sheetTitlesObserver);
    }


    public GoogleSignInClient getGoogleSignInOption(Context context) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestScopes(new Scope(SheetsScopes.SPREADSHEETS)).requestEmail().build();
        return GoogleSignIn.getClient(context, signInOptions);
    }


    public void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result).addOnSuccessListener(googleAccount -> {
            state.setValue(2);
            sheetHelper.updateSheetService();
        }).addOnFailureListener(exception -> {
            state.setValue(1);
        });
    }

    public void connectSheet(String sheetId) {
        sheetHelper.loadSpreadSheet(sheetId).addOnSuccessListener(aBoolean -> {
            if (aBoolean) {
                state.setValue(3);
                Methods.setAlarm(getApplication().getApplicationContext());
                //load necessary data
                sheetHelper.getSpecificRange("E1:H1", new int[]{0}).addOnSuccessListener(result -> {
                    Preference.setNames(result.get(0), getApplication().getApplicationContext());
                });
            }
        });
    }

    public void disconnectSheet() {
        sheetHelper.disconnect();
        state.setValue(2);
        Methods.cancelAlarm(getApplication().getApplicationContext());
    }
}
