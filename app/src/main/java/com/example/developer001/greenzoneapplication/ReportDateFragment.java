package com.example.developer001.greenzoneapplication;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReportDateFragment extends DialogFragment {

    private static final int MAX_YEAR = 2099;
    private DatePickerDialog.OnDateSetListener listener;

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Calendar cal = Calendar.getInstance();

        View dialog = inflater.inflate(R.layout.fragment_report_date, null);
        final NumberPicker monthPicker_from = (NumberPicker) dialog.findViewById(R.id.picker_month_from);
        final NumberPicker monthPicker_to = (NumberPicker) dialog.findViewById(R.id.picker_month_to);
        final NumberPicker yearPicker = (NumberPicker) dialog.findViewById(R.id.picker_year);

        monthPicker_from.setMinValue(1);
        monthPicker_from.setMaxValue(12);
        monthPicker_from.setValue(cal.get(Calendar.MONTH) + 1);

        monthPicker_to.setMinValue(1);
        monthPicker_to.setMaxValue(12);
        monthPicker_to.setValue(cal.get(Calendar.MONTH) + 1);

        int year = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(year);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setValue(year);

        builder.setView(dialog)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDateSet(null, yearPicker.getValue(), monthPicker_from.getValue(), monthPicker_to.getValue());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ReportDateFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}

