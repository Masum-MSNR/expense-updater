package com.msnr.expense_updater.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.msnr.expense_updater.R;
import com.msnr.expense_updater.adapters.SheetTitleAdapter;
import com.msnr.expense_updater.databinding.ActivitySettingsBinding;
import com.msnr.expense_updater.viewModels.HomeActivityVm;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements SheetTitleAdapter.OnClick {

    ActivitySettingsBinding binding;
    HomeActivityVm viewModel;
    ActivityResultLauncher<Intent> launcher;
    SheetTitleAdapter adapter;
    ArrayList<String> sheetTitles;
    String selectedTitle;
    long timer = 10000;
    Handler handler;
    Runnable runnable = () -> {
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
                String d4 = "Connected to " + viewModel.spreadSheetTitle + " [" + viewModel.currentSheetTitle + "]";
                binding.dt4.setText(d4);
                binding.detailsLl.setVisibility(View.VISIBLE);
                break;
        }
    };

    private final Observer<ArrayList<String>> titlesObserver = list -> {
        sheetTitles.clear();
        sheetTitles.addAll(list);
        adapter.notifyDataSetChanged();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        handler = new Handler();
        viewModel = new ViewModelProvider(this).get(HomeActivityVm.class);
        viewModel.init(this);
        viewModel.state.setValue(0);
        sheetTitles = new ArrayList<>(viewModel.sheetTitles.getValue());
        selectedTitle = viewModel.currentSheetTitle;

        adapter = new SheetTitleAdapter(sheetTitles, viewModel.currentSheetTitle, this);
        binding.sheetTitleRv.setLayoutManager(new GridLayoutManager(this, 2));
        binding.sheetTitleRv.setAdapter(adapter);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> viewModel.handleSignInResult(result.getData(), this));

        //observer
        viewModel.state.observe(this, stateObserver);
        viewModel.sheetTitles.observe(this, titlesObserver);

        start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                start();
        }
    }

    private void start() {
        viewModel.state.setValue(1);

        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            this, Collections.singleton(SheetsScopes.SPREADSHEETS));
            credential.setSelectedAccount(Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)).getAccount());
            viewModel.initiateSheetService(credential, this);
            viewModel.state.setValue(2);
            if (!viewModel.spreadSheetId.equals("")) {
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
            viewModel.loadSpreadSheet(binding.spreadSheetIdEt.getText().toString()).addOnSuccessListener(result -> {
                adapter.setSelectedTitle(viewModel.currentSheetTitle);
            });
        });

        binding.changeSheetBt.setOnClickListener(v -> {
            if (binding.sheetTitleRv.getVisibility() == View.VISIBLE) {
                handler.removeCallbacks(runnable);
                handler = new Handler();
                handler.postDelayed(runnable, timer);
                binding.changeSheetBt.setEnabled(false);
                binding.changeSheetBt.setText("---");
                viewModel.refreshSheetTitles().addOnSuccessListener(result -> {
                    handler.removeCallbacks(runnable);
                    handler = new Handler();
                    handler.postDelayed(runnable, timer);
                    viewModel.loadSheetDetails();
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
            viewModel.clearSpreadSheetDetails();
            viewModel.state.setValue(2);
        });
    }


    @Override
    public void onClick(String title) {
        viewModel.updateCurrentSheetTitle(title);
        adapter.setSelectedTitle(title);
        String d4 = "Connected to " + viewModel.spreadSheetTitle + " [" + viewModel.currentSheetTitle + "]";
        binding.dt4.setText(d4);
        binding.sheetTitleRv.setVisibility(binding.sheetTitleRv.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        handler.removeCallbacks(runnable);
        binding.changeSheetBt.setText(R.string.change_sheet);
    }
}