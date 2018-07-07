package com.example.bthvi.mycloassloaderapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import java.io.File;

public class SplashActivity extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        textView = (TextView) findViewById(R.id.text);
    }
    private PermissionUtils.PermissionGrant permissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode){

                case PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE:
                    init();
                    break;
                default:
            }
        }
    };
    @Override
    public void onResume(){
        super.onResume();
        PermissionUtils.requestPermission(this,PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE,permissionGrant);
    }
    private void init() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();

    // 遍历所有的修复dex , 因为可能是多个dex修复包
    File fileDir = externalStorageDirectory != null ?
            new File(externalStorageDirectory,"007"):
            new File(getFilesDir(), FixDexUtil.DEX_DIR);// data/user/0/包名/files/odex（这个可以任意位置）
        if (!fileDir.exists()){
        fileDir.mkdirs();
    }
        if (FixDexUtil.isGoingToFix(this)) {

        FixDexUtil.loadFixedDex(this, Environment.getExternalStorageDirectory());
        textView.setText("正在修复。。。。");

    }
     new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
            finish();
        }
    },3000);

}
    /**
     * 请求权限回调方法
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, permissionGrant);
    }
}
