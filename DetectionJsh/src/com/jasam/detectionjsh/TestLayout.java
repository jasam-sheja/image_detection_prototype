package com.jasam.detectionjsh;

import java.util.LinkedList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TestLayout extends Activity implements CvCameraViewListener2{
	
	private static final String  TAG = "test layout::Activity";
	
	private CameraBridgeVeiwCustom mOpenCvCameraView;
	
	private List<Mat> selected =  new LinkedList<Mat>();
	private int atImage = -1;
	
	private final int RESULT_LOAD_IMAGE = 1;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.test_layout);
		mOpenCvCameraView = (CameraBridgeVeiwCustom) findViewById(R.id.test_javaCameraView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		//mOpenCvCameraView.SetCaptureFormat();
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		Button b;
		b = (Button) findViewById(R.id.test_load_button);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(
				Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, RESULT_LOAD_IMAGE);				
			}
		});
		b = (Button) findViewById(R.id.test_next_button);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(selected.size()>0)
					atImage = (atImage+1)%selected.size();
			}
		});
		b = (Button) findViewById(R.id.test_prev_button);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(selected.size()>0)
					atImage = (atImage+selected.size()-1)%selected.size();
			}
		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	      
	     if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
	         Uri selectedImage = data.getData();
	         String[] filePathColumn = { MediaStore.Images.Media.DATA };
	 
	         Cursor cursor = getContentResolver().query(selectedImage,
	                 filePathColumn, null, null, null);
	         cursor.moveToFirst();
	 
	         int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	         String picturePath = cursor.getString(columnIndex);
	         cursor.close();
	                      
	         
	         Mat sel = Highgui.imread(picturePath, Highgui.CV_LOAD_IMAGE_COLOR);
	         Imgproc.cvtColor(sel, sel, Imgproc.COLOR_BGR2RGB);
	         atImage++;
	         selected.add(atImage,sel);	         
	         
	     }
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
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }

    public void onDestroy() {
    	Log.i(TAG, "destroying");
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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
		Mat res = atImage<0?null:selected.get(atImage).clone();
		if(res!=null && !res.size().equals(inputFrame.rgba().size())){
			Imgproc.resize(res, res, inputFrame.rgba().size());
		}
		return res;
	}
}
