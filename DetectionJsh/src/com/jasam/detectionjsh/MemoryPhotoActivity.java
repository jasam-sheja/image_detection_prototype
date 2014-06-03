package com.jasam.detectionjsh;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MemoryPhotoActivity extends Activity {
	Button go;
	ToggleButton sdcard;
	EditText folder;
	EditText image;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.memory_define_layout);
		
		folder = (EditText) findViewById(R.id.folder_editText);
		image = (EditText) findViewById(R.id.image_editText);
		
		sdcard = (ToggleButton) findViewById(R.id.from_sdcard);
		
		go = (Button) findViewById(R.id.Go_button);
		go.setOnClickListener(new OnClickListener() {
			String path;
			@Override
			public void onClick(View v) {
				path = (sdcard.isChecked()? 
						Environment.getRootDirectory().getAbsolutePath():
						Environment.getExternalStorageDirectory())+"/"+
						folder.getText().toString()+"/"+
						image.getText().toString();
				if(new File(path).exists()){
					Intent intent = new Intent(MemoryPhotoActivity.this,MemoryPhotoSubActivity.class);
					intent.putExtra("path", path);
					startActivity(intent);
				}else{
					Toast.makeText(MemoryPhotoActivity.this, "no such image :: "+path, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
