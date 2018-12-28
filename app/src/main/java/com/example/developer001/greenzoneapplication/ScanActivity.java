package com.example.developer001.greenzoneapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.developer001.greenzoneapplication.capture.AuthBfdCap;
import com.example.developer001.greenzoneapplication.capture.MorphoTabletFPSensorDevice;
import com.example.developer001.greenzoneapplication.support.Utils;
import com.morpho.morphosmart.sdk.ErrorCodes;

import java.io.IOException;
import java.util.HashMap;

public class ScanActivity extends Activity implements AuthBfdCap,
        View.OnClickListener {

    private static final String TAG = Class.class.getSimpleName();
    //Define GUI parameters
    private Button refresh_btn;
    private Button readTag;
    private Button verify;
    private ImageView imgFP1;
    private ImageView imgFP2;
    private TextView barcode;
    private TextView arb_name;
    private TextView eng_name;
    private TextView birth_date;
    private TextView publish_date;
    private TextView expire_date;
    private TextView email;
    private TextView company;

    //Define Functionality parameters
    //private byte[] greenzonekey=MifareClassic.KEY_DEFAULT;
    private byte[] greenzonekey={(byte)0xF0, (byte)0x87, (byte)0x58, (byte)0x7B, (byte)0xB0, (byte)0xB9};
    public static MorphoTabletFPSensorDevice fpSensorCap;
    private NfcAdapter mAdapter;
    private IntentFilter writeTagFilters[];
    private MifareClassic mTag;
    private static byte[] readed_temp1;
    private static byte[] readed_temp2;
    private int original_size;
    boolean success=true;
    private ProgressDialog pDialog;
    boolean isWorking = false;
    private static boolean verified=false;
    private static boolean fp1=false;
    private static boolean fp2=false;
    Bitmap fpImgBM1=null;
    Bitmap fpImgBM2=null;
    ScanActivity ref;
    boolean fpImg1=false;
    boolean fpImg2=false;
    boolean case1=false;
    boolean case2=false;
    boolean case3=false;
    boolean case4=false;
    boolean case4_second=false;
    boolean refresh=false;
    private int fp1Borders=R.drawable.edittext_bg;
    private int fp2Borders=R.drawable.edittext_bg;
    private int currentApiVersion;
    HashMap<String,Integer> first_result;
    HashMap<String,Integer> second_result;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        //initialize GUI
        initGUI();

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcInit();

        fpSensorCap = new MorphoTabletFPSensorDevice(this);
        fpSensorCap.open(this);

        ref=(ScanActivity)getLastNonConfigurationInstance();
        if(ref!=null && !ref.isRefresh()){
            restoreData();
        }

    }

    public void restoreData(){
        fp1Borders=ref.getFp1Borders();
        fp2Borders=ref.getFp2Borders();

        if(ref.getFpImgBM1()!=null){
            //Log.d("fp1","success");
            fpImgBM1=ref.getFpImgBM1();
            imgFP1.setImageBitmap(ref.getFpImgBM1());
            imgFP1.setBackgroundResource(ref.getFp1Borders());
        }
        if(ref.getFpImgBM2()!=null){
            fpImgBM2=ref.getFpImgBM2();
            imgFP2.setImageBitmap(ref.getFpImgBM2());
            imgFP2.setBackgroundResource(ref.getFp2Borders());
        }
        if(!ref.isSuccess()){
            success=false;
        }
        if(ref.getmTag()!=null){
            mTag=ref.getmTag();
        }
        if(!ref.getBarcode().getText().toString().equals("Barcode")){
            String bc=ref.getBarcode().getText().toString();
            barcode.setText(bc);
        }
        if(!ref.getArb_name().getText().toString().equals("الاسم بالعربي")){
            String an=ref.getArb_name().getText().toString();
            arb_name.setText(an);
        }
        if(!ref.getEng_name().getText().toString().equals("English name")){
            String an=ref.getEng_name().getText().toString();
            eng_name.setText(an);
        }
        if(!ref.getBirth_date().getText().toString().equals("الميلاد")){
            String bd=ref.getBirth_date().getText().toString();
            birth_date.setText(bd);
        }
        if(!ref.getExpire_date().getText().toString().equals("الانتهاء")){
            String bd=ref.getExpire_date().getText().toString();
            expire_date.setText(bd);
        }
        if(!ref.getPublish_date().getText().toString().equals("الاصدار")){
            String bd=ref.getPublish_date().getText().toString();
            publish_date.setText(bd);
        }
        if(!ref.getEmail().getText().toString().equals("البريد")){
            String bd=ref.getEmail().getText().toString();
            email.setText(bd);
        }
        if(!ref.getCompany().getText().toString().equals("شركة التوظيف")){
            String bd=ref.getCompany().getText().toString();
            company.setText(bd);
        }
    }

    public Object onRetainNonConfigurationInstance() {
        return this;
    }

    public void initGUI(){
        //initialize buttons
        refresh_btn=(Button)findViewById(R.id.refresh_btn);
        refresh_btn.setOnClickListener(this);
        refresh_btn.setEnabled(false);
        readTag=(Button)findViewById(R.id.read_btn);
        readTag.setOnClickListener(this);
        verify=(Button)findViewById(R.id.verify_btn);
        verify.setOnClickListener(this);
        verify.setEnabled(false);

        //initialize textviews
        barcode=(TextView)findViewById(R.id.txt_barcode);
        arb_name=(TextView)findViewById(R.id.txt_arname);
        eng_name=(TextView)findViewById(R.id.txt_enname);
        birth_date=(TextView)findViewById(R.id.txt_birthdate);
        publish_date=(TextView)findViewById(R.id.txt_publishdate);
        expire_date=(TextView)findViewById(R.id.txt_expiredate);
        email=(TextView)findViewById(R.id.txt_email);
        company=(TextView)findViewById(R.id.txt_company);

        //initialize imageview
        imgFP1=(ImageView)findViewById(R.id.imgFP1);

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void nfcInit(){
        // Intent filters for writing to a tag
        IntentFilter tagDetected = new IntentFilter();
        tagDetected.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    /** To know status of sensor Working/ideal */
    public void setButtonEnabled(boolean enabled) {
        isWorking = !enabled;
    }

    public void showAlertDialog(String message){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ScanActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ScanActivity.this);
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

    @Override
    protected void onNewIntent(Intent intent){
        // Tag writing mode
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag myTag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            mTag=MifareClassic.get(myTag);
            if(mTag!=null){
                if(mTag.getSize()!=MifareClassic.SIZE_4K){
                    showAlertDialog("this card is not compatible with the system!");
                }else{
                    try {
                        mTag.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                showAlertDialog("this card is not compatible with the system!");
            }

        }
    }

    public void startCapture(){
        fpSensorCap.setViewToUpdate(imgFP1);
        try {
            fpSensorCap.startCapture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.equals(verify)){
            if(readed_temp1==null && readed_temp2==null){
                showAlertDialog("This card doesn't contain any finger prints !");
                case1=true;
            }else{
                if(readed_temp1!=null && readed_temp2==null){
                    case2=true;
                    startCapture();
                }else{
                    if(readed_temp1==null && readed_temp2!=null){
                        case3=true;
                        startCapture();
                    }else{
                        if(readed_temp1!=null && readed_temp2!=null){
                            case4=true;
                            startCapture();
                        }
                    }
                }
            }

        }
        if(v.equals(readTag)){
            if(mTag!=null){
                CardReadTask task=new CardReadTask();
                task.execute();
                refresh_btn.setEnabled(true);
                verify.setEnabled(true);
            }else{
                showAlertDialog("Please insert your card");
            }
        }
        if(v.equals(refresh_btn)){
            verified=false;
            fp1=false;
            fp2=false;
            refresh=true;
            readed_temp1=null;
            readed_temp2=null;
            this.recreate();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void updateImageView(final ImageView imgPreview, final Bitmap previewBitmap, String message, final boolean flagComplete, final int captureError, final int matchingScore) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (imgPreview != null) {
                    if(fpImg1){
                        fpImgBM1=previewBitmap;
                    }else{
                        if(fpImg2){
                            fpImgBM2=previewBitmap;
                        }
                    }
                    imgPreview.setImageBitmap(previewBitmap);
                }

                if (captureError == ErrorCodes.MORPHOERR_TIMEOUT) {
                    showAlertDialog("Time Out!");
                    setButtonEnabled(true);

                    return;
                } else if (captureError == ErrorCodes.MORPHOERR_CMDE_ABORTED) {
                    showAlertDialog("Command has been aborted !");

                    setButtonEnabled(true);
                    return;
                }else if (captureError == ErrorCodes.MORPHOERR_NO_HIT) {
                    showAlertDialog("Authentication failed !");
                    setButtonEnabled(true);
                    return;
                }

                if (flagComplete && captureError == ErrorCodes.MORPHO_OK) {
                    setButtonEnabled(true);

                    if(case2){ //template 1 only exist
                        HashMap<String,Integer>res=fpSensorCap.verifyMatch(readed_temp1,fpSensorCap.templateBuffer);
                        String[] results=calculateResults(res);
                        showAlertDialog(results[0]+", "+results[1]);
                        case2=false;
                    }
                    if(case3){ //template 2 only exist
                        HashMap<String,Integer>res=fpSensorCap.verifyMatch(readed_temp2,fpSensorCap.templateBuffer);
                        String[] results=calculateResults(res);
                        showAlertDialog(results[0]+", "+results[1]);
                        case3=false;
                    }
                    if(case4){ //both templates exist
                        first_result=fpSensorCap.verifyMatch(readed_temp1,fpSensorCap.templateBuffer);
                        second_result=fpSensorCap.verifyMatch(readed_temp2,fpSensorCap.templateBuffer);
                        String[] results1=calculateResults(first_result);

                        String[] results2=calculateResults(second_result);
                        showAlertDialog("First fingerprint: "+results1[0]+", "+results1[1]+"\n"+"Second fingerprint: "+results2[0]+", "+results2[1]);
                        case4=false;
                    }

                }
            }
        });
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
        String matching_msg="Matching Score : "+matching_percentage+"%";
        results[0]=error_msg;
        results[1]=matching_msg;

        return results;
    }

    public byte[] readFpFromCard(int startIndex,int index_size){

        try{

            if(mTag.authenticateSectorWithKeyA(mTag.blockToSector(index_size), greenzonekey)){
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
                    if(mTag.authenticateSectorWithKeyA(mTag.blockToSector(i), greenzonekey)){
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

    public String readCard(int start,int end,int blocks_needed){
        try{
            byte[] out=new byte[16*blocks_needed];
            //byte[] readed_temp=new byte[original_size];
            int divider=4;
            int count=0;

            for(int i = start; i < end; i++){
                if(i % divider != divider-1){
                    if(mTag.authenticateSectorWithKeyA(mTag.blockToSector(i), greenzonekey)){
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

    public void getInfo(){

        final String showArName;
        final String showEnName;
        final String showBirthDate;
        final String showpubDate;
        final String showExpDate;
        final String showEmail;
        final String showCompany;

        //read barcode
        final String showBarcode=readCard(5,7,2);

        if(success){
            //read arabic name
            showArName=readCard(8,15,6);
        }else{
            return;
        }

        if(success){
            //read english name
            showEnName=readCard(16,23,6);
        }else{
            return;
        }

        if(success){
            //read birth date
            showBirthDate=readCard(36,39,3);
        }else{
            return;
        }

        if(success){
            //read publish date
            showpubDate=readCard(46,47,1);
        }else{
            return;
        }

        if(success){
            //read expire date
            showExpDate=readCard(40,41,1);

        }else{
            return;
        }

        if(success){
            //read email
            showEmail=readCard(56,59,3);
        }else{
            return;
        }

        if(success){
            //read company
            showCompany=readCard(48,55,6);
        }else{
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                barcode.setText(showBarcode);
                arb_name.setText(showArName);
                eng_name.setText(showEnName);
                birth_date.setText(showBirthDate);
                publish_date.setText(showpubDate);
                expire_date.setText(showExpDate);
                email.setText(showEmail);
                company.setText(showCompany);
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


    private class VerifyTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(ScanActivity.this);


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


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //hide the dialog
            asyncDialog.dismiss();

            super.onPostExecute(result);
        }

    }

    private class CardReadTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(ScanActivity.this);


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

            if(readed_temp1!=null){
                while(true){
                    success=true;
                    readed_temp2=readFpFromCard(192,126);
                    if(success){
                        //Log.d("size2=",""+readed_temp2.length);
                        break;
                    }
                }
            }

            while(true){
                success=true;
                getInfo();
                if(success){
                    //Log.d("size2=",""+readed_temp2.length);
                    verified=false;
                    break;
                }
            }
            if(!verified){


            }else{

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //hide the dialog
            asyncDialog.dismiss();

            super.onPostExecute(result);
        }

    }

    public ImageView getImgFP1() {
        return imgFP1;
    }

    public ImageView getImgFP2() {
        return imgFP2;
    }

    public TextView getBarcode() {
        return barcode;
    }

    public TextView getArb_name() {
        return arb_name;
    }

    public TextView getEng_name() {
        return eng_name;
    }

    public TextView getBirth_date() {
        return birth_date;
    }

    public TextView getPublish_date() {
        return publish_date;
    }

    public TextView getExpire_date() {
        return expire_date;
    }

    public TextView getEmail() {
        return email;
    }

    public TextView getCompany() {
        return company;
    }

    public static MorphoTabletFPSensorDevice getFpSensorCap() {
        return fpSensorCap;
    }

    public NfcAdapter getmAdapter() {
        return mAdapter;
    }

    public IntentFilter[] getWriteTagFilters() {
        return writeTagFilters;
    }

    public MifareClassic getmTag() {
        return mTag;
    }

    public static byte[] getReaded_temp1() {
        return readed_temp1;
    }

    public static byte[] getReaded_temp2() {
        return readed_temp2;
    }

    public int getOriginal_size() {
        return original_size;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public static boolean isVerified() {
        return verified;
    }

    public static boolean isFp1() {
        return fp1;
    }

    public static boolean isFp2() {
        return fp2;
    }

    public Bitmap getFpImgBM1() {
        return fpImgBM1;
    }

    public Bitmap getFpImgBM2() {
        return fpImgBM2;
    }

    public ScanActivity getRef() {
        return ref;
    }

    public boolean isFpImg1() {
        return fpImg1;
    }

    public boolean isFpImg2() {
        return fpImg2;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public int getFp1Borders() {
        return fp1Borders;
    }

    public int getFp2Borders() {
        return fp2Borders;
    }
}
