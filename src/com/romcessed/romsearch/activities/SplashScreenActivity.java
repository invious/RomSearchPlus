package com.romcessed.romsearch.activities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;

import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.EULA;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.searchproviders.Connector.AuthenticationNotRequiredException;
import com.romcessed.romsearch.tools.GlobalVars;
import com.romcessed.romsearch.tools.ScreenShotTools;

public class SplashScreenActivity extends Activity implements ViewSwitcher.ViewFactory{
	  
	TextSwitcher funnyTextSwitcher;
	List<String> quotes;
	  int quotePosition;
	  DefaultHttpClient client;
	private Handler handler = new Handler();
	private TextSwitcher minorProgressTextSwitcher;
	private TextSwitcher majorProgressTextSwitcher;

	
	
	 @Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash);
		
		funnyTextSwitcher = (TextSwitcher)findViewById(R.id.splash_switcher1);
		
		String[] unshuffledQuotes = getResources().getStringArray(R.array.quotes);
	    quotes = Arrays.asList(unshuffledQuotes); 
	    Collections.shuffle(quotes);

	    funnyTextSwitcher.setFactory(this);

        //Animation over = AnimationUtils.loadAnimation(this, android.R.anim.overshoot_interpolator);
        Animation fadeout = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        Animation fadein = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        
        funnyTextSwitcher.setInAnimation(fadein);
        funnyTextSwitcher.setOutAnimation(fadeout);
        
        minorProgressTextSwitcher = (TextSwitcher)findViewById(R.id.splash_switcher3);
        
        minorProgressTextSwitcher.setFactory(new ViewFactory() {			
			@Override
			public View makeView() {
		        TextView t = new TextView(SplashScreenActivity.this);
		        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/prstartk.ttf");
		        t.setTypeface(tf);
		        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		        t.setTextSize(12);
		        return t;
			}
		});
        
        
        majorProgressTextSwitcher = (TextSwitcher)findViewById(R.id.splash_switcher2);
        majorProgressTextSwitcher.setFactory(new ViewFactory() {			
			@Override
			public View makeView() {
		        TextView t = new TextView(SplashScreenActivity.this);
		        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/prstartk.ttf");
		        t.setTypeface(tf);
		        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		        t.setTextSize(16);
		        return t;
			}
		});
        
        handler.post(updateRunnable);
        
	}

		private Runnable updateRunnable = new Runnable() {
		    public void run() {
		        updateQuote();
		    	handler.postDelayed(this, 3000);
		    }
		};
		private Thread loadThead;
	 
	    private void updateQuote() {
	    	if(quotePosition==(quotes.size()-1)){
	    		quotePosition=0;
	    	}
			funnyTextSwitcher.setText(quotes.get(quotePosition));
			quotePosition++;
	    }
	 
	 
	    @Override
	protected void onStart() {
		super.onStart();
		loadThead = new Thread(){

			@Override
			public void run() {
				AsyncPreLoad loadThings = new AsyncPreLoad();
				loadThings.execute((Void)null);
			}
			
		};
		
		runOnUiThread(loadThead);
	}


	private class AsyncPreLoad extends AsyncTask<Void, String, Void>{
	  @Override
	  protected void onPreExecute(){
		  updateQuote();
	
	  }

	
	  @Override
	protected void onProgressUpdate(String... values) {
		  if(values[0].equals("major")){
			  majorProgressTextSwitcher.setText(values[1]);
		  }
	}

	@Override
	  protected Void doInBackground(Void... voids){
		GlobalVars.initializeUniversalClient();
		while(GlobalVars.getUniversalClient()==null){
			Log.v("client", "still null");
		}
		
		client=GlobalVars.getUniversalClient();
		for(SearchProvider sp : SearchProvider.values()){
			try {
				sp.getConnector().authenticateFromPreferencesStore(SplashScreenActivity.this);
				//new RomulationConnector().authenticateFromPreferencesStore(SplashScreenActivity.this);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AuthenticationNotRequiredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(ScreenShots()){
			ScreenShotTools.screenShotURLreference = getAllRomsFromInternet();
		}
		return null;
	  }

	public boolean ScreenShots(){
	      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SplashScreenActivity.this);
	      return prefs.getBoolean("uss", false);
	}
	
	  @Override
	  protected void onPostExecute(Void params){
		Intent intent = new Intent(SplashScreenActivity.this, ChooseProviderActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_USER_ACTION|Intent.FLAG_FROM_BACKGROUND);
		loadThead.interrupt();
		handler.removeCallbacks(updateRunnable);
		finish();
		startActivity(intent);
	  }
	  
		private TreeMap<String, String> parseOneConsoleList(final String urlPart){
			URL fullURL = null;
			final TreeMap<String, String> returnTreeMap = new TreeMap<String, String>();
			try {
				fullURL = new URL("http://www.vgmuseum.com/" + urlPart + ".html");
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			HttpGet get = new HttpGet(fullURL.toString());
			HttpResponse cResponse = null;
			
			String pageContent = null;
			try {
				cResponse = client.execute(get);
				pageContent = EntityUtils.toString(cResponse.getEntity());
				cResponse.getEntity().consumeContent();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
	    	final Pattern searchPattern = Pattern.compile("<li><a\\s.+?\"(.+?)\"((?=>)>(.+?)<|.+?>(.+?)<)");
	    	Matcher m = searchPattern.matcher(pageContent);
	        while(m.find()){
	        	String RomName;
	        	RomName = (m.group(4)!=null) ? m.group(4) : m.group(3);
	        	String RomScreenShotURL = m.group(1);
	        	String value;
	        	value = "http://www.vgmuseum.com/" + RomScreenShotURL;
	        	returnTreeMap.put(ScreenShotTools.cleanTitle(RomName), value);
	        }

	        
			return returnTreeMap;
			
		}
		
		public HashMap<Integer, TreeMap<String, String>> getAllRomsFromInternet(){
			String[] urls = null;
			HashMap<Integer, TreeMap<String, String>> allRoms = new HashMap<Integer, TreeMap<String,String>>();
			TreeMap<String, String> gameBoyCombined = new TreeMap<String, String>();
			urls = new String[]{"nes_b", "gb_b", "gba_b", "gbc_b", "genesis_b", "gg_b", "sms_b", "snes_b", "psx_b", "n64_b"};
			for(String url : urls){
				int consoleOrdinal = ScreenShotTools.getConsole(url).ordinal();
				if(url.equals("gb_b") || url.equals("gbc_b")){
					gameBoyCombined.putAll(parseOneConsoleList(url));
				} else {
					allRoms.put(ScreenShotTools.getConsole(url).ordinal(), parseOneConsoleList(url));
				}
				publishProgress(new String[]{"major", "Adding screenshots for: " + ScreenShotTools.getConsole(url).getProperName()});
			}
			allRoms.put(Console.GAMEBOY.ordinal(), gameBoyCombined);
			return allRoms;
		}
	  
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        		builder.setMessage("App loading. Exit?");
        		builder.setPositiveButton("Yes", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						setResult(RESULT_CANCELED);
						finish();
					}
				});
        		builder.setNegativeButton("No", new OnClickListener() {		
					public void onClick(DialogInterface dialog, int which) {
					}
				});
        		builder.setTitle("Exit?");
        		builder.show();
        		return true;
        }
        return super.onKeyDown(keyCode, event);
    }


	@Override
	public View makeView() {
        TextView t = new TextView(this);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/prstartk.ttf");
        t.setTypeface(tf);
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        t.setTextSize(20);
        return t;
	}
	

}