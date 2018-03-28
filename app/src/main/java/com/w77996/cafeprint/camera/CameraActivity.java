package com.w77996.cafeprint.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.lzy.imagepicker.util.Utils;
import com.w77996.cafeprint.R;
import com.w77996.cafeprint.activity.ImageCropActivity;
import com.w77996.cafeprint.utils.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity {

	public static final String TAG = "CameraSimple";
	private Camera mCamera;
	private CameraPreview mPreview;
	private FrameLayout mCameralayout;
	private ImageView mTakePictureBtn;
	private ImageView mSwitchCameraBtn;
	private File takeImageFile;
	private int mCameraId = CameraInfo.CAMERA_FACING_BACK;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_camera);

		if (!checkCameraHardware(this)) {
			Toast.makeText(CameraActivity.this, "�����֧��", Toast.LENGTH_SHORT)
					.show();
		} else {
			openCamera();
			initView();
			setCameraDisplayOrientation(this, mCameraId, mCamera);
		}

	}

	private void initView() {
		mTakePictureBtn = (ImageView) findViewById(R.id.btn_capture);
		mTakePictureBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCamera.autoFocus(mAutoFocusCallback);
			}
		});
		mSwitchCameraBtn = (ImageView) findViewById(R.id.btn_switch_camera);
		mSwitchCameraBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switchCamera();
			}
		});
	}

	// �ж�����Ƿ�֧��
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	// ��ȡ���ʵ��
	public Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(mCameraId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	// ��ʼԤ�����
	public void openCamera() {
		if (mCamera == null) {
			mCamera = getCameraInstance();
			mPreview = new CameraPreview(CameraActivity.this, mCamera);
			mPreview.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mCamera.autoFocus(null);
					return false;
				}
			});
			mCameralayout = (FrameLayout) findViewById(R.id.camera_preview);
			mCameralayout.addView(mPreview);
			mCamera.startPreview();
		}
	}

	// �ͷ����
	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	// ���ջص�
	private PictureCallback mPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(final byte[] data, Camera camera) {


			/*File pictureDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

			final String picturePath = pictureDir
					+ File.separator
					+ new DateFormat().format("yyyyMMddHHmmss", new Date())
							.toString() + ".jpg";*/
			final int cameraid = mCameraId;
			new Thread(new Runnable() {
				@Override
				public void run() {
                    if (Utils.existSDCard()) takeImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
                    else takeImageFile = Environment.getDataDirectory();
                    takeImageFile = createFile(takeImageFile, "IMG_", ".jpg");
					//File file = new File(picturePath);
					try {
						// ��ȡ��ǰ��ת�Ƕ�, ����תͼƬ
						Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
								data.length);
						if (cameraid == CameraInfo.CAMERA_FACING_BACK) {
							bitmap = rotateBitmapByDegree(bitmap, 90);
						} else {
							bitmap = rotateBitmapByDegree(bitmap, -90);
						}
						BufferedOutputStream bos = new BufferedOutputStream(
								new FileOutputStream(takeImageFile));
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
						bos.flush();
						bos.close();
						bitmap.recycle();
						Message msg = new Message();
						msg.what = 1;
						//通知主线程去更新UI
						mUIHandler.sendMessage(msg);

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

			mCamera.startPreview();
		}
	};

	// ��תͼƬ
	public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
		Bitmap returnBm = null;
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		try {
			returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
					bm.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
		}
		if (returnBm == null) {
			returnBm = bm;
		}
		if (bm != returnBm) {
			bm.recycle();
		}
		return returnBm;
	}

	// �������������
	public void setCameraDisplayOrientation(Activity activity, int cameraId,
			Camera camera) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;
		} else {
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	// �л�ǰ�úͺ�������ͷ
	public void switchCamera() {
		CameraInfo cameraInfo = new CameraInfo();
		Camera.getCameraInfo(mCameraId, cameraInfo);
		if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
			mCameraId = CameraInfo.CAMERA_FACING_FRONT;
		} else {
			mCameraId = CameraInfo.CAMERA_FACING_BACK;
		}
		mCameralayout.removeView(mPreview);
		releaseCamera();
		openCamera();
		setCameraDisplayOrientation(CameraActivity.this, mCameraId, mCamera);
	}

	// �۽��ص�
	private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if (success) {
				mCamera.takePicture(null, null, mPictureCallback);
			}
		}
	};

	/**
	 * 根据系统时间、前缀、后缀产生一个文件
	 */
	public static File createFile(File folder, String prefix, String suffix) {
		if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
		String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
		return new File(folder, filename);
	}

	private Handler mUIHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int i = msg.what;
			switch (i){
				case 1:
					Log.d("CameraActivity","finish");
					Intent intent = new Intent(CameraActivity.this, ImageCropActivity.class);
					intent.putExtra("imagefile",takeImageFile.getAbsolutePath());
					Log.d("CameraActivity",takeImageFile.getAbsolutePath()+takeImageFile.getName());
					setResult(Constants.PICTURE_CODE,intent);
					//startActivity(intent);
					finish();
					break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseCamera();
	}
}
