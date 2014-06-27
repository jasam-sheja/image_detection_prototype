package com.jasam.Filters;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class HistogramEqualizer implements Filter {

	@Override
	public void apply(Mat src, Mat dst) {
		if(src.type()!=CvType.CV_8UC1) throw new IllegalArgumentException("src.type()!=CvType.CV_8UC1");
		Imgproc.equalizeHist(src, dst);
	}

}
