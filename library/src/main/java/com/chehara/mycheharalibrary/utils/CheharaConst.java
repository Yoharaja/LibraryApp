package com.chehara.mycheharalibrary.utils;

import java.io.File;

import android.os.Build;
import android.os.Environment;

public class CheharaConst {
	public static final int DEVICE_API_INT = android.os.Build.VERSION.SDK_INT;
	public static final int ANROID_API_LOILLIPOP = android.os.Build.VERSION_CODES.LOLLIPOP;
	public static final int ANROID_API_KITKAT = android.os.Build.VERSION_CODES.KITKAT;
	public static final int ANROID_API_MARSHMALLOW = Build.VERSION_CODES.M;
	public static final String SAVE3GPFORMAT = ".3gp";
	public static final String SAVEFORMAT = ".mp4";
	public static final String IMAGEFORMAT = ".jpg";
	public static final String SDCARD = Environment
			.getExternalStorageDirectory().getAbsolutePath();
	public static final String CHEHARA_DIR = File.separator + "MyChehara";
	public static final String FILE_NAME = "VideoResume" + SAVEFORMAT;
	public static final String IMAGE_NAME = "ProfilePicture" + IMAGEFORMAT;

	// new server
	private static final String SERVER_IP = "http://54.254.137.0";
	// private static final String SERVER_PORT = "4040";
	private static final String SERVER_PORT = "1234";
	// public static final String
	// SAMPLE_RESUME="http://00825b4f7ad066589744-a82452be1825d7b707da180702e3faf0.r0.cf2.rackcdn.com/1447K5/webm.webm";

	// public static final String MEDIAHOST =
	// "http://d820er5mm3k0u.cloudfront.net";
	//public static final String MEDIAHOST = "http://d2qj6f6kq9h102.cloudfront.net";

	// public static final String SAMPLE_RESUME = MEDIAHOST +
	// "/sample/webm.webm";

	public static final String SAMPLE_RESUME = "http://d820er5mm3k0u.cloudfront.net/mychehara/webm.webm";

	//public static final String SAMPLE_RESUME_POSTER = MEDIAHOST
		//	+ "/mychehara/poster.jpg";

	private static final String LOCAL_IP = "http://192.168.1.57";
	private static final String LOCAL_PORT = "8084";
	private static final String IP = SERVER_IP;
	private static final String PORT = SERVER_PORT;

	// private static final String IP = SERVER_IP;
	// private static final String PORT = SERVER_PORT;

	// public static final String SERVERHOST = IP + ":" + PORT + "/mychehara";
	public static final String SERVERHOST = "http://my.chehara.com/mychehara";
	private static final String API = "/mychehara/api";

	public static final String HOST = SERVERHOST + API;
	public static final String ENDPOINT_FILE_UPLOAD = HOST + "/UploadServlet";
	public static final String ENDPOINT_PDF_UPLOAD = HOST + "/Uploadpdf";



}
