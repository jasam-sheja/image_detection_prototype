package com.jasam.detectionjsh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class UserTakeActivity2 extends Activity implements CvCameraViewListener2,OnTouchListener{
	private static final String  TAG = "UserTake::Activity-2";
	private Settings settings;
	
	private CameraBridgeVeiwCustom mOpenCvCameraView;
	
	private MatOfKeyPoint targetKeypoint;
	private MatOfKeyPoint mKeypoints;
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
                    mOpenCvCameraView.setOnTouchListener(UserTakeActivity2.this);
                    mOpenCvCameraView.enableView();
                   
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
            detecet = FeatureDetector.create(settings.getDetector());
            extractor = DescriptorExtractor.create(settings.getExtractor());
            matcher = DescriptorMatcher.create(settings.getMatcher());
            Settings.addObserver(new SettingsAdapter(){

				@Override
				public void onDetectorChanged(int oldtDetector) {
					detecet = FeatureDetector.create(settings.getDetector());
				}

				@Override
				public void onExtractorChanged(int oldExtractor) {
					extractor = DescriptorExtractor.create(settings.getExtractor());
				}

				@Override
				public void onMatcherChanged(int oldExtracot) {
					matcher = DescriptorMatcher.create(settings.getMatcher());
				}
            	
            });
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
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
		
	}

	@Override
	public void onCameraViewStopped() {
		
	}

	private void Blur(Mat gray){
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
	
	private List<DMatch> myMatcher(List<DMatch> matches,List<KeyPoint> keyPoints,List<KeyPoint> targetKeyPoints){
		double []p = new double[matches.size()];
		double avg = 0;
		for(int i=0;i<matches.size();++i){	
			p[i] = keyPoints.get(matches.get(i).queryIdx).pt.y-
					keyPoints.get(matches.get(i).queryIdx).pt.x*(
							keyPoints.get(matches.get(i).queryIdx).pt.y-
							targetKeyPoints.get(matches.get(i).trainIdx).pt.y
							)/(keyPoints.get(matches.get(i).queryIdx).pt.x-
									targetKeyPoints.get(matches.get(i).trainIdx).pt.x
									);
			avg+=p[i];
		}
		avg/=p.length;
		Log.i(TAG, "avg p = "+avg);
		List<DMatch> goodDMatches = new ArrayList<DMatch>();;
		if(!Double.isInfinite(avg)){
    		for(int i=0;i<p.length;++i){
    			if(Math.abs(avg-p[i])<Math.abs(avg/6)){
    				goodDMatches.add(matches.get(i));
    				Log.i(TAG, "add p["+i+"] = "+p[i]);
    			}else{
    				Log.i(TAG, "fail p["+i+"] = "+p[i]);
    			}
    		}
		}else{
			for(int i=0;i<p.length;++i){
    			if(Double.isInfinite(p[i])){
    				goodDMatches.add(matches.get(i));
    			}
    		}
		}
		return goodDMatches;
	}
	
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		boolean detected = false;
		Mat img_keypoint = inputFrame.rgba().clone();
		
	    mGray = inputFrame.gray();
	    if(settings.isBlurActive()){
	    	Log.d(TAG, "bluring the image");
	    	Blur(mGray);
	    }


    	mKeypoints = new MatOfKeyPoint();
    	detecet.detect(mGray, mKeypoints);
    	
    	
    	if(viewmatches){    
    		Mat mDescriptors = null;
	    	try{	    	
		    	mDescriptors = new Mat(targetdescriptors.rows(), targetdescriptors.cols(), targetdescriptors.type());
		    	extractor.compute( mGray, mKeypoints, mDescriptors );		    	
		    	
		    	MatOfDMatch matches = new MatOfDMatch();    		    	
		    	matcher.match(mDescriptors, matches);    		    	
		    	List<DMatch> lmatches = matches.toList();
		    	if(lmatches.size() <1) throw new NoMatchesException();
		    	
		    	Collections.sort(lmatches, new Comparator<DMatch>() {
	
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
		    	
//		    	double[] avgdist = new double[lmatches.size()],
//		    			mindist = new double[lmatches.size()],
//		    			maxdist = new double[lmatches.size()] ;
//		    	mindist[0] = lmatches.get(0).distance;
//		    	maxdist[0] = lmatches.get(0).distance;
//		    	avgdist[0] = lmatches.get(0).distance;
//		    	for(int i=1;i<lmatches.size();++i){
//		    		if(lmatches.get(i).distance<mindist[i-1]) 
//		    			mindist[i] = lmatches.get(i).distance;
//		    		else
//		    			mindist[i] = mindist[i-1];
//		    		if(lmatches.get(i).distance>maxdist[i-1])
//		    			maxdist[i] = lmatches.get(i).distance;
//		    		else
//		    			maxdist[i] = maxdist[i-1];
//		    		avgdist[i] += avgdist[i-1] + lmatches.get(i).distance;
//		    	}
		    	
		    	List<KeyPoint> lKeyPoints = mKeypoints.toList(),
		    			lTargetKeyPoints = targetKeypoint.toList();
		    	List<DMatch> lGoodDMatches = new ArrayList<DMatch>();
		    	
		    	if(lmatches.size()>3 && lmatches.get(3).distance<settings.getMinDist()){
		    		int perfect = 4;
		    		for(;perfect<lmatches.size() && lmatches.get(perfect).distance<settings.getMinDist();++perfect){}
		    		--perfect;
		    		List<DMatch> good_dmatches_temp = new ArrayList<DMatch>();
		    		int count ;
		    		count = Math.min(2*settings.getMinMatches(), lmatches.size());		    		
		    		count = Math.max(count, perfect);
		    		good_dmatches_temp = lmatches.subList(0, count);
		    		Log.i(TAG,"perfect matches = "+perfect+" of "+lmatches.size()+" matches, using "+good_dmatches_temp.size()+" match");
		    		if(good_dmatches_temp.get(Math.min(good_dmatches_temp.size()-1,settings.getMinMatches())).distance<=settings.getMinDist()){
		    			lGoodDMatches = good_dmatches_temp;
		    		}else{	    		
		    			lGoodDMatches = myMatcher(good_dmatches_temp, lKeyPoints, lTargetKeyPoints);
		    		}
		    	}	
		    	/*boolean success = false;
		    	for(int j=0;j<avgdist.length*0.9 && !success;j++){
		    		if(j>0){
		    			avgdist[j] -= avgdist[j-1];
		    			mindist[j] = lmatches.get(j).distance;
		    			maxdist[j] = lmatches.get(j).distance;
		    			for(int i=j+1;i<lmatches.size();++i){
				    		if(lmatches.get(i).distance<mindist[i-1]) 
				    			mindist[i] = lmatches.get(i).distance;
				    		else
				    			mindist[i] = mindist[i-1];
				    		if(lmatches.get(i).distance>maxdist[i-1])
				    			maxdist[i] = lmatches.get(i).distance;
				    		else
				    			maxdist[i] = maxdist[i-1];
				    	}
		    		}
			    	for(int i=avgdist.length-1;i>=avgdist.length*0.1 && !success;--i){
			    		//Log.d(TAG, "statistics: mindist = "+mindist[i]+" | maxdist = "+maxdist[i]+" | avgdist = "+avgdist[i]/(avgdist.length-j)+" | points = "+(avgdist.length-j));
				    	if(maxdist[i]-mindist[i]<10 && (maxdist[i]+mindist[i])/(avgdist[i]/(avgdist.length-j)) <2.5 && (maxdist[i]+mindist[i])/(avgdist[i]/(avgdist.length-j)) >1.5){
				    		Log.d(TAG, "statistics: mindist = "+mindist[i]+" | maxdist = "+maxdist[i]+" | avgdist = "+avgdist[i]/(avgdist.length-j)+" | points = "+(avgdist.length-j));
				    		lGoodDMatches = lmatches;
				    		success = true;
				    	}
			    	}
		    	}
		    	if(!success){
		    		lGoodDMatches = myMatcher(lmatches, lKeyPoints, lTargetKeyPoints);
		    	}*/
		    	
		    	
		    	MatOfDMatch goodDmatches = new MatOfDMatch();
		    	goodDmatches.fromList(lGoodDMatches);
		    	if(lGoodDMatches.size()>=settings.getMinMatches()){
		    		detected = true;
		    		Log.i(TAG, "image match ,detctoin success");
		    	}
		    	  	
		    	for(int i=0;i<lGoodDMatches.size();++i){
		    		Core.circle(img_keypoint,
		    				lKeyPoints.get(lGoodDMatches.get(i).queryIdx).pt,
		    				(int)mGray.size().width/200,
		    				new Scalar(25, 200, 255),
		    				-1);	  
		    		Core.circle(img_keypoint,
		    				lTargetKeyPoints.get(lGoodDMatches.get(i).trainIdx).pt,
		    				(int)mGray.size().width/200,
		    				new Scalar(255, 25, 200),
		    				1);
		    		Core.line(img_keypoint,
		    				lKeyPoints.get(lGoodDMatches.get(i).queryIdx).pt,
		    				lTargetKeyPoints.get(lGoodDMatches.get(i).trainIdx).pt,
		    				new Scalar(200, 255, 25),
		    				1);
		    	}	   
	    	}catch(NoMatchesException e){
	    		//do nothing
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		Log.d(TAG, "mGray type : "+mGray.type());
	    		Log.d(TAG, "target type : "+targetmGray.type());
	    		
	    		Log.d(TAG, "mGray size : "+mGray.rows()+" | "+mGray.cols());
	    		Log.d(TAG, "target size : "+targetmGray.rows()+" | "+targetmGray.cols());
	    		
	    		Log.d(TAG, "mGray descriptors type : "+mDescriptors.type());
	    		Log.d(TAG, "target descriptors : "+targetdescriptors.type());
	    		
	    		Log.d(TAG, "mGray descriptors size : "+mDescriptors.rows()+" | "+mDescriptors.cols());
	    		Log.d(TAG, "target descriptors size : "+targetdescriptors.rows()+" | "+targetdescriptors.cols());
	    		viewmatches = false;
	    		Toast.makeText(this, "exception", Toast.LENGTH_SHORT).show();
	    		return mGray;
	    	}
    	}
    	else{
    		List<KeyPoint> keyPoints = mKeypoints.toList();
    		for(int i=0;i<keyPoints.size();++i){
    			Core.circle(img_keypoint,keyPoints.get(i).pt,(int)mGray.size().width/150,new Scalar(255, 25, 200), 2);
    		}
    	}
    	if(viewmatches && detected){
    		Core.circle(img_keypoint, new Point(img_keypoint.width()/circuleScaler, img_keypoint.height()/circuleScaler), (int)Math.min(img_keypoint.width()/(circuleScaler*0.9), img_keypoint.height()/(circuleScaler*0.9)), new Scalar(0, 255, 1), -1);
    	}else if(viewmatches && !detected){
    		Core.circle(img_keypoint, new Point(img_keypoint.width()/circuleScaler, img_keypoint.height()/circuleScaler), (int)Math.min(img_keypoint.width()/(circuleScaler*0.9), img_keypoint.height()/(circuleScaler*0.9)), new Scalar(255, 0, 1), -1);
    	}else{
    		Core.circle(img_keypoint, new Point(img_keypoint.width()/circuleScaler, img_keypoint.height()/circuleScaler), (int)Math.min(img_keypoint.width()/(circuleScaler*0.9), img_keypoint.height()/(circuleScaler*0.9)), new Scalar(255, 150, 50), -1);
    	}
    	org.opencv.core.Size previewsize = new org.opencv.core.Size();
    	previewsize.height = mOpenCvCameraView.getResolution().height;
    	previewsize.width = mOpenCvCameraView.getResolution().width;
    	Imgproc.resize(img_keypoint, img_keypoint, previewsize);
    	return img_keypoint;
	}
	
	private final double circuleScaler = 80;
	
    @Override
    public boolean onTouch(View v, MotionEvent event) {
    	Log.d(TAG, "motion event : "+event.getActionMasked());
    	switch(event.getActionMasked()){
	    	case MotionEvent.ACTION_DOWN:
		        Log.i(TAG,"onTouch event");
		        viewmatches = !viewmatches;
		        Log.i(TAG,"onTouch event" + viewmatches);
		        if(viewmatches){
		        	try{
		        		targetmGray = mGray.clone();
				        targetKeypoint = mKeypoints;
				        targetdescriptors = new Mat();
				        extractor.compute( targetmGray, targetKeypoint, targetdescriptors );
				        if(targetdescriptors.rows() == 0 || targetdescriptors.cols() == 0 || targetdescriptors.type() != CvType.CV_8U){
				        	throw new NullPointerException();
				        }
				        List<Mat> descriptors = new ArrayList<Mat>();
				        descriptors.add(targetdescriptors);
				        matcher.clear();
				        matcher.add(descriptors);
				        matcher.train();
		        	}catch(NullPointerException e){
		        		viewmatches = false;
		        		Log.e(TAG, "fail to track");
		        		e.printStackTrace();
		        		Toast.makeText(this, "fail", Toast.LENGTH_SHORT);
		        		return false;
		        	}
		        	Toast.makeText(this, " target frame saved", Toast.LENGTH_SHORT).show();
		        }else{
		        	Toast.makeText(this, " reset trarget", Toast.LENGTH_SHORT).show();
		        }
		        break;
	        default:
	        	break;
    	}
        
        return false;
    }

}


class NoMatchesException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1356609143534106165L;
	//TODO extend throwable
}