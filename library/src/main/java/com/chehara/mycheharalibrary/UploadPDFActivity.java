package com.chehara.mycheharalibrary;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.chehara.mycheharalibrary.daemon.UploadPDFDaemon;
import com.chehara.mycheharalibrary.utils.CheharaConst;
import com.chehara.mycheharalibrary.utils.CheharaUtils;

import java.io.File;
import java.net.URI;

public class UploadPDFActivity extends AppCompatActivity {

    private int PICK_PDF_REQUEST = 4;
    Uri uri;
    int MAX_SIZE = 1 * 1024;
    public static String Email;
    String path = CheharaConst.SDCARD + CheharaConst.CHEHARA_DIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pdf);

        requestPDF();
    }

    private void requestPDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select PDF"),
                PICK_PDF_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST
                && resultCode == RESULT_OK && data != null
                && data.getData() != null) {

            try {

                uri = data.getData();

                String videoPath = uri.getPath();

                Log.e("TAG", videoPath);

                File f = new File(videoPath);

                long length = f.length();

                length = length / 1024;

                // if(videoPath.SubString(videoPath.lastIndexOf(".")))

                String substring = videoPath.substring(videoPath
                        .lastIndexOf("."));

                Log.e("TAG", substring);

                if ((!substring.equalsIgnoreCase(".pdf"))) {

//					Toast.makeText(getActivity(), "Upload PDF only",
//							Toast.LENGTH_LONG).show();
                    String txt = "Upload PDF Files only";
                    CheharaUtils.showMessageOKCancel(UploadPDFActivity.this, txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPDF();
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
                                + "Resume.pdf"));

                        UploadPDFDaemon uploadDaemon = new UploadPDFDaemon(
                                UploadPDFActivity.this);
                        uploadDaemon.setFileName("Resume.pdf");
                        uploadDaemon.setSourceFileUri(path + File.separator
                                + "Resume.pdf");
                        uploadDaemon.setEmail(Email);
                        uploadDaemon.start();
                    } else {
//						Toast.makeText(getActivity(), "size not greater 1 mb",
//								Toast.LENGTH_LONG).show();

                        String txt = "File size should not exceed 1 mb";
                        CheharaUtils.showMessageOKCancel(UploadPDFActivity.this, txt, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPDF();
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

}
