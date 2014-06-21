package com.jasam.detectionjsh;

public class SettingsAdapter implements settingsObserver {

	@Override
	public void onBlurTypeChanged(String oldType) {}

	@Override
	public void onBlurSizeChanged(int oldSize) {}

	@Override
	public void onDetectorChanged(int oldtDetector) {}

	@Override
	public void onExtractorChanged(int oldExtractor) {}

	@Override
	public void onMatcherChanged(int oldExtracot) {}

	@Override
	public void onMinDistChanged(double oldMinDist) {}

	@Override
	public void onSettingsSaved() {}

	@Override
	public void onMinMatchesChanged(int oldMinMatches) {}

}
