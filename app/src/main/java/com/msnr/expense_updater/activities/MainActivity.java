package com.msnr.expense_updater.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.msnr.expense_updater.R;
import com.msnr.expense_updater.adapters.SheetTitleAdapter;
import com.msnr.expense_updater.databinding.ActivityMainBinding;
import com.msnr.expense_updater.dialogs.EnterExpenseDialog;
import com.msnr.expense_updater.utils.Methods;
import com.msnr.expense_updater.viewModels.SettingsActivityVm;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SheetTitleAdapter.OnClick {

    private ActivityMainBinding binding;
    private SettingsActivityVm viewModel;
    private ActivityResultLauncher<Intent> launcher;
    private SheetTitleAdapter adapter;
    private ArrayList<String> sheetTitles;
    private final long timer = 10000;
    private Handler handler;
    private final Runnable runnable = () -> {
        binding.sheetTitleRv.setVisibility(View.GONE);
        binding.changeSheetBt.setText(R.string.change_sheet);
    };


    private final Observer<Integer> stateObserver = integer -> {
        switch (integer) {
            case 1:
                binding.connectWithGoogleLl.setVisibility(View.VISIBLE);
                binding.connectWithSpreadSheetLl.setVisibility(View.VISIBLE);
                binding.connectWithGoogleBt.setVisibility(View.VISIBLE);
                binding.connectWithSpreadSheetBt.setVisibility(View.VISIBLE);
                binding.connectWithSpreadSheetBt.setEnabled(false);
                binding.spreadSheetIdIl.setVisibility(View.VISIBLE);
                binding.spreadSheetIdIl.setEnabled(false);
                binding.connectedToGoogleLl.setVisibility(View.GONE);
                binding.connectedToSpreadSheetLl.setVisibility(View.GONE);
                binding.detailsLl.setVisibility(View.GONE);
                break;
            case 2:
                binding.connectWithGoogleLl.setVisibility(View.GONE);
                binding.connectWithSpreadSheetLl.setVisibility(View.VISIBLE);
                binding.connectWithGoogleBt.setVisibility(View.GONE);
                binding.connectWithSpreadSheetBt.setVisibility(View.VISIBLE);
                binding.connectWithSpreadSheetBt.setEnabled(true);
                binding.spreadSheetIdIl.setVisibility(View.VISIBLE);
                binding.spreadSheetIdIl.setEnabled(true);
                binding.connectedToGoogleLl.setVisibility(View.VISIBLE);
                binding.connectedToSpreadSheetLl.setVisibility(View.GONE);
                binding.detailsLl.setVisibility(View.GONE);
                break;
            case 3:
                binding.connectWithGoogleLl.setVisibility(View.GONE);
                binding.connectWithSpreadSheetLl.setVisibility(View.GONE);
                binding.connectWithGoogleBt.setVisibility(View.GONE);
                binding.connectWithSpreadSheetBt.setVisibility(View.GONE);
                binding.spreadSheetIdIl.setVisibility(View.GONE);
                binding.connectedToGoogleLl.setVisibility(View.VISIBLE);
                binding.connectedToSpreadSheetLl.setVisibility(View.VISIBLE);
                String d4 = "Connected to " + viewModel.sheetHelper.getSpreadSheetTitle() + " [" + viewModel.sheetHelper.getCurrentSheetTitle() + "]";
                binding.dt4.setText(d4);
                binding.detailsLl.setVisibility(View.VISIBLE);
                break;
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    private final Observer<ArrayList<String>> titlesObserver = list -> {
        sheetTitles.clear();
        sheetTitles.addAll(list);
        adapter.setSelectedTitle(viewModel.sheetHelper.getCurrentSheetTitle());
        adapter.notifyDataSetChanged();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handler = new Handler();
        viewModel = new ViewModelProvider(this).get(SettingsActivityVm.class);
        sheetTitles = new ArrayList<>(Objects.requireNonNull(viewModel.sheetTitles.getValue()));
        adapter = new SheetTitleAdapter(sheetTitles, viewModel.sheetHelper.getCurrentSheetTitle(), this);
        binding.sheetTitleRv.setLayoutManager(new GridLayoutManager(this, 2));
        binding.sheetTitleRv.setAdapter(adapter);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> viewModel.handleSignInResult(result.getData()));

        //observer
        viewModel.state.observe(this, stateObserver);
        viewModel.sheetTitles.observe(this, titlesObserver);

        start();

    }

    private void start() {
        viewModel.state.setValue(1);

        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            viewModel.state.setValue(2);
            if (!viewModel.sheetHelper.getSpreadSheetId().equals("")) {
                viewModel.state.setValue(3);
            }
        }

        //listeners
        binding.connectWithGoogleBt.setOnClickListener(v -> {
            launcher.launch(viewModel.getGoogleSignInOption(this).getSignInIntent());
        });

        binding.connectWithSpreadSheetBt.setOnClickListener(v -> {
            if (binding.spreadSheetIdEt.getText().toString().equals("")) {
                binding.spreadSheetIdEt.setError("Please enter a spreadsheet id!");
                return;
            }
            viewModel.connectSheet(binding.spreadSheetIdEt.getText().toString());
        });

        binding.changeSheetBt.setOnClickListener(v -> {
            if (binding.sheetTitleRv.getVisibility() == View.VISIBLE) {
                handler.removeCallbacks(runnable);
                handler = new Handler();
                handler.postDelayed(runnable, timer);
                binding.changeSheetBt.setEnabled(false);
                binding.changeSheetBt.setText("---");
                viewModel.sheetHelper.refreshSheetTitles().addOnSuccessListener(result -> {
                    handler.removeCallbacks(runnable);
                    handler = new Handler();
                    handler.postDelayed(runnable, timer);
                    viewModel.sheetHelper.loadSheetDetails();
                    binding.changeSheetBt.setEnabled(true);
                    binding.changeSheetBt.setText(R.string.refresh);
                }).addOnFailureListener(e -> {
                    handler.removeCallbacks(runnable);
                    handler = new Handler();
                    handler.postDelayed(runnable, timer);
                    binding.changeSheetBt.setEnabled(true);
                    binding.changeSheetBt.setText(R.string.refresh);
                });
            } else {
                binding.changeSheetBt.setText(R.string.refresh);
                binding.sheetTitleRv.setVisibility(View.VISIBLE);
                handler.postDelayed(runnable, timer);
            }

        });

        binding.disconnectBt.setOnClickListener(v -> {
            viewModel.disconnectSheet();
        });

        binding.btnAddExpense.setOnClickListener(v -> {
            EnterExpenseDialog dialog = new EnterExpenseDialog();
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), "Enter Expense");
        });
    }

    @Override
    public void onClick(String title) {
        Methods.cancelAlarm(this);
        viewModel.sheetHelper.updateCurrentSheetTitle(title);
        adapter.setSelectedTitle(title);
        String d4 = "Connected to " + viewModel.sheetHelper.getSpreadSheetTitle() + " [" + viewModel.sheetHelper.getCurrentSheetTitle() + "]";
        binding.dt4.setText(d4);
        binding.sheetTitleRv.setVisibility(binding.sheetTitleRv.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        handler.removeCallbacks(runnable);
        binding.changeSheetBt.setText(R.string.change_sheet);
        Methods.setAlarm(this);
    }
}