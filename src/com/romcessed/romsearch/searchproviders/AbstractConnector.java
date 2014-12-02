package com.romcessed.romsearch.searchproviders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.webkit.WebView;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.ServiceCommunicableActivity;
import com.romcessed.romsearch.tools.GlobalVars;

public abstract class AbstractConnector implements Connector {
	
	public abstract boolean isDirectDownload();
	
	public String TAG;
	
	protected String currentItemTitle;
	/**
	 * DefaultHttpClient used throughout the {@link AbstractConnector}
	 */
	protected final DefaultHttpClient client;

	
	/**
	 * Default constructor. Sets up {@link #client}
	 */
	public AbstractConnector() {
		client = GlobalVars.getUniversalClient();
	}
	public abstract String getHost();
	public abstract com.romcessed.romsearch.EULAcapsule getEULAcapsule(Activity activity);
	
	@Override
	public boolean usernameAvailable(String username) throws ClientProtocolException, IOException, UsernameVerificationNotAvaiableException {
		throw new UsernameVerificationNotAvaiableException();
	}

	/**
	 * Default implementation throws AuthenticationNotRequiredException
	 * 
	 * @throws AuthenticationNotRequiredException
	 */
	@Override
	public boolean authenticateUser(String username, String password) throws UnsupportedEncodingException, ClientProtocolException, IOException, AuthenticationNotRequiredException {
		throw new AuthenticationNotRequiredException();
	}
	
	public abstract boolean registerUser(String fullname, String username, String password, String email, boolean terms) throws UnsupportedEncodingException;

	@Override
	public abstract AsyncTask<String, String, ArrayList<SearchResult>> browse(Context context, Category category, Console console, int page) throws ExecutionException, InterruptedException;

	@Override
	public String getDescription(Context context, SearchResult sr) throws ExecutionException, InterruptedException {
		return null;
	}

	
	@Override
	public abstract ArrayList<Console> getAvailableConsoles(Context context);
	
	@Override
	public abstract boolean isCaptchaRequired();

	/**
	 * Default implementation throws {@link CaptchaNotRequiredException}
	 */
	@Override
	public Bitmap getCaptcha() throws ClientProtocolException, IOException, CaptchaNotRequiredException {
		throw new CaptchaNotRequiredException();
	}

	@Override
	public abstract Console getConsole(String s);
	
	@Override
	public abstract String getConsoleString(Console c);
	
	@Override
	public final String getCurrentItemTitle() {
		return currentItemTitle;
	}

	@Override
	public abstract int getLogoDrawableID();

	/**
	 * Default implementation throws AuthenticationNotRequiredException
	 */
	@Override
	public boolean isAuthenticated() throws AuthenticationNotRequiredException {
		throw new AuthenticationNotRequiredException();
	}
	
	public final boolean authenticateFromPreferencesStore(Context context) throws AuthenticationNotRequiredException, ClientProtocolException, IOException {
		SharedPreferences settings = context.getSharedPreferences(TAG+"_CREDENTIALS", 0);
		String username = settings.getString("USERNAME", "");
		String password = settings.getString("PASSWORD", "");
		return authenticateUser(username, password);
	}
	
	public abstract boolean isPasswordNeededDuringRegistration();
	
	public final void saveAuthenticationCredentials(Context context, String username, String password, boolean savePassword) {
	      SharedPreferences settings = context.getSharedPreferences(TAG+"_CREDENTIALS", 0);
	      SharedPreferences.Editor editor = settings.edit();
	      editor.putString("USERNAME", username);
	      if(savePassword){
	    	  editor.putString("PASSWORD", password);
	      } else {
	    	  editor.remove("PASSWORD");
	      }
	      editor.putBoolean("SAVEPASSWORD", savePassword);
	      editor.commit();
	}

	@Override
	public abstract AsyncTask<String, String, ArrayList<SearchResult>> search(Context context, String query, Console console) throws ExecutionException, InterruptedException;

	/**
	 * Default implementation
	 * @return false
	 */
	@Override
	public boolean isSearchSupported() {
		return false;
	}
	
	public abstract ArrayList<Category> getAvailableCategories();


	public void customizeWebView(ServiceCommunicableActivity activity, WebView webview, SearchResult mRom) {
		
	}

}
