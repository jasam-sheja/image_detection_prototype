package com.jasam.detectionjsh;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class UserTakeActivity4Chose extends Activity {

	private static final String  TAG = "User Take Activity 4 Chose";
	
	private final int RESULT_LOAD_IMAGE = 1;
	
	private ArrayList<String> selected =  new ArrayList<String>();
	private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
	private void addImage(int loc ,String path){
		selected.add(loc, path);           
        images.add(loc, BitmapFactory.decodeFile(path));
	}
	private void removeImage(int loc){
		if(loc<0 || loc>selected.size())
			return;
		selected.remove(loc);
		images.remove(loc);
	}
	
	private int atImage = -1;
	private void setAtImage(int i){
		atImage = i;
		if(atImage != -1 && atImage < selected.size()){			
			image.setImageBitmap(BitmapFactory.decodeFile(selected.get(atImage)));
		}else if(atImage == -1){
			image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		}
	}
	
	
	private ImageView image;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.user_take_4);
		
		image = (ImageView) findViewById(R.id.imageView1);
		
		ImageButton b;
		b = (ImageButton) findViewById(R.id.user_take_load_imageButton);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(
				Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, RESULT_LOAD_IMAGE);				
			}
		});
		
		b = (ImageButton) findViewById(R.id.user_take_next_imageButton);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(selected.size()>0)
					setAtImage((atImage+1)%selected.size());
			}
		});
		
		b = (ImageButton) findViewById(R.id.user_take_prev_imageButton);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(selected.size()>0)
					setAtImage((atImage+selected.size()-1)%selected.size());
			}
		});
	
		b = (ImageButton) findViewById(R.id.user_take_detect_imageButton);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent  = new Intent(UserTakeActivity4Chose.this, UserTakeActivity4.class);
				intent.putStringArrayListExtra("images", selected);
				
				startActivity(intent);
			}
		});
		
		b = (ImageButton) findViewById(R.id.user_take_delete_imageButton);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeImage(atImage);
				if(selected.size()>0)
					setAtImage((atImage+selected.size()-1)%selected.size());
				else{
					setAtImage(-1);
				}
			}
		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	      
	     if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
	         Uri selectedImage = data.getData();
	         String[] filePathColumn = { MediaStore.Images.Media.DATA };
	 
	         Cursor cursor = getContentResolver().query(selectedImage,
	                 filePathColumn, null, null, null);
	         cursor.moveToFirst();
	 
	         int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	         addImage(atImage + 1, cursor.getString(columnIndex));
	         setAtImage(atImage + 1);
	         cursor.close();
	                  
	     }
	}
	
}
