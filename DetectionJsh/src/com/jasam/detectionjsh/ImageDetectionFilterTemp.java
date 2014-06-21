package com.jasam.detectionjsh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;

public class ImageDetectionFilterTemp {
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
	private final Scalar mLineColor = new Scalar(0, 255, 0);
	private final Settings settings;

	
	private void SetupSettings(){
		mFeatureDetector = FeatureDetector.create(settings.getDetector());
		mDescriptorExtractor = DescriptorExtractor.create(settings.getExtractor());
		mDescriptorMatcher = DescriptorMatcher.create(settings.getMatcher());
		
		Settings.addObserver(new SettingsAdapter(){

			@Override
			public void onDetectorChanged(int oldtDetector) {
				mFeatureDetector = FeatureDetector.create(settings.getDetector());
			}

			@Override
			public void onExtractorChanged(int oldExtractor) {
				mDescriptorExtractor = DescriptorExtractor.create(settings.getExtractor());
			}

			@Override
			public void onMatcherChanged(int oldExtracot) {
				mDescriptorMatcher = DescriptorMatcher.create(settings.getMatcher());
			}
			
		});
	}
	
	public ImageDetectionFilterTemp(){
		settings = Settings.getInstance();
		SetupSettings();
	}
	
	public int getReferencesCount(){
		return mReferenceImage.size();
	}
	
	public void addReference(Mat rgb){
		int index = mReferenceImage.size();
		mReferenceImage.add(rgb.clone());
		final Mat referenceImageGray = new Mat();
		Imgproc.cvtColor(mReferenceImage.get(index), referenceImageGray, Imgproc.COLOR_BGR2GRAY);
		mSceneCorners.add(new Mat(4, 1, CvType.CV_32FC2));
		mReferenceCorners.add(new Mat(4, 1, CvType.CV_32FC2));
		mReferenceCorners.get(index).put(0, 0, new double[] {0.0, 0.0});
		mReferenceCorners.get(index).put(1, 0, new double[] {referenceImageGray.cols(), 0.0});
		mReferenceCorners.get(index).put(2, 0, new double[] {referenceImageGray.cols(), referenceImageGray.rows()});
		mReferenceCorners.get(index).put(3, 0, new double[] {0.0, referenceImageGray.rows()});
		mReferenceKeypoints.add(new MatOfKeyPoint());
		mFeatureDetector.detect(referenceImageGray, mReferenceKeypoints.get(index));
		mReferenceDescriptors.add(new Mat());
		mDescriptorExtractor.compute(referenceImageGray, mReferenceKeypoints.get(index), mReferenceDescriptors.get(index));
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
		mMatches.clear();
		for(int i=0;i<mReferenceImage.size();i++){
			mMatches.add(new MatOfDMatch());
			mDescriptorMatcher.match(mSceneDescriptors, mReferenceDescriptors.get(i), mMatches.get(i));
		}
	}

	public void apply(final Mat src, final Mat dst) {
		if(getReferencesCount()==0){
			src.copyTo(dst);
			return;
		}
		Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY);
		mFeatureDetector.detect(mGraySrc, mSceneKeypoints);
		mDescriptorExtractor.compute(mGraySrc, mSceneKeypoints,	mSceneDescriptors);
		Match();
		findSceneCorners();
		draw(src, dst);
	}

	private void findSceneCorners() {
		for(int i=0;i<mMatches.size();i++){
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
				//if (dist > maxDist) {
					//maxDist = dist;
				//}
			}
			// The thresholds for minDist are chosen subjectively
			// based on testing. The unit is not related to pixel
			// distances; it is related to the number of failed tests
			// for similarity between the matched descriptors.
			if (minDist > 50.0) {
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

	protected void draw(Mat src, Mat dst) {
		if (dst != src) {
		src.copyTo(dst);
		}
		for(int i=0;i<mSceneCorners.size();i++){
			if (mSceneCorners.get(i).height() < 4) {
//				// The target has not been found.
//				// Draw a thumbnail of the target in the upper-left
//				// corner so that the user knows what it is.
//				int height = mReferenceImage.get(i).height();
//				int width = mReferenceImage.get(i).width();
//				int maxDimension = Math.min(dst.width(), dst.height()) / 2;
//				double aspectRatio = width / (double)height;
//				if (height > width) {
//					height = maxDimension;
//					width = (int)(height * aspectRatio);
//				} else {
//					width = maxDimension;
//					height = (int)(width / aspectRatio);
//				}
//				Mat dstROI = dst.submat(0, height, 0, width);
//				Imgproc.resize(mReferenceImage.get(i), dstROI, dstROI.size(),
//				0.0, 0.0, Imgproc.INTER_AREA);
				continue;
			}
			// Outline the found target in green.
			Core.line(dst, new Point(mSceneCorners.get(i).get(0, 0)),
					new Point(mSceneCorners.get(i).get(1, 0)), mLineColor, 4);
			Core.line(dst, new Point(mSceneCorners.get(i).get(1, 0)),
					new Point(mSceneCorners.get(i).get(2, 0)), mLineColor, 4);
			Core.line(dst, new Point(mSceneCorners.get(i).get(2, 0)),
					new Point(mSceneCorners.get(i).get(3, 0)), mLineColor, 4);
			Core.line(dst, new Point(mSceneCorners.get(i).get(3,0)),
					new Point(mSceneCorners.get(i).get(0, 0)), mLineColor, 4);
		}
	}
}
