package com.jasam.Filters;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class HomogenousBlurFilter implements Filter {

	public HomogenousBlurFilter(Size ksize){
		this(ksize,new Point(-1, -1));
	}
	
	public HomogenousBlurFilter(Size ksize,Point anchor){
		setKernelSize(ksize);
		setAnchorPoint(anchor);
	}
	
	Size ksize;
	Point anchor;
	
	public Size getKernelSize(){
		return ksize;
	}
	
	public void setKernelSize(Size ksize){
		this.ksize = ksize;
	}
	
	public Point getAnchorPoint(){
		return anchor;
	}
	
	public void setAnchorPoint(Point anchor){
		this.anchor = anchor;
	}
	
	
	@Override
	public void apply(Mat src, Mat dst) {
		if(dst.type()!=src.type()) throw new IllegalArgumentException("dst.type()!=src.type()");
		if(!dst.size().equals(src)) throw new IllegalArgumentException("!dst.size().equals(src)");
		Imgproc.blur(src, dst, ksize, anchor);

	}

}
