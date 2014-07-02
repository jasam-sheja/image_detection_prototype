package com.jasam.detectionjsh.imageProccessing;

import com.jasam.detectionjsh.SettingsActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class ImageProccessignActivity extends ListActivity {
	final String[] activitys;

	public ImageProccessignActivity() {
		activitys = new String[] { "blur" };
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getListView().setAdapter(
				new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, activitys));
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (((String) parent.getItemAtPosition(position))
						.equalsIgnoreCase("blur")) {
					Intent intent = new Intent(ImageProccessignActivity.this,
							SmothingSubActivity.class);
					startActivity(intent);
				}

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Settings");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().toString().equalsIgnoreCase("Settings")) {
			startActivity(new Intent(this, SettingsActivity.class));
		}
		return true;
	}
}
