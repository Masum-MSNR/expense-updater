package com.msnr.expense_updater.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.EditText;


import com.msnr.expense_updater.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePicker {

    private int _day, _month, _Year;
    DatePickerDialog.OnDateSetListener dateListener;
    Calendar myCalendar;
    DatePickerDialog datePickerDialog;
    EditText editText;

    public DatePicker() {
    }

    public void initDialog(Context context, EditText editText) {
        this.editText = editText;
        myCalendar = Calendar.getInstance();
        dateListener = (v, year, monthOfYear, dayOfMonth) -> {
            _Year = year;
            _month = monthOfYear + 1;
            _day = dayOfMonth;
            editText.setText(new StringBuilder().append(_month).append("/").append(_day).append("/").append(_Year));
        };
        datePickerDialog = new DatePickerDialog(context, R.style.customDatePickerDialogTheme, dateListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public void showDialog() {
        datePickerDialog.show();
    }

    public int getNewIntDate() {
        String date = "";
        date += _Year;
        if ((_month) < 10) {
            date += 0;
        }
        date += (_month);
        if (_day < 10) {
            date += 0;
        }
        date += _day;

        return Integer.parseInt(date);
    }

    public int getCurrentIntDate() {
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = ft.format(dNow);
        return Integer.parseInt(date);
    }
}
