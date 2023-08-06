package com.msnr.expense_updater.repo;

import androidx.lifecycle.MutableLiveData;

public class StateRepo {
    private static StateRepo instance;
    private MutableLiveData<Integer> state;

    private StateRepo() {
        state = new MutableLiveData<>();
    }

    public static StateRepo getInstance() {
        if (instance == null) {
            instance = new StateRepo();
        }
        return instance;
    }

    public MutableLiveData<Integer> getState() {
        return state;
    }

    public void setState(int state) {
        this.state.setValue(state);
    }
}
