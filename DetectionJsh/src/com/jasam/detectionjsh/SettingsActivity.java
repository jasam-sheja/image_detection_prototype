package com.jasam.detectionjsh;

import java.io.IOException;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends ListActivity {
	private static final String  TAG = "Settings Activity";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String[] objs = new String[]{"blur","blur size",
				"detector",
				"extractor",
				"matcher","Min Dist Matching","Min Matches",
				"Save"};
		getListView().setAdapter(new mylistadapter(this, objs));
		
		getListView().setOnItemClickListener(new myItemClickListner());
	}
	
	
	//section
	
	//end
	private static class mylistadapter extends ArrayAdapter<String>{

		public mylistadapter(Context context, String[] objects) {
			super(context,R.layout.settings_row_2, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Settings settings = Settings.getInstance();
			LayoutInflater inflater = (LayoutInflater) getContext()
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(R.layout.settings_row_2, parent,false);
			LinearLayout ll = (LinearLayout) row.findViewById(R.id.settings_row_linearlayout);
			TextView tv = (TextView) row.findViewById(R.id.settings_row2_textView);
			tv.setText(getItem(position));
			if(getItem(position).equalsIgnoreCase("blur")){
				Spinner sp = new Spinner(getContext());
				sp.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Settings.BLUR_TYPES));
				sp.setSelection(Settings.BLUR_TYPES.indexOf(settings.getBlurType()));
				
				sp.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						settings.setBlurType(Settings.BLUR_TYPES.get(position));						
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {}
				});
				ll.addView(sp);
			}else if(getItem(position).equals("blur size")){
				
				final TextView prog = new TextView(getContext());
				prog.setText(settings.getBlurSize()+"");
				
				SeekBar sb = new SeekBar(getContext());
				sb.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				sb.setMax(Settings.MaxBlurSize);
				sb.setProgress(settings.getBlurSize());
				sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						settings.setBlurSize(seekBar.getProgress());
						seekBar.setProgress(settings.getBlurSize());
						prog.setText(settings.getBlurSize()+"");						
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						prog.setText(progress+"");						
					}
				});
				
			    
				
				LinearLayout lli = new LinearLayout(getContext());
				lli.setOrientation(LinearLayout.VERTICAL);
				lli.addView(sb);
				lli.addView(prog);
				
				ll.addView(lli);
			}else if(getItem(position).equals("detector")){
				Spinner sp = new Spinner(getContext());
				sp.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Settings.Detector_TYPES));
				sp.setSelection(Settings.Detector_TYPES.indexOf(settings.getDetectorName()));				
				sp.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						settings.setDetectorName(Settings.Detector_TYPES.get(position));						
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						
					}
				});
				ll.addView(sp);
			}else if(getItem(position).equals("extractor")){
				Spinner sp = new Spinner(getContext());
				sp.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Settings.Extractor_TYPES));
				sp.setSelection(Settings.Extractor_TYPES.indexOf(settings.getExtractorName()));				
				sp.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						settings.setExtractorName(Settings.Extractor_TYPES.get(position));						
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						
					}
				});
				ll.addView(sp);
			}
			else if(getItem(position).equals("matcher")){
				Spinner sp = new Spinner(getContext());
				sp.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Settings.Matcher_TYPES));
				sp.setSelection(Settings.Matcher_TYPES.indexOf(settings.getMatcherName()));				
				sp.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						settings.setMatcherName(Settings.Matcher_TYPES.get(position));						
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						
					}
				});
				ll.addView(sp);
			}else if(getItem(position).equals("Min Dist Matching")){
				
				final TextView prog = new TextView(getContext());
				prog.setText(settings.getMinDist()+"");
				
				SeekBar sb = new SeekBar(getContext());
				sb.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				sb.setMax(1000);
				sb.setProgress((int)(settings.getMinDist()*10));
				sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						settings.setMinDist(seekBar.getProgress()/10.0);
						seekBar.setProgress((int)(settings.getMinDist()*10));
						prog.setText(settings.getMinDist()+"");						
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						prog.setText(progress/10.0+"");						
					}
				});
				
			    
				
				LinearLayout lli = new LinearLayout(getContext());
				lli.setOrientation(LinearLayout.VERTICAL);
				lli.addView(sb);
				lli.addView(prog);
				
				ll.addView(lli);
			}else if(getItem(position).equals("Min Matches")){
				View tView = inflater.inflate(R.layout.settings_min_matches_row, parent, false);
				final TextView prog = (TextView) tView.findViewById(R.id.settings_min_matches_textView);
				prog.setText(settings.getMinMatches()+"");
				//prog.setLayoutParams(new LayoutParams(200, LayoutParams.WRAP_CONTENT));
				
				final SeekBar sb = (SeekBar) tView.findViewById(R.id.settings_min_matches_seekBar);
				//sb.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				sb.setMax(100);
				sb.setProgress(settings.getMinMatches());
				sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						settings.setMinMatches(seekBar.getProgress());
						seekBar.setProgress(settings.getMinMatches());
						prog.setText(settings.getMinMatches()+"");						
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						prog.setText(progress+"");						
					}
				});
				
				Button tButton;
				tButton = (Button) tView.findViewById(R.id.settings_min_matches_button_minuse);
				tButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						sb.setProgress(sb.getProgress()-1);
					}
				});
				tButton.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						sb.setProgress(sb.getProgress()-20);
						return true;
					}
				});
				tButton = (Button) tView.findViewById(R.id.settings_min_matches_button_plus);
				tButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						sb.setProgress(sb.getProgress()+1);
					}
				});
				tButton.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						sb.setProgress(sb.getProgress()+20);
						return true;
					}
				});

				ll.addView(tView);
			}
			return row;
		}
	}

	public static class myItemClickListner implements OnItemClickListener{	

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.i(TAG,"saving settings to: "+Settings.getInstance().getFilePath());
			if(((String)parent.getItemAtPosition(position)).equalsIgnoreCase("save")){
				try {
					Settings.getInstance().writeSettings();
				} catch (IOException e) {
					Toast.makeText(parent.getContext(), "fail to save", Toast.LENGTH_SHORT).show();
					Log.i(TAG,"fail saving settings");
					e.printStackTrace();
				} catch (Exception e) {
					Toast.makeText(parent.getContext(), "fail to save", Toast.LENGTH_SHORT).show();
					Log.i(TAG,"fail saving settings");
					e.printStackTrace();
				}
				Log.i(TAG,"success saving settings");
			}
			
		}
		
	}
}
