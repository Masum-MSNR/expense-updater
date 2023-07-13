package com.msnr.expense_updater.repo;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.sheets.v4.Sheets;

public class UserRepo {
    public static UserRepo instance;

    GoogleAccountCredential credential;
    Sheets service;


    public UserRepo() {
    }

    public static UserRepo getInstance() {
        if (instance == null)
            instance = new UserRepo();
        return instance;
    }

    public GoogleAccountCredential getCredential() {
        return credential;
    }

    public void setCredential(GoogleAccountCredential credential) {
        this.credential = credential;
    }

    public Sheets getService() {
        return service;
    }

    public void setService(Sheets service) {
        this.service = service;
    }
}
