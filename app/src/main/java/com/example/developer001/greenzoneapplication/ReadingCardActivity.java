package com.example.developer001.greenzoneapplication;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.developer001.greenzoneapplication.capture.AuthBfdCap;
import com.example.developer001.greenzoneapplication.capture.MorphoTabletFPSensorDevice;
import com.example.developer001.greenzoneapplication.support.Utils;
import com.morpho.morphosmart.sdk.ErrorCodes;

import java.io.IOException;
import java.util.HashMap;

public class ReadingCardActivity extends AppCompatActivity implements AuthBfdCap {

    private Button save;
    private EditText salary;
    private TextView txt_name;
    private TextView txt_id;
    private ImageView imgFP;
    private int employer_id;
    private String employer_name;
    private int selected_month;
    private int selected_year;
    private double salary_value;


    private MifareClassic mTag;
    private NfcAdapter mAdapter;
    private IntentFilter writeTagFilters[];

    private static byte[] readed_temp1;


    private DatabaseHelper db; //reference to the sqllite database that contain the CRUD API's

    private int original_size;
    private boolean success;
    private boolean verified=false;
    public static MorphoTabletFPSensorDevice fpSensorCap;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_report){
            Intent i=new Intent(ReadingCardActivity.this, ReportActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_card);

        save= (Button) findViewById(R.id.btn_save);
        salary= (EditText) findViewById(R.id.txt_sallary);
        imgFP= (ImageView) findViewById(R.id.img_fp);
        txt_id= (TextView) findViewById(R.id.txt_id);
        txt_name= (TextView) findViewById(R.id.txt_name);

        db = new DatabaseHelper(getApplicationContext());

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcInit();

        fpSensorCap = new MorphoTabletFPSensorDevice(this);
        fpSensorCap.open(this);

        showAlertDialog("please put your card on the reader");

    }

    private void nfcInit(){
        // Intent filters for writing to a tag
        IntentFilter tagDetected = new IntentFilter();
        tagDetected.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    public void startCapture(){
        fpSensorCap.setViewToUpdate(imgFP);

        try {
            fpSensorCap.startCapture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlertDialog(String message){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ReadingCardActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ReadingCardActivity.this);
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

    public byte[] readFpFromCard(int startIndex,int index_size){

        try{

            if(mTag.authenticateSectorWithKeyA(mTag.blockToSector(index_size), MifareClassic.KEY_DEFAULT)){
                byte[] size=mTag.readBlock(index_size);
                String s = new String(size, "UTF-8");

                String showSize="";
                for(int i=0;i<s.length();i++){
                    if(Character.isDigit(s.charAt(i))){
                        showSize=showSize+s.charAt(i);
                    }else{
                        break;
                    }
                }
                try{

                    original_size=Integer.parseInt(showSize);
                }catch(NumberFormatException e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAlertDialog("this card doesn't hold any data!");
                        }
                    });

                    success=true;
                    return null;
                }

            }else{
                success=false;
                //Log.d("Exception","false authentication" + " " + index_size);
                return null ;
            }

            int blocks_needed=(int) Math.ceil((double)original_size/16);
            byte[] out=new byte[16*blocks_needed];
            byte[] readed_temp=new byte[original_size];
            int divider=startIndex<128?4:16;
            int count=0;

            for(int i = startIndex; i <
                    (startIndex+blocks_needed); i++){
                if(i % divider != divider-1){
                    if(mTag.authenticateSectorWithKeyA(mTag.blockToSector(i), MifareClassic.KEY_DEFAULT)){
                        //Log.d("Success","Done authenticating block" +" "+i);

                        byte[] temp=mTag.readBlock(i);
                        System.arraycopy(temp,0,out,count*16,16);
                        count=count+1;

                    }else{
                        success=false;
                        //Log.d("Exception","false authentication" + " " + i);
                        return null;
                    }
                }
            }

            for(int j=0;j<readed_temp.length;j++){
                readed_temp[j]=out[j];
            }
            return readed_temp;
        }catch(IOException e){
            //Log.d("orgsize","omaaaaaarIO");
        }

        return  null;
    }

    public String readBlocks(int block) {
        String output = "";
        try {
            if (mTag.authenticateSectorWithKeyA(mTag.blockToSector(block), MifareClassic.KEY_DEFAULT)) {
                byte[] out = mTag.readBlock(block);
                output = new String(Base64.decode(out, Base64.DEFAULT));
                Log.d("Success", "Done authenticating block" + " " + block);
                Log.d("Readed", "" + output);

            } else {

                Log.d("Exception", "false authentication" + " " + block);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    public String readCard(int start,int end,int blocks_needed){
        try{
            byte[] out=new byte[16*blocks_needed];
            //byte[] readed_temp=new byte[original_size];
            int divider=4;
            int count=0;

            for(int i = start; i < end; i++){
                if(i % divider != divider-1){
                    if(mTag.authenticateSectorWithKeyA(mTag.blockToSector(i), MifareClassic.KEY_DEFAULT)){
                        //Log.d("Success","Done authenticating block" +" "+i);

                        byte[] temp=mTag.readBlock(i);
                        System.arraycopy(temp,0,out,count*16,16);
                        count=count+1;

                    }else{
                        success=false;
                        //Log.d("Exception","false authentication" + " " + i);
                        return null;
                    }
                }
            }
            String s = new String(out, "UTF-8");
            String showOut="";
            for(int i=0;i<s.length();i++){
                if(Character.isLetterOrDigit(s.charAt(i))){
                    showOut=showOut+s.charAt(i);
                }
            }
            //Log.d("showString", showOut);
            return  showOut;
        }catch(IOException e){
            //Log.d("orgsize","omaaaaaarIO");
        }
        return null;
    }

    public void getDataFromCard(){
        final String showEnName;
        final String showCompany;
        final String showDevelop;
        final String showEmail;
        final String showAge;
        final String showMobile;
        final String showAddress;

        //read barcode
        final String showId=readBlocks(8);

        if(success){
            //read arabic name
            showEnName=readCard(4,5,1);
        }else{
            return;
        }

        /*if(success){
            //read english name
            showCompany=readCard(16,23,1);
        }else{
            return;
        }

        if(success){
            //read birth date
            showDevelop=readCard(36,39,3);
        }else{
            return;
        }

        if(success){
            //read publish date
            showEmail=readCard(46,47,1);
        }else{
            return;
        }

        if(success){
            //read expire date
            showAddress=readCard(40,41,1);

        }else{
            return;
        }*/

        if(success){
            //read email
            showEmail=readCard(12,13,1);
        }else{
            return;
        }

        if(success){
            //read company
            showCompany=readCard(16,17,1);
        }else{
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_id.setText(showId);
                txt_name.setText(showEnName);

                /*eng_name.setText(showEnName);
                birth_date.setText(showBirthDate);
                publish_date.setText(showpubDate);
                expire_date.setText(showExpDate);*/
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent){ //read the card here and save a reference to it
        // Tag writing mode
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag myTag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            mTag= MifareClassic.get(myTag);
            if(mTag!=null){
                if(mTag.getSize()!=MifareClassic.SIZE_4K && mTag.getSize()!=MifareClassic.SIZE_1K){
                    showAlertDialog("this card is not compatible with the system!");
                }else{
                    try {
                        mTag.connect();
                        new CardReadTask().execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                showAlertDialog("this card is not compatible with the system!");
            }

        }
    }

    @Override
    public void updateImageView(final ImageView imgPreview, final Bitmap previewBitmap, String message, final boolean flagComplete, final int captureError, final int matchingScore) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (imgPreview != null) {

                    imgPreview.setImageBitmap(previewBitmap);
                }

                if (captureError == ErrorCodes.MORPHOERR_TIMEOUT) {
                    showAlertDialog("Time Out!");

                    return;
                } else if (captureError == ErrorCodes.MORPHOERR_CMDE_ABORTED) {
                    showAlertDialog("Command has been aborted !");
                    return;
                }else if (captureError == ErrorCodes.MORPHOERR_NO_HIT) {
                    showAlertDialog("Authentication failed !");
                    return;
                }

                if (flagComplete && captureError == ErrorCodes.MORPHO_OK) {

                    HashMap<String,Integer>res=fpSensorCap.verifyMatch(readed_temp1,fpSensorCap.templateBuffer);
                    String[] results=calculateResults(res);
                    showAlertDialog(results[0]+", "+results[1]);
                    if(verified){

                        db.addEmployerSalary(employer_name, salary_value, employer_id, 0, selected_month, selected_year);
                        //db.addSalary(salaary,employer_id);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                Utils.showWirelessSettingsDialog(this);
            }else{

                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                mAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(fpSensorCap!=null){
            fpSensorCap.cancelLiveAcquisition();
        }
        if (mAdapter != null) {
            //Disable foreground dispatch to the given activity.
            //An activity must call this method before its onPause() callback completes.
            mAdapter.disableForegroundDispatch(this);
        }
    }

    public void SaveOnDatabase(View view) {
        //step 1: verify fingerprint on the card
        //step 2: save data on the database
        AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
        view.startAnimation(buttonClick);
        Bundle b=getIntent().getBundleExtra("Date");
        selected_month=b.getInt("month");
        selected_year=b.getInt("year");
        salary_value=Double.parseDouble(salary.getText().toString());
        Log.d("test_id",""+employer_id);
        Log.d("test_salaary",""+salary_value);

        if(mTag!=null){
            if(!db.isAlreadySubmitted(employer_id, selected_month, selected_year)){
                CheckBox match=(CheckBox) findViewById(R.id.match_check);
                if(!match.isChecked()){
                    if(!salary.getText().equals("")){
                        startCapture();
                    }
                }else{
                    db.addEmployerSalary(employer_name, salary_value, employer_id, 1, selected_month, selected_year, readed_temp1);
                }
            }else{
                showAlertDialog("This ID already has received his salary this month!");
            }
        }else{
            showAlertDialog("please put the card on the reader!");
        }


    }

    public String[] calculateResults(HashMap<String,Integer>res){
        String error_msg="";
        String[] results=new String[2];
        switch(res.get("error")){
            case -8: error_msg="Authentication failed !"; break;
            case -26: error_msg="Command has been aborted !"; break;
            case 0: error_msg="No errors"; break;
            case -47: error_msg="The finger can be too moist or the scanner is wet !"; break;
        }
        double value=res.get("matching score");
        double factor=17000;
        double matching_percentage=(value/factor)*100;
        if(matching_percentage>=50.0){
            verified=true;
        }
        String matching_msg="Matching Score : "+matching_percentage+"%";
        results[0]=error_msg;
        results[1]=matching_msg;

        return results;
    }

    public void MatchingInstruction(View view) {
        showAlertDialog("If you don't want to match please check the box");
    }

    private class CardReadTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(ReadingCardActivity.this);


        @Override
        protected void onPreExecute() {
            //set message of the dialog
            asyncDialog.setMessage("Wait until process complete please hold on to your card and don't remove it");
            //show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //don't touch dialog here it'll break the application
            //do some lengthy stuff like calling login webservice

            while(true){
                success=true;
                readed_temp1=readFpFromCard(128,124);
                if(success){
                    //Log.d("size1=",""+readed_temp1.length);
                    break;
                }
            }

            while(true){
                success=true;
                getDataFromCard();
                if(success){
                    //Log.d("size2=",""+readed_temp2.length);
                    verified=false;
                    break;
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //hide the dialog
            asyncDialog.dismiss();

            showAlertDialog("Card read successfully");

            employer_id=Integer.parseInt(txt_id.getText().toString());
            employer_name=txt_name.getText().toString();

            super.onPostExecute(result);
        }

    }
}
