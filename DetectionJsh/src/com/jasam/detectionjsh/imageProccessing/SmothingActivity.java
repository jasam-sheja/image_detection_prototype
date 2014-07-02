package com.jasam.detectionjsh.imageProccessing;

import com.jasam.detectionjsh.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;

public class SmothingActivity extends Activity {
	Button go;
	RadioButton homogeneous;
	RadioButton gaussian;
	RadioButton median;
	RadioButton bilateral;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.smothing_layout);

		homogeneous = (RadioButton) findViewById(R.id.Homogeneous_blur);
		gaussian = (RadioButton) findViewById(R.id.Gaussian_blur);
		median = (RadioButton) findViewById(R.id.Median_blur);
		bilateral = (RadioButton) findViewById(R.id.Bilateral_Filter);

		go = (Button) findViewById(R.id.go_smothing);
		go.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intenet = null;
				if (homogeneous.isChecked()) {
					intenet = new Intent(SmothingActivity.this,
							SmothingSubActivity.class);
					intenet.putExtra("blur", "homogeneous");
				} else if (gaussian.isChecked()) {
					intenet = new Intent(SmothingActivity.this,
							SmothingSubActivity.class);
					intenet.putExtra("blur", "gaussian");
				} else if (median.isChecked()) {
					intenet = new Intent(SmothingActivity.this,
							SmothingSubActivity.class);
					intenet.putExtra("blur", "median");
				} else if (bilateral.isChecked()) {
					intenet = new Intent(SmothingActivity.this,
							SmothingSubActivity.class);
					intenet.putExtra("blur", "bilateral");
				}
				if (intenet != null)
					startActivity(intenet);
			}
		});
	}

}
