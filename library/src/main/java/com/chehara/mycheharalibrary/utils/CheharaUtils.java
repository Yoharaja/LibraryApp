package com.chehara.mycheharalibrary.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.chehara.mycheharalibrary.R;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint({"SimpleDateFormat", "InlinedApi"})
@SuppressWarnings("deprecation")
public class CheharaUtils {
    static final String tag = CheharaUtils.class.getSimpleName();

    public static final String INTERVIEWTIME = "interviewtime";
    public static final String EXTEND = "extend";
    public static final String EXTRATIME = "extratime";
    public static final String EMAIL = "email";
    static long differntInterviewTime;

    /*
     * public static boolean isVideoFileAvailable(String interviewCode){ File
     * file=new
     * File(CheharaConst.SDCARD+CheharaConst.CHEHARA_DIR+File.separator+
     * interviewCode+CheharaConst.SAVEFORMAT); return file.exists(); }
     */
    public static boolean isVideoFileAvailable(String sourceFileUri) {
        File file = new File(sourceFileUri);
        return file.exists();
    }

    public static String getFileUploadWarningText(Context context) {
        return context.getResources().getString(
                R.string.txt_file_upload_warning);
    }

    public static String getFileUploadFinishText(Context context) {
        return context.getResources().getString(R.string.after_upload_text);
    }

    public static boolean hasFrontCamCameraApi() {
        boolean hasFrontCam = false;
        CameraInfo ci = new CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT)
                hasFrontCam = true;
        }
        return hasFrontCam;
    }

    public static boolean isOnline(Context context) {// requires network state
        // access permisstion
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    // set fullscreen activity
    public static void makeFullScreen(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT < 16) {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }



    public static boolean checkPermission(String permission, Context context) {
        int result = ContextCompat.checkSelfPermission(context, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener,String postiveButton) {

        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(postiveButton, okListener)
                .setNegativeButton("Cancel", cancelListener)
                .create()
                .show();
    }

    public static void showSetting(Context context, String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {

        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                //.setNegativeButton("Cancel", cancelListener)
                .create()
                .show();
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

}
