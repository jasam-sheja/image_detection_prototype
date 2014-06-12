package com.jasam.detectionjsh;

import java.util.Dictionary;

import com.jasam.detectionjsh.imageProccessing.ImageProccessignActivity;
import com.jasam.detectionjsh.imageProccessing.SmothingSubActivity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends ListActivity {
	
	final String[] activitys;
	
	public MainActivity(){
		activitys = new String[]{"User Camera Photo Take","User Camera Photo Take 2",
								"Hard photo memory",
								"Image proccessing",
								"Settings"};
	}

	Button userDefine;
	Button memoryDefine;
	Button imageproccessing;
	Button settinsButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getListView().setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, activitys));
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(((String)parent.getItemAtPosition(position)).equalsIgnoreCase("User Camera Photo Take")){
					Intent intent = new Intent(MainActivity.this, UserTakeActivity.class);
					startActivity(intent);
				}else if(((String)parent.getItemAtPosition(position)).equalsIgnoreCase("User Camera Photo Take 2")){
					Intent intent = new Intent(MainActivity.this, UserTakeActivity2.class);
					startActivity(intent);
				}else if(((String)parent.getItemAtPosition(position)).equalsIgnoreCase("Hard photo memory")){
					Intent intent = new Intent(MainActivity.this, MemoryPhotoActivity.class);
					startActivity(intent);
				}else if(((String)parent.getItemAtPosition(position)).equalsIgnoreCase("Image proccessing")){
					Intent intent = new Intent(MainActivity.this, ImageProccessignActivity.class);
					startActivity(intent);	
				}else if(((String)parent.getItemAtPosition(position)).equalsIgnoreCase("Settings")){
					Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
					startActivity(intent);
				}
				
			}
		});
		
//		setContentView(R.layout.activity_main);
//		
//		userDefine = (Button) findViewById(R.id.user_define_photo_button);
//		userDefine.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(MainActivity.this, UserTakeActivity.class);
//				startActivity(intent);
//			}
//		});
//		
//		memoryDefine = (Button) findViewById(R.id.memory_photo_button);
//		memoryDefine.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(MainActivity.this, MemoryPhotoActivity.class);
//				startActivity(intent);				
//			}
//		});
//		
//		imageproccessing = (Button) findViewById(R.id.image_processing);
//		imageproccessing.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(MainActivity.this, ImageProccessignActivity.class);
//				startActivity(intent);				
//			}
//		});
//		
//		settinsButton = (Button) findViewById(R.id.settings_button);
//		settinsButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//				startActivity(intent);				
//			}
//		});
	}
}
