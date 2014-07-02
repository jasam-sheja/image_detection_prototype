package com.jasam.detectionjsh.imageProccessing;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.jasam.detectionjsh.CameraBridgeVeiwCustom;
import com.jasam.detectionjsh.Settings;
import com.jasam.detectionjsh.SettingsActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SmothingSubActivity extends Activity implements
		CvCameraViewListener2, OnTouchListener {

	private static final String TAG = "Sample::Detect::Activity";
	private Settings settings;

	private CameraBridgeVeiwCustom mOpenCvCameraView;

	private Mat mRGB;
	private Mat targetmRGB;
	boolean smothing = true;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				/* Now enable camera view to start receiving frames */
				mOpenCvCameraView.enableView();

			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Log.d(TAG, "Creating and seting view");
		mOpenCvCameraView = (CameraBridgeVeiwCustom) new CameraBridgeVeiwCustom(
				this, -1);
		setContentView(mOpenCvCameraView);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setOnTouchListener(this);

		settings = Settings.getInstance();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Settings");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().toString().equalsIgnoreCase("Settings")) {
			startActivity(new Intent(this, SettingsActivity.class));
		}
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if (smothing && settings.isBlurActive()) {
			mRGB = inputFrame.rgba();
			targetmRGB = new Mat(mRGB.rows(), mRGB.cols(), mRGB.type());
			if (settings.getBlurType().equalsIgnoreCase(
					Settings.BLUR_HOMOGENEOUS)) {
				Imgproc.blur(mRGB, targetmRGB, new org.opencv.core.Size(
						settings.getBlurSize(), settings.getBlurSize()));
			} else if (settings.getBlurType().equalsIgnoreCase(
					Settings.BLUR_GAUSSIAN)) {
				Imgproc.GaussianBlur(mRGB, targetmRGB,
						new org.opencv.core.Size(settings.getBlurSize(),
								settings.getBlurSize()), 0, 0);
			} else if (settings.getBlurType().equalsIgnoreCase(
					Settings.BLUR_MEDIAN)) {
				Imgproc.medianBlur(mRGB, targetmRGB, settings.getBlurSize());
			}
			// else if(settings.getBlurType().equalsIgnoreCase("bilateral")){
			// Mat temp = new Mat(mRGB.rows(), mRGB.cols(), CvType.CV_8UC3);
			// mRGB.convertTo(temp, CvType.CV_8UC3);
			// targetmRGB = new Mat(temp.rows(), temp.cols(), temp.type());
			// Imgproc.bilateralFilter(temp, targetmRGB, 5,10,2);
			// }

			return targetmRGB;
		} else {
			return inputFrame.rgba();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		smothing = !smothing;
		Toast.makeText(this,
				(smothing ? "smothing" : "not smothing") + CvType.CV_8UC3,
				Toast.LENGTH_SHORT).show();

		return false;
	}

}
