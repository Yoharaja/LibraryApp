package com.chehara.mycheharalibrary;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.chehara.mycheharalibrary.utils.CheharaConst;
import com.chehara.mycheharalibrary.utils.CheharaUtils;
import com.chehara.mycheharalibrary.widget.CustomDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by answerz on 10/11/16.
 */
public class MyChehara {


    public static void recordVideo(String Email, Context context) {
        List<String> permissionsNeeded = new ArrayList<String>();
        String camera = Manifest.permission.CAMERA, record = Manifest.permission.RECORD_AUDIO, read = Manifest.permission.READ_EXTERNAL_STORAGE, write = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        Intent intent;

        try {
            if (CheharaConst.DEVICE_API_INT >= CheharaConst.ANROID_API_MARSHMALLOW) {

                if (!CheharaUtils.checkPermission(camera, context))
                    permissionsNeeded.add(camera);

                if (!CheharaUtils.checkPermission(record, context))
                    permissionsNeeded.add(record);

                if (!CheharaUtils.checkPermission(read, context))
                    permissionsNeeded.add(read);

                if (!CheharaUtils.checkPermission(write, context))
                    permissionsNeeded.add(write);

                if (permissionsNeeded.size() != 0) {
                    throw new IllegalStateException("");
                }
            }

            if (CheharaConst.DEVICE_API_INT >= CheharaConst.ANROID_API_LOILLIPOP) {
                LollipopRecordActivity.email = Email;
                intent = new Intent(context,
                        LollipopRecordActivity.class);
                Log.e("TAG", "lollipoprecord activity loaded");
            } else {
                RecordActivity.email = Email;
                intent = new Intent(context, RecordActivity.class);
                Log.e("TAG", "normalrecord activity loaded");
            }
            intent.putExtra(RecordActivity.UPLOADABLE, true);
            intent.putExtra("EMAIL", Email);
            context.startActivity(intent);


            // Toast.makeText(context,"Add Permission in Application",To)

        } catch (IllegalStateException e) {
            CustomDialog.buildAlertDialogTitle(context, "Add Permission in Application", "").show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void uploadVideo(String Email, Context context) {
        List<String> permissionsNeeded = new ArrayList<String>();
        String read = Manifest.permission.READ_EXTERNAL_STORAGE, write = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        Intent intent;

        try {
            if (CheharaConst.DEVICE_API_INT >= CheharaConst.ANROID_API_MARSHMALLOW) {

                if (!CheharaUtils.checkPermission(read, context))
                    permissionsNeeded.add(read);

                if (!CheharaUtils.checkPermission(write, context))
                    permissionsNeeded.add(write);


                if (permissionsNeeded.size() != 0) {
                    throw new IllegalStateException("");
                }
            }

            intent = new Intent(context,
                    UploadActivity.class);
            intent.putExtra("EMAIL", Email);
            UploadActivity.Email = Email;
            context.startActivity(intent);


            // Toast.makeText(context,"Add Permission in Application",To)

        } catch (IllegalStateException e) {
            CustomDialog.buildAlertDialogTitle(context, "Add Permission in Application", "").show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadPDF(String Email, Context context) {
        List<String> permissionsNeeded = new ArrayList<String>();
        String read = Manifest.permission.READ_EXTERNAL_STORAGE, write = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        Intent intent;

        try {
            if (CheharaConst.DEVICE_API_INT >= CheharaConst.ANROID_API_MARSHMALLOW) {

                if (!CheharaUtils.checkPermission(read, context))
                    permissionsNeeded.add(read);

                if (!CheharaUtils.checkPermission(write, context))
                    permissionsNeeded.add(write);


                if (permissionsNeeded.size() != 0) {
                    throw new IllegalStateException("");
                }
            }

            intent = new Intent(context,
                    UploadPDFActivity.class);
            intent.putExtra("EMAIL", Email);
            UploadPDFActivity.Email = Email;
            context.startActivity(intent);


            // Toast.makeText(context,"Add Permission in Application",To)

        } catch (IllegalStateException e) {
            CustomDialog.buildAlertDialogTitle(context, "Add Permission in Application", "").show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
