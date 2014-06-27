package com.jasam.Filters;

import org.opencv.core.Point;


public interface TrackFilter extends Filter{
	Point[] getTrackedCenters();
	Object getTracked(Point at);
}
