package com.arpaul.geocare;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.util.Predicate;
import com.arpaul.customalertlibrary.dialogs.CustomDialog;
import com.arpaul.customalertlibrary.popups.statingDialog.CustomPopupType;
import com.arpaul.customalertlibrary.popups.statingDialog.PopupListener;
import com.arpaul.geocare.common.AppConstant;
import com.arpaul.geocare.common.AppPreference;
import com.arpaul.geocare.dataaccess.GCCPConstants;
import com.arpaul.geocare.geofence.GeoFenceNotiService;
import com.arpaul.utilitieslib.FileUtils;
import com.arpaul.utilitieslib.LogUtils;
import com.arpaul.utilitieslib.PermissionUtils;
import com.arpaul.utilitieslib.UnCaughtException;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public abstract class BaseActivity extends AppCompatActivity implements PopupListener {

    public LayoutInflater baseInflater;
    public LinearLayout llBody;
    public Toolbar toolbarBase;

    private CustomDialog cDialog;
    public AppPreference preference;
    public Typeface tfRegular,tfBold;
    public FirebaseAnalytics mFirebaseAnalytics;
    private final String BASE_TAG = "BaseActivity";
    //https://developer.android.com/studio/publish/app-signing.html#release-mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(BaseActivity.this,"aritrarpal@gmail.com",getString(R.string.app_name)));

        setContentView(R.layout.activity_base);

        initialiseBaseControls();

        bindBaseControls();

        initialize();

        if (savedInstanceState == null) {
            startService(new Intent(BaseActivity.this, GeoFenceNotiService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (Build.VERSION.SDK_INT >= 21) {
                if(new PermissionUtils().checkPermission(BaseActivity.this, new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE}) != 0){
                    new PermissionUtils().verifyLocation(BaseActivity.this,new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, GeocareSettingsActivity.class));
            return true;
        } else if (id == R.id.action_signin) {
            startActivity(new Intent(this, SignInActivity.class));
            return true;
        } else if (id == R.id.action_chat) {
            startActivity(new Intent(this, ChatActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public abstract void initialize();

    private void bindBaseControls(){
        if(preference == null)
            preference = new AppPreference(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

//        setTitle("You");
    }

    public void showSettingsAlert()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showCustomDialog(getString(R.string.gpssettings),getString(R.string.gps_not_enabled),getString(R.string.settings),getString(R.string.cancel),getString(R.string.settings), CustomPopupType.DIALOG_ALERT,false);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int copyFile = 0;
        if (requestCode == 1) {
            for(int i = 0; i < permissions.length; i++){
                if(permissions[i].equalsIgnoreCase(android.Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[i] == 1)
                    copyFile++;
                else if(permissions[i].equalsIgnoreCase(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == 1)
                    copyFile++;
            }

            if(copyFile == 2)
                copyFile();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.debugLog(BASE_TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == AppConstant.REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);
                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                LogUtils.debugLog(BASE_TAG, "Invitations sent: " + ids.length);
            } else {
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "not sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);
                // Sending failed or it was canceled, show failure message to
                // the user
                LogUtils.debugLog(BASE_TAG, "Failed to send invitation.");
            }
        }
    }

    public void copyAppDbtoSdcard(){
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                if(new PermissionUtils().checkPermission(BaseActivity.this, new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE}) != 0){
                    new PermissionUtils().verifyLocation(BaseActivity.this,new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE});
                } else
                    copyFile();
            } else
                copyFile();

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void copyFile(){
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);

            File Db = new File(/*"/data/data/"*/getFilesDir().getPath() + info.packageName + "/databases/" + GCCPConstants.DATABASE_NAME);
            Date d = new Date();

            String path = Environment.getExternalStorageDirectory() + AppConstant.EXTERNAL_FOLDER;
            LogUtils.infoLog("FOLDER_PATH", path);
            File fileDir = new File(path);
            if(!fileDir.exists())
                fileDir.mkdirs();

            File file = new File(Environment.getExternalStorageDirectory() + AppConstant.EXTERNAL_FOLDER + GCCPConstants.DATABASE_NAME);
            file.createNewFile();
            file.setWritable(true);

            FileUtils.copyFile(new FileInputStream(Db), new FileOutputStream(file));
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Shows Dialog with user defined buttons.
     * @param title
     * @param message
     * @param okButton
     * @param noButton
     * @param from
     * @param isCancelable
     */
    public void showCustomDialog(final String title, final String message, final String okButton, final String noButton, final String from, boolean isCancelable){
        runOnUiThread(new RunShowDialog(title,message,okButton,noButton,from, isCancelable));
    }

    /**
     * Shows Dialog with user defined buttons.
     * @param title
     * @param message
     * @param okButton
     * @param noButton
     * @param from
     * @param dislogType
     * @param isCancelable
     */
    public void showCustomDialog(final String title, final String message, final String okButton, final String noButton, final String from, CustomPopupType dislogType, boolean isCancelable){
        runOnUiThread(new RunShowDialog(title,message,okButton,noButton,from, dislogType, isCancelable));
    }

    /**
     * Hides custom Dialog if open.
     */
    public void hideCustomDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cDialog != null && cDialog.isShowing())
                    cDialog.dismiss();
            }
        });
    }

    class RunShowDialog implements Runnable {
        private String strTitle;// FarmName of the materialDialog
        private String strMessage;// Message to be shown in materialDialog
        private String firstBtnName;
        private String secondBtnName;
        private String from;
        private String params;
        private boolean isCancelable=false;
        private CustomPopupType dislogType = CustomPopupType.DIALOG_NORMAL;
        public RunShowDialog(String strTitle, String strMessage, String firstBtnName, String secondBtnName, String from, boolean isCancelable)
        {
            this.strTitle 		= strTitle;
            this.strMessage 	= strMessage;
            this.firstBtnName 	= firstBtnName;
            this.secondBtnName	= secondBtnName;
            this.isCancelable 	= isCancelable;
            if (from != null)
                this.from = from;
            else
                this.from = "";
        }

        public RunShowDialog(String strTitle, String strMessage, String firstBtnName, String secondBtnName, String from, CustomPopupType dislogType, boolean isCancelable)
        {
            this.strTitle 		= strTitle;
            this.strMessage 	= strMessage;
            this.firstBtnName 	= firstBtnName;
            this.secondBtnName	= secondBtnName;
            this.dislogType     = dislogType;
            this.isCancelable 	= isCancelable;
            if (from != null)
                this.from = from;
            else
                this.from = "";
        }

        @Override
        public void run() {
            showNotNormal();
        }

        private void showNotNormal(){
            try{
                if (cDialog != null && cDialog.isShowing())
                    cDialog.dismiss();

                cDialog = new CustomDialog(BaseActivity.this, BaseActivity.this,strTitle,strMessage,
                        firstBtnName, secondBtnName, from, dislogType);

                cDialog.show();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnButtonYesClick(String from) {
        dialogYesClick(from);
    }

    @Override
    public void OnButtonNoClick(String from) {
        dialogNoClick(from);
    }

    public void dialogYesClick(String from) {
        if(from.contains(getString(R.string.settings))){
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            hideCustomDialog();
        }
    }

    public void dialogNoClick(String from) {
        if(from.equalsIgnoreCase("")){

        }
    }

    public void hideKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static ViewGroup getParentView(View v) {
        ViewGroup vg = null;

        if(v != null)
            vg = (ViewGroup) v.getRootView();

        return vg;
    }

    public static <T> Collection<T> filter(Collection<T> col, Predicate<T> predicate) {

        Collection<T> result = new ArrayList<T>();
        if(col!=null)
        {
            for (T element : col) {
                if (predicate.apply(element)) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    public static void applyTypeface(ViewGroup v, Typeface f, int style) {
        if(v != null) {
            int vgCount = v.getChildCount();
            for(int i=0;i<vgCount;i++) {
                if(v.getChildAt(i) == null) continue;
                if(v.getChildAt(i) instanceof ViewGroup)
                    applyTypeface((ViewGroup)v.getChildAt(i), f, style);
                else {
                    View view = v.getChildAt(i);
                    if(view instanceof TextView)
                        ((TextView)(view)).setTypeface(f, style);
                    else if(view instanceof EditText)
                        ((EditText)(view)).setTypeface(f, style);
                    else if(view instanceof Button)
                        ((Button)(view)).setTypeface(f, style);
                }
            }
        }
    }

    private void createTypeFace(){
        tfRegular  = Typeface.createFromAsset(this.getAssets(),"fonts/Myriad Pro Regular.ttf");
        tfBold       = Typeface.createFromAsset(this.getAssets(),"fonts/Myriad Pro Regular.ttf");
    }

    private void initialiseBaseControls(){
        baseInflater            = 	this.getLayoutInflater();
        llBody                  = (LinearLayout) findViewById(R.id.llBody);

        toolbarBase             = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbarBase);

        createTypeFace();
    }
}
