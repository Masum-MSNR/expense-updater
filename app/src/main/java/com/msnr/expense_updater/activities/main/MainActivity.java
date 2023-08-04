package com.msnr.expense_updater.activities.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.msnr.expense_updater.databinding.ActivityMainBinding;
import com.msnr.expense_updater.dialogs.EnterExpenseDialog;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        start();

    }

    private void start() {
        binding.btnAddExpense.setOnClickListener(v -> {
            EnterExpenseDialog dialog = new EnterExpenseDialog();
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), "Enter Expense");
        });
    }
}