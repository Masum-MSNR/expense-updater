package com.msnr.expense_updater.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.msnr.expense_updater.R;
import com.msnr.expense_updater.databinding.AdapterSheetTitleBinding;

import java.util.ArrayList;

public class SheetTitleAdapter extends RecyclerView.Adapter<SheetTitleAdapter.ViewHolder> {

    Context context;
    ArrayList<String> titles;
    OnClick listener;
    String selectedTitle;

    public SheetTitleAdapter(ArrayList<String> titles, String selectedTitle, OnClick listener) {
        this.titles = titles;
        this.selectedTitle = selectedTitle;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_sheet_title, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        int p = h.getAdapterPosition();
        h.binding.titleTv.setText(titles.get(p));
        if (selectedTitle.equals(titles.get(p))) {
            h.binding.cv.setCardBackgroundColor(context.getResources().getColor(R.color.light_dark_gray));
        } else {
            h.binding.cv.setCardBackgroundColor(context.getResources().getColor(R.color.lighter_gray));
        }
        h.itemView.setOnClickListener(v -> {
            listener.onClick(titles.get(p));
        });
    }

    public void setSelectedTitle(String selectedTitle) {
        this.selectedTitle = selectedTitle;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AdapterSheetTitleBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = AdapterSheetTitleBinding.bind(itemView);
        }
    }

    public interface OnClick {
        void onClick(String title);
    }
}
