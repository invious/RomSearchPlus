package com.romcessed.romsearch.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.ServiceCommunicableActivity;
import com.romcessed.romsearch.searchproviders.AbstractConnector;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.tools.GlobalVars;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class webViewActivity extends ServiceCommunicableActivity{

	private WebView webview;
	private SearchResult mRom;

	private boolean downloadCrutchInstalled(){
		List<ApplicationInfo> installedPackages = getPackageManager().getInstalledApplications(0);
		for(ApplicationInfo app : installedPackages){
			if(app.packageName.equals("org.ouroborus.android.download")){ //unfinished packagename
				return true;
			}
		}
		return false;
	}
	

      	     
	
	public void informAboutDownloading(){
		SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
		if(!settings.getBoolean("HOWTOLONGCLICKDOWNLOAD", false)){
			int timesShown = 0;
			final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
	        builder2.setTitle("When click doesn't work!");
	        builder2.setCancelable(false);
	        builder2.setNegativeButton("Remind me next time", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {

	            }
	        });
	        builder2.setPositiveButton("I've done it before", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
                	SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
        	    	SharedPreferences.Editor editor = settings.edit();
        	    	editor.putBoolean("HOWTOLONGCLICKDOWNLOAD", true);
        	    	editor.commit();
	        	    }
	        });
	        builder2.setMessage("YOU *NEED* TO\n\n__HOLD DOWN__\n\nON A LINK AND SELECT\n\nSAVE LINK\n\nIF CLICKING ON THE DOWNLOAD LINK DOESNT WORK!!!!!!");
	        builder2.show();
		}
		if(!settings.getBoolean("HOWTODOWNLOADZIPS", false)){
			final AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	        builder1.setTitle("About Downloading Files");
	        builder1.setCancelable(false);
	        builder1.setNegativeButton("Show me next time", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {

	            }
	        });
	        builder1.setPositiveButton("I get it", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	                SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
	        	      SharedPreferences.Editor editor = settings.edit();
	        	      editor.putBoolean("HOWTODOWNLOADZIPS", true);
	        	      editor.commit();
	            }
	        });
	        builder1.setMessage("ROMs either come in zip files, or other compression formats (.RAR, .7z, etc.)\n\nSometimes the archive is extracted for you. Other times you will have to extract it. For .7z files use SevenZip, (AndroZip doesn't work correctly), and for other files use AndroZip.\n\nThere are links to both under preferences.");
	        builder1.show();
		}
	}
	//To download a file, locate the download link on the page, click and hold the link until the context menu appears, and then select \"Save Link\"\n\n
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_view_layout);
		
		informAboutDownloading();
		
		mRom = (SearchResult) getIntent().getSerializableExtra("com.android.contacts.webViewActivity");
		
		AbstractConnector connector = (AbstractConnector) SearchProvider.getConnectorFromID(mRom.getConnectorID());
		
		webview = (WebView)findViewById(R.id.wv_webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        //webSettings.set
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
	    connector.customizeWebView(this, webview, mRom);
		webview.loadDataWithBaseURL(mRom.getWebViewBaseURL(), createDownloadButton(mRom.getURLSuffix()), null,"utf-8", "www.google.com");

	}

	private String createDownloadButton(String downloadLinkURL){
		return "<center><a href=\"" + downloadLinkURL + "\"><img src=\"file:///android_asset/download.png\"></a></center>";
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	private static boolean isCallable(Context context, Intent intent) {  
	         List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,   
	             PackageManager.MATCH_DEFAULT_ONLY);  
	         return list.size() > 0;  
	 } 

	private boolean AndFTPInstalled(){
        boolean installed = false;
		List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);  
		for(int i=0;i<packs.size();i++) {  
			PackageInfo p = packs.get(i);  
			if(p.packageName.contains("lysesoft.andftp")){
				return true;
			}
		}
		return false;
	}
}
