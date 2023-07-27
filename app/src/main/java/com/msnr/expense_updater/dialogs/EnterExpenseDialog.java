package com.msnr.expense_updater.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.api.client.util.Value;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.msnr.expense_updater.R;
import com.msnr.expense_updater.databinding.DialogEntryAreaBinding;
import com.msnr.expense_updater.helpers.SheetHelper;
import com.msnr.expense_updater.utils.DatePicker;
import com.msnr.expense_updater.utils.Preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EnterExpenseDialog extends AppCompatDialogFragment {

    private DatePicker datePicker;
    private DialogEntryAreaBinding binding;
    private String selectedCategory = "Home Food";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.Dialog);
        binding = DialogEntryAreaBinding.inflate(getLayoutInflater());

        datePicker = new DatePicker();
        datePicker.initDialog(requireActivity(), binding.etDate);

        List<String> names = Preference.getNames(requireActivity());

        binding.etPaid1.setHint(names.get(0));
        binding.etPaid2.setHint(names.get(1));
        binding.etPaid3.setHint(names.get(2));
        binding.etPaid4.setHint(names.get(3));


        binding.etDate.setOnClickListener(v -> datePicker.showDialog());

        textChangeListener(binding.etDate);
        textChangeListener(binding.etItemDescription);

        //init spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.categories));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCategory.setAdapter(adapter);

        binding.spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        paidListener(binding.etPaid1);
        paidListener(binding.etPaid2);
        paidListener(binding.etPaid3);
        paidListener(binding.etPaid4);

        shareListener(binding.etShare1);
        shareListener(binding.etShare2);
        shareListener(binding.etShare3);
        shareListener(binding.etShare4);

        binding.cbShare1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.etShare1.setEnabled(!isChecked);
            binding.etShare1.setText(isChecked ? "1" : "");
        });

        binding.cbShare2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.etShare2.setEnabled(!isChecked);
            binding.etShare2.setText(isChecked ? "1" : "");
        });

        binding.cbShare3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.etShare3.setEnabled(!isChecked);
            binding.etShare3.setText(isChecked ? "1" : "");
        });

        binding.cbShare4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.etShare4.setEnabled(!isChecked);
            binding.etShare4.setText(isChecked ? "1" : "");
        });

        binding.btnAddExpense.setOnClickListener(v -> {
            if (!formValidator()) return;

            String date = binding.etDate.getText().toString();
            String itemDescription = binding.etItemDescription.getText().toString();
            String paid1 = binding.etPaid1.getText().toString();
            String paid2 = binding.etPaid2.getText().toString();
            String paid3 = binding.etPaid3.getText().toString();
            String paid4 = binding.etPaid4.getText().toString();

            String finalPaid1 = paid1.isEmpty() ? "0" : paid1;
            String finalPaid2 = paid2.isEmpty() ? "0" : paid2;
            String finalPaid3 = paid3.isEmpty() ? "0" : paid3;
            String finalPaid4 = paid4.isEmpty() ? "0" : paid4;

            String share1 = binding.etShare1.getText().toString();
            String share2 = binding.etShare2.getText().toString();
            String share3 = binding.etShare3.getText().toString();
            String share4 = binding.etShare4.getText().toString();

            String finalShare1  = share1.isEmpty() ? "0" : share1;
            String finalShare2 = share2.isEmpty() ? "0" : share2;
            String finalShare3 = share3.isEmpty() ? "0" : share3;
            String finalShare4 = share4.isEmpty() ? "0" : share4;


            SheetHelper sheetHelper = new SheetHelper(requireActivity());

            sheetHelper.loadLastEmptyIndex().addOnSuccessListener(result -> {
                ValueRange valueRange = new ValueRange().setValues(Collections.singletonList(
                        Arrays.asList(date, selectedCategory, itemDescription, "=SUM(E" + result + ":H" + result + ")", finalPaid1, finalPaid2, finalPaid3, finalPaid4, "", finalShare1, finalShare2, finalShare3, finalShare4)));
                sheetHelper.updateSpecificRow(result, valueRange).addOnSuccessListener(res->{
                    Toast.makeText(requireActivity(), "Expense Added", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            });


        });

        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        return dialog;
    }

    private boolean formValidator() {
        if (binding.etDate.getText().toString().isEmpty()) {
            binding.etDate.setError("Required!");
            return false;
        }
        if (binding.etItemDescription.getText().toString().isEmpty()) {
            binding.etItemDescription.setError("Required!");
            return false;
        }

        if (binding.etPaid1.getText().toString().isEmpty()
                && binding.etPaid2.getText().toString().isEmpty()
                && binding.etPaid3.getText().toString().isEmpty()
                && binding.etPaid4.getText().toString().isEmpty()
        ) {
            binding.etPaid1.setError("Required!");
            binding.etPaid2.setError("Required!");
            binding.etPaid3.setError("Required!");
            binding.etPaid4.setError("Required!");
            return false;
        }

        if (binding.etShare1.getText().toString().isEmpty()
                && binding.etShare2.getText().toString().isEmpty()
                && binding.etShare3.getText().toString().isEmpty()
                && binding.etShare4.getText().toString().isEmpty()
        ) {
            binding.etShare1.setError("Required!");
            binding.etShare2.setError("Required!");
            binding.etShare3.setError("Required!");
            binding.etShare4.setError("Required!");
            return false;
        }

        return true;
    }

    private void textChangeListener(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void paidListener(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.etPaid1.setError(null);
                binding.etPaid2.setError(null);
                binding.etPaid3.setError(null);
                binding.etPaid4.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void shareListener(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.etShare1.setError(null);
                binding.etShare2.setError(null);
                binding.etShare3.setError(null);
                binding.etShare4.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
