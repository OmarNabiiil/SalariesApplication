package com.example.developer001.greenzoneapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

public class DateActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextView date;
    private int selected_month;
    private int selected_year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);

        date= (TextView) findViewById(R.id.txt_date);
    }

    public void SubmitTime(View view) {
        int month=selected_month;
        int year=selected_year;
        Bundle b=new Bundle();
        b.putInt("month", month);
        b.putInt("year", year);
        Intent i=new Intent(DateActivity.this, ReadingCardActivity.class);
        i.putExtra("Date", b);
        startActivity(i);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selected_month=month;
        selected_year=year;
        date.setText(month+" - "+year);
    }

    public void SelectDate(View view) {
        DatePickerFragment pd=new DatePickerFragment();
        pd.setListener(this);
        pd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
    }

    /*public void getSelectedDate(){
        date= (DatePicker) findViewById(R.id.simpleDatePicker);


        int day=date.getDayOfMonth();
        int month=date.getMonth()+1;
        int year=date.getYear();

        Log.d("day", ""+day);
        Log.d("month", ""+month);
        Log.d("year", ""+year);

        Bundle b=new Bundle();
        b.putInt("day", day);
        b.putInt("month", month);
        b.putInt("year", year);
        Intent i=new Intent(DateActivity.this, ReadingCardActivity.class);
        i.putExtra("Date", b);
        startActivity(i);
    }*/
}
