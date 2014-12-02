package com.romcessed.romsearch;

import java.util.ArrayList;

import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.searchproviders.Connector.AuthenticationNotRequiredException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SearchProviderButton extends RelativeLayout {

	private Connector connector;
	private Context context;
	private View inflatedLayout;
	private ArrayList<ImageView> consoleIcons;
	private ImageView logo;
	private TextView searchTV, browseTV;
	private TextView andTV;
	private ImageView lockIV;
	private ImageView globeIV;

	
	
	public SearchProviderButton(Context context, Connector connector) {
		super(context);
		this.connector=connector;
		
		load(context);
		
		LayoutInflater li = LayoutInflater.from(context);
		View v = li.inflate(R.layout.provider_view, this, true);
			
	}



	public SearchProviderButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.SearchProviderButton);
		connector = SearchProvider.valueOf(a.getString(R.styleable.SearchProviderButton_providerName)).getConnector();
		
		load(context);
	
	}

	private void load(Context context){
		consoleIcons = new ArrayList<ImageView>();
		
		this.context = context;
		setFocusable(true);
		setBackgroundColor(Color.WHITE);
		setVisibility(VISIBLE);
		
		//setOnClickListener(listenerAdapter);
		setClickable(true);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		
		 //the <include> in the layout file has added these views to this 
		  //so search this for the views
		  ImageView logo = (ImageView) this.findViewById(R.id.pv_logo);
		  logo.setImageResource(connector.getLogoDrawableID());

			
			browseTV = (TextView)this.findViewById(R.id.pv_tv_browse);
			searchTV = (TextView)this.findViewById(R.id.pv_tv_search);
			andTV = (TextView)this.findViewById(R.id.pv_tv_and);
			lockIV = (ImageView)findViewById(R.id.pv_lock);
			globeIV = (ImageView)findViewById(R.id.pv_globe);
			try {
				if(connector.isAuthenticated()){}
			} catch (AuthenticationNotRequiredException e) {
				lockIV.setVisibility(View.INVISIBLE);
			}
			if(!connector.isDirectDownload()){
				globeIV.setImageResource(R.drawable.globe);
			} else {
				globeIV.setImageResource(R.drawable.direct_dl);
			}
			if(!connector.isSearchSupported()){
				andTV.setText("");
				searchTV.setVisibility(GONE);
			}
			
			setConsoleIcons();
			
	}

	public Connector getConnector(){
		return connector;
	}
	
	public void setConnector(Connector connector){
		this.connector = connector;
	}
	
	
	
	private void setConsoleIcons(){
		int[] consoleIconDrawables = {R.id.pv_console1,R.id.pv_console2, R.id.pv_console3,
				R.id.pv_console4, R.id.pv_console5, R.id.pv_console6, R.id.pv_console7, 
				R.id.pv_console8, R.id.pv_console9};  
	
		ArrayList<Console> availConsole = connector.getAvailableConsoles(context);
		availConsole.remove(Console.ALL);
		
		int counter = 0;
		for(int consoleIVid : consoleIconDrawables){
			ImageView curConsoleImageView = (ImageView)this.findViewById(consoleIVid);
			if(counter < availConsole.size()){
				curConsoleImageView.setImageResource(availConsole.get(counter).getDrawable());
			} else {
				curConsoleImageView.setVisibility(GONE);
			}
			consoleIcons.add(curConsoleImageView);
			counter++; 
		}
	}
	
	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus == true){
			this.setBackgroundColor(Color.rgb(255, 165, 0));
		} else {
			this.setBackgroundColor(Color.WHITE);
		}
	}
	
	
}
