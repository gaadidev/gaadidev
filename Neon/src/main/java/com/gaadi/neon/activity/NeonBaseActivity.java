package com.gaadi.neon.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gaadi.neon.interfaces.OnPermissionResultListener;
import com.gaadi.neon.util.ManifestPermission;
import com.gaadi.neon.util.PermissionType;
import com.scanlibrary.R;

/**
 * @author princebatra
 * @version 1.0
 * @since 25/1/17
 */
public abstract class NeonBaseActivity extends AppCompatActivity {

    protected FrameLayout frameLayout;
    protected Toolbar toolbar;
    private OnPermissionResultListener permissionResultListener;
    private final int permissionRequestCode=1;
    private final int permissionCodeForAPI30=2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.NeonLibTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);

        if(toolbar!=null){
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        }
        if (getSupportActionBar() != null){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_arrow);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public void setTitle(CharSequence title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this,R.color.neon_toolbar_icons_color)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        super.setTitle(s);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void askForPermissionIfNeeded(PermissionType permissionType,
                                            OnPermissionResultListener listener) throws ManifestPermission {

        permissionResultListener = listener;

        switch (permissionType){
            case read_calender:
                goForPermission(new String[]{Manifest.permission.READ_CALENDAR});
                break;
            case write_calender:
                goForPermission(new String[]{Manifest.permission.WRITE_CALENDAR});
                break;
            case camera:
                goForPermission(new String[]{Manifest.permission.CAMERA});
                break;
            case read_contacts:
                goForPermission(new String[]{Manifest.permission.READ_CONTACTS});
                break;
            case write_contacts:
                goForPermission(new String[]{Manifest.permission.WRITE_CONTACTS});
                break;
            case get_accounts:
                goForPermission(new String[]{Manifest.permission.GET_ACCOUNTS});
                break;
            case access_fine_locations:
                goForPermission(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                break;
            case access_course_locations:
                goForPermission(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION});
                break;
            case record_audio:
                goForPermission(new String[]{Manifest.permission.RECORD_AUDIO});
                break;
            case read_phone_state:
                goForPermission(new String[]{Manifest.permission.READ_PHONE_STATE});
                break;
            case call_phone:
                goForPermission(new String[]{Manifest.permission.CALL_PHONE});
                break;
            case read_call_log:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    goForPermission(new String[]{Manifest.permission.READ_CALL_LOG});
                }
                break;
            case write_call_log:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    goForPermission(new String[]{Manifest.permission.WRITE_CALL_LOG});
                }
                break;
            case add_voice_mail:
                goForPermission(new String[]{Manifest.permission.ADD_VOICEMAIL});
                break;
            case use_sip:
                goForPermission(new String[]{Manifest.permission.USE_SIP});
                break;
            case process_outgoing_calls:
                goForPermission(new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS});
                break;
            case body_sensors:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    goForPermission(new String[]{Manifest.permission.BODY_SENSORS});
                }
                break;
            case send_sms:
                goForPermission(new String[]{Manifest.permission.SEND_SMS});
                break;
            case receieve_sms:
                goForPermission(new String[]{Manifest.permission.RECEIVE_SMS});
                break;
            case read_sms:
                goForPermission(new String[]{Manifest.permission.READ_SMS});
                break;
            case receive_wap_push:
                goForPermission(new String[]{Manifest.permission.RECEIVE_WAP_PUSH});
                break;
            case receive_mms:
                goForPermission(new String[]{Manifest.permission.RECEIVE_MMS});
                break;
            case read_external_storage:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&  Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    goForPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
                }
                break;
            case write_external_storage:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && Build.VERSION.SDK_INT < Build.VERSION_CODES.R ) {
                    if (Environment.isExternalStorageManager()) {
                        permissionResultListener.onResult(true);
                    } else {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                            startActivityForResult(intent, permissionCodeForAPI30);
                        } catch (Exception e) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivityForResult(intent, permissionCodeForAPI30);
                        }
                    }
                } else {
                    goForPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
                break;

            default:
                permissionResultListener.onResult(false);

        }

    }

    private void goForPermission(String[] permissionName) throws ManifestPermission{

        if(!mentionedInManifest(permissionName[0])){
            throw new ManifestPermission(getString(R.string.manifest_permission_error,permissionName[0]));
        }

        if((android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) ||
                (ContextCompat.checkSelfPermission(this,permissionName[0]) == PackageManager.PERMISSION_GRANTED)){
            permissionResultListener.onResult(true);
            return;
        }
        ActivityCompat.requestPermissions(this,permissionName,permissionRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case permissionRequestCode: {
                permissionResultListener.onResult(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (permissionCodeForAPI30 == resultCode) {
            if (Environment.isExternalStorageManager()) {
                permissionResultListener.onResult(true);
            } else {
                Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private boolean mentionedInManifest(String permission)
    {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                for (String p : info.requestedPermissions) {
                    if (p.equals(permission)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
