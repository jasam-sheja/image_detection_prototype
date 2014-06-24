package com.vsn.auth;

import java.util.HashMap;
import com.jasam.detectionjsh.MainActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "VSNpref";

	// All Shared Preferences Keys
	private static final String IS_LOGGEDIN = "IsLoggedIn";

	// User name (make variable public to access from outside)
	public static final String KEY_USERNAME = "username";

	// Email address (make variable public to access from outside)
	public static final String KEY_PASSWORD = "password";

	// Token
	public static final String KEY_TOKEN = "token";

	// Token secret
	public static final String KEY_TOKEN_SECRET = "tokensecret";

	// Constructor
	@SuppressLint("CommitPrefEdits")
	public SessionManager(Context context) {
		this.context = context;
		pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	// Create login session
	public void createLoginSession(String username, String password,
			String token, String tokenSecret) {
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGGEDIN, true);
		// Storing username in pref
		editor.putString(KEY_USERNAME, username);
		// Storing password in pref
		editor.putString(KEY_PASSWORD, password);
		// Stroring token
		editor.putString(KEY_TOKEN, token);
		// Stroing tokenSecret
		editor.putString(KEY_TOKEN_SECRET, tokenSecret);
		// commit changes
		editor.commit();
	}

	public void checkLogin() {
		// Check login status
		if (!this.isLoggedIn()) {
			// user is not logged in redirect him to Login Activity
			Intent intent = new Intent(context, MainActivity.class);
			// Closing all the Activities
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			// Add new Flag to start new Activity
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Staring Login Activity
			context.startActivity(intent);
		}
	}

	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		// username
		user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
		// password
		user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
		user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
		user.put(KEY_TOKEN_SECRET, pref.getString(KEY_TOKEN_SECRET, null));

		// return user
		return user;
	}

	public void logoutUser() {
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();

		// After logout redirect user to login activity
		Intent i = new Intent(context, MainActivity.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// Add new Flag to start new Activity
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// Staring Login Activity
		context.startActivity(i);
	}

	// Get Login State
	public boolean isLoggedIn() {
		return pref.getBoolean(IS_LOGGEDIN, false);
	}
}
