package com.msnr.expense_updater.activities.connection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.msnr.expense_updater.R;
import com.msnr.expense_updater.adapters.SheetTitleAdapter;
import com.msnr.expense_updater.databinding.ActivityConnectionBinding;
import com.msnr.expense_updater.databinding.ActivityMainBinding;
import com.msnr.expense_updater.dialogs.EnterExpenseDialog;
import com.msnr.expense_updater.utils.Methods;

import java.util.ArrayList;
import java.util.Objects;

public class ConnectionActivity extends AppCompatActivity implements SheetTitleAdapter.OnClick {

    private ActivityConnectionBinding binding;
    private ConnectionViewModel viewModel;

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> viewModel.handleSignInResult(result.getData())
    );

    private SheetTitleAdapter adapter;
    private ArrayList<String> sheetTitles;

    private final long timer = 10000;
    private Handler handler;
    private final Runnable runnable = () -> {
        binding.sheetTitleRv.setVisibility(View.GONE);
        binding.changeSheetBt.setText(R.string.change_sheet);
    };

    private final Observer<Integer> stateObserver = state -> {
        binding.connectWithGoogleLl.setVisibility(state == 1 ? View.VISIBLE : View.GONE);
        binding.connectWithSpreadSheetLl.setVisibility(state == 1 || state == 2 ? View.VISIBLE : View.GONE);
        binding.connectWithGoogleBt.setVisibility(state == 1 ? View.VISIBLE : View.GONE);
        binding.connectWithSpreadSheetBt.setVisibility(state == 1 || state == 2 ? View.VISIBLE : View.GONE);
        binding.connectWithSpreadSheetBt.setEnabled(state == 2);
        binding.spreadSheetIdIl.setVisibility(state == 1 || state == 2 ? View.VISIBLE : View.GONE);
        binding.spreadSheetIdIl.setEnabled(state == 2);
        binding.connectedToGoogleLl.setVisibility(state == 2 || state == 3 ? View.VISIBLE : View.GONE);
        binding.connectedToSpreadSheetLl.setVisibility(state == 3 ? View.VISIBLE : View.GONE);
        binding.detailsLl.setVisibility(state == 3 ? View.VISIBLE : View.GONE);

        if (state == 3) {
            String d4 = "Connected to " + viewModel.getSheetHelper().getSpreadSheetTitle() + " [" + viewModel.getSheetHelper().getCurrentSheetTitle() + "]";
            binding.dt4.setText(d4);
        }
    };


    @SuppressLint("NotifyDataSetChanged")
    private final Observer<ArrayList<String>> titlesObserver = list -> {
        sheetTitles.clear();
        sheetTitles.addAll(list);
        adapter.setSelectedTitle(viewModel.getSheetHelper().getCurrentSheetTitle());
        adapter.notifyDataSetChanged();
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ConnectionViewModel.class);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handler = new Handler();

        sheetTitles = new ArrayList<>(Objects.requireNonNull(viewModel.getSheetTitles().getValue()));
        adapter = new SheetTitleAdapter(sheetTitles, viewModel.getSheetHelper().getCurrentSheetTitle(), this);
        binding.sheetTitleRv.setLayoutManager(new GridLayoutManager(this, 2));
        binding.sheetTitleRv.setAdapter(adapter);

        viewModel.getState().observe(this, stateObserver);
        viewModel.getSheetTitles().observe(this, titlesObserver);

        initListeners();
    }

    private void initListeners() {
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
                viewModel.getSheetHelper().refreshSheetTitles().addOnSuccessListener(result -> {
                    handler.removeCallbacks(runnable);
                    handler = new Handler();
                    handler.postDelayed(runnable, timer);
                    viewModel.getSheetHelper().loadSheetDetails();
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

    }

    @Override
    public void onClick(String title) {
        Methods.cancelAlarm(this);
        viewModel.getSheetHelper().updateCurrentSheetTitle(title);
        adapter.setSelectedTitle(title);
        String d4 = "Connected to " + viewModel.getSheetHelper().getSpreadSheetTitle() + " [" + viewModel.getSheetHelper().getCurrentSheetTitle() + "]";
        binding.dt4.setText(d4);
        binding.sheetTitleRv.setVisibility(binding.sheetTitleRv.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        handler.removeCallbacks(runnable);
        binding.changeSheetBt.setText(R.string.change_sheet);
        Methods.setAlarm(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }
}