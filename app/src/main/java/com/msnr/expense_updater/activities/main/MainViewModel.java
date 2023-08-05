package com.msnr.expense_updater.activities.main;


import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.msnr.expense_updater.helpers.SheetHelper;
import com.msnr.expense_updater.repo.StateRepo;

public class MainViewModel extends AndroidViewModel {

    private SheetHelper sheetHelper;
    private StateRepo stateRepo;

    public MainViewModel(@NonNull Application application) {
        super(application);
        init(getApplication());
    }

    public void init(Context context) {
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

    public MutableLiveData<Integer> getState() {
        return stateRepo.getState();
    }

}
