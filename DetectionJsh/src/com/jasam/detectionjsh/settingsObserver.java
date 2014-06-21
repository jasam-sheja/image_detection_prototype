package com.jasam.detectionjsh;

public interface settingsObserver {
	void onBlurTypeChanged(String oldType);
	void onBlurSizeChanged(int oldSize);
	void onDetectorChanged(int oldtDetector);
	void onExtractorChanged(int oldExtractor);
	void onMatcherChanged(int oldExtracot);
	void onMinDistChanged(double oldMinDist);
	void onMinMatchesChanged(int oldMinMatches);
	void onSettingsSaved();
}
