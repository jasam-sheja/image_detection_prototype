package com.jasam.detectionjsh;

import com.vsn.auth.SessionManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends Activity {
	public static final String TAG = "VSN::MainActivity";
	private static EditText etxtUsername, etxtPassword;
	public SessionManager sessionManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Change layout to activity_main
		setContentView(R.layout.activity_main);
		
		// Set session manager
		this.sessionManager = new SessionManager(getApplicationContext());
		
		// Handling text input
		etxtUsername = (EditText) findViewById(R.id.etxtUsername);
		etxtPassword = (EditText) findViewById(R.id.etxtPassword);
		
		if (sessionManager.getUserDetails().get("name") != null) {
			etxtUsername.setText(sessionManager.getUserDetails().get("name"));
		}
		
		// Adding OnClickListener to login button
		((Button) findViewById(R.id.btnLogin)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Getting username from text field etxtUsername
				String username = etxtUsername.getText().toString();
				// Getting username from text field etxtPassword
				String password = etxtPassword.getText().toString();
				/*
				 * Authenticator auth = new Authenticator(username,password);
				 * if (auth.Succeed()){
				 * startActivity(intent);
				 * }
				 */
				if (username.equals("Ammar") && password.equals("toto")) {
					if (((CheckBox) findViewById(R.id.chkBoxRememberMe)).isChecked()) {
						sessionManager.createLoginSession(username, password);
						AlertDialog a = new AlertDialog.Builder(MainActivity.this).create();
						a.setTitle("Session Manager Data");
						a.setMessage(sessionManager.getUserDetails().toString());
						a.show();						
					}
					//Intent intent = new Intent(MainActivity.this, UserTakeActivity.class);
					//startActivity(intent);
				}				
			}
		});
	}
}
