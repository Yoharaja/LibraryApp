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
        String  camera = Manifest.permission.CAMERA,  record = Manifest.permission.RECORD_AUDIO;
        Intent intent;

        if (!CheharaUtils.checkPermission(camera, context))
            permissionsNeeded.add(camera);

        if (!CheharaUtils.checkPermission(record, context))
            permissionsNeeded.add(record);


        if (permissionsNeeded.size() == 0) {
            if (CheharaConst.DEVICE_API_INT >= CheharaConst.ANROID_API_LOILLIPOP) {
                intent = new Intent(context,
                        LollipopRecordActivity.class);
                Log.e("TAG", "lollipoprecord activity loaded");
            } else {
                intent = new Intent(context, RecordActivity.class);
                Log.e("TAG", "normalrecord activity loaded");
            }
            intent.putExtra(RecordActivity.UPLOADABLE, true);
            intent.putExtra("EMAIL", Email);
            context.startActivity(intent);
        } else {
            CustomDialog.buildAlertDialogTitle(context, "Add Permission in Application", "").show();
            // Toast.makeText(context,"Add Permission in Application",To)
        }

    }
}
