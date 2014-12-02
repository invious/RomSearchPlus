package com.romcessed.romsearch.activities;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.ConsoleHandler;

import org.apache.http.client.ClientProtocolException;

import com.romcessed.romsearch.Category;
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
public class BrowseActivity extends ListActivity {

	public static final int RESULT_REQUEST_CODE = 1;
	public static final String SERIALIZABLE_KEY = "QUERY";
	public static final String BUNDLE_KEY = "RESULTS";
    Console console;
    Category category;
	AsyncTask<String, String, ArrayList<SearchResult>> browseTask;
	
    public BrowseActivity() {
		this(Console.ALL);
	}

	public BrowseActivity(Console c){
    	super();
    	this.console = c;
    }
    
	final Handler mHandler = new Handler();
	private Thread searchThread;
	private int pageExtra;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		 String consoleExtra = getIntent().getStringExtra(Console.BUNDLE_TAG);
		    if(consoleExtra!=null){
		    	console = Console.getConsoleFromExtra(consoleExtra);
		    }
		    
		    String categoryExtra = getIntent().getStringExtra(Category.BUNDLE_TAG);
		    if(categoryExtra!=null){
		    	this.category = Category.getCategoryFromExtra(categoryExtra);
		    }
		    
		    pageExtra = getIntent().getIntExtra("PAGE", 1);

		    final Connector connector = SearchProvider.getConnectorFromID(getIntent().getIntExtra("CONNECTOR", -1));
		    
		    Thread browseThread = new Thread(){

				@Override
				public void run() {
					try {
						browseTask = connector.browse(BrowseActivity.this, category, console, pageExtra);
						browseTask.execute("");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
					
				};
		
				runOnUiThread(browseThread);
				ArrayList<SearchResult> result = null;
				
				try {
					result = browseTask.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Bundle queryBundle = new Bundle();
				queryBundle.putSerializable(SERIALIZABLE_KEY, (Serializable) result);
				Intent i = new Intent();
				i.putExtra(BUNDLE_KEY, queryBundle); //Put in our browse results
				i.putExtra(Category.BUNDLE_TAG, category.putExtra()); //put our category in return intent
				i.putExtra(Console.BUNDLE_TAG, console.putExtra()); //put our console in return intent
	            setResult(RESULT_OK, i);
	            finish();

	}
	





}
