package com.chehara.mycheharalibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.chehara.mycheharalibrary.fileupload.CountingOutputStream;
import com.chehara.mycheharalibrary.fileupload.ProgressListener;
import com.chehara.mycheharalibrary.utils.CheharaConst;
import com.chehara.mycheharalibrary.utils.CheharaUtils;
import com.chehara.mycheharalibrary.widget.CustomDialog;


@SuppressWarnings("deprecation")
public class RecordActivity extends Activity {
    String TAG = getClass().getSimpleName();
    public static final String UPLOADABLE = "uploadable";
    boolean uploadable = true;

    static final int CAM_BACK = 0;
    static final int CAM_FRONT = 1;
    private CountDownTimer countDownTimer;
    // private boolean timerHasStarted = false;
    private final long startTime = 91 * 1000;
    private final long interval = 1 * 1000;

    SurfaceView surfaceView;
    // SurfaceHolder surfaceHolder;

    int fragmentIndex = 0;
    String txtFileUploadWarning;
    String txtFileUploadFinish;

    String host = CheharaConst.ENDPOINT_FILE_UPLOAD;
    private Button btnPreview;
    private VideoView videoview;
    Button btnUpload;
    String dir = CheharaConst.SDCARD + CheharaConst.CHEHARA_DIR;
    String fileName = CheharaConst.FILE_NAME;
    String sourceFileUri = dir + File.separator + fileName;
    String email="";
    ProgressBar horizontalProgressBar, progressBar;
    TextView txtPercentage, txtProgressMsg;

    Button btnRetry, btnRetryUpload;
    Camera camera;
    long lastBackPressTime;
    CamPreview preview;
    LinearLayout activityLayout;
    MediaRecorder recorder = new MediaRecorder();
    RelativeLayout containerLayout;
    ToggleButton tglBtnCapture, tglBtnCamera;
    Button backapp;
    TextView timer, txtFinish;
    boolean uploaded = false;

    private boolean isSafeCameraOpen() {
        boolean qOpened = false;
        try {
            releaseCameraAndPreview();
            Log.v(TAG, "Cam open");
            camera = Camera.open();
            qOpened = (camera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
        return qOpened;
    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void startRecording() throws IOException {

        File f = new File(dir);
        if (!f.exists()) {
            f.mkdir();
        }
        recorder = new MediaRecorder();
        camera.unlock();
        recorder.setCamera(camera);
        Log.v(TAG, String.valueOf(preview.surfaceHolder.getSurface()));
        recorder.setPreviewDisplay(preview.surfaceHolder.getSurface());
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setMaxDuration(90000);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion <= android.os.Build.VERSION_CODES.GINGERBREAD) {
            recorder.setProfile(CamcorderProfile
                    .get(CamcorderProfile.QUALITY_HIGH));
        } else {
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
                recorder.setProfile(CamcorderProfile
                        .get(CamcorderProfile.QUALITY_480P));
                Log.e(TAG, "480");
            } else {
                recorder.setProfile(CamcorderProfile
                        .get(CamcorderProfile.QUALITY_HIGH));
                Log.e(TAG, "High");
            }
        }
        recorder.setOutputFile(sourceFileUri);
        recorder.prepare();
        recorder.start();

        countDownTimer = new MyCountDownTimer(startTime, interval);
        // if (!timerHasStarted) {
        countDownTimer.start();
        // start.setVisibility(View.GONE);
        // timerHasStarted = true;
        // }

    }

    protected void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                camera.lock();
                countDownTimer.cancel();
                timer.setText("Over");
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            // recorder.release();
            // camera.release();

        }
    }

    private void releaseMediaRecorder() {
        Log.d(TAG, "releaseMediaRecorder() invoked");
        if (recorder != null) {
            recorder.reset(); // clear recorder configuration
            recorder.release(); // release the recorder object
            recorder = null;
            camera.lock(); // lock camera for later use
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_record);
        try {
            Intent intent = getIntent();
            if (intent.hasExtra(UPLOADABLE)) {
                uploadable = intent.getBooleanExtra(UPLOADABLE, uploadable);
            }

            if (intent.hasExtra("EMAIL")) {
                email = intent.getStringExtra("EMAIL");
            }
            txtFileUploadWarning = CheharaUtils.getFileUploadWarningText(this);
            txtFileUploadFinish = CheharaUtils.getFileUploadFinishText(this);
            surfaceView = (SurfaceView) findViewById(R.id.recordVideo);
            tglBtnCapture = (ToggleButton) findViewById(R.id.recordBtn);
            timer = (TextView) findViewById(R.id.timer);
            timer.setVisibility(View.INVISIBLE);
            backapp = (Button) findViewById(R.id.backApp);
            tglBtnCamera = (ToggleButton) findViewById(R.id.cameraBtn);
            tglBtnCamera.setVisibility(View.INVISIBLE);

            txtFinish = (TextView) findViewById(R.id.textViewFinishText);
            // txtFinish.setVisibility(View.GONE);
            txtFinish.setText(txtFileUploadWarning);
            txtFinish.setVisibility(View.VISIBLE);

            btnRetry = (Button) findViewById(R.id.buttonRetry);
            btnRetry.setEnabled(false);
            btnRetry.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RecordActivity.this,
                            RecordActivity.class);
                    intent.putExtra(UPLOADABLE, uploadable);
                    startActivity(intent);
                    finish();
                }
            });

            // Google Anaylitics

            String screenName = "VideoResume";

            btnUpload = (Button) findViewById(R.id.buttonUpload);
            btnUpload.setEnabled(false);

            final LinearLayout layoutUpload = (LinearLayout) findViewById(R.id.linearLayoutProgress);
            layoutUpload.setVisibility(View.GONE);
            horizontalProgressBar = (ProgressBar) findViewById(R.id.horizontalProgressBar);
            progressBar = (ProgressBar) findViewById(R.id.normalProgressBar);
            progressBar.setVisibility(View.GONE);
            txtPercentage = (TextView) findViewById(R.id.textViewProgressPercentage);
            txtProgressMsg = (TextView) findViewById(R.id.textViewProgressMessage);
            btnRetryUpload = (Button) findViewById(R.id.buttonUploadRetry);
            btnRetryUpload.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnRetryUpload.setVisibility(View.GONE);
                    backapp.setEnabled(false);
                    new MySampleUploadDaemon(RecordActivity.this).start();
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

            btnUpload.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    layoutUpload.setVisibility(View.VISIBLE);
                    videoview.setVisibility(View.GONE);
                    surfaceView.setVisibility(View.INVISIBLE);
                    timer.setVisibility(View.GONE);
                    btnRetry.setEnabled(false);
                    tglBtnCamera.setVisibility(View.INVISIBLE);
                    tglBtnCapture.setEnabled(false);
                    btnPreview.setEnabled(false);
                    backapp.setEnabled(false);
                    new MySampleUploadDaemon(RecordActivity.this).start();
                    progressBar.setVisibility(View.VISIBLE);
                    btnUpload.setEnabled(false);
                }
            });


            videoview = (VideoView) findViewById(R.id.videoview);
            videoview.setVisibility(View.GONE);
            btnPreview = (Button) findViewById(R.id.buttonPreview);
            final MediaController controller = new MediaController(
                    RecordActivity.this);
            controller.setAnchorView(videoview);
            videoview.setMediaController(controller);
            btnPreview.setEnabled(false);
            btnPreview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (camera != null) {
                        camera.stopPreview();
                        camera.release();
                    }
                    // backapp.setEnabled(false);
                    btnPreview.setEnabled(false);
                    tglBtnCapture.setEnabled(false);
                    timer.setVisibility(View.INVISIBLE);
                    btnRetry.setEnabled(true);
                    tglBtnCamera.setVisibility(View.INVISIBLE);
                    surfaceView.setVisibility(View.GONE);
                    if (uploadable)
                        btnUpload.setEnabled(true);
                    videoview.setVisibility(View.VISIBLE);
                    videoview.setVideoPath(sourceFileUri);
                    videoview.start();
                    videoview.requestFocus();
                }
            });

            if (isSafeCameraOpen()) {

                preview = new CamPreview(this, camera);
                backapp.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        stopRecording();
                        // if(!uploaded)camera.stopPreview();

                        finish();
                    }
                });

                tglBtnCapture.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // String msg;

                        if (tglBtnCapture.isChecked()) {
                            timer.setVisibility(View.VISIBLE);
                            btnPreview.setEnabled(false);
                            btnUpload.setEnabled(false);
                            // tglBtnCapture.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_video_stop,
                            // 0, 0, 0);
                            try {

                                // surfaceView.setVisibility(View.VISIBLE);
                                videoview.setVisibility(View.GONE);
                                tglBtnCamera.setVisibility(View.INVISIBLE);
                                backapp.setEnabled(false);
                                startRecording();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // tglBtnCapture.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_video_record,
                            // 0, 0, 0);
                            timer.setVisibility(View.INVISIBLE);
                            stopRecording();
                            releaseMediaRecorder();
                            if (Camera.getNumberOfCameras() >= 2) {
                                tglBtnCamera.setVisibility(View.VISIBLE);
                            }
                            // camera.stopPreview(); //optional
                            if (uploadable)
                                btnUpload.setEnabled(true);
                            backapp.setEnabled(true);
                            btnPreview.setEnabled(true);
                        }

                    }
                });

                if (Camera.getNumberOfCameras() >= 2) {

                    // if you want to open front facing camera use this line
                    // camera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
                    tglBtnCamera.setVisibility(View.VISIBLE);
                    tglBtnCamera.setOnClickListener(new OnClickListener() {
                        // int currentCameraId;

                        @Override
                        public void onClick(View v) {

                            camera.stopPreview();
                            camera.release();
                            if (tglBtnCamera.isChecked()) {
                                camera = Camera
                                        .open(CameraInfo.CAMERA_FACING_FRONT);
                                // currentCameraId = 2;
                            } else {
                                camera = Camera.open();
                                // currentCameraId = 1;
                            }

                            try {
                                camera.setPreviewDisplay(preview.surfaceHolder);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            camera.startPreview();
                        }
                    });
                }

            } else {
                //Toast.makeText(this, "Can't accquire camera resource",
                //	Toast.LENGTH_LONG).show();
                //CAMERA ACCESS FAILED. CHECK CAMERA.
                String txt = "Camera access failed.Check Camera";
                CustomDialog.buildAlertDialogTitle(RecordActivity.this,
                        txt, "MyChehara Alert").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestory() invoked");
        super.onDestroy();
    }

	/*
     * @Override public void onPause() { super.onPause(); Log.d(TAG,
	 * "onPause() invoked"); if (camera != null) { camera.stopPreview();
	 * camera.release(); }
	 */

    @Override
    public void onStop() {

        Log.d(TAG, "onStop() invoked");
        // camera.stopPreview();
        releaseCameraAndPreview();

        super.onStop();

    }

    @Override
    public void onBackPressed() {
        // disable hardware back button
    }

    public void exitActivity() {
        stopRecording();
        finish();
    }

    class CamPreview extends SurfaceView implements SurfaceHolder.Callback {
        static final String tag = "CamPreview";

        Camera camera;

        public SurfaceHolder surfaceHolder;
        Context context;
        int screenWidth, screenHeight;
        List<Camera.Size> supportedSizes;
        List<Camera.Size> picSizes;
        List<Camera.Size> vidSizes;
        Camera.Size optimalSize;
        Camera.Size picSize;
        Camera.Size vidSize;

        public CamPreview(Context context, Camera camera)
                throws NullPointerException {
            super(context);
            Log.d(tag, "CamPreview()");
            this.context = context;
            // surfaceHolder = getHolder();
            surfaceHolder = surfaceView.getHolder();
            this.camera = camera;
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surfaceHolder.setKeepScreenOn(true);

            supportedSizes = camera.getParameters().getSupportedPreviewSizes();

            picSizes = camera.getParameters().getSupportedPictureSizes();

            // vidSizes=camera.getParameters().getSupportedVideoSizes();

            String tmp = "Screen:";
            for (Camera.Size s : supportedSizes) {
                tmp += s.width + "*" + s.height + "\t";
            }

            if (picSizes != null || picSizes.size() > 0) {
                tmp += "\nPicture:";
                for (Camera.Size s : picSizes) {
                    tmp += s.width + "*" + s.height + "\t";
                }
            }

            // Display display = getWindowManager().getDefaultDisplay();
            // Point size = new Point();
            // display.getSize(size);
            // screenWidth = size.x;
            // screenHeight=size.y;
            // Log.d(tag,
            // "Screen width: "+screenWidth+" screenHeight: "+screenHeight);
			/*
			 * if(vidSizes!=null || vidSizes.size()>0){ tmp+="\nVideo:";
			 * for(Camera.Size s:vidSizes){ tmp+=s.width+"*"+s.height+"\t"; } }
			 */
            Log.d(tag, tmp);
        }

        /*
         * @Override protected void onMeasure(int widthMeasureSpec, int
         * heightMeasureSpec) throws NullPointerException{ Log.d(tag,
         * "onMeasure()"); int width = resolveSize(getSuggestedMinimumWidth(),
         * widthMeasureSpec); int height =
         * resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
         * Log.d(tag, "mesured width "+width+" height "+height); float
         * ratio=0.0f; if (supportedSizes != null) { optimalSize =
         * getOptimalPreviewSize(supportedSizes, screenWidth, screenHeight);
         * width=optimalSize.width; height=optimalSize.height;
         *
         * vidSize=getOptimalPreviewSize(vidSizes, width, height);
         * width=vidSize.width; height=vidSize.height; if(optimalSize.height >=
         * optimalSize.width){ ratio = (float) optimalSize.height / (float)
         * optimalSize.width; setMeasuredDimension((int)(width*ratio), width);
         * }else{ ratio = (float) optimalSize.width / (float)
         * optimalSize.height; //setMeasuredDimension(width, (int) (width *
         * ratio)); setMeasuredDimension( height,(int)(height*ratio));
         * }Log.e(tag, "ratio :"+ratio); }else{ Log.e(tag,
         * "supported sizes null"); }
         *
         * Log.e(tag,
         * "Optimal width:"+optimalSize.width+" screen height:"+optimalSize
         * .height+" ratio :"+ratio); Log.d(tag, "onMeasure() finished");
         * super.onMeasure(widthMeasureSpec, heightMeasureSpec); }
         */
        public void surfaceCreated(SurfaceHolder holder)
                throws NullPointerException {
            Log.d(TAG, "surfaceCreated()");
            // Log.v(TAG, String.valueOf(holder));
            try {

                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

            Log.d(tag, "surfaceChanged() width:" + width + "Height:" + height);
            try {
                Parameters parameters = camera.getParameters();
                List<Camera.Size> previewSizes = parameters
                        .getSupportedPreviewSizes();

                Camera.Size previewSize;

                camera.setParameters(parameters);
                Display display = ((WindowManager) this.getContext()
                        .getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();

                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        previewSize = getOptimalPreviewSize(previewSizes, height,
                                width);// for correction of display
                        parameters.setPreviewSize(previewSize.height,
                                previewSize.width);
                        surfaceHolder.setFixedSize(previewSize.height,
                                previewSize.width);// portrait
                        camera.setDisplayOrientation(90);
                        Log.d("measure", "display rotaion 0 deg; width:"
                                + previewSize.width + " height:"
                                + previewSize.height);

                        break;
                    case Surface.ROTATION_90:
                        previewSize = getOptimalPreviewSize(previewSizes, width,
                                height);
                        surfaceHolder.setFixedSize(previewSize.width,
                                previewSize.height);// landscape
                        parameters.setPreviewSize(previewSize.width,
                                previewSize.height);
                        camera.setDisplayOrientation(0);
                        Log.d("measure", "display rotaion 90 deg; width:"
                                + previewSize.width + " height:"
                                + previewSize.height);
                        break;
                    case Surface.ROTATION_180:
                        Log.d("measure", "display rotaion 180 deg");
                        break;
                    case Surface.ROTATION_270:
                        Log.d("measure", "display rotaion 270 deg");
                        camera.setDisplayOrientation(180);
                        break;
                }
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed()");

            // camera.stopPreview();

            // this.getHolder().removeCallback(this);
            camera.release();
            camera = null;
            // timer.setText("Force Stop");
            if (countDownTimer != null) {
                countDownTimer.cancel();

            }
        }

        public void onPause() {
            if (camera != null) {
                camera.setPreviewCallback(null);
                this.getHolder().removeCallback(this);
                camera.release();
            }
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes,
                                                  int w, int h) {
            final double ASPECT_TOLERANCE = 0.2;
            double targetRatio = (double) h / w;

            // double targetRatio = (double) w / h;
            // Log.e(tag,
            // "TargetRatio:"+targetRatio+" tolerance:"+ASPECT_TOLERANCE);
            if (sizes == null)
                return null;
            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;
            int targetHeight = h;
            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
            return optimalSize;
        }

    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            stopRecording();
            try {
                if (camera != null) {
                    camera.stopPreview();
                }
            } catch (Exception e) {
            }

            tglBtnCapture.toggle();// newly added
            tglBtnCapture.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_video_record, 0, 0, 0);

            backapp.setEnabled(true);
            if (Camera.getNumberOfCameras() >= 2) {
                tglBtnCamera.setVisibility(View.VISIBLE);
            } else {
                tglBtnCamera.setVisibility(View.INVISIBLE);
            }
            btnPreview.setEnabled(true);

            if (uploadable)
                btnUpload.setEnabled(true);
            // finish();

        }

        @Override
        public void onTick(long millisUntilFinished) {

            timer.setText(millisUntilFinished / 1000 + " Sec");

        }
    }

    public class MySampleUploadDaemon extends AsyncTask<Void, String, String> {
        String tag = getClass().getSimpleName();
        int statuscode;
        ProgressListener progressListener;
        Context context;
        long totalSize = 0;

        public MySampleUploadDaemon(Context context) {
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

        public int getStatuscode() {
            return statuscode;
        }

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            horizontalProgressBar.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String msg = values[0];
            try {
                int percent = Integer.parseInt(msg);
                horizontalProgressBar.setProgress(percent);
                txtPercentage.setText(percent + "%");
            } catch (NumberFormatException e) {
                txtProgressMsg.setText(msg);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String message) {
            Log.e(tag, "onPostExecute(): " + message);
            //publishProgress(message);
            //	Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            CustomDialog.buildAlertDialogTitle(RecordActivity.this,
                    message, "MyChehara Alert").show();
            progressBar.setVisibility(View.GONE);

            if (message.indexOf("Completed") != -1) {
                Log.e("Test", "back onClick() invoked");

                btnPreview.setEnabled(false);
                timer.setVisibility(View.INVISIBLE);
                tglBtnCamera.setVisibility(View.INVISIBLE);
                tglBtnCapture.setEnabled(false);
                btnUpload.setEnabled(false);
                btnRetry.setEnabled(false);
                txtFinish.setText(txtFileUploadFinish);
                txtFinish.setVisibility(View.VISIBLE);
                uploaded = true;
                btnRetryUpload.setVisibility(View.GONE);
                // exitActivity();
            } else {
                btnRetryUpload.setVisibility(View.VISIBLE);
                txtFinish.setVisibility(View.GONE);
            }
            backapp.setEnabled(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            publishProgress("Process Started");
            String msg;
            File sourceFile = new File(sourceFileUri);
            if (!sourceFile.isFile()) {
                msg = "Source file not exist";
                Log.e(tag, msg);
                return msg;
            } else {
                HttpURLConnection conn = null;
                CountingOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                try {
                    String tmp;
                    FileInputStream fileInputStream = new FileInputStream(
                            sourceFile);
                    publishProgress("Connecting with server");
                    URL url = new URL(host + "?chehara_email=" + email);
                    Log.e("test", "host" + url);
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
                        msg = "Video Resume upload has been completed";
                    } else {
                        //VIDEO RESUME UPLOAD FAILED. RETRY
                        msg = "Video Resume upload failed. Retry";

                        Log.e(tag, msg + "; status code:" + statuscode);
                    }
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                } catch (HttpRetryException e) {
                    msg = "Host Connection Error ";
                    Log.e(tag, msg + e.toString());
                } catch (SecurityException e) {
                    msg = "Security Error";
                    Log.e(tag, msg + e.toString());
                } catch (MalformedURLException e) {
                    msg = "MalformedUrl Error";
                    Log.e(tag, msg + e.toString());
                } catch (NullPointerException e) {
                    msg = "NullPointer Error";
                    Log.e(tag, msg + e.toString());
                } catch (IOException e) {
                    msg = "IO Error";
                    Log.e(tag, msg + e.toString());
                } catch (Exception e) {
                    msg = "Error";
                    Log.e(tag, msg + e.toString());
                }
                return msg;
            } // End else block
        }

        public void start() {
            this.execute();
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

}