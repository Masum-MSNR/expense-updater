package com.msnr.expense_updater.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.msnr.expense_updater.R;
import com.msnr.expense_updater.databinding.DialogEntryAreaBinding;
import com.msnr.expense_updater.utils.DatePicker;

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

        binding.etDate.setOnClickListener(v -> datePicker.showDialog());

        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogFadeAnimation;
        return dialog;
    }
}
