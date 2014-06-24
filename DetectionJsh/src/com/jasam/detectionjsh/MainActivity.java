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
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String TAG = "VSN::MainActivity";
	private static EditText etxtUsername, etxtPassword;
	private static final String loginURL = "/login";
	public SessionManager sessionManager;
	public Authorizator authorizator;
	public Token token;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set session manager
		this.sessionManager = new SessionManager(getApplicationContext());
		String storedUsername = sessionManager.getUserDetails().get(
				SessionManager.KEY_USERNAME);
		String storedPassword = sessionManager.getUserDetails().get(
				SessionManager.KEY_PASSWORD);

		// Set authorizator
		this.authorizator = new Authorizator(sessionManager.getUserDetails()
				.get(SessionManager.KEY_TOKEN), sessionManager.getUserDetails()
				.get(SessionManager.KEY_TOKEN_SECRET), TwitterApi.SSL.class);

		// If was logged in and authorizator created a token (token and secret
		// provided)
		// And open new intent
		if (sessionManager.isLoggedIn() && authorizator.getToken() != null) {
			Intent intent = new Intent(getApplicationContext(),
					UserTakeActivity.class);
			startActivity(intent);
		} else {
			// Change layout to activity_main (login) if token not present
			setContentView(R.layout.activity_main);

			// Handling text input
			etxtUsername = (EditText) findViewById(R.id.etxtUsername);
			etxtPassword = (EditText) findViewById(R.id.etxtPassword);
			etxtUsername.setText(storedUsername);
			etxtPassword.setText(storedPassword);

			// Adding OnClickListener to login button
			((Button) findViewById(R.id.btnLogin))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// Getting username from text field etxtUsername
							String username = etxtUsername.getText().toString();
							// Getting username from text field etxtPassword
							String password = etxtPassword.getText().toString();

							// Check login validity
							Token auth = authenticate(username, password);
							if (auth != null) {
								// Store username, password and token
								// credintials to remember
								if (rememberMe()) {
									sessionManager.createLoginSession(username,
											password, auth.getToken(),
											auth.getSecret());
								}
								Intent intent = new Intent(MainActivity.this,
										UserTakeActivity.class);
								startActivity(intent);
							} else {
								Toast.makeText(getApplicationContext(),
										"Invalid Credentials",
										Toast.LENGTH_LONG).show();
							}
						}
					});
		}
	}

	private boolean rememberMe() {
		return ((CheckBox) findViewById(R.id.chkBoxRememberMe)).isChecked();
	}

	private Token authenticate(String username, String password) {
		// Perform implicit authentication to server and return token
		return new Token("toto", "toto");
	}
}

/*
 * if (authorizator.getToken() == null) { Intent browserIntent = new
 * Intent(Intent.ACTION_VIEW, Uri.parse(authorizator.getAuthenticationUrl()));
 * startActivity(browserIntent); AlertDialog.Builder alert = new
 * AlertDialog.Builder(MainActivity.this); alert.setTitle("Authorization");
 * alert.setMessage("Enter authorization code");
 * 
 * // Set an EditText view to get user input final EditText input = new
 * EditText(MainActivity.this); alert.setView(input);
 * alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() { public
 * void onClick(DialogInterface dialog, int whichButton) {
 * authorizator.createToken(input.getText().toString()); } }); alert.show(); }
 * 
 * try { if (authorizator.sendRequest(Verb.POST, new URL(loginURL))!= null) { if
 * (((CheckBox) findViewById(R.id.chkBoxRememberMe)).isChecked()) {
 * sessionManager.createLoginSession(username, password,
 * authorizator.getToken().getToken(), authorizator.getToken().getSecret()); }
 * Intent intent = new Intent(MainActivity.this, UserTakeActivity.class);
 * startActivity(intent); } } catch (MalformedURLException e) {
 * e.printStackTrace(); }
 */
