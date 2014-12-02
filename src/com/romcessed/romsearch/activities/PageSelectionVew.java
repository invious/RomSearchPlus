package com.romcessed.romsearch.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.R.id;
import com.romcessed.romsearch.R.layout;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.tools.GlobalVars;
import com.romcessed.romsearch.tools.ScreenShotTools;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PageSelectionVew extends ListActivity {
	private static final String ICON_KEY = "icon";
	private static final String LABEL_KEY = "label";
		
	
	private static final String PAGE_BUNDLE_TAG = "PageSelectionViewPAGE";
	
	
	private Console console;
	private PageListAdapter mAdapter;
	final ArrayList<String> pages = new ArrayList<String>();
    public PageSelectionVew() {
		super();
	}

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        String consoleExtra = getIntent().getStringExtra(Console.BUNDLE_TAG);
	    if(consoleExtra!=null){
	    	console = Console.getConsoleFromExtra(consoleExtra);
	    }
        
        this.mAdapter = new PageListAdapter(this, R.layout.category_list_item);
        setListAdapter(mAdapter);
        
	}
	
	private void populatePageArray(){
		for (int counter=1; counter < 30; counter++){
			pages.add("Page " + Integer.valueOf(counter));
		}
	}
	
	private Integer getPage(String fullPageRowLabel){
		return Integer.valueOf(fullPageRowLabel.replaceAll("\\D", ""));
	}
	
    @Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(PageSelectionVew.this, RomListView.class);
		
		Integer page = getPage(pages.get(position));
		
		i.putExtra(PAGE_BUNDLE_TAG, page);
		i.putExtra(Console.BUNDLE_TAG, console.putExtra());
		PageSelectionVew.this.startActivity(i);
	}
    
 // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
        switch (requestCode) {
            case BrowseActivity.RESULT_REQUEST_CODE:
                // This is the standard resultCode that is sent back if the
                // activity crashed or didn't doesn't supply an explicit result.
                if (resultCode == RESULT_CANCELED){

                } 
                else {
                    ArrayList<SearchResult> recentQueryResults = (ArrayList<SearchResult>)data.getBundleExtra(
                    		BrowseActivity.BUNDLE_KEY).getSerializable(BrowseActivity.SERIALIZABLE_KEY);
                	GlobalVars.setRecentQueryResults(recentQueryResults);
            		Integer page = data.getIntExtra(PAGE_BUNDLE_TAG, 1);
                	Intent i = new Intent(PageSelectionVew.this, RomListView.class);
            		i.putExtra(PAGE_BUNDLE_TAG, page);
            		i.putExtra(Console.BUNDLE_TAG, console.putExtra());
            		startActivity(i);
                }
            default:
                break;
        }
    }

    
	private class PageListAdapter extends ArrayAdapter<Category> {

        private ArrayList<String> items;
        private Context context;
        
        public PageListAdapter(Context context, int textViewResourceId) {
                super(context, textViewResourceId);
                this.context = context;
                this.items = pages;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.category_list_item, null);
                }
                String itemLabel = items.get(position);
                if (itemLabel != null) {
                	TextView text1 = (TextView) v.findViewById(R.id.catlst_toptext);
                		if(text1 != null){
                        	text1.setText(itemLabel);
                		}
                }
                return v;
        }
}
	
	@Override
	public boolean onSearchRequested() {
	     Bundle appData = new Bundle();
	     Toast.makeText(this, "Searching: " + console.getProperName(), Toast.LENGTH_SHORT);
	     appData.putString(Console.BUNDLE_TAG, console.putExtra());
	     //appData.putInt("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector)); THis doesn't work... but I don't use this Activity yet.
	     startSearch(null, false, appData, false);
	     return true;
	 }
	
	public boolean onCreateOptionsMenu(Menu menu) {  
		    super.onCreateOptionsMenu(menu);  
		    getMenuInflater().inflate(R.menu.search_menu, menu);  
		    return true;  
		} 

	
	@Override  
	public boolean onPrepareOptionsMenu(Menu menu) {  
	    menu.findItem(R.id.mnu_search_item).setTitle("Search " + console.name());
	    return true;  
	}
	
	@Override  
	public boolean onOptionsItemSelected(MenuItem item) {  
	    switch (item.getItemId()) {  
	    case R.id.mnu_search_item:
	    	onSearchRequested();
	        return true;  
	    }   
	    return false; //should never happen  
	} 
}


