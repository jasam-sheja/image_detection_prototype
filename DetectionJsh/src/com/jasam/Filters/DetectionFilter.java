package com.jasam.Filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.util.Log;

public class DetectionFilter implements TrackFilter {
    private final String TAG = "detectionFilter"; 
	Filter subFilter;
	
	public DetectionFilter(Mat detected){
		this(new EmptyFilter(), detected);
	}
	
	public DetectionFilter(Filter father, Mat detected){
		this.subFilter = father;
		this.imageDetected = detected;
		mFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);
		mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
	}	
	
	private final List<Mat> mReferenceImage = new ArrayList<Mat>();
	private final List<MatOfKeyPoint> mReferenceKeypoints = new ArrayList<MatOfKeyPoint>();
	private final List<Mat> mReferenceDescriptors = new ArrayList<Mat>();
	// CVType defines the color depth, number of channels, and
	// channel layout in the image.
	private final List<Mat> mReferenceCorners = new ArrayList<Mat>();//(4, 1, CvType.CV_32FC2);
	private final MatOfKeyPoint mSceneKeypoints = new MatOfKeyPoint();
	private final Mat mSceneDescriptors = new Mat();
	private final Mat mCandidateSceneCorners = new Mat(4, 1, CvType.CV_32FC2);
	private final List<Mat> mSceneCorners = new ArrayList<Mat>();//(4, 1,	CvType.CV_32FC2);
	private final MatOfPoint mIntSceneCorners = new MatOfPoint();
	private final Mat mGraySrc = new Mat();
	private final List<MatOfDMatch> mMatches = new ArrayList<MatOfDMatch>();
	private FeatureDetector mFeatureDetector;
	private DescriptorExtractor mDescriptorExtractor;
	private DescriptorMatcher mDescriptorMatcher;
	
	private List<Integer> Current = new LinkedList<Integer>();
	private List<Integer> power = new LinkedList<Integer>();
	private final int maxCurrent = 3;
	private final int maxDetectoin = 5;
	private Mat imageDetected; 
	private Point[] detectedPos;
	
	private Queue<Integer> queue = new  LinkedList<Integer>();
	
	
	public void addReference(Mat rgb){
		int index = mReferenceImage.size();
		if(mReferenceImage.size()<maxCurrent)
		{
			Current.add(index);
			Log.d(TAG+"s", "put in current "+index);
		}else{
			queue.add(index);
			Log.d(TAG+"s", "put in queue "+index);
		}
		power.add(0);
		mReferenceImage.add(rgb.clone());
		Imgproc.cvtColor(mReferenceImage.get(index), mReferenceImage.get(index), Imgproc.COLOR_RGBA2GRAY);
		subFilter.apply(mReferenceImage.get(index), mReferenceImage.get(index));
		mSceneCorners.add(new Mat(4, 1, CvType.CV_32FC2));
		mReferenceCorners.add(new Mat(4, 1, CvType.CV_32FC2));
		mReferenceCorners.get(index).put(0, 0, new double[] {0.0, 0.0});
		mReferenceCorners.get(index).put(1, 0, new double[] {mReferenceImage.get(index).cols(), 0.0});
		mReferenceCorners.get(index).put(2, 0, new double[] {mReferenceImage.get(index).cols(), mReferenceImage.get(index).rows()});
		mReferenceCorners.get(index).put(3, 0, new double[] {0.0, mReferenceImage.get(index).rows()});
		mReferenceKeypoints.add(new MatOfKeyPoint());
		mFeatureDetector.detect(mReferenceImage.get(index), mReferenceKeypoints.get(index));
		mReferenceDescriptors.add(new Mat());
		mDescriptorExtractor.compute(mReferenceImage.get(index), mReferenceKeypoints.get(index), mReferenceDescriptors.get(index));
		mMatches.add(new MatOfDMatch());
	}
	
	public void addReference(final Context context,	final int referenceImageResourceID) throws IOException{
		Mat rgb = Utils.loadResource(context, referenceImageResourceID, Highgui.CV_LOAD_IMAGE_COLOR);
		Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_BGR2RGBA);
		addReference(rgb);
	}
	
	public void addReference(final String path) {
		Mat rgb = Highgui.imread(path);
		Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_BGR2RGBA);
		addReference(rgb);
	}

	public void Match(){
		//mMatches.clear();
		for(int k=0;k<Current.size();k++){
			Integer i = Current.get(k);
			//mMatches.add(new MatOfDMatch());
			mDescriptorMatcher.match(mSceneDescriptors, mReferenceDescriptors.get(i), mMatches.get(i));
		}
	}

	private void setDetectables(){
		for(int k=0;k<Current.size();k++){
			Integer i = Current.get(k);
			if(power.get(i) <= -1*maxDetectoin){
				Log.d(TAG+"s", "remove from current "+i);
				queue.add(i);
				Current.remove(i);
				power.set(i, 0);
			}
		}
		for(int i=Current.size();i<maxCurrent && queue.size()>0;i++){
			Current.add(queue.poll());
		}
		String l = "";
		for(int i:Current){
			l+= i +", ";
		}
		Log.d(TAG+"s", "in current "+l);
		 l = "";
		for(int i:queue){
			l+= i +", ";
		}
		Log.d(TAG+"s", "in queque "+l);
	}
	
	private boolean anyDetectable(){
		return Current.size()>0;
	}
	public void apply(final Mat src, final Mat dst) {		
		setDetectables();
		
		if(!anyDetectable()){
			src.copyTo(dst);
			return;
		}
		if(src.type()!=CvType.CV_8UC1)
			Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY);
		subFilter.apply(mGraySrc, mGraySrc);
		mFeatureDetector.detect(mGraySrc, mSceneKeypoints);
		mDescriptorExtractor.compute(mGraySrc, mSceneKeypoints,	mSceneDescriptors);
		Match();
		findSceneCorners();
		findTrackedCenters();
		draw(src, dst);
	}

	private void findSceneCorners() {
		for(int k=0;k<Current.size();k++){
			Integer i = Current.get(k);
			List<DMatch> matchesList = mMatches.get(i).toList();
			if (matchesList.size() < 4) {
				// There are too few matches to find the homography.
				mSceneCorners.get(i).create(0, 0, mSceneCorners.get(i).type());
				continue;
			}
			List<KeyPoint> referenceKeypointsList =	mReferenceKeypoints.get(i).toList();
			List<KeyPoint> sceneKeypointsList =	mSceneKeypoints.toList();
			// Calculate the max and min distances between keypoints.
			//double maxDist = 0.0;
			double minDist = Double.MAX_VALUE;
			for(DMatch match : matchesList) {
			double dist = match.distance;
				if (dist < minDist) {
					minDist = dist;
				}
			}
			Log.d(TAG,"mindist for "+i+" is "+minDist);
			// The thresholds for minDist are chosen subjectively
			// based on testing. The unit is not related to pixel
			// distances; it is related to the number of failed tests
			// for similarity between the matched descriptors.
			if (minDist > 40.0) {
				// The target is completely lost.
				// Discard any previously found corners.
				mSceneCorners.get(i).create(0, 0, mSceneCorners.get(i).type());
				continue;
			} else if (minDist > 25.0) {
				// The target is lost but maybe it is still close.
				// Keep any previously found corners.
				continue;
			}
			// Identify "good" keypoints based on match distance.
			ArrayList<Point> goodReferencePointsList = new ArrayList<Point>();
			ArrayList<Point> goodScenePointsList = new ArrayList<Point>();
			double maxGoodMatchDist = 1.75 * minDist;
			for(DMatch match : matchesList) {
				if (match.distance < maxGoodMatchDist) {
					goodReferencePointsList.add(referenceKeypointsList.get(match.trainIdx).pt);
					goodScenePointsList.add(sceneKeypointsList.get(match.queryIdx).pt);
				}
			}
			if (goodReferencePointsList.size() < 4 ||
				goodScenePointsList.size() < 4) {
				// There are too few good points to find the homography.
				continue;
			}
			MatOfPoint2f goodReferencePoints = new MatOfPoint2f();
			goodReferencePoints.fromList(goodReferencePointsList);
			MatOfPoint2f goodScenePoints = new MatOfPoint2f();
			goodScenePoints.fromList(goodScenePointsList);
			Mat homography = Calib3d.findHomography(goodReferencePoints, goodScenePoints);
			Core.perspectiveTransform(mReferenceCorners.get(i),mCandidateSceneCorners, homography);
			mCandidateSceneCorners.convertTo(mIntSceneCorners, CvType.CV_32S);
			if (Imgproc.isContourConvex(mIntSceneCorners)) {
				mCandidateSceneCorners.copyTo(mSceneCorners.get(i));
			}
		}
	}

	private void findTrackedCenters(){
		List<Point> centers = new ArrayList<Point>();
		for(int k=0;k<Current.size();k++){
			Integer i = Current.get(k);
			if (mSceneCorners.get(i).height() < 4){
				power.set(i, Math.max(power.get(i)-1,-maxDetectoin));
				continue;
			}
			centers.add(getCenter(mSceneCorners.get(i)));
			power.set(i,Math.min(power.get(i)+1,maxDetectoin));
		}
		detectedPos = Arrays.copyOf(centers.toArray(), centers.size(),Point[].class);
		Log.d(TAG,"find "+centers.size()+", put"+detectedPos.length);
	}
	
	private Point getCenter(Mat corners){
		Point c = new Point(0, 0);
		for(int i=0;i<4;i++){
			Point temp = new Point(corners.get(i, 0));
			c.x+=temp.x;
			c.y+=temp.y;
		}
		c.x/=4;
		c.y/=4;
		return c;
	}
	
	protected void draw(Mat src, Mat dst) {
		if (dst != src) {
			src.copyTo(dst);
		}
		for(Point c : getTrackedCenters()){
			int height = imageDetected.height();
			int width = imageDetected.width();
			int maxDimension = Math.min(dst.width(), dst.height()) / 5;
			double aspectRatio = width / (double)height;
			if (height > width) {
				height = maxDimension;
				width = (int)(height * aspectRatio);
			} else {
				width = maxDimension;
				height = (int)(width / aspectRatio);
			}
			height += height%2;
			width += width%2;
			
			width = Math.min(width, (dst.cols() - (int)c.x-1)*2);
			width = Math.min(width, (int)(c.x-1)/2);
			height = Math.min(height, (dst.rows() - (int)c.y-1)*2);
			height = Math.min(height, (int)(c.y-1)/2);
			
			try{
			Mat dstROI ,temp = new Mat();
				dstROI = dst.submat((int)(c.y-height/2), (int)(c.y+height/2), (int)(c.x-width/2), (int)(c.x+width/2));
			Imgproc.resize(imageDetected, temp, dstROI.size(), 0.0, 0.0, Imgproc.INTER_AREA);
			Core.addWeighted(dstROI, 0.6, temp, 0.4, 0, dstROI);
			}catch(Exception e){
				e.printStackTrace();
				Log.d(TAG, "dst rows = "+dst.rows()+" dst cols = "+dst.cols()+" || width = "+width+" height = "+height+" || ceter"+c.toString());
			}
		}
	}

	
	@Override
	public Point[] getTrackedCenters() {
		return detectedPos;
	}

	@Override
	public Object getTracked(Point at) {
		// TODO Auto-generated method stub
		return null;
	}

}
