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
import com.romcessed.romsearch.searchproviders.EmuParadiseBIOSConnector;
import com.romcessed.romsearch.tools.GlobalVars;
import com.romcessed.romsearch.tools.ScreenShotTools;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class CategorySelectionView extends ListActivity {
	private static final String ICON_KEY = "icon";
	private static final String LABEL_KEY = "label";
		
	private Console console;
	private CategoryListAdapter mAdapter;
	private Connector connector;
	private Bundle savedBundle;
	
    public CategorySelectionView() {
		super();
	}

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedBundle = savedInstanceState;
        setContentView(R.layout.main);
        
        GlobalVars.setRecentQueryResults(null);
        
        String consoleExtra = getIntent().getStringExtra(Console.BUNDLE_TAG);
	    if(consoleExtra!=null){
	    	console = Console.getConsoleFromExtra(consoleExtra);
	    }
	    
	    connector = SearchProvider.getConnectorFromID(getIntent().getIntExtra("CONNECTOR", -1));
	    
        this.mAdapter = new CategoryListAdapter(this, R.layout.category_list_item);
        setListAdapter(mAdapter);
        
	    if(console==Console.PSX){
	    	advertiseSevenZip();
	    	advertiseECM();
	    }

	}
	
	private void advertiseECM(){

		SharedPreferences p = getSharedPreferences("FIRSTASKS", 0);
		if(!p.getBoolean("UNECM", false)){
				
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Use un-ECM");
	        builder.setCancelable(false);
	        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
	      	      SharedPreferences.Editor editor = settings.edit();
	      	      editor.putBoolean("UNECM", true);
	      	      editor.commit();
	            }
	        });
	        builder.setNeutralButton("Go to Market", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
		            SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
		            SharedPreferences.Editor editor = settings.edit();
	      	      	editor.putBoolean("UNECM", true); //We asked
	      	      	editor.commit();
	            	
	            	Intent i = new Intent();
	    			i.setAction(android.content.Intent.ACTION_VIEW);
	    			i.setData(Uri.parse("market://details?id=" + "com.romcessed.unecm"));
	    			startActivity(i);
	            }
	        });
	        builder.setMessage("If you come accross .ecm file formats:\n\nPSX roms may come zipped multiple times and at the core may contain a file that ends with .ecm\n\nThis filetype is not currently compatible with psx4droid.\n\nun-ECM for Playstation is a tool that can be downloaded to convert these files to .bin, a format which psx4droid understands.\n\nYou can find a link to the app on the market in the preferences page");
	        builder.create().show();
		}
	}
		
		private void advertiseSevenZip(){

			SharedPreferences p = getSharedPreferences("FIRSTASKS", 0);
			if(!p.getBoolean("SEVENZIP", false)){
					
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Use Sevenzip");
		        builder.setCancelable(false);
		        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
		      	      SharedPreferences.Editor editor = settings.edit();
		      	      editor.putBoolean("SEVENZIP", true);
		      	      editor.commit();
		            }
		        });
		        builder.setNeutralButton("Go to Market", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
			            SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
			            SharedPreferences.Editor editor = settings.edit();
		      	      	editor.putBoolean("SEVENZIP", true); //We asked
		      	      	editor.commit();
		            	
		            	Intent i = new Intent();
		    			i.setAction(android.content.Intent.ACTION_VIEW);
		    			i.setData(Uri.parse("market://details?id=" + "com.hagia.sevenzip"));
		    			startActivity(i);
		            }
		        });
		        builder.setMessage("AndroZip apparently doesn't unzip .7z files correctly!!\n\nThankfully there is now a dedicated 7z app on the android market. PSX Roms commonly are in .ecm format, which are compressed in .7z archives. Use Sevenzip to extract your PSX Roms. You can click below to go there now, or find it later in the preferences page under tools.");
		        builder.create().show();
			}
	
	}
	
    @Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(CategorySelectionView.this, RomListView.class);
		final ArrayList<Category> categories = connector.getAvailableCategories();
		Category clickedCategory = categories.get(position);
		i.putExtra(Category.BUNDLE_TAG, clickedCategory.putExtra());
		i.putExtra(Console.BUNDLE_TAG, console.putExtra());
        i.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector));
		CategorySelectionView.this.startActivity(i);
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
            		Category category = Category.getCategoryFromExtra(data.getStringExtra(Category.BUNDLE_TAG));
                	Intent i = new Intent(CategorySelectionView.this, RomListView.class);
            		i.putExtra(Category.BUNDLE_TAG, category.putExtra());
            		i.putExtra(Console.BUNDLE_TAG, console.putExtra());
            		startActivity(i);
                }
            default:
                break;
        }
    }

    
	private class CategoryListAdapter extends ArrayAdapter<Category> {

        private ArrayList<Category> items;
        private Context context;
        
        public CategoryListAdapter(Context context, int textViewResourceId) {
                super(context, textViewResourceId, connector==null ? new EmuParadiseBIOSConnector().getAvailableCategories() : connector.getAvailableCategories());
                if(connector==null){
                	finish();
                }
                this.context = context;
                this.items = connector.getAvailableCategories();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.category_list_item, null);
                }
                Category category = items.get(position);
                if (category != null) {
                	TextView text1 = (TextView) v.findViewById(R.id.catlst_toptext);
                		if(text1 != null){
                        	text1.setText(category.toString());
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
	     appData.putInt("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector));
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


