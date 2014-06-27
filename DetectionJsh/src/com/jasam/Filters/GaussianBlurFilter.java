package com.jasam.Filters;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class GaussianBlurFilter implements Filter {
	
	public GaussianBlurFilter(Size ksize){
		this(ksize,0,0);
	}
	
	public GaussianBlurFilter(Size ksize,int sigmaX){
		this(ksize,sigmaX,0);
	}
	
	public GaussianBlurFilter(Size ksize,int sigmaX,int sigmaY){
		setKernelSize(ksize);
		setsigmaX(sigmaX);
		setsigmaY(sigmaY);
	}

	private Size ksize;
	private int sigmaX,sigmaY;
	
	public Size getKernelSize(){
		return ksize;
	}
	
	public void setKernelSize(Size ksize){
		this.ksize = ksize;
	}
	
	public int getsigmaX(){
		return sigmaX;
	}
	
	public int getsigmaY(){
		return sigmaY;
	}
	
	public void setsigmaX(int sigmaX){
		this.sigmaX = sigmaX;
	}
	
	public void setsigmaY(int sigmaY){
		this.sigmaY  = sigmaY;
	}

	@Override
	public void apply(Mat src, Mat dst) {
		if(dst.type()!=src.type()) throw new IllegalArgumentException("dst.type()!=src.type()");
		if(!dst.size().equals(src)) throw new IllegalArgumentException("!dst.size().equals(src)");
		Imgproc.GaussianBlur(src, dst, ksize, sigmaX, sigmaY);
	}

}
