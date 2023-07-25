package com.msnr.expense_updater.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.msnr.expense_updater.R;
import com.msnr.expense_updater.databinding.DialogEntryAreaBinding;
import com.msnr.expense_updater.utils.DatePicker;
import com.msnr.expense_updater.utils.Preference;

import java.util.ArrayList;
import java.util.List;

public class EnterExpenseDialog extends AppCompatDialogFragment {

    private DatePicker datePicker;
    private DialogEntryAreaBinding binding;

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

        //init spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.categories));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCategory.setAdapter(adapter);

        binding.etDate.setOnClickListener(v -> datePicker.showDialog());

        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        return dialog;
    }
}
