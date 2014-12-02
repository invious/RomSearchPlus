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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class RomListView extends ListActivity implements OnClickListener {
	private static final String TAG = "RomListView";

	private ArrayList<SearchResult> romlist;
	private RomAdapter mAdapter;
	private static Console console;
	private Category category;
	private int currentPageNumber = 1; //If this was called from category screen, it was called with page 1 as a parameter;
	private Bundle savedBundle;
	
	ProgressDialog mProgressDialog;
	private ViewSwitcher switcher;

	private View progressView;

	protected Connector connector;
	
    public RomListView() {
		super();
	}
    
    

    @Override
	public void finish() {
		super.finish();
		GlobalVars.setRecentQueryResults(null);
	}



	@Override /* Load More Button Was Clicked */
	public void onClick(View arg0) {
		Toast.makeText(RomListView.this, "LoadButtonClicked", Toast.LENGTH_SHORT);
		
		Thread updateUiOnClick = new Thread(){

			@Override
			public void run() {
				switcher.showNext();
				ArrayList<SearchResult> additionalRoms = new ArrayList<SearchResult>();
				currentPageNumber++;
				Intent i = new Intent(RomListView.this, BrowseActivity.class);
				i.putExtra(Category.BUNDLE_TAG, category.putExtra());
				i.putExtra(Console.BUNDLE_TAG, console.putExtra());
				i.putExtra("PAGE", currentPageNumber);
		        i.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector));
				RomListView.this.startActivityForResult(i, BrowseActivity.RESULT_REQUEST_CODE);
			}
			
		};
		
		runOnUiThread(updateUiOnClick);
    }
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedBundle = savedInstanceState;
        setContentView(R.layout.main);
        
        connector = SearchProvider.getConnectorFromID(getIntent().getIntExtra("CONNECTOR", -1));
        
        console = Console.ALL;
        
        String consoleExtra = getIntent().getStringExtra(Console.BUNDLE_TAG);
	    if(consoleExtra!=null){
	    	console = Console.getConsoleFromExtra(consoleExtra);
	    }
	    
	    if(GlobalVars.getRecentQueryResults()==null){
	    	romlist = new ArrayList<SearchResult>();
	    } else {
	    	romlist = GlobalVars.getRecentQueryResults();
	    }
	    
	    String categoryExtra = getIntent().getStringExtra(Category.BUNDLE_TAG);
	    if(categoryExtra!=null){
	    	category = Category.getCategoryFromExtra(categoryExtra);
			switcher = new ViewSwitcher(this);
	    	Button footer = (Button)View.inflate(this, R.layout.load_more_items_button, null);
	  	  	progressView = View.inflate(this, R.layout.load_more_items_btn_indeter, null);
	  	  	switcher.addView(footer);
		  	switcher.addView(progressView);
		  	
		  	getListView().addFooterView(switcher);
		  	switcher.showNext();
	    	
	    	Intent i = new Intent(RomListView.this, BrowseActivity.class);
			i.putExtra(Category.BUNDLE_TAG, categoryExtra);
			i.putExtra(Console.BUNDLE_TAG, consoleExtra);
			i.putExtra("PAGE", 1);
	        i.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector));
			RomListView.this.startActivityForResult(i, BrowseActivity.RESULT_REQUEST_CODE);
	    }
	    
        //romlist = GlobalVars.getRecentQueryResults();
	    
	    
	    
        this.mAdapter = new RomAdapter(this, R.layout.imgtestlayout, romlist);
        setListAdapter(mAdapter);
        
    }

    
    
    private class AddMoreResultsThread extends Thread {

    	ArrayList<SearchResult> resultsToAdd;
    	
    	public AddMoreResultsThread(ArrayList<SearchResult> resultsToAdd){
    		this.resultsToAdd = resultsToAdd;
    	}
    	
        @Override
        public void run() {
        	ArrayList<SearchResult> concurrentArrayHelper = new ArrayList<SearchResult>(resultsToAdd);
        	for(SearchResult sr : concurrentArrayHelper){
                mAdapter.notifyDataSetChanged();
                mAdapter.add(sr);
        	}
            mAdapter.notifyDataSetChanged();
        }
    };
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//GlobalVars.setCurrentRom(romlist.get(position));
		Intent i = new Intent(RomListView.this, RomDetailActivity.class);
		i.putExtra(RomDetailActivity.BUNDLE_TAG, (Serializable) romlist.get(position));
		RomListView.this.startActivity(i);
	}
    
    
	private class RomAdapter extends ArrayAdapter<SearchResult> implements Filterable {

        private ArrayList<SearchResult> items;
        private Context context;
        
        public RomAdapter(Context context, int textViewResourceId, ArrayList<SearchResult> items) {
                super(context, textViewResourceId, items);
                this.context = context;
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.icon_detail_list_item, null);
                }
                SearchResult sr = items.get(position);              
                if (sr != null) {
                	
                    if(sr.getConsole()==null){
                    	sr = new SearchResult("Website having issues", console.UNKNOWN, sr.getConnectorID());
                    }
                	
                        ImageView icon = (ImageView) v.findViewById(R.id.icon);
                        TextView text1 = (TextView) v.findViewById(R.id.text1);
                        TextView text2 = (TextView) v.findViewById(R.id.text2);
                        if (icon != null && sr.getConsole()!=null) {
                            int resourceId = sr.getConsole().getDrawable();  
                        	icon.setImageResource(resourceId);                           }
                        if(text1 != null){
                              text1.setText(sr.getTitle());
                        }
                        if(text2 != null){
                            text2.setText(sr.getConsole().getProperName() + "\n"+ sr.getSubInfo1_Title() + ": " + sr.getSubInfo1());
                      }
                }
                return v;
        }
}
	//Search from search
	@Override
	public boolean onSearchRequested() {
	    GlobalVars.setRecentQueryResults(null); 
		Bundle appData = new Bundle();
	     appData.putString(Console.BUNDLE_TAG, console.putExtra());
	     appData.putInt("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector));
	     startSearch(null, false, appData, false);
	     return true;
	 }
	
	
	 // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
        switch (requestCode) {
            case BrowseActivity.RESULT_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED){
                	Log.v(TAG, "Uhh onActivity Result cancelled!");
                } 
                else {
                    ArrayList<SearchResult> additionalRoms = (ArrayList<SearchResult>)data.getBundleExtra(
                    		BrowseActivity.BUNDLE_KEY).getSerializable(BrowseActivity.SERIALIZABLE_KEY);
                	if(additionalRoms.size()==1){ //No more pages
                		getListView().removeFooterView(switcher);
                	} else {
                		romlist.addAll(additionalRoms);
                		mAdapter.notifyDataSetChanged();
                		GlobalVars.setRecentQueryResults(romlist);
                		if(switcher.getCurrentView()==progressView){
                			switcher.showPrevious();
                		}
                	}
                }
            default:
                break;
        }
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


