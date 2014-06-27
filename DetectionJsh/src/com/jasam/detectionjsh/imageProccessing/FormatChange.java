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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class FormatChange extends Activity implements CvCameraViewListener2,OnTouchListener {

	private static final String  TAG = "FormatChange";
	
	
	private CameraBridgeVeiwCustom mOpenCvCameraView;
	
	private Mat	mRGB;
	private Mat targetmRGB;
	private int format = 0;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* Now enable camera view to start receiving frames */
                    mOpenCvCameraView.enableView();
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Log.d(TAG, "Creating and seting view");
        mOpenCvCameraView = (CameraBridgeVeiwCustom) new CameraBridgeVeiwCustom(this, -1);
        setContentView(mOpenCvCameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);
	}
	
	public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
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
		final int count = 5;
		Mat res = new Mat();
		if(format%count == 0){
			return inputFrame.rgba();
		}else if(format%count == 1){
			return inputFrame.gray();
		}if(format%count == 2){
			Imgproc.cvtColor(inputFrame.rgba(), res, Imgproc.COLOR_RGBA2BGR);
			return res;
		}
		return null;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		format++;		
		return false;
	}

}
