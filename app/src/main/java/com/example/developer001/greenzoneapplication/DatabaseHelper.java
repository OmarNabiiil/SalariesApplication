package com.example.developer001.greenzoneapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 5;

    // Database Name
    private static final String DATABASE_NAME = "employeeManager";

    // Table Names
    private static final String TABLE_EMPLOYER_SALARY = "employer_salary";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // Employer_salary column name
    private static final String KEY_NAME = "name";
    private static final String KEY_EMPLOYER_ID = "employer_id";
    private static final String KEY_SALARY="salary";
    private static final String KEY_STATE="state";
    private static final String KEY_FINGERPRINT="fingerprint";
    private static final String KEY_MONTH="month";
    private static final String KEY_YEAR="year";

    private static final String CREATE_TABLE_EMPLOYER_SALARY="CREATE TABLE "
            + TABLE_EMPLOYER_SALARY + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME
            + " TEXT," + KEY_SALARY + " REAL," + KEY_EMPLOYER_ID + " INTEGER," + KEY_STATE+ " NUMERIC," + KEY_FINGERPRINT+ " BLOB," + KEY_MONTH + " INTEGER," + KEY_YEAR + " INTEGER," + KEY_CREATED_AT + " DATETIME" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_EMPLOYER_SALARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYER_SALARY);

        // create new tables
        onCreate(db);

    }

    public void addEmployerSalary(String name, double salary, int employer_id, int state, int month, int year){

        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues value=new ContentValues();
        value.put(KEY_NAME, name);
        value.put(KEY_SALARY, salary);
        value.put(KEY_EMPLOYER_ID, employer_id);
        value.put(KEY_STATE, state);
        value.put(KEY_MONTH, month);
        value.put(KEY_YEAR, year);
        value.put(KEY_CREATED_AT, getDateTime());

        db.insert(TABLE_EMPLOYER_SALARY,null,value);

    }

    public void addEmployerSalary(String name, double salary, int employer_id, int state, int month, int year, byte[] fingerprint){

        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues value=new ContentValues();
        value.put(KEY_NAME, name);
        value.put(KEY_SALARY, salary);
        value.put(KEY_EMPLOYER_ID, employer_id);
        value.put(KEY_STATE, state);
        value.put(KEY_FINGERPRINT, fingerprint);
        value.put(KEY_MONTH, month);
        value.put(KEY_YEAR, year);
        value.put(KEY_CREATED_AT, getDateTime());

        db.insert(TABLE_EMPLOYER_SALARY,null,value);

    }

    public List<Employer> getMonthsSalary(int month, int year){ //this method gets all the employers received their salaries at this specific date
        List<Employer> all_employers=new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_EMPLOYER_SALARY + " WHERE " + KEY_MONTH + " = " + month + " AND " + KEY_YEAR + " = " + year;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                String name=c.getString(c.getColumnIndex(KEY_NAME));
                long id=c.getLong(c.getColumnIndex(KEY_EMPLOYER_ID));
                int state=c.getInt(c.getColumnIndex(KEY_STATE));
                String date=c.getString(c.getColumnIndex(KEY_CREATED_AT));
                double salary=c.getDouble(c.getColumnIndex(KEY_SALARY));
                int salary_month=month;
                Log.e(LOG, ""+salary_month);
                Log.e(LOG, date);
                Log.e(LOG, ""+salary);
                Employer e=new Employer();
                e.setName(name);
                e.setID((int) id);
                e.setState(state==1?"No Match":"Matched");
                e.setSalary(salary);
                e.setReceived_date(date);
                e.setSalary_month(salary_month);
                all_employers.add(e);
            } while (c.moveToNext());
        }

        return all_employers;
    }

    public boolean isAlreadySubmitted(int id, int month, int year){ // this method checks if the employer already received his salary at this month of the year or not

        String selectQuery = "SELECT  * FROM " + TABLE_EMPLOYER_SALARY + " WHERE " + KEY_MONTH + " = " + month + " AND " + KEY_YEAR + " = " + year + " AND " + KEY_EMPLOYER_ID + " = " + id;

        return isAlreadyExist(selectQuery);
    }

    public boolean isAlreadyExist(String query){
        SQLiteDatabase sqldb = this.getReadableDatabase();
        Cursor cursor = sqldb.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * get datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

}
