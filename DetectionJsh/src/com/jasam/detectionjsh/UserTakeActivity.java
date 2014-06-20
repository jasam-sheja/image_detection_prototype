package com.jasam.detectionjsh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
//import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import android.hardware.Camera.Size;

import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

public class UserTakeActivity extends Activity implements CvCameraViewListener2,OnTouchListener{

	private static final String  TAG = "Sample::Detect::Activity";
	private Settings settings;
	
	private CameraBridgeVeiwCustom mOpenCvCameraView;
	private List<Size> mResolutionList;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;
	
	private MatOfKeyPoint targetKeypoint;
	private MatOfKeyPoint keypoints;
	private Mat	mGray;
	private Mat targetmGray , targetdescriptors;
	private Boolean viewmatches = false;
	
	FeatureDetector detecet ;
	DescriptorExtractor extractor ;
	DescriptorMatcher matcher ;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* Now enable camera view to start receiving frames */
                    mOpenCvCameraView.setOnTouchListener(UserTakeActivity.this);
                    mOpenCvCameraView.enableView();
                   
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
            detecet = FeatureDetector.create(FeatureDetector.ORB);
            extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
            matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*Log.d(TAG, "Creating and seting view");
        
        setContentView(R.id.activity_surface_view);
        /*mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(this, -1);
        setContentView(mOpenCvCameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);/
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);*/
        
        Log.d(TAG, "Creating and setting view");
        mOpenCvCameraView = (CameraBridgeVeiwCustom) new CameraBridgeVeiwCustom(this, -1);
        setContentView(mOpenCvCameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        Log.i(TAG, "reading settings");
        settings = Settings.getInstance();
        Log.i(TAG, "blur type: "+settings.getBlurType());
        Log.i(TAG, "blur size = "+settings.getBlurSize());
	}
	
	@Override
    public void onPause()
    {
		Log.i(TAG, "pausing");
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        viewmatches = false;
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
		
		
		mResolutionMenu = menu.addSubMenu("Resolution");
		mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        int idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
         }
        menu.add("Settings");
		menu.add("Info");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {        
        if (item.getTitle().toString().equalsIgnoreCase("Resolution"))
        {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }
        else if(item.getTitle().toString().equalsIgnoreCase("Settings")){
			startActivity(new Intent(this, SettingsActivity.class));
		}else if(item.getTitle().toString().equalsIgnoreCase("Info")){
			String info = "";
			if(settings.getBlurSize()>0){
				info+="blur type: "+settings.getBlurType();
				info+="blur size = "+settings.getBlurSize();
			}
			if(info.length()>0){
				Toast.makeText(this, info, Toast.LENGTH_LONG).show();
			}
		}

        return true;
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
		Mat img_keypoint = new Mat();
		
	    mGray = inputFrame.gray();
	    if(settings.isBlurActive()){
	    	Log.d(TAG, "bluring the image");
			if(settings.getBlurType().equalsIgnoreCase(Settings.BLUR_HOMOGENEOUS)){
				Log.i(TAG, "blur:: using"+settings.getBlurType());
				Imgproc.blur(mGray, mGray, new org.opencv.core.Size(settings.getBlurSize(),settings.getBlurSize()));
			}else if(settings.getBlurType().equalsIgnoreCase(Settings.BLUR_GAUSSIAN)){
				Log.i(TAG, "blur:: using"+settings.getBlurType());
				Imgproc.GaussianBlur(mGray, mGray, new org.opencv.core.Size(settings.getBlurSize(), settings.getBlurSize()),0,0);
			}else if(settings.getBlurType().equalsIgnoreCase(Settings.BLUR_MEDIAN)){
				Log.i(TAG, "blur:: using"+settings.getBlurType());
				Imgproc.medianBlur(mGray, mGray, settings.getBlurSize());
			}
	    }


    	keypoints = new MatOfKeyPoint();
    	detecet.detect(mGray, keypoints);
    	
    	if(viewmatches){    		
	    	
	    	Mat descriptors_1;
	    	descriptors_1 = new Mat();
	    	extractor.compute( mGray, keypoints, descriptors_1 );
	    	
	    	
	    	MatOfDMatch matches = new MatOfDMatch();    	
	    	//matcher.match(descriptors_1,targetdescriptors , matches);
	    	
	    	//Features2d.drawMatches(mGray, keypoints, targetmGray, targetKeypoint, matches, img_keypoint);
	    	
	    	matcher.match(descriptors_1, matches);    	
	    	List<DMatch> dmatches = matches.toList();
	    	
	    	
	    	//double min_dist = 1000/*,avg_dist = 0*/;
//	    	for( int i = 0; i < dmatches.size(); i++ ){ 
//	    		if( dmatches.get(i).distance < min_dist ) 
//	    			min_dist = dmatches.get(i).distance;
//	    		avg_dist += dmatches.get(i).distance;
//			}
	    	//Log.i(TAG, "min dist = "+min_dist);
	    	//avg_dist/=dmatches.size();
	    	//min_dist = Math.min(min_dist + avg_dist/9,40);
	    	
	    	Collections.sort(dmatches, new Comparator<DMatch>() {

				@Override
				public int compare(DMatch lhs, DMatch rhs) {
					if(lhs.distance>rhs.distance)
						return 1;
					else if(lhs.distance<rhs.distance)
						return -1;
					else
						return 0;
				}
	    		
			});
	    	List<DMatch> good_dmatches = new ArrayList<DMatch>();
	    	if(dmatches.size()>3 && dmatches.get(3).distance<70){
	    		good_dmatches = dmatches.subList(0, Math.max(4, dmatches.size()/10));
	    	}
	    	//Log.i(TAG,"matches number = "+dmatches.size());
	    	//Log.i(TAG, "avg dist = "+avg_dist);
	    	//Log.i(TAG, "requir dist = "+min_dist);
	    	
//	    	for( int i = 0; i < dmatches.size(); i++ ){ 
//	    		if( dmatches.get(i).distance <= min_dist )
//	    		{ good_dmatches.add(dmatches.get(i)); }
//	    	}
//	    	Log.i(TAG,"good matches number = "+good_dmatches.size());
	    	
	    	
	    	/*if(good_dmatches.size()<(int)(targetKeypoint.toList().size()*0.9)){
	    		return inputFrame.rgba();
	    	}*/
	    	
	    	MatOfDMatch goodDmatches = new MatOfDMatch();
	    	goodDmatches.fromList(good_dmatches);


	    	Features2d.drawMatches(mGray, keypoints, targetmGray, targetKeypoint, goodDmatches, img_keypoint);
	    	
	    	LinkedList<Point> objList = new LinkedList<Point>();
	    	LinkedList<Point> sceneList = new LinkedList<Point>();

	    	List<KeyPoint> keypoints_objectList = keypoints.toList();
	    	List<KeyPoint> keypoints_sceneList = targetKeypoint.toList();

	    	for(int i = 0; i<good_dmatches.size(); i++){
	    	    objList.addLast(keypoints_objectList.get(good_dmatches.get(i).queryIdx).pt);
	    	    sceneList.addLast(keypoints_sceneList.get(good_dmatches.get(i).trainIdx).pt);
	    	}
	    	if(objList.size()>3 && sceneList.size()>3){
		    	MatOfPoint2f obj = new MatOfPoint2f();
		    	obj.fromList(objList);
		    	
		    	MatOfPoint2f scene = new MatOfPoint2f();
		    	scene.fromList(sceneList);
		    	
		    	Mat hg = Calib3d.findHomography(obj, scene);
	
		    	Mat obj_corners = new Mat(4,1,CvType.CV_32FC2);
		    	Mat scene_corners = new Mat(4,1,CvType.CV_32FC2);
	
		    	obj_corners.put(0, 0, new double[] {0,0});
		    	obj_corners.put(1, 0, new double[] {mGray.cols(),0});
		    	obj_corners.put(2, 0, new double[] {mGray.cols(),mGray.rows()});
		    	obj_corners.put(3, 0, new double[] {0,mGray.rows()});
	
		    	Core.perspectiveTransform(obj_corners,scene_corners, hg);
	
		    	Core.line(img_keypoint, new Point(scene_corners.get(0,0)), new Point(scene_corners.get(1,0)), new Scalar(0, 255, 0),4);
		    	Core.line(img_keypoint, new Point(scene_corners.get(1,0)), new Point(scene_corners.get(2,0)), new Scalar(0, 255, 0),4);
		    	Core.line(img_keypoint, new Point(scene_corners.get(2,0)), new Point(scene_corners.get(3,0)), new Scalar(0, 255, 0),4);
		    	Core.line(img_keypoint, new Point(scene_corners.get(3,0)), new Point(scene_corners.get(0,0)), new Scalar(0, 255, 0),4);
	    	}
    	}
    	else{
    		Features2d.drawKeypoints(mGray, keypoints, img_keypoint);
    	}
    	org.opencv.core.Size previewsize = new org.opencv.core.Size();
    	previewsize.height = mOpenCvCameraView.getResolution().height;
    	previewsize.width = mOpenCvCameraView.getResolution().width;
    	Imgproc.resize(img_keypoint, img_keypoint, previewsize);
    	return img_keypoint;
	}
	
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");
        viewmatches = !viewmatches;
        Log.i(TAG,"onTouch event" + viewmatches);
        
//        Rect roi = new Rect((int)(mGray.width()*0.05),
//        					(int)(mGray.height()*0.05),
//        					(int)(mGray.width()*0.9),
//        					(int)(mGray.height()*0.9));
        if(viewmatches){
        	try{
//	        targetmGray = new Mat(mGray, roi);
//	        targetmGray = targetmGray.clone();
        		targetmGray = mGray.clone();
		        targetKeypoint = keypoints;
		        targetdescriptors = new Mat();
		        extractor.compute( targetmGray, targetKeypoint, targetdescriptors );
		        List<Mat> descriptors = new ArrayList<Mat>();
		        descriptors.add(targetdescriptors);
		        matcher.clear();
		        matcher.add(descriptors);
		        matcher.train();
        	}catch(NullPointerException e){
        		viewmatches = false;
        		Log.e(TAG, "fail to track");
        		Toast.makeText(this, "fail", Toast.LENGTH_SHORT);
        		return false;
        	}
        	Toast.makeText(this, " target frame saved", Toast.LENGTH_SHORT).show();
        }else{
        	Toast.makeText(this, " reset trarget", Toast.LENGTH_SHORT).show();
        }
        
        return false;
    }

}
