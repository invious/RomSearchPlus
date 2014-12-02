package com.romcessed.romsearch.tools;

import java.util.ArrayList;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.activities.SplashScreenActivity;
import com.romcessed.romsearch.searchproviders.Connector;

public class GlobalVars {

	private static ArrayList<SearchResult> recentQueryResults;
	private static Connector currentConnector;
	private static SearchResult currentRom;
	private static ClientConnectionManager connectionManager; 

	/**
	 * The {@link DefaultHttpClient} used throughout the project to perform all connections
	 */
	private static DefaultHttpClient universalClient;
	
	/**
	 * 
	 * @return true if {@link #universalClient} was initialized, false if it was already initialized.
	 */
	public static boolean initializeUniversalClient(){
		if(universalClient!=null){
			return false;
		}
		HttpParams params = new BasicHttpParams(); 
		HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.9) Gecko/20100315 Firefox/3.5.9");
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http",
        PlainSocketFactory.getSocketFactory(), 80));
        connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        connectionManager.closeExpiredConnections();
        universalClient = new DefaultHttpClient(connectionManager, params); 
		universalClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		return true;
	}
	
	public static boolean cleanClient(){
		if(connectionManager==null){
			return false;
		}
		connectionManager.closeExpiredConnections();
		return true;
	}
	
	public static DefaultHttpClient getUniversalClient(){
		while(universalClient==null){
			initializeUniversalClient();
		}
		cleanClient();
		return universalClient;
	}
	
	public static void setRecentQueryResults(ArrayList<SearchResult> recentQueryResults) {
		GlobalVars.recentQueryResults = recentQueryResults;
	}

	public static ArrayList<SearchResult> getRecentQueryResults() {
		if(recentQueryResults==null){
			return null;
		} else {
			return new ArrayList<SearchResult>(recentQueryResults);
		}
	}


	public static void setCurrentRom(SearchResult currentRom) {
		GlobalVars.currentRom = currentRom;
	}

	public static SearchResult getCurrentRom() {
		return new SearchResult(currentRom);
	}

	public static String getSaveLocation(Context context, Console console){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.getBoolean("save_loc_check", false)){
			String customSaveLoc = prefs.getString("save_loc_et", "/ROMS/" + console + "/");
			if(customSaveLoc.charAt(0)!='/'){
				customSaveLoc = "/" + customSaveLoc;
			}
			if(customSaveLoc.charAt(customSaveLoc.length()-1)!='/'){
				customSaveLoc = customSaveLoc + "/";
			}
			return customSaveLoc;
		} else {
			return "/ROMS/" + console.name() + "/";
		}
	}
	
}
