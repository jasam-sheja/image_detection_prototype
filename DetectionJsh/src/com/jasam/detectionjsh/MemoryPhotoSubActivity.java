package com.jasam.detectionjsh;

import java.util.ArrayList;
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
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.WindowManager;
import android.widget.Toast;

public class MemoryPhotoSubActivity extends Activity implements
		CvCameraViewListener2 {

	private String photopath;

	private static final String TAG = "Sample::Detect::Activity";

	private CameraBridgeVeiwCustom mOpenCvCameraView;
	private List<Size> mResolutionList;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;

	private MatOfKeyPoint targetKeypoint;
	private MatOfKeyPoint keypoints;
	private Mat mGray;
	private Mat targetmGray, targetdescriptors;

	FeatureDetector detecet;
	DescriptorExtractor extractor;
	DescriptorMatcher matcher;

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
			detecet = FeatureDetector.create(FeatureDetector.FAST);
			extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
			matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
			getparameters();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		mResolutionMenu = menu.addSubMenu("Resolution");
		mResolutionList = mOpenCvCameraView.getResolutionList();
		mResolutionMenuItems = new MenuItem[mResolutionList.size()];

		ListIterator<Size> resolutionItr = mResolutionList.listIterator();
		int idx = 0;
		while (resolutionItr.hasNext()) {
			Size element = resolutionItr.next();
			mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
					Integer.valueOf(element.width).toString() + "x"
							+ Integer.valueOf(element.height).toString());
			idx++;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG,
				"called onOptionsItemSelected; selected Group: "
						+ item.getGroupId());
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item.getGroupId() == 2) {
			int id = item.getItemId();
			Size resolution = mResolutionList.get(id);
			mOpenCvCameraView.setResolution(resolution);
			resolution = mOpenCvCameraView.getResolution();
			String caption = Integer.valueOf(resolution.width).toString() + "x"
					+ Integer.valueOf(resolution.height).toString();
			Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
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
		if (detecet == null)
			detecet = FeatureDetector.create(FeatureDetector.FAST);
		// FeatureDetector detecet =
		// FeatureDetector.create(FeatureDetector.HARRIS);
		keypoints = new MatOfKeyPoint();
		detecet.detect(mGray, keypoints);

		if (extractor == null)
			extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
		// DescriptorExtractor extractor =
		// DescriptorExtractor.create(DescriptorExtractor.BRISK);

		Mat descriptors_1;
		descriptors_1 = new Mat();
		extractor.compute(mGray, keypoints, descriptors_1);

		if (matcher == null)
			matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
		// DescriptorMatcher matcher =
		// DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(descriptors_1, targetdescriptors, matches);

		// Features2d.drawMatches(mGray, keypoints, targetmGray, targetKeypoint,
		// matches, img_keypoint);

		List<DMatch> dmatches = matches.toList();

		double max_dist = 0;
		double min_dist = 100;
		for (int i = 0; i < descriptors_1.rows(); i++) {
			double dist = dmatches.get(i).distance;
			if (dist < min_dist)
				min_dist = dist;
			if (dist > max_dist)
				max_dist = dist;
		}

		List<DMatch> good_dmatches = new ArrayList<DMatch>();
		for (int i = 0; i < descriptors_1.rows(); i++) {
			if (dmatches.get(i).distance <= Math.max(2 * min_dist, 0.02))

			{
				good_dmatches.add(dmatches.get(i));
			}
		}

		/*
		 * if(good_dmatches.size()<(int)(targetKeypoint.toList().size()*0.9)){
		 * return inputFrame.rgba(); }
		 */

		MatOfDMatch goodDmatches = new MatOfDMatch();
		goodDmatches.fromList(good_dmatches);

		Features2d.drawMatches(mGray, keypoints, targetmGray, targetKeypoint,
				goodDmatches, img_keypoint);

		LinkedList<Point> objList = new LinkedList<Point>();
		LinkedList<Point> sceneList = new LinkedList<Point>();

		List<KeyPoint> keypoints_objectList = keypoints.toList();
		List<KeyPoint> keypoints_sceneList = targetKeypoint.toList();

		for (int i = 0; i < good_dmatches.size(); i++) {
			objList.addLast(keypoints_objectList.get(good_dmatches.get(i).queryIdx).pt);
			sceneList
					.addLast(keypoints_sceneList.get(good_dmatches.get(i).trainIdx).pt);
		}
		if (objList.size() > 3 && sceneList.size() > 3) {
			MatOfPoint2f obj = new MatOfPoint2f();
			obj.fromList(objList);

			MatOfPoint2f scene = new MatOfPoint2f();
			scene.fromList(sceneList);

			Mat hg = Calib3d.findHomography(obj, scene);

			Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
			Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

			obj_corners.put(0, 0, new double[] { 0, 0 });
			obj_corners.put(1, 0, new double[] { mGray.cols(), 0 });
			obj_corners.put(2, 0, new double[] { mGray.cols(), mGray.rows() });
			obj_corners.put(3, 0, new double[] { 0, mGray.rows() });

			Core.perspectiveTransform(obj_corners, scene_corners, hg);

			Core.line(img_keypoint, new Point(scene_corners.get(0, 0)),
					new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0),
					4);
			Core.line(img_keypoint, new Point(scene_corners.get(1, 0)),
					new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0),
					4);
			Core.line(img_keypoint, new Point(scene_corners.get(2, 0)),
					new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0),
					4);
			Core.line(img_keypoint, new Point(scene_corners.get(3, 0)),
					new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0),
					4);
		}

		Imgproc.resize(img_keypoint, img_keypoint, mGray.size());
		return img_keypoint;
	}

	private void getparameters() {
		photopath = getIntent().getStringExtra("path");
		targetmGray = new Mat();
		Imgproc.cvtColor(Highgui.imread(photopath), targetmGray,
				Imgproc.COLOR_RGB2GRAY);
		targetKeypoint = new MatOfKeyPoint();
		detecet.detect(targetmGray, targetKeypoint);
		targetdescriptors = new Mat();
		extractor.compute(targetmGray, targetKeypoint, targetdescriptors);
	}

	// int maxCorners = 100;
	// double qualitylevel = 0.01;
	// double mindistance = 10.0;
	// @Override
	// public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	// Mat img_keypoint = new Mat();
	//
	// mGray = inputFrame.gray();
	// MatOfPoint matofpoint = new MatOfPoint();
	// Imgproc.goodFeaturesToTrack(mGray, matofpoint, maxCorners,
	// qualitylevel,mindistance );
	// List<KeyPoint> temp = new LinkedList<KeyPoint>();
	// Converters.Mat_to_vector_KeyPoint(Converters.vector_Point_to_Mat(matofpoint.toList()),
	// temp);
	// keypoints = new MatOfKeyPoint();
	// keypoints.fromList(temp);
	//
	// Mat descriptors_1;
	// descriptors_1 = new Mat();
	// extractor.compute( mGray, keypoints, descriptors_1 );
	//
	// MatOfDMatch matches = new MatOfDMatch();
	// matcher.match(descriptors_1, targetdescriptors, matches);
	//
	//
	// Features2d.drawMatches(mGray, keypoints, targetmGray, targetKeypoint,
	// matches, img_keypoint);
	//
	// LinkedList<Point> objList = new LinkedList<Point>();
	// LinkedList<Point> sceneList = new LinkedList<Point>();
	//
	// List<KeyPoint> keypoints_objectList = keypoints.toList();
	// List<KeyPoint> keypoints_sceneList = targetKeypoint.toList();
	// List<DMatch> dmatches = matches.toList();
	// for(int i = 0; i<dmatches.size(); i++){
	// objList.addLast(keypoints_objectList.get(dmatches.get(i).queryIdx).pt);
	// sceneList.addLast(keypoints_sceneList.get(dmatches.get(i).trainIdx).pt);
	// }
	// if(objList.size()>3 && sceneList.size()>3){
	// MatOfPoint2f obj = new MatOfPoint2f();
	// obj.fromList(objList);
	//
	// MatOfPoint2f scene = new MatOfPoint2f();
	// scene.fromList(sceneList);
	//
	// Mat hg = Calib3d.findHomography(obj, scene);
	//
	// Mat obj_corners = new Mat(4,1,CvType.CV_32FC2);
	// Mat scene_corners = new Mat(4,1,CvType.CV_32FC2);
	//
	// obj_corners.put(0, 0, new double[] {0,0});
	// obj_corners.put(1, 0, new double[] {mGray.cols(),0});
	// obj_corners.put(2, 0, new double[] {mGray.cols(),mGray.rows()});
	// obj_corners.put(3, 0, new double[] {0,mGray.rows()});
	//
	// Core.perspectiveTransform(obj_corners,scene_corners, hg);
	//
	// Core.line(img_keypoint, new Point(scene_corners.get(0,0)), new
	// Point(scene_corners.get(1,0)), new Scalar(0, 255, 0),4);
	// Core.line(img_keypoint, new Point(scene_corners.get(1,0)), new
	// Point(scene_corners.get(2,0)), new Scalar(0, 255, 0),4);
	// Core.line(img_keypoint, new Point(scene_corners.get(2,0)), new
	// Point(scene_corners.get(3,0)), new Scalar(0, 255, 0),4);
	// Core.line(img_keypoint, new Point(scene_corners.get(3,0)), new
	// Point(scene_corners.get(0,0)), new Scalar(0, 255, 0),4);
	// }
	//
	// Imgproc.resize(img_keypoint, img_keypoint, mGray.size());
	// return img_keypoint;
	// }
	//
	// private void getparameters(){
	// photopath = getIntent().getStringExtra("path");
	// targetmGray = new Mat();
	// Imgproc.cvtColor(Highgui.imread(photopath), targetmGray,
	// Imgproc.COLOR_RGB2GRAY);
	//
	// MatOfPoint matofpoint = new MatOfPoint();
	// Imgproc.goodFeaturesToTrack(targetmGray, matofpoint, maxCorners,
	// qualitylevel,mindistance );
	// List<KeyPoint> temp = new LinkedList<KeyPoint>();
	// Converters.Mat_to_vector_KeyPoint(Converters.vector_Point_to_Mat(matofpoint.toList()),
	// temp);
	// targetKeypoint = new MatOfKeyPoint();
	// targetKeypoint.fromList(temp);
	//
	//
	//
	// targetdescriptors = new Mat();
	// extractor.compute(targetmGray, targetKeypoint, targetdescriptors);
	// }
}
