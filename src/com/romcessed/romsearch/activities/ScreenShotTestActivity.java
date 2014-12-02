package com.romcessed.romsearch.activities;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.R.id;
import com.romcessed.romsearch.R.layout;
import com.romcessed.romsearch.searchproviders.RombayConnector;
import com.romcessed.romsearch.tools.ScreenShotTools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ScreenShotTestActivity extends Activity {
	private static final String TAG = "ScreenShotTestActivity";
	ImageView imView;
	Button bt3;
	Button bt4;
	Gallery g;
	TextView tv;
	ImageAdapter imageAdapter;
	ArrayList<Bitmap> bitmaps;
	
	int ClickCount = 0;
	String romName;
	Console console;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenshot_test);
		bt3 = (Button)findViewById(R.id.scrt_get_imagebt);
		bt4 = (Button)findViewById(R.id.scrt_test_searchbt);
		imView = (ImageView)findViewById(R.id.scrt_imview);
		tv = (TextView)findViewById(R.id.scrt_label);
		g = (Gallery)findViewById(R.id.scrt_gallery);
		
		ScreenShotTools sst = new ScreenShotTools();
		
		Log.v(TAG, "Getting Screenshot Library...");
		sst.initializeScreenShotLibrary(this);
		
		bitmaps = new ArrayList<Bitmap>();
		setGalleryAdapter();
		
	    bt3.setOnClickListener(resetGalleryListener);
			
	}
	
	public void setGalleryAdapter(){
		imageAdapter = new ImageAdapter(this, bitmaps);
		
	 	g.setAdapter(imageAdapter);
        g.setCallbackDuringFling(false);
        g.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(ScreenShotTestActivity.this, LargeScreenshotView.class);
				i.putExtra(ScreenShotTools.BUNDLE_TAG, bitmaps);
				ScreenShotTestActivity.this.startActivity(i);
				
			}
		
        });
        g.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View v, int position, long id) {
					
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//
			}
        	
        });
	}
	
	View.OnClickListener getImgListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view) {
			
		}
	};
	
	View.OnClickListener resetGalleryListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view) {
			switch(ClickCount){
			case 0:
				romName = "Super Mario Bros 3";
				console = Console.NES;
				break;
			case 1:
				romName = "Super Mario Bros 2";
				console = Console.NES;
				break;
			case 2:
				romName = "Evil Super Mario Bros 3";
				console = Console.NES;
				break;
			case 3:
				romName = "Metal Gear Solid";
				console = Console.PSX;
				break;
			case 4:
				romName = "Metal Gear Solid VR Missions";
				console = Console.PSX;
				break;
			default:
				romName = "Mario Bros";
				console = Console.NES;
				break;
			}
			tv.setText("Rom Name: " + romName + " Console: ");
			bitmaps = ScreenShotTools.getScreenshots(romName, console, ScreenShotTestActivity.this);
			setGalleryAdapter();
			ClickCount++;
		}
	};
	
	public class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;

		public ImageAdapter(Context c, ArrayList<Bitmap> bitmaps) {
			mContext = c;
		}

		public int getCount() {
			return bitmaps.size();
		}

		public Object getItem(int position) {
			return bitmaps.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);
			Bitmap b = bitmaps.get(position);
			i.setImageBitmap(bitmaps.get(position));
			i.setAdjustViewBounds(true);
			i.setMaxHeight(200);
			i.setMaxWidth(200);
			i.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));
			i.setScaleType(ImageView.ScaleType.FIT_XY);
			
			//i.setBackgroundResource(mGalleryItemBackground);
			
			return i;
		}

	} 
	
}
