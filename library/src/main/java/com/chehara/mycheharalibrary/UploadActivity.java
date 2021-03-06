package com.chehara.mycheharalibrary;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.chehara.mycheharalibrary.daemon.UploadVideoDaemon;
import com.chehara.mycheharalibrary.utils.CheharaConst;
import com.chehara.mycheharalibrary.utils.CheharaUtils;
import com.chehara.mycheharalibrary.utils.ImageFilePath;
import com.chehara.mycheharalibrary.widget.CustomDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class UploadActivity extends AppCompatActivity {

    int MAX_SIZE = 25 * 1024;
    private int PICK_VIDEO_REQUEST = 1;
    Uri uri;
    public static String Email;
    String path = CheharaConst.SDCARD + CheharaConst.CHEHARA_DIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_upload);
            requestVideo();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST
                && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            uri = data.getData();

            try {

                String videoPath = new ImageFilePath().getPath(UploadActivity.this,
                        uri);
                Log.e("TAG", videoPath);

                File f = new File(videoPath);

                long length = f.length();

                length = length / 1024;

                // if(videoPath.SubString(videoPath.lastIndexOf(".")))

                String substring = videoPath.substring(videoPath
                        .lastIndexOf("."));

                Log.e("TAG", substring);

                if ((!substring.equalsIgnoreCase(".mp4"))) {

//					Toast.makeText(getActivity(),
//							"Resume Video only mp4 format", Toast.LENGTH_LONG)
//							.show();

                    String txt = "Video Resume only mp4 format";
                    // CustomDialog.buildAlertDialogTitle(UploadActivity.this, txt, "MyChehara Alert").show();
                    CheharaUtils.showMessageOKCancel(UploadActivity.this, txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestVideo();
                        }

                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, "Retry");
                } else {
                    if (MAX_SIZE > length) {

                        CheharaUtils.copyFile(f, new File(path + File.separator
                                + "VideoResume.mp4"));

                        UploadVideoDaemon uploadDaemon = new UploadVideoDaemon(
                                UploadActivity.this);
                        uploadDaemon.setFileName("VideoResume.mp4");
                        uploadDaemon.setSourceFileUri(path + File.separator
                                + "VideoResume.mp4");
                        uploadDaemon.setEmail(Email);
                        uploadDaemon.start();
                    } else {
//						Toast.makeText(getActivity(), "size not greater 10 mb",
//
// 		Toast.LENGTH_LONG).show();

                        String txt = "File size should not exceed 25 mb";
                        // CustomDialog.buildAlertDialogTitle(UploadActivity.this, txt, "MyChehara Alert").show();
                        CheharaUtils.showMessageOKCancel(UploadActivity.this, txt, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestVideo();
                            }

                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }, "Retry");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }



    public void requestVideo() {
        Intent intent = new Intent();
        //  isGalleryImage = true;
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_PICK);

        startActivityForResult(
                Intent.createChooser(intent, "Select Video"),
                PICK_VIDEO_REQUEST);
    }
}
