package com.chehara.mycheharalibrary.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chehara.mycheharalibrary.fileupload.CountingOutputStream;
import com.chehara.mycheharalibrary.fileupload.ProgressListener;
import com.chehara.mycheharalibrary.utils.CheharaConst;
import com.chehara.mycheharalibrary.utils.CheharaUtils;
import com.chehara.mycheharalibrary.widget.CustomDialog;

public class UploadVideoDaemon extends AsyncTask<Void, String, String> {
    String tag = this.getClass().getSimpleName();
    String endpoint_picture = CheharaConst.ENDPOINT_FILE_UPLOAD;
    //String endpoint_picture = CheharaConst.ENDPOINT_UPLOAD_TESTPROFILE_PICTURE;
    Context context;
    String message;
    String sourceFileUri, fileName;
    int statuscode;


    Intent intent;

    LinearLayout layoutUpload;
    TextView txtPercentage, txtProgressMsg, txtFinish;
    ProgressBar horizontalProgressBar, progressBar;
    ProgressListener progressListener;
    long totalSize;
    ProgressDialog pdialog;

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    String Email;

    public void setSourceFileUri(String sourceFileUri) {
        this.sourceFileUri = sourceFileUri;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected void onPreExecute() {
        pdialog = new ProgressDialog(context);
        pdialog.setMessage("Uploading Video....");
        pdialog.show();
        super.onPreExecute();
    }

    public UploadVideoDaemon(Context context) {
        this.context = context;

        this.progressListener = new ProgressListener() {
            @Override
            public void transferred(long num) {
                int percent = 0;
                if (totalSize != 0) {
                    percent = (int) ((num / (float) totalSize) * 100);
                }

                publishProgress("" + percent);
            }
        };

    }

    @Override
    protected String doInBackground(Void... params) {
        message = "";

        publishProgress("Wait for response...");
        String response;
        try {
            File sourceFile = new File(sourceFileUri);
            if (!sourceFile.isFile()) {
                message = "Source file not exist";
                Log.e(tag, message);
                return message;
            } else {

                String email = Email;
                HttpURLConnection conn = null;
                CountingOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                String tmp;
                FileInputStream fileInputStream = new FileInputStream(
                        sourceFile);
                publishProgress("Connecting with server");
                URL url = new URL(endpoint_picture + "?chehara_email=" + email);
                Log.e("test", "Host: " + url.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setChunkedStreamingMode(1024);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new CountingOutputStream(conn.getOutputStream(),
                        this.progressListener);
                tmp = twoHyphens + boundary + lineEnd;
                dos.write(tmp.getBytes());
                tmp = "Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd;
                dos.write(tmp.getBytes());
                dos.write(lineEnd.getBytes());

                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();
                totalSize = bytesAvailable;

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // Log.e("Test","max bytes: "+bytesAvailable+"  bufferSize:"+bufferSize);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                publishProgress("File uploading started");
                while (bytesRead > 0) {
                    // Log.e("Test","read byte: "+bytesRead+" byte avail:"+bytesAvailable);
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                publishProgress("File uploaded successfully");
                // send multipart form data necesssary after file data...
                dos.write(lineEnd.getBytes());
                tmp = twoHyphens + boundary + twoHyphens + lineEnd;
                dos.write(tmp.getBytes());
                publishProgress("Waiting for uploading status confirmation");
                // Responses from the server (code and message)
                statuscode = conn.getResponseCode();

                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + statuscode);
                if (statuscode == 200) {
                    //VIDEO RESUME UPLOAD HAS BEEN COMPLETED.
                    message = "Video Resume upload has been completed";
                } else {
                    //VIDEO RESUME UPLOAD FAILED. RETRY.
                    message = "Video Resume upload failed. Retry";

                    Log.e(tag, message + "; status code:" + statuscode);
                }
                fileInputStream.close();
                dos.flush();
                dos.close();
            }

            // if(!isUploadVideoOnly&& intent!=null){
            // ((Activity)context).startActivity(intent);
            // }

            // for call from fragment
            /*
             * if(isUploadVideoOnly &&
			 * fragmentIndex==HomeNavActivity.MENU_JOB_INVITATION){
			 * HomeNavActivity home=(HomeNavActivity)context;
			 * home.selectNavMenuItem(fragmentIndex, true, isAnswerUploaded); }
			 */

        } catch (IllegalStateException e) {
            Log.e(tag, e.toString());
            message = "error:" + e.toString();
            // Toast.makeText(context, e.getMessage(),
            // Toast.LENGTH_SHORT).show();

        } catch (SecurityException e) {
            Log.e(tag, "Security Error :" + e.toString());
            message = "error:" + e.toString();
        } catch (NullPointerException e) {
            Log.e(tag, "Nullvalue Error :" + e.toString());
            message = "error:" + e.toString();
        } catch (HttpRetryException e) {
            message = "Host Connection Error ";
            Log.e(tag, message + e.toString());
        } catch (MalformedURLException e) {
            message = "MalformedUrl Error";
            Log.e(tag, message + e.toString());
        } catch (IOException e) {
            message = "IO Error";
            Log.e(tag, message + e.toString());
        } catch (Exception e) {
            message = "Error " + e.getMessage();
            Log.e(tag, "Error :" + e.toString());
        } finally {

        }
        return message;
    }

    @Override
    protected void onPostExecute(final String message) {
        Log.e(tag, "onPostExecute(): " + message);
        pdialog.cancel();
        //publishProgress(message);

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                //  CustomDialog.buildAlertDialogTitle(context,
                //   message, "MyChehara Alert").show();

                if (message.indexOf("completed") != -1) {
                    ((Activity) context).finish();
                } else {
                    CheharaUtils.showMessageOKCancel(context, message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new UploadVideoDaemon(context).start();;
                        }

                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((Activity) context).finish();
                        }
                    }, "Retry");
                }
            }
        });

    }

    public void start() {
        this.execute();
    }

}
