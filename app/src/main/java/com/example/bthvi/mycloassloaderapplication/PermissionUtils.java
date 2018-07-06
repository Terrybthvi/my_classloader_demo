package com.example.bthvi.mycloassloaderapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

/**
 * android 6.0 动态权限申请
 * Created by leiqi on 2016/12/8.
 */

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();
    public static final int CODE_RECORD_AUDIO = 0;
    public static final int CODE_GET_ACCOUNTS = 1;
    public static final int CODE_READ_PHONE_STATE = 2;
    public static final int CODE_CALL_PHONE = 3;
    public static final int CODE_CAMERA = 4;
    public static final int CODE_PERMISSION_ACCESS_MESSAGE = 5;
    public static final int CODE_WRITE_EXTERNAL_STORAGE = 6;
    public static final int CODE_SDINFO = 7;
    public static final int CODE_SDINFO_SET = 8;
    public static final int CODE_SDINFO_CAMERA = 9;
    public static final int CODE_MULTI_PERMISSION = 100;

    public static final String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String PERMISSION_GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;
    public static final String PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String PERMISSION_CALL_PHONE = Manifest.permission.CALL_PHONE;
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    public static final String PERMISSION_ACCESS_MESSAGE = Manifest.permission.SEND_SMS;
    public static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static final String[] requestPermissions = {
            PERMISSION_RECORD_AUDIO,
            PERMISSION_GET_ACCOUNTS,
            PERMISSION_READ_PHONE_STATE,
            PERMISSION_CALL_PHONE,
            PERMISSION_CAMERA,
            PERMISSION_ACCESS_MESSAGE,
            PERMISSION_WRITE_EXTERNAL_STORAGE
    };

    public interface PermissionGrant {
        void onPermissionGranted(int requestCode);
    }

    /**
     * Requests permission.
     *
     * @param activity
     * @param requestCode request code, e.g. if you need request CAMERA permission,parameters is PermissionUtils.CODE_CAMERA
     */
    public static void requestPermission(final Activity activity, final int requestCode, PermissionGrant permissionGrant) {
        if (activity == null) {
            return;
        }
        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            return;
        }

        final String requestPermission = requestPermissions[requestCode];

        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang.RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以判断系统版本，低于23就不申请权限，直接做你想做的。permissionGrant.onPermissionGranted(requestCode);
//        if (Build.VERSION.SDK_INT < 23) {
//            permissionGrant.onPermissionGranted(requestCode);
//            return;
//        }

        int checkSelfPermission;
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
        } catch (RuntimeException e) {
            Toast.makeText(activity, "please open this permission", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                shouldShowRationale(activity, requestCode, requestPermission);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{requestPermission}, requestCode);
            }

        } else {
            permissionGrant.onPermissionGranted(requestCode);
        }
    }

    private static void requestMultiResult(Activity activity, String[] permissions, int[] grantResults, PermissionGrant permissionGrant) {
        if (activity == null) {
            return;
        }
        permissionGrant.onPermissionGranted(CODE_MULTI_PERMISSION);
    }


    /**
     * 一次申请多个权限
     */
    public static void requestMultiPermissions(final Activity activity, PermissionGrant grant) {

        final List<String> permissionsList = getNoGrantedPermission(activity, false);
        final List<String> shouldRationalePermissionsList = getNoGrantedPermission(activity, true);

        //TODO checkSelfPermission
        if (permissionsList == null || shouldRationalePermissionsList == null) {
            return;
        }
        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]),
                    CODE_MULTI_PERMISSION);
        } else {
            grant.onPermissionGranted(CODE_MULTI_PERMISSION);
        }
    }


    private static void shouldShowRationale(final Activity activity, final int requestCode, final String requestPermission) {
        //TODO
        String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
        showMessageOKCancel(activity, "提示: " + permissionsHint[requestCode], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{requestPermission},
                        requestCode);
            }
        });
    }

    private static void showMessageOKCancel(final Activity context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("确定", okListener)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();

    }

    /**
     * 请求权限回调方法
     * @param activity
     * @param requestCode  Need consistent with requestPermission
     * @param permissions
     * @param grantResults
     */
    public static void requestPermissionsResult(final Activity activity, final int requestCode, @NonNull String[] permissions,
                                                @NonNull int[] grantResults, PermissionGrant permissionGrant) {

        if (activity == null) {
            return;
        }

        if (requestCode == CODE_MULTI_PERMISSION) {
            requestMultiResult(activity, permissions, grantResults, permissionGrant);
            return;
        }

        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            Toast.makeText(activity, "illegal requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //TODO success, do something, can use callback
            permissionGrant.onPermissionGranted(requestCode);

        } else {
            //TODO hint user this permission function
            String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
            openSettingActivity(activity, "Result" + permissionsHint[requestCode]);
        }

    }

    private static void openSettingActivity(final Activity activity, String message) {

        showMessageOKCancel(activity, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        });
    }


    /**
     * @param activity
     * @param isShouldRationale true: return no granted and shouldShowRequestPermissionRationale permissions, false:return no granted and !shouldShowRequestPermissionRationale
     * @return
     */
    public static ArrayList<String> getNoGrantedPermission(Activity activity, boolean isShouldRationale) {

        ArrayList<String> permissions = new ArrayList<String>();

        for (int i = 0; i < requestPermissions.length; i++) {
            String requestPermission = requestPermissions[i];


            //TODO checkSelfPermission
            int checkSelfPermission = -1;
            try {
                checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
            } catch (RuntimeException e) {
                Toast.makeText(activity, "please open those permission", Toast.LENGTH_SHORT)
                        .show();
                return null;
            }

            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                    if (isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                } else {
                    if (!isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                }
            }
        }
        return permissions;
    }

}
