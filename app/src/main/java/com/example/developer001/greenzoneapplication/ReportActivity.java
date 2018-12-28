package com.example.developer001.greenzoneapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.PageOrientation;
import jxl.format.PaperSize;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import static com.morpho.android.usb.USBManager.context;

public class ReportActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private RecyclerView recyclerView;
    private List<Employer> row_list;
    private MyAdapter mAdapter;
    private TableRow headers;
    private TextView date;
    boolean exported=false;
    private HorizontalScrollView hScroll;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = new DatabaseHelper(getApplicationContext());

        date= (TextView) findViewById(R.id.txt_date);
        headers= (TableRow) findViewById(R.id.my_header);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        hScroll = (HorizontalScrollView) findViewById(R.id.scrollHorizontal);
        row_list = new ArrayList<>();
        mAdapter = new MyAdapter(getApplicationContext(), row_list);

        hScroll.setOnTouchListener(new View.OnTouchListener() { //outer scroll listener
            private float mx, my, curX, curY;
            private boolean started = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                recyclerView.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            private float mx, my, curX, curY;
            private boolean started = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

    }

    public void SelectDate(View view) {
        ReportDateFragment pd=new ReportDateFragment();
        pd.setListener(this);
        pd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month_from, int month_to) {
        //get all the months salaries from the database here

        if(month_from<month_to){
            date.setText("Months : from "+month_from+" to "+month_to+", Year : "+year);
            List<Employer> all_salaries=new ArrayList<>();
            for(int current_month=month_from; current_month<=month_to; current_month++){
                all_salaries.addAll(db.getMonthsSalary(current_month, year));
            }
            row_list.clear();
            row_list.addAll(all_salaries);

            mAdapter.notifyDataSetChanged();
        }else{
            if(month_from==month_to){
                date.setText(month_from+" - "+year);
                List<Employer> all_salaries=db.getMonthsSalary(month_from, year);
                row_list.clear();
                row_list.addAll(all_salaries);

                mAdapter.notifyDataSetChanged();
            }else{
                showAlertDialog("you should choose a reasonable range!");
            }
        }

    }

    public void Save(View view) {
        if(!row_list.isEmpty()){
            exportToExcel();
            exported=true;
            Toast.makeText(this, "Exported successfully",
                    Toast.LENGTH_LONG).show();
        }else{
            showAlertDialog("No data to export !");
        }
    }

    private void exportToExcel() {
        final String fileName = "SalariesList.xls";
        //create directory if not exist

        File direct = new File(Environment.getExternalStorageDirectory() + "/Salaries report");

        File file = new File(direct, fileName);
        Log.d("report test","entered "+getFilesDir().getAbsolutePath());

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet("SalariesList", 0);


            try {
                sheet.addCell(new Label(0, 0, "ID")); // column and row
                sheet.addCell(new Label(1, 0, "Name"));
                sheet.addCell(new Label(2, 0, "Salary"));
                sheet.addCell(new Label(3, 0, "State"));
                sheet.addCell(new Label(4, 0, "Received Date"));
                sheet.addCell(new Label(5, 0, "Salary Month"));

                //add the list to the excel sheet here

                for(int i=0; i<row_list.size(); i++){
                    Employer e=row_list.get(i);
                    sheet.addCell(new Label(0, i+1, e.getID()+""));
                    sheet.addCell(new Label(1, i+1, e.getName()+""));
                    sheet.addCell(new Label(2, i+1, e.getSalary()+""));
                    sheet.addCell(new Label(3, i+1, e.getState()+""));
                    sheet.addCell(new Label(4, i+1, e.getReceived_date()+""));
                    sheet.addCell(new Label(5, i+1, e.getSalary_month()+""));
                }
                sheet.setPageSetup(PageOrientation.PORTRAIT, PaperSize.A4,0.3, 0.3);

            } catch (RowsExceededException e) {
                e.printStackTrace();
                Log.e("catchone",e.getMessage()+"");
            } catch (WriteException e) {
                e.printStackTrace();
                Log.e("catchtwo",e.getMessage()+"");
            }
            workbook.write();
            try {
                workbook.close();
            } catch (WriteException e) {
                e.printStackTrace();
                Log.e("catchthree",e.getMessage()+"");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("catchfour",e.getMessage()+"");
        }
    }

    public void showAlertDialog(String message){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ReportActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ReportActivity.this);
        }
        builder.setTitle("Warning!")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void Print(View view) {
        if(!exported){
            exportToExcel();
        }
        Intent i=new Intent(ReportActivity.this, PrintActivity.class);
        startActivity(i);
    }
}
