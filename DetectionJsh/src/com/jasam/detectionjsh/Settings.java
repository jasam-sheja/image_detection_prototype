package com.jasam.detectionjsh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

public class Settings {

	private static final String TAG = "Settings";

	private Settings(String xmlfile) {
		this.filepath = xmlfile;
		if (new File(filepath).exists())
			try {
				readSettings();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	private void readSettings() throws XmlPullParserException, IOException {
		Log.i(TAG, "start read xml from" + filepath);
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		XmlPullParser parser = factory.newPullParser();
		parser.setInput(new FileReader(filepath));
		int eventType = parser.getEventType();
		String name = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				break;

			case XmlPullParser.START_TAG:
				name = parser.getName();
				Log.i("xml reading", "reading name: " + name);
				if (name.equalsIgnoreCase(BlurTag)) {
					blurType = parser.getAttributeValue(null, "type");
					if (blurType == null)
						throw new XmlPullParserException(
								"missing blur type attriput", parser, null);
					if (!validBlurType(blurType))
						throw new XmlPullParserException("wrong blur type "
								+ blurType, parser, null);
					blurSize = Integer.parseInt(parser.nextText());
					if (!validBlurSize(blurSize))
						throw new XmlPullParserException("invalid blur size "
								+ blurSize, parser, null);
				} else if (name.equalsIgnoreCase(Detector_Tag)) {
					Detector = parser.nextText();
					if (!validDetector(Detector))
						throw new XmlPullParserException("wrong Detector type "
								+ Detector, parser, null);
				} else if (name.equalsIgnoreCase(Extractor_Tag)) {
					Extractor = parser.nextText();
					if (!validExtractor(Extractor))
						throw new XmlPullParserException(
								"wrong Extractor type " + Extractor, parser,
								null);
				} else if (name.equalsIgnoreCase(Matcher_Tag)) {
					String mds = parser.getAttributeValue(null,	MatcherATTmin_dist);
					if (mds == null)
						throw new XmlPullParserException(
								"missing min dist attriput", parser, null);
					minDist = Double.parseDouble(mds);
					if (!validMinDist(minDist))
						throw new XmlPullParserException("wrong minDist value "
								+ minDist, parser, null);
					String mm = parser.getAttributeValue(null, MatcherATTmin_matches);
					if (mm == null)
						throw new XmlPullParserException(
								"missing min matches attriput", parser, null);
					minMatches = Integer.parseInt(mm);
					if (!validMinMatches(minMatches))
						throw new XmlPullParserException("wrong minMatches value "
								+ minMatches, parser, null);
					Matcher = parser.nextText();
					if (!validMatcher(Matcher))
						throw new XmlPullParserException("wrong Matcher type "
								+ Matcher, parser, null);
				} else {
					skip(parser);
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				if (name.equalsIgnoreCase(BlurTag)) {
					// TODO
				}
				break;
			}
			eventType = parser.next();
		}
		Log.i(TAG, "end read xml from" + filepath);
	}

	public void writeSettings(String filepath) throws IOException {
		Log.i(TAG, "saving xml to" + filepath);
		XmlSerializer serializer = Xml.newSerializer();
		File outfile = new File(filepath);
		try {
			if (!outfile.createNewFile()) {
				outfile.delete();
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(outfile);
			serializer.setOutput(writer, "UTF-8");
			serializer.startDocument(null, true);

			// blur tags
			serializer.startTag(null, BlurTag);
			serializer.attribute(null, BlurAttType, blurType);
			serializer.text(blurSize + "");
			serializer.endTag(null, BlurTag);

			/* Feature Detector */
			serializer.startTag(null, Detector_Tag);
			serializer.text(Detector);
			serializer.endTag(null, Detector_Tag);

			/* Descriptor Extractor */
			serializer.startTag(null, Extractor_Tag);
			serializer.text(Extractor);
			serializer.endTag(null, Extractor_Tag);

			/* Descriptor Matcher */
			serializer.startTag(null, Matcher_Tag);
			serializer.attribute(null, MatcherATTmin_dist, minDist + "");
			serializer.attribute(null, MatcherATTmin_matches, minMatches + "");
			serializer.text(Matcher);
			serializer.endTag(null, Matcher_Tag);

			serializer.endDocument();
			serializer.flush();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		Log.i(TAG, "finish saving xml to" + filepath);
		fireSaving();
	}

	public void writeSettings() throws IOException {
		writeSettings(filepath);
	}

	/* blur */
	// tag
	private final static String BlurTag = "blur";
	// att type
	public final static String BlurAttType = "type";
	public final static String BLUR_GAUSSIAN = "gaussian";
	public final static String BLUR_HOMOGENEOUS = "homogeneous";
	public final static String BLUR_MEDIAN = "median";
	public final static String BLUR_NONE = "noBlur";
	public final static List<String> BLUR_TYPES = new ArrayList<String>();
	static {
		BLUR_TYPES.add(Settings.BLUR_GAUSSIAN);
		BLUR_TYPES.add(Settings.BLUR_HOMOGENEOUS);
		BLUR_TYPES.add(BLUR_MEDIAN);
		BLUR_TYPES.add(BLUR_NONE);
	}

	private String blurType = BLUR_GAUSSIAN;

	private static boolean validBlurType(String t) {
		return (t.equalsIgnoreCase(BLUR_GAUSSIAN)
				|| t.equalsIgnoreCase(BLUR_HOMOGENEOUS)
				|| t.equalsIgnoreCase(BLUR_NONE) || t
					.equalsIgnoreCase(BLUR_MEDIAN));
	}

	public String getBlurType() {
		return blurType;
	}

	public void setBlurType(String type) {
		if (validBlurType(type))
			fireBlurTypeChange(type);
	}

	// txt size
	public static final int MaxBlurSize = 7;
	public static final int MinBlurSize = 1;
	private int blurSize = 5;

	private static boolean validBlurSize(int s) {
		return s >= MinBlurSize && s <= MaxBlurSize;
	}

	public int getBlurSize() {
		return blurSize;
	}

	public void setBlurSize(int s) {
		if (validBlurSize(s) && isBlurActive()) {
			int news = s;
			news = s;
			if ((blurType.equalsIgnoreCase(BLUR_GAUSSIAN) || blurType
					.equalsIgnoreCase(BLUR_MEDIAN)) && news % 2 == 0) {
				--news;
				if (news < 0)
					news = 1;
			}
			fireBlurSizeChange(news);
		}
	}

	public boolean isBlurActive() {
		return !blurType.equalsIgnoreCase(BLUR_NONE);
	}

	/* Feature Detector */
	// tag
	private final static String Detector_Tag = "FeatureDetector";
	// txt type
	public final static String Detector_FAST = "FAST";
	public final static String Detector_ORB = "ORB";
	public final static String Detector_STAR = "STAR";
	public final static List<String> Detector_TYPES = new ArrayList<String>();
	static {
		Detector_TYPES.add(Detector_FAST);
		Detector_TYPES.add(Detector_ORB);
		Detector_TYPES.add(Detector_STAR);
	}

	private String Detector = Detector_ORB;

	public int getDetector() {
		return matchDetector(Detector);
	}

	public String getDetectorName() {
		return Detector;
	}

	public void setDetector(int detector) {
		if (matchDetector(detector) != null)
			fireDetectorChange(detector);
	}

	public void setDetectorName(String name) {
		if (validDetector(name))
			fireDetectorChange(name);
	}

	private static boolean validDetector(String d) {
		return (d.equalsIgnoreCase(Detector_FAST) ||
				d.equalsIgnoreCase(Detector_ORB) ||
				d.equalsIgnoreCase(Detector_STAR));
	}

	private static int matchDetector(String detector) {
		if (detector.equalsIgnoreCase(Detector_FAST)) {
			return FeatureDetector.FAST;
		}
		if (detector.equalsIgnoreCase(Detector_ORB)) {
			return FeatureDetector.ORB;
		}
		if (detector.equalsIgnoreCase(Detector_STAR)) {
			return FeatureDetector.STAR;
		}
		return -1;
	}

	private static String matchDetector(int detector) {
		switch (detector) {
		case FeatureDetector.FAST:
			return Detector_FAST;
		case FeatureDetector.ORB:
			return Detector_ORB;
		case FeatureDetector.STAR:
			return Detector_STAR;
		default:
			return null;
		}
	}

	/* Descriptor Extractor */
	// tag
	private final static String Extractor_Tag = "FeatureExtractor";
	// txt type
	public final static String Extractor_BRIEF = "BRIEF";
	public final static String Extractor_ORB = "ORB";
	public final static String Extractor_FREAK = "FREAK";
	public final static List<String> Extractor_TYPES = new ArrayList<String>();
	static {
		Extractor_TYPES.add(Extractor_BRIEF);
		Extractor_TYPES.add(Extractor_ORB);
		Extractor_TYPES.add(Extractor_FREAK);
	}

	private String Extractor = Extractor_ORB;

	public int getExtractor() {
		return matchExtractor(Extractor);
	}

	public void setExtractor(int extractor) {
		if (matchExtractor(extractor) != null) {
			fireExtractorChange(extractor);
		}
	}

	public String getExtractorName() {
		return Extractor;
	}

	public void setExtractorName(String name) {
		if (validExtractor(name))
			fireExtractorChange(name);
	}

	private static boolean validExtractor(String e) {
		return (e.equalsIgnoreCase(Extractor_BRIEF) || 
				e.equalsIgnoreCase(Extractor_ORB) ||
				e.equalsIgnoreCase(Extractor_FREAK));
	}

	private static int matchExtractor(String extractor) {
		if (extractor.equalsIgnoreCase(Extractor_BRIEF)) {
			return DescriptorExtractor.BRIEF;
		}
		if (extractor.equalsIgnoreCase(Extractor_ORB)) {
			return DescriptorExtractor.ORB;
		}
		if (extractor.equalsIgnoreCase(Extractor_FREAK)) {
			return DescriptorExtractor.FREAK;
		}
		return -1;
	}

	private static String matchExtractor(int extractor) {
		switch (extractor) {
		case DescriptorExtractor.BRIEF:
			return Extractor_BRIEF;
		case DescriptorExtractor.ORB:
			return Extractor_ORB;
		case DescriptorExtractor.FREAK:
			return Extractor_FREAK;
		default:
			return null;
		}
	}

	/* Descriptor Matcher */
	// tag
	private final static String Matcher_Tag = "FeatureMatcher";
	// txt type
	public final static String Matcher_BRUTEFORCE = "BRUTEFORCE";
	public final static String Matcher_BRUTEFORCE_HAMMING = "BRUTEFORCE_HAMMING";
	public final static List<String> Matcher_TYPES = new ArrayList<String>();
	static {
		Matcher_TYPES.add(Matcher_BRUTEFORCE);
		Matcher_TYPES.add(Matcher_BRUTEFORCE_HAMMING);
	}

	private String Matcher = Matcher_BRUTEFORCE_HAMMING;

	public int getMatcher() {
		return matchMatcher(Matcher);
	}

	public String getMatcherName() {
		return Matcher;
	}

	public void setMatcher(int matcher) {
		if (matchMatcher(matcher) != null) {
			fireMatcherChange(matcher);
		}
	}

	public void setMatcherName(String name) {
		if (validMatcher(name))
			fireMatcherChange(name);
	}

	private static boolean validMatcher(String m) {
		return (m.equalsIgnoreCase(Matcher_BRUTEFORCE) || m
				.equalsIgnoreCase(Matcher_BRUTEFORCE_HAMMING));
	}
	
	private static int matchMatcher(String matcher) {
		if (matcher.equalsIgnoreCase(Matcher_BRUTEFORCE)) {
			return DescriptorMatcher.BRUTEFORCE;
		}
		if (matcher.equalsIgnoreCase(Matcher_BRUTEFORCE_HAMMING)) {
			return DescriptorMatcher.BRUTEFORCE_HAMMING;
		}
		return -1;
	}

	private static String matchMatcher(int matcher) {
		switch (matcher) {
		case DescriptorMatcher.BRUTEFORCE:
			return Matcher_BRUTEFORCE;
		case DescriptorMatcher.BRUTEFORCE_HAMMING:
			return Matcher_BRUTEFORCE_HAMMING;
		default:
			return null;
		}
	}
	
	// att min_dist
	public final static String MatcherATTmin_dist = "min_dist";
	
	private double minDist = 50;

	public double getMinDist() {
		return minDist;
	}

	public void setMinDist(double min_dist) {
		if (validMinDist(min_dist))
			fireMinDistChange(min_dist);
	}
	
	private static boolean validMinDist(double md) {
		return md > 0;
	}
	
	//att min_matches
	public final static String MatcherATTmin_matches = "min_matches";
	
	public int minMatches = 20;
	public int getMinMatches(){
		return minMatches;
	}
 	public void setMinMatches(int min_matches){
 		if(validMinMatches(min_matches))
 			fireMinMatchesChange(min_matches);
 	}

	private static boolean validMinMatches(int mm){
		return mm > 0;
	}
	
	

	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	private String filepath = Environment.getDataDirectory().toString() + "/"
			+ "jsh.xml";

	public String getFilePath() {
		return new String(filepath);
	}

	public static Settings getInstance() {
		if (appsettings == null)
			appsettings = new Settings(Environment
					.getExternalStorageDirectory().toString()
					+ "/"
					+ "settings.xml");
		return appsettings;
	}

	private static Settings appsettings;
	private static List<settingsObserver> observers = new LinkedList<settingsObserver>();

	public static void addObserver(settingsObserver so) {
		observers.add(so);
	}

	public static void removeObservers(settingsObserver so) {
		observers.remove(so);
	}

	public static void clearObservers() {
		observers.clear();
	}

	private void fireBlurTypeChange(String newType) {
		String old = blurType;
		blurType = newType;
		for (settingsObserver observer : observers) {
			observer.onBlurTypeChanged(old);
		}
	}

	private void fireBlurSizeChange(int newSize) {
		int old = blurSize;
		blurSize = newSize;
		for (settingsObserver observer : observers) {
			observer.onBlurSizeChanged(old);
		}
	}

	private void fireDetectorChange(int newType) {
		int old = matchDetector(Detector);
		Detector = matchDetector(newType);
		for (settingsObserver observer : observers) {
			observer.onDetectorChanged(old);
		}
	}

	private void fireDetectorChange(String newType) {
		int old = matchDetector(Detector);
		Detector = newType;
		for (settingsObserver observer : observers) {
			observer.onDetectorChanged(old);
		}
	}

	private void fireExtractorChange(int newType) {
		int old = matchExtractor(Extractor);
		Extractor = matchExtractor(newType);
		for (settingsObserver observer : observers) {
			observer.onExtractorChanged(old);
		}
	}

	private void fireExtractorChange(String newType) {
		int old = matchExtractor(Extractor);
		Extractor = newType;
		for (settingsObserver observer : observers) {
			observer.onExtractorChanged(old);
		}
	}

	private void fireMatcherChange(int newType) {
		int old = matchMatcher(Matcher);
		Matcher = matchMatcher(newType);
		for (settingsObserver observer : observers) {
			observer.onMatcherChanged(old);
		}
	}

	private void fireMatcherChange(String newType) {
		int old = matchMatcher(Matcher);
		Matcher = newType;
		for (settingsObserver observer : observers) {
			observer.onMatcherChanged(old);
		}
	}

	private void fireMinDistChange(double newDist) {
		double old = minDist;
		minDist = newDist;
		for (settingsObserver observer : observers) {
			observer.onMinDistChanged(old);
		}
	}
	
	private void fireMinMatchesChange(int newMatches){
		int old = minMatches;
		minMatches = newMatches;
		for (settingsObserver observer : observers) {
			observer.onMinMatchesChanged(old);
		}
	}

	private void fireSaving() {
		for (settingsObserver observer : observers) {
			observer.onSettingsSaved();
		}
	}
}
