package com.romcessed.romsearch.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.ConsoleHandler;

import org.apache.http.client.ClientProtocolException;

import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.searchproviders.RombayConnector;
import com.romcessed.romsearch.tools.GlobalVars;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Demo showing the use of a custom list item and SimpleAdapter.
 * 
 */
public class SearchActivity extends ListActivity {
	/** Keys used for row Map */
	private static final String ICON_KEY = "icon";
	private static final String LABEL_KEY = "label";
	private static final String DETAIL_KEY = "detail";
	
	private String query;
	private ArrayList<SearchResult> searchResults;
	private HashMap<String, SearchResult> listItems;
	private Console console = Console.ALL;
	
	private String currentItemTitle;
	private ProgressDialog mProgressDialog = null;
	
    
    
    
    public SearchActivity() {
		this(Console.ALL);
	}

	public SearchActivity(Console c){
    	super();
    	this.console = c;
    }
    
	final Handler mHandler = new Handler();
	private Thread searchThread;
	private Connector connector;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String consoleExtra;
	    
	    
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      query = intent.getStringExtra(SearchManager.QUERY);
	      Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);
	      if (appData != null) {
		      consoleExtra = appData.getString(Console.BUNDLE_TAG);
		      console = Console.getConsoleFromExtra(consoleExtra);
		      connector = SearchProvider.getConnectorFromID(appData.getInt("CONNECTOR", -1));
	      }
	    }
		
        Thread doSearch = new Thread(){
            @Override
            public void run() {
            	try {
					AsyncTask<String, String, ArrayList<SearchResult>> asynctask = connector.search(SearchActivity.this, query, console);
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
	





}
