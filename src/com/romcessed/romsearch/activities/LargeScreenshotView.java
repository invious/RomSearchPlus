package com.romcessed.romsearch.activities;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ViewSwitcher.ViewFactory;

import com.romcessed.romsearch.R;
import com.romcessed.romsearch.tools.ScreenShotTools;

public class LargeScreenshotView extends Activity {


	ImageSwitcher iSwitcher;
	ArrayList<Bitmap> bitmaps;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		bitmaps = (ArrayList<Bitmap>) i.getSerializableExtra(ScreenShotTools.BUNDLE_TAG); 
		setContentView(R.layout.screenshot_dialog);
		setTitle("Screenshot Viewer");
		Gallery gallery = (Gallery) findViewById(R.id.Gallery01);
		gallery.setAdapter(new ImageAdapter(this));
		
	}

	public class ImageAdapter extends BaseAdapter {

		private Context ctx;

		public ImageAdapter(Context c) {
			ctx = c; 
		}

		@Override
		public int getCount() {
			return bitmaps.size();
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {

			ImageView iView = new ImageView(ctx);
			iView.setImageBitmap(bitmaps.get(arg0));
			iView.setScaleType(ImageView.ScaleType.FIT_XY);
			iView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.FILL_PARENT, Gallery.LayoutParams.FILL_PARENT));
			return iView;
		}

	}


}