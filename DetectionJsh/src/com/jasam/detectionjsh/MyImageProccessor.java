package com.jasam.detectionjsh;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class MyImageProccessor extends SettingsAdapter{
	Settings settings;
	
	private FeatureDetector detector;
	private DescriptorExtractor extractor;
	private DescriptorMatcher matcher;
	
	Mat target;
	Mat description;
	MatOfKeyPoint keypoints;
	
	Mat scene;
	Mat sceneDescription;
	MatOfKeyPoint scenekeypoints;
	
	public MyImageProccessor(){
		settings = Settings.getInstance();
		Settings.addObserver(this);
		
		detector = FeatureDetector.create(settings.getDetector());
		extractor = DescriptorExtractor.create(settings.getExtractor());
		matcher = DescriptorMatcher.create(settings.getMatcher());
	}
	
	private void Blur(Mat gray){
		if(settings.getBlurType().equalsIgnoreCase(Settings.BLUR_HOMOGENEOUS)){
			Imgproc.blur(gray, gray, new org.opencv.core.Size(settings.getBlurSize(),settings.getBlurSize()));
		}else if(settings.getBlurType().equalsIgnoreCase(Settings.BLUR_GAUSSIAN)){
			Imgproc.GaussianBlur(gray, gray, new org.opencv.core.Size(settings.getBlurSize(), settings.getBlurSize()),0,0);
		}else if(settings.getBlurType().equalsIgnoreCase(Settings.BLUR_MEDIAN)){
			Imgproc.medianBlur(gray, gray, settings.getBlurSize());
		}
	}
	
	public boolean setTarget(Mat target){
		if(target != null){
			removeTarget();
			this.target = target.clone();
			keypoints = new MatOfKeyPoint();
			detector.detect(this.target, keypoints);
			description = new Mat();
			extractor.compute(this.target, keypoints, description);
			if(description.rows()==0 || description.cols() == 0 || description.type() != CvType.CV_8U)
				return false;
			matcher.clear();
			List<Mat> temp = new ArrayList<Mat>();
			temp.add(description);
			matcher.add(temp);
			matcher.train();
			return true;
		}
		return false;
	}

	public boolean addScene(Mat scene){
		if(!haveTarget()) return false;
		this.scene = scene.clone();
		scenekeypoints = new MatOfKeyPoint();
		detector.detect(this.scene, scenekeypoints);
		sceneDescription = new Mat();
		extractor.compute(this.scene, scenekeypoints, sceneDescription);
		if(sceneDescription.rows()==0 || sceneDescription.cols() == 0 || sceneDescription.type() != CvType.CV_8U)
			return false;
		
		
		
		return true;
	}
	
	public boolean haveTarget(){
		return target!= null || description!=null || keypoints!=null;
	}
	
	public void removeTarget(){
		if(haveTarget()){
			target.release();
			description.release();
			keypoints.release();
			
			target = null;
			description = null;
			keypoints = null;
		}
	}

	@Override
	public void onDetectorChanged(int oldtDetector) {
		detector = FeatureDetector.create(settings.getDetector());
	}

	@Override
	public void onExtractorChanged(int oldExtractor) {
		extractor = DescriptorExtractor.create(settings.getExtractor());
	}

	@Override
	public void onMatcherChanged(int oldExtracot) {
		matcher = DescriptorMatcher.create(settings.getMatcher());
	}
	
	
}
