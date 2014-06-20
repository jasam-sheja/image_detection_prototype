package com.jasam.detectionjsh;

import java.util.Dictionary;

import com.jasam.detectionjsh.imageProccessing.ImageProccessignActivity;
import com.jasam.detectionjsh.imageProccessing.SmothingSubActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);
		
		((Button) findViewById(R.id.btnLogin)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = ((EditText) findViewById(R.id.etxtUsername)).getText().toString();
				String password = ((EditText) findViewById(R.id.etxtPassword)).getText().toString();
				/*
				 * Authenticator auth = new Authenticator(username,password);
				 * if (auth.Succeed()){
				 * startActivity(intent);
				 * }
				 */
				Intent intent = new Intent(MainActivity.this, UserTakeActivity.class);
				startActivity(intent);
			}
		});
	}
}
