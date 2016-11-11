package com.chehara.mycheharalibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
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
import com.chehara.mycheharalibrary.widget.AutoFitTextureView;
import com.chehara.mycheharalibrary.widget.CustomDialog;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("NewApi")
public class LollipopRecordActivity extends Activity {

    public static final String UPLOADABLE = "uploadable";
    boolean uploadable = true;
    CountDownTimer countDownTimer;
    boolean timerHasStarted = false;
    boolean uploaded = false;
    final long startTime = 91 * 1000;
    final long interval = 1 * 1000;
    String host = CheharaConst.ENDPOINT_FILE_UPLOAD;
    // String host = CheharaConst.ENDPOINT_UPLOAD_TESTPROFILE_PICTURE;
    Button btnPreview, btnUpload, btnRetry, backapp, btnRetryUpload;
    VideoView videoview;
    String dir = CheharaConst.SDCARD + CheharaConst.CHEHARA_DIR;
    String fileName = CheharaConst.FILE_NAME;
    // String fileName = "VideoResume.3gp";
    String sourceFileUri = dir + File.separator + fileName;
    String email = "";
    ProgressBar horizontalProgressBar, progressBar;
    TextView txtPercentage, txtProgressMsg, timer, txtFinish;
    LinearLayout activityLayout, layoutUpload;
    RelativeLayout containerLayout;
    ToggleButton tglBtnCamera, tglBtnCapture;
    String[] lCameraIds;

    // RelativeLayout controlLayout;

    int fragmentIndex = 0;
    String txtFileUploadWarning = "Please do not close the app or disconnect the internet until your video interview uploaded successfully";
    String txtFileUploadFinish;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private final static String TAG = LollipopRecordActivity.class
            .getSimpleName();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private AutoFitTextureView mTextureView;

    private CameraDevice mCameraDevice;

    private CameraCaptureSession mPreviewSession;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            Log.e(TAG, "onSurfaceTextureAvailable()" + width + "*" + height);
            openCamera(width, height, lCameraIds[0]);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            Log.e(TAG,
                    "onSurfaceTextureSizeChanged()  call configureTransform("
                            + width + "," + height + ")");
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.e(TAG, "onSurfaceTextureDestroyed()");
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };

    private Size mPreviewSize;

    private Size mVideoSize;

    private CaptureRequest.Builder mPreviewBuilder;

    private MediaRecorder mMediaRecorder;

    private boolean mIsRecordingVideo;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.e(TAG, "onOpened()");
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(),
                        mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.e(TAG, "onDisconnected()");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            Log.e(TAG, "onError()");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = LollipopRecordActivity.this;
            if (null != activity) {
                activity.finish();
            }
        }

    };

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3
                    && size.getWidth() <= 720) {
                // if (size.getWidth() == size.getHeight() * 16 / 9 &&
                // size.getWidth() <= 720) {
                Log.e(TAG, "suitable video size " + size.getWidth() + "*"
                        + size.getHeight());
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size ");
        return choices[choices.length - 1];
    }

    private static Size chooseOptimalSize(Size[] choices, int width,
                                          int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the
        // preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w
                    && option.getWidth() >= width
                    && option.getHeight() >= height) {

                // if (option.getHeight() == option.getWidth() * w / h &&
                // option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_lollipop_record);
        try {
            Intent intent = getIntent();

            txtFileUploadWarning = CheharaUtils.getFileUploadWarningText(this);
            txtFileUploadFinish = CheharaUtils.getFileUploadFinishText(this);
            if (intent.hasExtra(UPLOADABLE)) {
                uploadable = intent.getBooleanExtra(UPLOADABLE, uploadable);
            }
            if (intent.hasExtra("EMAIL")) {
                email = intent.getStringExtra("EMAIL");
            }

            // if (intent.hasExtra(HomeNavActivity.FRAGMENT_INDEX)) {
            //   fragmentIndex = getIntent().getIntExtra(
            //          HomeNavActivity.FRAGMENT_INDEX, 0);
            // } else {
            fragmentIndex = 0;
            // }

            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                lCameraIds = manager.getCameraIdList();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            mTextureView = (AutoFitTextureView) findViewById(R.id.recordVideo);
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            tglBtnCapture = (ToggleButton) findViewById(R.id.recordBtn);

            // Google Anaylitics

            String screenName = "VideoResumeLollipop";

            timer = (TextView) findViewById(R.id.timer);
            timer.setVisibility(View.INVISIBLE);
            backapp = (Button) findViewById(R.id.backApp);
            tglBtnCamera = (ToggleButton) findViewById(R.id.cameraBtn);
            tglBtnCamera.setVisibility(View.INVISIBLE);

            txtFinish = (TextView) findViewById(R.id.textViewFinishText);
            // txtFinish.setVisibility(View.GONE);
            txtFinish.setText(txtFileUploadWarning);
            btnRetry = (Button) findViewById(R.id.buttonRetry);
            btnRetry.setEnabled(false);
            btnRetry.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LollipopRecordActivity.this,
                            LollipopRecordActivity.class);
                    intent.putExtra(UPLOADABLE, uploadable);
                    startActivity(intent);
                    finish();
                }
            });

            btnUpload = (Button) findViewById(R.id.buttonUpload);
            btnUpload.setEnabled(false);

            // controlLayout=(RelativeLayout)findViewById(R.id.layoutControls);
            layoutUpload = (LinearLayout) findViewById(R.id.linearLayoutProgress);
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
                    new MySampleUploadDaemon(LollipopRecordActivity.this)
                            .start();
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

            btnUpload.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    layoutUpload.setVisibility(View.VISIBLE);
                    videoview.setVisibility(View.GONE);
                    timer.setVisibility(View.GONE);
                    mTextureView.setVisibility(View.INVISIBLE);
                    btnRetry.setEnabled(false);
                    tglBtnCamera.setVisibility(View.INVISIBLE);
                    tglBtnCapture.setEnabled(false);
                    btnPreview.setEnabled(false);
                    backapp.setEnabled(false);
                    new MySampleUploadDaemon(LollipopRecordActivity.this)
                            .start();
                    progressBar.setVisibility(View.VISIBLE);
                    btnUpload.setEnabled(false);
                }
            });


            videoview = (VideoView) findViewById(R.id.videoview);
            videoview.setVisibility(View.GONE);
            btnPreview = (Button) findViewById(R.id.buttonPreview);
            final MediaController controller = new MediaController(
                    LollipopRecordActivity.this);
            controller.setAnchorView(videoview);
            videoview.setMediaController(controller);
            btnPreview.setEnabled(false);
            btnPreview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeCamera();
                    // if (camera != null) {
                    // camera.stopPreview();
                    // camera.release();
                    // }
                    // backapp.setEnabled(false);

                    btnPreview.setEnabled(false);
                    tglBtnCapture.setEnabled(false);
                    timer.setVisibility(View.INVISIBLE);
                    btnRetry.setEnabled(true);
                    tglBtnCamera.setVisibility(View.INVISIBLE);
                    mTextureView.setVisibility(View.GONE);
                    if (uploadable)
                        btnUpload.setEnabled(true);
                    videoview.setVisibility(View.VISIBLE);
                    videoview.setVideoPath(sourceFileUri);
                    videoview.start();
                    videoview.requestFocus();
                }
            });

            backapp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsRecordingVideo)
                        stopRecordingVideo();

                 //   Intent homeIntent = new Intent(LollipopRecordActivity.this,
                        //    HomeNavActivity.class);
                //    homeIntent.putExtra(HomeNavActivity.FRAGMENT_INDEX,
                      //      fragmentIndex);
                  //  startActivity(homeIntent);
                    finish();
                }
            });

            tglBtnCapture.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // String msg;
                    if (tglBtnCapture.isChecked()) {
                        Log.e(TAG, "button capture checked");
                        // tglBtnCapture.setCompoundDrawablesWithIntrinsicBounds(
                        // R.drawable.ic_video_stop, 0,0, 0);
                        timer.setVisibility(View.VISIBLE);
                        btnPreview.setEnabled(false);
                        btnUpload.setEnabled(false);
                        // msg = "Recording Started";
                        try {
                            // surfaceView.setVisibility(View.VISIBLE);
                            videoview.setVisibility(View.GONE);
                            tglBtnCamera.setVisibility(View.INVISIBLE);
                            backapp.setEnabled(false);

                            if (mMediaRecorder == null) {
                                openCamera(mTextureView.getWidth(),
                                        mTextureView.getHeight(), lCameraIds[0]);
                                // startPreview();
                                //  mMediaRecorder = new MediaRecorder();
                                //  setUpMediaRecorder();
                            }

                            startRecordingVideo();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "button capture not checked");
                        // tglBtnCapture.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_video_record,
                        // 0, 0, 0);


                        timer.setVisibility(View.INVISIBLE);
                        tglBtnCapture.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_video_record_disabled, 0, 0, 0);
                        tglBtnCapture.setEnabled(false);

                        // msg = "Recording End. File stored in " +
                        // sourceFileUri;
                        // countDownTimer.cancel();
                        stopRecordingVideo();
                        // releaseMediaRecorder();
                        if (lCameraIds.length >= 2) {
                            tglBtnCamera.setVisibility(View.VISIBLE);
                        } else {
                            tglBtnCamera.setVisibility(View.INVISIBLE);
                        }
                        // camera.stopPreview(); //optional
                        if (uploadable)
                            btnUpload.setEnabled(true);
                        backapp.setEnabled(true);
                        btnPreview.setEnabled(true);
                    }
                }
            });

            if (lCameraIds.length >= 2) {
                Log.e(TAG, lCameraIds.length + " camera available");
                tglBtnCamera.setVisibility(View.VISIBLE);
                tglBtnCamera.setOnClickListener(new OnClickListener() {
                    // int currentCameraId;
                    @Override
                    public void onClick(View v) {
                        closeCamera();
                        if (tglBtnCamera.isChecked()) {
                            openCamera(mTextureView.getWidth(),
                                    mTextureView.getHeight(), lCameraIds[1]);
                            // currentCameraId = 2;
                        } else {
                            openCamera(mTextureView.getWidth(),
                                    mTextureView.getHeight(), lCameraIds[0]);
                            // currentCameraId = 1;
                        }
                    }
                });
            } else {
                Log.e(TAG, lCameraIds.length + " camera available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight(),
                    lCameraIds[0]);
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

	/*
     * private void openCamera(String cameraId){ final Activity activity =
	 * Camera2VideoActivity.this; if (null == activity ||
	 * activity.isFinishing()) { return; } CameraManager manager =
	 * (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE); try {
	 * if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
	 * throw new RuntimeException("Time out waiting to lock camera opening."); }
	 * CameraCharacteristics characteristics =
	 * manager.getCameraCharacteristics(cameraId); StreamConfigurationMap map =
	 * characteristics
	 * .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP); Size[]
	 * previewSizes=map.getOutputSizes(SurfaceTexture.class); String t="";
	 * for(Size s:previewSizes){ t+= s.getWidth()+"*"+s.getHeight()+" "; }
	 * Log.e("Supported surfaceSize", t);
	 * 
	 * Size[] videoSizes=map.getOutputSizes(MediaRecorder.class); t=""; for(Size
	 * s:videoSizes){ t+= s.getWidth()+"*"+s.getHeight()+" "; }
	 * Log.e("Supported videoSize", t);
	 * 
	 * mPreviewSize=getOptimalPreviewSize(previewSizes, 4, 3);
	 * mVideoSize=getOptimalPreviewSize(videoSizes, 4, 3); Log.e(TAG,
	 * "videosSize: "+mVideoSize.getWidth()+"*"+mVideoSize.getHeight()); //
	 * mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class)); //
	 * mPreviewSize =
	 * chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width,
	 * height, mVideoSize);
	 * 
	 * int orientation = getResources().getConfiguration().orientation; if
	 * (orientation == Configuration.ORIENTATION_LANDSCAPE) {
	 * mTextureView.setAspectRatio(mPreviewSize.getWidth(),
	 * mPreviewSize.getHeight()); } else {
	 * mTextureView.setAspectRatio(mPreviewSize.getHeight(),
	 * mPreviewSize.getWidth()); } // configureTransform(width, height);
	 * mMediaRecorder = new MediaRecorder(); manager.openCamera(cameraId,
	 * mStateCallback, null); } catch (CameraAccessException e) {
	 * Toast.makeText(activity, "Cannot access the camera.",
	 * Toast.LENGTH_SHORT).show(); activity.finish(); } catch
	 * (NullPointerException e) { new ErrorDialog().show(getFragmentManager(),
	 * "dialog"); } catch (InterruptedException e) { throw new
	 * RuntimeException("Interrupted while trying to lock camera opening."); } }
	 */

    private void openCamera(int width, int height, String cameraId) {
        Log.e(TAG, "openCamera() :" + width + "*" + height);
        final Activity activity = LollipopRecordActivity.this;
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity
                .getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException(
                        "Time out waiting to lock camera opening.");
            }
            // String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager
                    .getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sSize = map.getOutputSizes(SurfaceTexture.class);

            String t = "";
            for (Size s : sSize) {
                t += s.getWidth() + "*" + s.getHeight() + " ";
            }
            Log.e("Supported surfaceSize", t);

            Size[] vSize = map.getOutputSizes(MediaRecorder.class);
            t = "";
            for (Size s : vSize) {
                t += s.getWidth() + "*" + s.getHeight() + " ";
            }
            Log.e("Supported videoSize", t);

            mVideoSize = chooseVideoSize(map
                    .getOutputSizes(MediaRecorder.class));
            Log.e(TAG,
                    "VideoSize: " + mVideoSize.getWidth() + "*"
                            + mVideoSize.getHeight());
            mPreviewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture.class), width, height,
                    mVideoSize);

            // mPreviewSize=getOptimalPreviewSize(sSize, mVideoSize.getWidth(),
            // mVideoSize.getHeight());

            Log.e(TAG, "PreviewSize: " + mPreviewSize.getWidth() + "*"
                    + mPreviewSize.getHeight());
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(),
                        mPreviewSize.getHeight());

            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(),
                        mPreviewSize.getWidth());
            }
            configureTransform(width, height);
            // configureTransform(mPreviewSize.getWidth(),
            // mPreviewSize.getHeight());
            mMediaRecorder = new MediaRecorder();
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            //Toast.makeText(activity, "Cannot access the camera.",
            //	Toast.LENGTH_SHORT).show();
            String txt = "Camera access failed.Check Camera";
            CustomDialog.buildAlertDialogTitle(LollipopRecordActivity.this,
                    txt, "MyChehara Alert").show();
            activity.finish();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not
            // supported on the
            // device this code runs.
            new ErrorDialog().show(getFragmentManager(), "dialog");
        } catch (InterruptedException e) {
            throw new RuntimeException(
                    "Interrupted while trying to lock camera opening.");
        }
    }

    private void closeCamera() {
        Log.e(TAG, "closeCamera()");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {

                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(
                    "Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void startPreview() {
        Log.e(TAG, "startPreview()");
        if (null == mCameraDevice || !mTextureView.isAvailable()
                || null == mPreviewSize) {
            return;
        }
        try {
            setUpMediaRecorder();

            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(),
                    mPreviewSize.getHeight());
            Log.e(TAG, "startPreview() previewsize: " + mPreviewSize.getWidth()
                    + "*" + mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            List<Surface> surfaces = new ArrayList<Surface>();
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);

            mPreviewBuilder.addTarget(previewSurface);

            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            mCameraDevice.createCaptureSession(surfaces,
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(
                                CameraCaptureSession cameraCaptureSession) {
                            mPreviewSession = cameraCaptureSession;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(
                                CameraCaptureSession cameraCaptureSession) {
                            Activity activity = LollipopRecordActivity.this;
                            if (null != activity) {
                                //Toast.makeText(activity, "Failed",
                                //	Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        Log.e(TAG, "updatePreview()");
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        // mode settings
        builder.set(CaptureRequest.CONTROL_MODE,
                CameraMetadata.CONTROL_MODE_AUTO);

    }

    /**
     * Configures the necessary transformation to `mTextureView`. This method
     * should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Log.e(TAG, "configureTransform() " + viewWidth + "*" + viewHeight);
        Activity activity = LollipopRecordActivity.this;
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(),
                mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY
                    - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        Log.e(TAG, "setUpMediaRecorder()");
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdir();
        }
        CamcorderProfile profile;
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P))
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        else
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        Log.e(TAG, "480p  :" + profile.videoFrameWidth + "*"
                + profile.videoFrameHeight);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setMaxDuration(90000);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(sourceFileUri);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        Log.e(TAG, "setUpMediaRecorder() videosize: " + mVideoSize.getWidth()
                + "*" + mVideoSize.getHeight());
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(),
                mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
        mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int orientation = ORIENTATIONS.get(rotation);
        mMediaRecorder.setOrientationHint(orientation);
        mMediaRecorder.prepare();
    }

    private void startRecordingVideo() {
        Log.e(TAG, "startRecordingVideo()");
        try {
            // mButtonVideo.setText(R.string.stop);
            mIsRecordingVideo = true;

            mMediaRecorder.start();
            countDownTimer = new MyCountDownTimer(startTime, interval);
            // if (!timerHasStarted) {
            countDownTimer.start();
            // start.setVisibility(View.GONE);
            timerHasStarted = true;
            // }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingVideo() {
        Log.e(TAG, "stopRecordingVideo()");
        // UI
        // mButtonVideo.setText(R.string.record);
        // Stop recording
        try {
            // controlLayout.setVisibility(View.INVISIBLE);
            // mPreviewSession.stopRepeating();//repeating request
            // discard all pending request
            mPreviewSession.abortCaptures();
            mPreviewSession.close();


            CountDownTimer delaytimer = new CountDownTimer(500, 500) {

                @Override
                public void onTick(long millisUntilFinished) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onFinish() {
                    try {
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        mMediaRecorder.release();
                        Log.e("TAG", "record finish");

                        Activity activity = LollipopRecordActivity.this;
                        if (null != activity) {
                            //   Toast.makeText(activity, "Video saved ",
                            //    Toast.LENGTH_SHORT).show();
                        }
                        countDownTimer.cancel();
                        timer.setText("Over");
                        //timer.setVisibility(View.VISIBLE);
                        Log.e("TAG", "Timer text change");
                        mIsRecordingVideo = false;
                        // controlLayout.setVisibility(View.VISIBLE);
                        closeCamera();

                        //openCamera(mTextureView.getWidth(),
                        //    mTextureView.getHeight(), lCameraIds[0]);
                        // startPreview();

                        // new code
                        backapp.setEnabled(true);
                        // if (Camera.getNumberOfCameras() >= 2) {
                        // tglBtnCamera.setVisibility(View.VISIBLE);
                        // }else{
                        // tglBtnCamera.setVisibility(View.INVISIBLE);
                        // }
                        btnPreview.setEnabled(true);

                        if (uploadable)
                            btnUpload.setEnabled(true);
                        // finish();
                        // -------------------------
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            delaytimer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight()
                    - (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    public static class ErrorDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage("This device doesn't support Camera2 API.")
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface dialogInterface, int i) {
                                    activity.finish();
                                }
                            }).create();
        }

    }

    @Override
    public void onBackPressed() {
    }

    @SuppressWarnings("unused")
    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;
        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight + " view="
                + viewWidth + "x" + viewHeight + " newView=" + newWidth + "x"
                + newHeight + " off=" + xoff + "," + yoff);
        Matrix txform = new Matrix();
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight
                / viewHeight);
        // txform.postRotate(10); // just for fun
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }

    @SuppressWarnings("unused")
    private Size getOptimalPreviewSize(Size[] sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        // final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            stopRecordingVideo();
            tglBtnCapture.toggle();// newly added
            tglBtnCapture.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_video_record, 0);
            backapp.setEnabled(true);
            tglBtnCamera.setVisibility(View.VISIBLE);
            btnPreview.setEnabled(true);
            tglBtnCapture.setChecked(false);
            // tglBtnCapture.setText("Record");
            if (uploadable)
                btnUpload.setEnabled(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            timer.setText("" + millisUntilFinished / 1000 + " Sec");
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
            // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            CustomDialog.buildAlertDialogTitle(LollipopRecordActivity.this,
                    message, "MyChehara Alert").show();
            progressBar.setVisibility(View.GONE);

            if (message.indexOf("completed") != -1) {
                Log.e("Test", "back onClick() invoked");
                btnPreview.setEnabled(false);
                timer.setVisibility(View.INVISIBLE);
                tglBtnCamera.setVisibility(View.INVISIBLE);
                tglBtnCapture.setEnabled(false);
                btnUpload.setEnabled(false);
                btnRetry.setEnabled(false);
                txtFinish.setText(txtFileUploadFinish);
                txtFinish.setVisibility(View.VISIBLE);
                // backapp.setEnabled(true);
                uploaded = true;
                btnRetryUpload.setEnabled(false);
                btnRetryUpload.setVisibility(View.GONE);
                // exitActivity();
            } else {
                btnRetryUpload.setEnabled(true);
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
                    Log.e("TAG", host + "?chehara_email=" + email);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setChunkedStreamingMode(1024);
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
                    // Log.e("Test",
                    // "max bytes: "+bytesAvailable+"  bufferSize:"+bufferSize);

                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    publishProgress("File uploading started");
                    while (bytesRead > 0) {
                        // Log.e("Test",
                        // "read byte: "+bytesRead+" byte avail:"+bytesAvailable);
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
                } catch (ConnectException e) {
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

    @Override
    protected void onStop() {

        super.onStop();
    }
}
