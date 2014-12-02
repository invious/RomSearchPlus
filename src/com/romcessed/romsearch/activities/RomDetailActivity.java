package com.romcessed.romsearch.activities;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;

import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.ServiceCommunicableActivity;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.searchproviders.Connector.CaptchaNotRequiredException;
import com.romcessed.romsearch.tools.GlobalVars;
import com.romcessed.romsearch.tools.ScreenShotTools;

public class RomDetailActivity extends ServiceCommunicableActivity {
	private static final String TAG = "RomDetailActivity";
	public static final String BUNDLE_TAG = "VIEWROM";
	
	SearchResult mRom;
	private Bundle savedBundle;
	Button btn_DownloadButton;

	ImageButton btn_Google;

	ImageButton btn_Wikipedia;
	ImageView iv_Captcha;
	TextView tv_Title, tv_Detail1, tv_Detail2, tv_Detail3;
	Gallery gal_Screenshots;
	EditText et_CaptchaEntry;
	View captchaView, galView;
	ViewSwitcher captchaSwitcher;
	ViewSwitcher gallerySwitcher;
	LoadCaptchaTask captchaTask;
	LoadScreenShotTask screenshotTask;
	
	String prettyTitle;
	private LoadDescriptionTask loadDescriptionTask;
	
	public RomDetailActivity() {
		super();
	}

	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		boolean canceled = captchaTask.cancel(true);
		Log.v(TAG, "Switched task cancelled?: " + canceled);
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		savedBundle=savedInstanceState;
		setContentView(R.layout.romview_layout);
		
		mRom = (SearchResult) getIntent().getSerializableExtra(BUNDLE_TAG);
		
		String romFileName = getIntent().getStringExtra("ROMFILE");
		int romID = getIntent().getIntExtra("ROMID", -1);
		
		if(romID!=-1){
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mNotificationManager.cancel(Integer.valueOf(romID));
		}
		
		if(romFileName!=null){
			File romFile = new File(romFileName);
			Intent i = new Intent();
			i.setAction(android.content.Intent.ACTION_VIEW);
			i.setDataAndType((Uri.fromFile(romFile)), "application/zip");
			startActivity(i);
			finish();
		}

		//Find views
		btn_DownloadButton =(Button)findViewById(R.id.rv_btn_dwnload);
		btn_DownloadButton.setOnClickListener(downloadButtonListener());
		
		
		btn_Google = (ImageButton)findViewById(R.id.rv_imgbtn_goog);
		btn_Google.setOnClickListener(googleButtonListener());
		btn_Wikipedia = (ImageButton)findViewById(R.id.rv_imgbtn_wiki);
		btn_Wikipedia.setOnClickListener(wikipediaButtonListener());

		tv_Title = (TextView)findViewById(R.id.rv_tv_romtitle);
		tv_Detail1 = (TextView)findViewById(R.id.rv_tv_detail1);
		tv_Detail2 = (TextView)findViewById(R.id.rv_tv_detail2);
		tv_Detail3 = (TextView)findViewById(R.id.rv_tv_detail3);

		
		et_CaptchaEntry = (EditText)findViewById(R.id.rv_et_captcha);
		
		populateView();
		
		//Place indeterminate progress bars for our ImageViews until computation is complete
		captchaTask = new LoadCaptchaTask();
		screenshotTask = new LoadScreenShotTask();
		loadDescriptionTask = new LoadDescriptionTask();
		intializeSwitchers();
		
		if(!SearchProvider.getConnectorFromID(mRom.getConnectorID()).isCaptchaRequired()){
			iv_Captcha.setVisibility(View.INVISIBLE);
			et_CaptchaEntry.setVisibility(View.GONE);
			btn_DownloadButton.setWidth(LayoutParams.FILL_PARENT);
		}
		
		btn_DownloadButton.setVisibility(View.INVISIBLE);
		
		captchaTask.execute((Void) null);
		screenshotTask.execute((Void) null);
		loadDescriptionTask.execute((Void) null);
	}
	

	
	private String debugTEMPbaseUrl = "http://doperoms.com/files/roms/sony_playstation_psx/Metal+Gear+Solid+-+VR+Missions+%28USA%29.zip/176998/Metal+Gear+Solid+-+VR+Missions+.zip";
	private String debugTEMPhtmlcontent = "<center><a href=\"" + "http://www.doperoms.com/files/roms/sony_playstation_psx/GETFILE_Metal Gear Solid - VR Missions (USA).zip" + "\"><img src=\"file:///android_asset/download.png\"></a></center>";
	
	public void populateView(){
		int icon = (mRom.getConsole()==Console.ALL) ? R.drawable.icon : mRom.getConsole().getDrawable();
		ArrayList<String> goodCodes = mRom.getGoodCodes();
		prettyTitle = goodCodes.get(0);
		goodCodes.remove(0);
		
		String goodCodesString = "";
		for(String code : goodCodes){
			goodCodesString += code + "\n";
		}
		
		tv_Title.setText(mRom.getTitle());
		tv_Detail1.setText("Console: " + mRom.getConsole().getProperName());
		tv_Detail2.setText(mRom.getSubInfo1_Title() + ": " + mRom.getSubInfo1());
		
		String detail3 = "File size: " + mRom.getFileSize() + "\n" +
		"Country Code: " + mRom.getCountryCode() + "\n" +
		"GoodCodes: " + goodCodesString;
		tv_Detail3.setText(detail3);
	}
	
	private OnClickListener downloadButtonListener(){
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mRom.getWebViewBaseURL()!=null){
					Intent i = new Intent(RomDetailActivity.this, webViewActivity.class);
					i.putExtra("com.android.contacts.webViewActivity", (Serializable) mRom);
					startActivity(i);
					finish();
				} else {
					try {
						comService.downloadHttp(mRom, mRom.getConnectorID(), et_CaptchaEntry.getText().toString());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finish();
				}
			}
		};
	}
	
	
	private OnClickListener googleButtonListener(){
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
            	Intent i = new Intent();
    			i.setAction(android.content.Intent.ACTION_VIEW);
    			i.setData(Uri.parse("http://www.google.com/search?q=" + URLEncoder.encode(prettyTitle)));
    			startActivity(i);
			}
		};
	}
	
	private OnClickListener wikipediaButtonListener(){
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
            	Intent i = new Intent();
    			i.setAction(android.content.Intent.ACTION_VIEW);
    			i.setData(Uri.parse("http://en.wikipedia.org/wiki/Special:Search?search=" + URLEncoder.encode(prettyTitle)));
    			startActivity(i);
			}
		};
	}
	
	
	
	@Override
	protected void onActivityReady() {
		super.onActivityReady();
		btn_DownloadButton.setVisibility(View.VISIBLE);
	}

	
	private class LoadDescriptionTask extends AsyncTask<Void, Void, Void> {
		private String description;

		@Override
		protected Void doInBackground(Void... params) {
			Connector connector = SearchProvider.getConnectorFromID(mRom.getConnectorID());
			try {
				description = connector.getDescription(RomDetailActivity.this, mRom);
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override /* Background Task is Done */
		protected void onPostExecute(Void result) {
			if(description!=null){
				tv_Detail3.setText(tv_Detail3.getText() + "\nDescription:\n" + description);
			}
		}
		
	}
	
	private class LoadCaptchaTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Connector connector = SearchProvider.getConnectorFromID(mRom.getConnectorID());
			try {
				iv_Captcha.setImageBitmap(connector.getCaptcha());
				Thread.sleep(5); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CaptchaNotRequiredException e) {

				e.printStackTrace();
			}
			return null;
		}
		

		@Override /* Background Task is Done */
		protected void onPostExecute(Void result) {
			captchaSwitcher.showNext();
		}
		
	}
	
	private class LoadScreenShotTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				setGalleryAdapter(gal_Screenshots, ScreenShotTools.getScreenshots(prettyTitle,
						mRom.getConsole(), RomDetailActivity.this));
				Thread.sleep(5); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override /* Background Task is Done */
		protected void onPostExecute(Void result) {
			gallerySwitcher.showNext();
		}
		
	}
		void intializeSwitchers(){
			captchaSwitcher = (ViewSwitcher) findViewById(R.id.rv_vs_captcha);
			gallerySwitcher = (ViewSwitcher) findViewById(R.id.rv_vs_screenshots);
			
			View progressBar1 = View.inflate(RomDetailActivity.this, R.layout.indeter, null);
			View progressBar2 = View.inflate(RomDetailActivity.this, R.layout.indeter, null);
			
			captchaSwitcher.addView(progressBar1);
			gallerySwitcher.addView(progressBar2);
			
			captchaView = getLayoutInflater().inflate(R.layout.vs_captcha_imageview, null);
			galView = getLayoutInflater().inflate(R.layout.vs_screenshot_gallery, null);

			iv_Captcha = (ImageView) captchaView.findViewById(R.id.vs_captcha_iv);
			gal_Screenshots = (Gallery) galView.findViewById(R.id.vs_screenshot_gal);
			
			captchaSwitcher.addView(captchaView);
			gallerySwitcher.addView(galView);
		}
		
		
	
//TODO: does this break everything, or fix downloads?
	@Override
		protected void onStop() {
			super.onStop();
			unbindService(mConnection);
		}



	private class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;
		private ArrayList<Bitmap> bitmaps;
		
		public ImageAdapter(Context context, ArrayList<Bitmap> bitmaps) {
			mContext = context;
			this.bitmaps = bitmaps;
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
			i.setMaxHeight(150);
			i.setMaxWidth(150);
			i.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));
			i.setScaleType(ImageView.ScaleType.FIT_XY);
			return i;
		}

	}
	
	public void setGalleryAdapter(Gallery g, final ArrayList<Bitmap> bitmaps){
		ImageAdapter imageAdapter = new ImageAdapter(this, bitmaps);
	 	g.setAdapter(imageAdapter);
        g.setCallbackDuringFling(false);
        g.setOnItemClickListener(new OnItemClickListener() {
        	
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(RomDetailActivity.this, LargeScreenshotView.class);
				i.putExtra(ScreenShotTools.BUNDLE_TAG, bitmaps);
				RomDetailActivity.this.startActivity(i);
				
			}
		
        });
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		savedBundle = state;
		super.onRestoreInstanceState(state);
	} 

	@Override
	protected void onSaveInstanceState(Bundle state) {
		if(savedBundle!=null){state = savedBundle;}
		super.onRestoreInstanceState(state);
	} 
	
}
