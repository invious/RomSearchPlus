package com.romcessed.romsearch.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.os.Bundle;
import android.util.Log;
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

public class ConsoleSelectionView extends ListActivity {
	private static final String TAG = "ConsoleSelectionView";
	
	private static final String ICON_KEY = "icon";
	private static final String LABEL_KEY = "label";
		
	private Connector connector;
	private ConsoleListAdapter mAdapter;
	private ArrayList<Console> availConsoles;
	private Bundle savedBundle;
    public ConsoleSelectionView() {
		super();
		// TODO Auto-generated constructor stub
	}


	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        savedBundle = savedInstanceState;
        
        connector = SearchProvider.getConnectorFromID(getIntent().getIntExtra("CONNECTOR", -1));
        availConsoles = connector.getAvailableConsoles(this);
        
        this.mAdapter = new ConsoleListAdapter(this, R.layout.console_list_item, availConsoles);
        setListAdapter(mAdapter);
        
	}
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(ConsoleSelectionView.this, CategorySelectionView.class);
		Console clickedConsole = availConsoles.get(position);
		if(clickedConsole==Console.ALL){
			onSearchRequested();
			return;
		}
		i.putExtra(Console.BUNDLE_TAG, clickedConsole.putExtra());
		i.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector));
		ConsoleSelectionView.this.startActivity(i);
	}
    
    
	private class ConsoleListAdapter extends ArrayAdapter<Console> {

        private ArrayList<Console> items;
        private Context context;
        
        public ConsoleListAdapter(Context context, int textViewResourceId, ArrayList<Console> aConsoles) {
                super(context, textViewResourceId, aConsoles);
                this.context = context;
                this.items = aConsoles;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.console_list_item, null);
                }
                Console console = items.get(position);
                if (console != null) {
                        ImageView icon = (ImageView) v.findViewById(R.id.consolelst_icon);
                        TextView text1 = (TextView) v.findViewById(R.id.consolelst_toptext);
                        if (icon != null && console!=null) {
                            int resourceId = console.getDrawable();  
                        	icon.setImageResource(resourceId);                           }
                        if(text1 != null){
                              text1.setText(console.getProperName());
                        }
                }
                return v;
        }
}
	
	//Haven't chosen a console yet, Search ALL
	@Override
	public boolean onSearchRequested() {
	     Bundle appData = new Bundle();
	     Toast.makeText(this, "Searching: " + Console.ALL.getProperName(), Toast.LENGTH_SHORT);
	     appData.putString(Console.BUNDLE_TAG, Console.ALL.putExtra());
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
    menu.findItem(R.id.mnu_search_item).setTitle("Search " + Console.ALL);
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


