package com.msnr.expense_updater.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.msnr.expense_updater.R;
import com.msnr.expense_updater.activities.connection.ConnectionActivity;
import com.msnr.expense_updater.databinding.ActivityMainBinding;
import com.msnr.expense_updater.dialogs.EnterExpenseDialog;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    private final Observer<Integer> stateObserver = state -> {
        binding.connectLl.setVisibility(state == 3 ? View.GONE : View.VISIBLE);
        binding.btnSetUp.setVisibility(state == 3 ? View.GONE : View.VISIBLE);
        binding.btnAddExpense.setVisibility(state == 3 ? View.VISIBLE : View.GONE);
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setSupportActionBar(binding.toolbar);

        viewModel.getState().observe(this, stateObserver);

        initListeners();
    }

    private void initListeners() {

        binding.btnSetUp.setOnClickListener(v -> {
            startActivity(new Intent(this, ConnectionActivity.class));
        });


        binding.btnAddExpense.setOnClickListener(v -> {
            EnterExpenseDialog dialog = new EnterExpenseDialog();
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), "Enter Expense");
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.set_up) {
            startActivity(new Intent(this, ConnectionActivity.class));
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
}