package com.jasam.detectionjsh;

import java.io.IOException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.jasam.Filters.DetectionFilter;
import com.jasam.Filters.Filter;

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

public class UserTakeActivity5 extends Activity implements CvCameraViewListener2,OnTouchListener{
	private static final String  TAG = "UserTake::Activity-4";
	
	private CameraBridgeVeiwCustom mOpenCvCameraView;
	private DetectionFilter filter;

	private Mat lastMat;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
					try {
						Mat rgb;
						rgb = Utils.loadResource(UserTakeActivity5.this, android.R.drawable.ic_menu_add, Highgui.CV_LOAD_IMAGE_COLOR);
						Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_BGR2RGBA);
	                    filter = new DetectionFilter(rgb);
					} catch (IOException e) {
						e.printStackTrace();
						finish();
					}
            		
                    for(String path:getIntent().getStringArrayListExtra("images")){
                    	filter.addReference(path);
                    }
                    
                    /* Now enable camera view to start receiving frames */
                    mOpenCvCameraView.setOnTouchListener(UserTakeActivity5.this);
                    mOpenCvCameraView.enableView();
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
            lastMat = new Mat();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Log.d(TAG, "Creating and setting view");
        mOpenCvCameraView = (CameraBridgeVeiwCustom) new CameraBridgeVeiwCustom(this, -1);
        setContentView(mOpenCvCameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);
	}
	
    @Override
    public void onPause()
    {
		Log.i(TAG, "pausing");
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
    	Log.i(TAG, "resuming");
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
    	Log.i(TAG, "destroying");
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Settings");
		return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {        
        if(item.getTitle().toString().equalsIgnoreCase("Settings")){
			startActivity(new Intent(this, SettingsActivity.class));
		}
        return true;
    }
    
    @Override
	public void onCameraViewStarted(int width, int height) {
		Log.i(TAG, "frameSize = "+width+"|"+height);
	}

	@Override
	public void onCameraViewStopped() {
		
	}

	
	
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		inputFrame.rgba().assignTo(lastMat);
		Mat res = new Mat();
		filter.apply(lastMat, res);
		
    	return res;
	}
	
    @Override
    public boolean onTouch(View v, MotionEvent event) {
    	Log.d(TAG, "motion event : "+event.getActionMasked());
    	switch(event.getActionMasked()){
	    	case MotionEvent.ACTION_DOWN:
		        
		        break;
	        default:
	        	break;
    	}
        
        return false;
    }

}
