package com.romcessed.romsearch.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.RelativeLayout.LayoutParams;

import com.romcessed.romsearch.EULA;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchProviderButton;
import com.romcessed.romsearch.R.id;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.searchproviders.DopeRomsConnector;
import com.romcessed.romsearch.searchproviders.RomhackingConnector;
import com.romcessed.romsearch.searchproviders.Connector.AuthenticationNotRequiredException;
import com.romcessed.romsearch.tools.GlobalVars;
import com.romcessed.romsearch.tools.NetworkUtils;

public class ChooseProviderActivity extends Activity {
	private TextView titleTextView;
	private SearchProviderButton romBayServiceProvider, romulationServiceProvider;
	private ArrayList<SearchProviderButton> buttons, toPaintRed;
	private boolean loaded = false;
	private RelativeLayout tempLayout;
	private checkWebsiteTask cwt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tempLayout = new RelativeLayout(ChooseProviderActivity.this);
        ProgressBar tempProgress = new ProgressBar(ChooseProviderActivity.this);
        tempProgress.setIndeterminate(true);
        tempProgress.setId(6346346);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        tempLayout.addView(tempProgress, lp);
		setContentView(tempLayout);
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {  
			    switch(msg.what) {
			    case 1:
			    	handler.postDelayed(RunableLoadUI, 200);
			    	break;
			    case 2:
			    	Runnable runOnUI = new Runnable() {	
						@Override
						public void run() {
					    	cwt = new checkWebsiteTask();
					    	cwt = (checkWebsiteTask) cwt.execute("");
						}
					};
					runOnUiThread(runOnUI);
			    	break;
			    case 3:
			    	cwt.cancel(true);
			    	websiteDownProgressDialog.dismiss();
			    	break;
			    }
			}
			
		};
        
        EULA.show(this, "Please agree to continue", "I agree", "I disagree", "enduser", R.string.enduser);
        EULA.show(this, "Please agree to continue", "I agree", "I disagree", "firstLaw", R.string.firstLaw);
        askAboutScreenShots();
        changesDialog();
		handler.sendEmptyMessage(1);
        
	}
	
	private Handler handler;
	
	private Runnable RunableLoadUI = new Runnable(){

		@Override
		public void run() {
			Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/CRACKMAN.TTF");
	        
	        LayoutInflater li = LayoutInflater.from(ChooseProviderActivity.this);
	        RelativeLayout v = (RelativeLayout) li.inflate(R.layout.choose_provider, null);
	        Thread.yield(); //TODO: Remove?
	        RelativeLayout insideRL = (RelativeLayout) v.findViewById(id.rel_cp_inside);
	        titleTextView = (TextView) insideRL.findViewById(R.id.csp_tv_title);
	        titleTextView.setTypeface(tf);
	        
	        insideRL.removeAllViews();
	        Thread.yield(); //TODO: Remove?
	        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	        titleTextView.setId(1);
	        insideRL.addView(titleTextView);
	        int layoutCounter = 2;
	        buttons = new ArrayList<SearchProviderButton>();
	        toPaintRed = new ArrayList<SearchProviderButton>();
	        
	        //Loop through all "real" search providers
	        for(SearchProvider sp : SearchProvider.values()){
	        	Connector connector = sp.getConnector();
	        	SearchProviderButton spb = new SearchProviderButton(ChooseProviderActivity.this, connector);
	        	Thread.yield(); //TODO: Remove?
	        	buttons.add(spb);
	        	spb.setId(layoutCounter);
	        	spb.setPadding(15, 15, 15, 15);
	        	spb.setBackgroundResource(R.drawable.background_selector);
	        	spb.setOnClickListener(SPBOnClickListener);
	            try {
					if(!connector.isAuthenticated() || connector.isAuthenticated())
						registerForContextMenu(spb);
				} catch (AuthenticationNotRequiredException e) {
					
				}
	            spb.setOnLongClickListener(SPBOnLongClickListener);
	        	lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	        	lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
	        	if(layoutCounter!=0){
	        		int oneLess = layoutCounter-1;
	        		lp.addRule(RelativeLayout.BELOW, oneLess);
	        	}
	        	insideRL.addView(spb, lp);
	        	Thread.yield(); //TODO: Remove?
	        	layoutCounter++;
	        }

	        //Add the hack searchProviderButton, update layoutCounter
	        layoutCounter = addHackSPBButton(layoutCounter, insideRL);
	        
	        setContentView(v);
	        Thread.yield(); //TODO: Remove?
	        handler.sendEmptyMessage(2);
		}
		
	};
	
	private int addHackSPBButton(int layoutCounter, RelativeLayout insideRL){
		Connector connector = new RomhackingConnector();
    	SearchProviderButton spb = new SearchProviderButton(ChooseProviderActivity.this, connector);
    	buttons.add(spb);
    	spb.setId(layoutCounter);
    	spb.setPadding(15, 15, 15, 15);
    	spb.setBackgroundResource(R.drawable.background_selector);
        try {
			if(!connector.isAuthenticated() || connector.isAuthenticated())
				registerForContextMenu(spb);
		} catch (AuthenticationNotRequiredException e) {
			
		}
        spb.setOnClickListener(RomHackSearchOnClickListener);
    	LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    	lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
    	if(layoutCounter!=0){
    		int oneLess = layoutCounter-1;
    		lp.addRule(RelativeLayout.BELOW, oneLess);
    	}
    	insideRL.addView(spb, lp);
    	layoutCounter++;
    	return layoutCounter;
	}
	
	private void changesDialog(){
		HashMap<Integer, String> changes = new HashMap<Integer, String>();
		changes.put(9, "+ Experimental Features!\n\nThese are features that are currently in development, and are unstable," +
				"but are provided for those who can deal with occasional problems.\n\nYou can access extra consoles for Emuparadise" +
				"(PSX, GBA, N64) by enabling extra consoles under Experimental Features under Preferences.\n\n\n" +
				"+ unECM\n\nNow you can find a tool to convert .ecm files to .bin files which are playable in psx4droid. Find a link to the app under preferences!");
		
		changes.put(10, "+ Rom Hacks!\n\nNow you can search for Rom Hacks! " +
				"Select Romhacking from the list of ROM providers.\n\nDownload droid IPS from the Android Market to patch your ROMs and create entirely new games!");
		
		changes.put(18, "+ New Tools!\n\n" +
		"Turns out AndroZip can't handle large .7z files that typically contain PSX Roms. I added a link under Preferences -> Tools to an app called SevenZip that can handle PSX Rom .7z files");

		for(Entry<Integer, String> entry : changes.entrySet()){
			
			final String changesPrefString = "changes+" + entry.getKey();
			SharedPreferences p = getSharedPreferences("FIRSTASKS", 0);
			if(!p.getBoolean(changesPrefString, false)){
					
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Changes");
		        builder.setCancelable(false);
		        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
		      	      SharedPreferences.Editor editor = settings.edit();
		      	      editor.putBoolean(changesPrefString, true);
		      	      editor.commit();
		            }
		        });
		        builder.setMessage(entry.getValue());
		        builder.show();
			}
		}
	
	}
	
	private ProgressDialog websiteDownProgressDialog;

	
	class checkWebsiteTask extends AsyncTask<String, String, Integer> {

		@Override
		protected void onPostExecute(Integer result) {
			for(final SearchProviderButton spb : toPaintRed){
	        		spb.setBackgroundColor(Color.RED);
	        		spb.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Toast.makeText(ChooseProviderActivity.this,spb.getConnector().getHost() + " is down",Toast.LENGTH_SHORT).show();
						}
					});
			}
			handler.sendEmptyMessage(3);
		}

		@Override
		protected void onPreExecute() {
			toPaintRed = new ArrayList<SearchProviderButton>();
			websiteDownProgressDialog = new ProgressDialog(ChooseProviderActivity.this);
			websiteDownProgressDialog.setCancelable(true);
			websiteDownProgressDialog.setIndeterminate(true);
			websiteDownProgressDialog.setTitle("Checking provider status");
			websiteDownProgressDialog.setMessage("Checking if any websites are down...");
			websiteDownProgressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... params) {
			for(final SearchProviderButton spb : buttons){
				if(NetworkUtils.downForEveryone(spb.getConnector())){
	        		toPaintRed.add(spb);
	        	}
			}
			return 0;
		}
		
	};
	

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);
	  MenuInflater inflater = getMenuInflater();
	  inflater.inflate(R.menu.mnu_choose_provider, menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.choose_provider, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.mnu_prefs:
	    	Intent settingsActivity = new Intent(this, PrefencesActivity.class);
	    	startActivity(settingsActivity);
	        return true;
	    case R.id.mnu_help:
	    	displayHelp();
	    	return true;
	    case R.id.mnu_refresh:
	    	handler.sendEmptyMessage(2);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  switch (item.getItemId()) {
	  case R.id.mnu_edit_cred:
		  startActivity(new Intent(ChooseProviderActivity.this, LoginActivity.class));
		  return true;
	  default:
	    return super.onContextItemSelected(item);
	  }
	}
	
	View.OnClickListener SPBOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v instanceof SearchProviderButton){
				SearchProviderButton button = (SearchProviderButton) v;
				Connector connector = button.getConnector();
				Intent clickIntent = new Intent();
				clickIntent.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector));
				try {
					if(connector.isAuthenticated()){
						clickIntent.setClass(ChooseProviderActivity.this, ConsoleSelectionView.class);
					} else {
						clickIntent.setClass(ChooseProviderActivity.this, LoginActivity.class);
					}
					startActivity(clickIntent);
				} catch (AuthenticationNotRequiredException e) {
					clickIntent.setClass(ChooseProviderActivity.this, ConsoleSelectionView.class);
					startActivity(clickIntent);
				}
			}
		}
	};
	
	View.OnClickListener RomHackSearchOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent clickIntent = new Intent();
			clickIntent.setClass(ChooseProviderActivity.this, RomHackSearch.class);
			startActivity(clickIntent);
		}
	};
	
	View.OnLongClickListener SPBOnLongClickListener = new OnLongClickListener() {
    	@Override
		public boolean onLongClick(View v) {
    		v.showContextMenu();
    		return true;
		}
	};

private void displayHelp(){

	LayoutInflater factory = LayoutInflater.from(ChooseProviderActivity.this);
    final View textEntryView = factory.inflate(R.layout.help_layout, null);
    Builder myDialog = new AlertDialog.Builder(ChooseProviderActivity.this);
    myDialog.setView(textEntryView);
    myDialog.show();
}

private void askAboutScreenShots(){
	SharedPreferences p = getSharedPreferences("FIRSTASKS", 0);
	if(!p.getBoolean("SCREENSHOTS", false)){
			
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Screenshots?");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
      	      SharedPreferences.Editor editor = settings.edit();
      	      editor.putBoolean("SCREENSHOTS", true); //We asked
      	      editor.commit();
      	      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChooseProviderActivity.this);
      	      Editor e = prefs.edit();
      	      e.putBoolean("uss", true);
      	      e.commit();
            }
        });
        builder.setNegativeButton("Not Right Now", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
        	      SharedPreferences.Editor editor = settings.edit();
        	      editor.putBoolean("SCREENSHOTS", true); //We asked
        	      editor.commit();
	      	      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChooseProviderActivity.this);
	      	      Editor e = prefs.edit();
	      	      e.putBoolean("uss", false);
	      	      e.commit();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.setMessage("Do you want to enable the screenshot library? If enabled, almost all ROMs will have screenshots. (Slows initial loading of app. Recommended to use WiFi. Changes will take effect next time you open the app.)");
        builder.create().show();
	}
}

@Override
protected void onPause() {
	super.onPause();
	
}



@Override
protected void onRestart() {
	super.onRestart();
	if(tempLayout!=null){
		handler.sendEmptyMessage(2);
	}
}



}
