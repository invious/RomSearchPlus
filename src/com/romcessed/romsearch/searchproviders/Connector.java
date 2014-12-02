package com.romcessed.romsearch.searchproviders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.SearchResult;

public interface Connector {
	
	public static String TAG = null;
	
	public boolean isDirectDownload();
	
	public int getLogoDrawableID();
	public abstract com.romcessed.romsearch.EULAcapsule getEULAcapsule(Activity activity);
	
	public boolean isPasswordNeededDuringRegistration();
	
	public void saveAuthenticationCredentials(Context context, String username, String password, boolean savePassword);
	public boolean isAuthenticated() throws AuthenticationNotRequiredException;
	
	//Probably should just move to AbstractConnector
	public boolean registerUser(String fullname, String username, String password, String email, boolean terms) throws UnsupportedEncodingException;
	public boolean usernameAvailable(String username) throws ClientProtocolException, IOException, UsernameVerificationNotAvaiableException;
	//
		
	public boolean authenticateFromPreferencesStore(Context context) throws AuthenticationNotRequiredException, ClientProtocolException, IOException;
	
	public abstract boolean authenticateUser(String username, String password) throws UnsupportedEncodingException, ClientProtocolException, IOException, AuthenticationNotRequiredException;
	
	public AsyncTask<String, String, ArrayList<SearchResult>> search(Context context, String query, Console console) throws ExecutionException, InterruptedException;
	
	public AsyncTask<String, String, ArrayList<SearchResult>> browse(Context context, Category category, Console console, int page) throws ExecutionException, InterruptedException;
	
	
	public boolean isCaptchaRequired();
	
	/**
	 * Gets the string that the website uses for a specific console in URIs
	 * @param c {@link Console}
	 * @return website-specific string representation
	 */
	public String getConsoleString(Console c);
	
	/**
	 * Gets the {@link Console} from the website's string representation
	 * @param s The website-specific string representation
	 * @return matching {@link Console}
	 */
	public Console getConsole(String s);
	
	public String getCurrentItemTitle();
	
	public ArrayList<Console> getAvailableConsoles(Context context);
	
	public ArrayList<Category> getAvailableCategories();
	
	public Bitmap getCaptcha() throws ClientProtocolException, IOException, CaptchaNotRequiredException;
	
	public class AuthenticationNotRequiredException extends Exception{
		
	}

	public class UsernameVerificationNotAvaiableException extends Exception{

		@Override
		public String getMessage() {
			return "This search provider does not support username availability checking.";
		}
		
	}
	
	public class CaptchaNotRequiredException extends Exception{
		@Override
		public String getMessage() {
			return "This search provider does not require captchas";
		}
	}

	public boolean isSearchSupported();
	public String getHost();

	/** Get description for particular search result. Used after rom is selected, not in bulk, as description is lifted from deep-linked page**/
	String getDescription(Context context, SearchResult sr) throws ExecutionException, InterruptedException;
	
}
