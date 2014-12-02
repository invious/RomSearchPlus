package com.romcessed.romsearch.activities;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.searchproviders.RomhackingConnector;

public class RomHackSearch extends Activity {

	TextView title;
	Spinner spin_Category, spin_Platform;
	EditText edit_Search;
	Button btn_Search;
	Connector connector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.rom_hack_search);
		
		title = (TextView)findViewById(R.id.rhs_tv_title);
		spin_Category = (Spinner)findViewById(R.id.rhs_category);
		spin_Platform = (Spinner)findViewById(R.id.rhs_platform);
		edit_Search = (EditText)findViewById(R.id.rhs_et);
		btn_Search = (Button)findViewById(R.id.rhs_btn);
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/CRACKMAN.TTF");
		title.setTypeface(tf);
		
		String[] hackCategories = new String[5];
		hackCategories[0] = "Any";
		hackCategories[1] = "Complete";
		hackCategories[2] = "Improvement";
		hackCategories[3] = "Spoof";
		hackCategories[4] = "Addendum";
		
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, hackCategories);
        spin_Category.setAdapter(adapter);
        
        ArrayList<Console> consoles = new ArrayList<Console>();
        for (Console c: Console.values()){
        	if(c!=Console.UNKNOWN)
        		consoles.add(c);
        }
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, consoles);
        spin_Platform.setAdapter(adapter);
        
        connector = new RomhackingConnector();
        
        edit_Search.setText("");
        edit_Search.setHint("Rom Hack Title");

        
        btn_Search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(edit_Search.getText().toString().length()>0){
					doSearch();
				}
			}
		});
        
        aboutRomHacks();

	}

	private void doSearch(){
		final String query = generateQuery();
        Thread doSearch = new Thread(){
            @Override
            public void run() {
            	try {
					AsyncTask<String, String, ArrayList<SearchResult>> asynctask = connector.search(RomHackSearch.this, query, Console.valueOf(spin_Platform.getSelectedItem().toString()));
					asynctask.execute(query);
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        };
		
        runOnUiThread(doSearch);
	}
	
	private String generateQuery(){
		//genre=&platform=&game=&category=&perpage=50&page=hacks&hacksearch=Go&title=mario&author=
		StringBuffer query = new StringBuffer("genre=");
		query.append("&platform=" + getConsoleID(Console.valueOf(spin_Platform.getSelectedItem().toString())));
		query.append("&game=");
		query.append("&category=" + getCategoryID());
		query.append("&perpage=50");
		query.append("&page=hacks");
		query.append("&hacksearch=Go");
		query.append("&title=" + URLEncoder.encode(edit_Search.getText().toString()) + "&author=");
		return query.toString();
	}
	
	public String getCategoryID(){
		int pos = spin_Category.getSelectedItemPosition();
		switch (pos) {
		case 0:
			return "";
		case 2:
			return "5";
		case 4:
			return "6";
		default:
			return String.valueOf(pos);
		}
	}
	
	public String getConsoleID(Console console) {
		switch(console){
		case GENESIS:
			return "11";
		case PSX:
			return "17";
		case SNES:
			return "9";
		case GAMEBOYADVANCE:
			return "10";
		case GAMEBOY:
			return "8";
		case N64:
			return "27";
		case NES:
			return "1";
		default:
			return "";
		}
	}
	
	private void aboutRomHacks(){
		SharedPreferences p = getSharedPreferences("FIRSTASKS", 0);
		if(!p.getBoolean("WHATAREPATCHES", false)){
				
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Patch Info");
	        builder.setCancelable(true);
	        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
	            SharedPreferences.Editor editor = settings.edit();
      	      	editor.putBoolean("WHATAREPATCHES", true); //We asked
      	      	editor.commit();
      	      	
	            }
	        });
	        
	        builder.setNegativeButton("Show Me Next Time", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	      	      	
	            }
	        });
	        builder.setNeutralButton("Go to Market", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
		            SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
		            SharedPreferences.Editor editor = settings.edit();
	      	      	editor.putBoolean("WHATAREPATCHES", true); //We asked
	      	      	editor.commit();
	            	
	            	Intent i = new Intent();
	    			i.setAction(android.content.Intent.ACTION_VIEW);
	    			i.setData(Uri.parse("market://details?id=" + "com.romcessed.ips"));
	    			startActivity(i);
	            }
	        });
	        builder.setMessage("Rom hacks come in Patch form. A patch is basically a list of changes that can be combined with the original game to form a completely new game. To be able to play them, you need a Rom Patcher.\n\nYou can find \"droid IPS\" on the Android Market. There is also a link under Preferences -> Tools\n\ndroid IPS will allow you to combine all .IPS patch files with their original ROMs to create entirely new games.\n\nUse this search to find patches. After you download them, you will find the files in their own folder."); 
	        builder.create().show();
		}
	}
}
