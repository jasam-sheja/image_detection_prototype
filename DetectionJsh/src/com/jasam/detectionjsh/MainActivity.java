package com.jasam.detectionjsh;

import java.net.MalformedURLException;
import java.net.URL;

import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import com.vsn.auth.Authorizator;
import com.vsn.auth.SessionManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends Activity {
	public static final String TAG = "VSN::MainActivity";
	private static EditText etxtUsername, etxtPassword;
	private static final String loginURL = "/login";
	public SessionManager sessionManager;
	public Authorizator authenticator;
	public Token token;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Change layout to activity_main
		setContentView(R.layout.activity_main);
		
		// Set session manager
		this.sessionManager = new SessionManager(getApplicationContext());
		
		// Authenticator
		this.authenticator = new Authorizator(
				sessionManager.getUserDetails().get(SessionManager.KEY_TOKEN),
				sessionManager.getUserDetails().get(SessionManager.KEY_TOKEN_SECRET),
				TwitterApi.SSL.class
				);
		
		// Handling text input
		etxtUsername = (EditText) findViewById(R.id.etxtUsername);
		etxtPassword = (EditText) findViewById(R.id.etxtPassword);
		
		if (sessionManager.isLoggedIn()) {
			etxtUsername.setText(sessionManager.getUserDetails().get(SessionManager.KEY_USERNAME));
			etxtPassword.setText(sessionManager.getUserDetails().get(SessionManager.KEY_PASSWORD));
		}
		
		// Adding OnClickListener to login button
		((Button) findViewById(R.id.btnLogin)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Getting username from text field etxtUsername
				String username = etxtUsername.getText().toString();
				// Getting username from text field etxtPassword
				String password = etxtPassword.getText().toString();
				
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authenticator.getAuthenticationUrl()));
				startActivity(browserIntent);
				
				AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
				alert.setTitle("Authorization");
				alert.setMessage("Enter authorization code");

				// Set an EditText view to get user input 
				final EditText input = new EditText(MainActivity.this);
				alert.setView(input);

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						authenticator.createToken(input.getText().toString());
						}
					});
				alert.show();
				
				try {
					if (authenticator.sendRequest(Verb.POST, new URL(loginURL))!= null) {
						if (((CheckBox) findViewById(R.id.chkBoxRememberMe)).isChecked()) {
							sessionManager.createLoginSession(username, password,
									authenticator.getToken().getToken(),
									authenticator.getToken().getSecret());
						}
						Intent intent = new Intent(MainActivity.this, UserTakeActivity.class);
						startActivity(intent);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}				
			}
		});
	}
}
