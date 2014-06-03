package com.jasam.detectionjsh;

import com.jasam.detectionjsh.imageProccessing.SmothingActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	Button userDefine;
	Button memoryDefine;
	Button imageproccessing;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		userDefine = (Button) findViewById(R.id.user_define_photo_button);
		userDefine.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, UserTakeActivity.class);
				startActivity(intent);
			}
		});
		
		memoryDefine = (Button) findViewById(R.id.memory_photo_button);
		memoryDefine.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, MemoryPhotoActivity.class);
				startActivity(intent);				
			}
		});
		
		imageproccessing = (Button) findViewById(R.id.image_processing);
		imageproccessing.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SmothingActivity.class);
				startActivity(intent);				
			}
		});
	}
}
