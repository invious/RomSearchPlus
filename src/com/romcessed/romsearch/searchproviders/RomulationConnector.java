package com.romcessed.romsearch.searchproviders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.EULAcapsule;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.tools.GlobalVars;

public class RomulationConnector extends AbstractConnector {

	

	public RomulationConnector() {
		super();
		TAG="RomulationConnector";
	}


	@Override
	public boolean authenticateUser(String username, String password) throws UnsupportedEncodingException, ClientProtocolException, IOException, AuthenticationNotRequiredException {
		
		HttpPost loginPOST = new HttpPost("http://www.romulation.net/user/login/");
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("username", username));
		formparams.add(new BasicNameValuePair("password", password));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, HTTP.DEFAULT_CONTENT_CHARSET);
		loginPOST.setEntity(entity);

		HttpResponse res = client.execute(loginPOST);
		HttpEntity ent = res.getEntity();
		ent.consumeContent();

        return isAuthenticated();
	}

	@Override
	public boolean isAuthenticated() throws AuthenticationNotRequiredException {
		List<Cookie> cookies = client.getCookieStore().getCookies();
		for(Cookie c: cookies){
			Log.v(TAG, c.getName() + " " + c.getValue());
			if(c.getName().equals("PHPSESSID")){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSearchSupported() {
		return true;
	}

	//POST http://www.romulation.net/PSX/29/
	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> browse(Context context, Category category, Console console, int page) throws ExecutionException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ArrayList<Console> getAvailableConsoles(Context context) {
		ArrayList<Console> al = new ArrayList<Console>();
		al.add(Console.ALL);
		al.add(Console.GAMEBOYADVANCE);
		al.add(Console.PSX);
		return al;
	}

	
	public Console getConsole(String s){
		Console console = null;
		if(s.equals("GBA")){
			console = Console.GAMEBOYADVANCE;
		} else if(s.equals("PSX")){
			console = Console.PSX;
		} else {
			console = Console.UNKNOWN;
		}

		return console;
	}

	public String getConsoleString(Console c){
		String consoleSearchString = null;
		switch(c){
		case ALL:
			consoleSearchString = "all";
			break;
		case PSX:
			consoleSearchString = "PSX";
			break;
		case GAMEBOYADVANCE:
			consoleSearchString="GBA";
			break;
		}
		return consoleSearchString;
	}
	

	@Override
	public int getLogoDrawableID() {
		return R.drawable.romulation_logo;
	}

	@Override
	public boolean registerUser(String fullname, String username, String password, String email, boolean terms) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> search(Context context, String query, Console console) throws ExecutionException, InterruptedException {
		return new SearchForRomsTask(context, query, console);
	}

	@Override
	public boolean usernameAvailable(String username)
			throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	private class SearchForRomsTask extends AsyncTask<String, String, ArrayList<SearchResult>> {

		private final Context context;
		private ProgressDialog progressDialog;
		private Console console;
		private String query;
		
		final Pattern searchPattern = Pattern.compile("<td.+?><.+?\"(.+?)\">\\[(.+?)\\]\\s*(.+?)</a></td>\\s+<td.+?</td>\\s+<td>(.+?)<");
		
		public SearchForRomsTask(Context context, String query, Console console){
			super();
			this.context = context;
			this.console = console;
			this.query = query;
		}
		
	     @Override
		protected void onPreExecute() {
	        progressDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
	        if(console==Console.ALL){
	        	progressDialog.setIcon(R.drawable.icon);
	        } else {
	        	progressDialog.setIcon(console.getDrawable());
	        }
	        progressDialog.setCancelable(false);
	        progressDialog.setTitle(/*"Searching for \"" + */query/* + "\""*/);
	        progressDialog.setMessage("Searching...");
	        progressDialog.show();
		}


		protected void onProgressUpdate(String... progress) {
	         progressDialog.setMessage(progress[0]);
	     }

	     protected void onPostExecute(ArrayList<SearchResult> result) {
	    	 Toast.makeText(context, "Found " + result.size() + " ROMs", Toast.LENGTH_LONG);
	    	 progressDialog.dismiss();
	 		Intent i = new Intent();
			i.putExtra(Console.class.getSimpleName(), console.toString());
			i.setAction("com.romcessed.romsearch.action.LIST_ROMS");
			context.startActivity(i);
	     }

		@Override
		//"/downloads/ProtectedFile/" means can't download
		protected ArrayList<SearchResult> doInBackground(String... params) {
			ArrayList<SearchResult> results = new ArrayList<SearchResult>();
				try {
					String url = "http://www.romulation.net/downloads/search/" + getConsoleString(console)+ "/" + URLEncoder.encode(query);
					HttpGet searchGET = new HttpGet(url);
					
					
					//searchPOST.setEntity(paramsEntity);
					
					ResponseHandler<String> handler = new ResponseHandler<String>() {
					    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					        HttpEntity entity = response.getEntity();
					        String html; 
					        
					        if (entity != null) {
					        	html = EntityUtils.toString(entity);
					        	return html;
					        } else {
					            return null;
					        }
					    }
					};
					
					String html = client.execute(searchGET, handler);
					
					searchGET.abort();
					
			        Matcher m = searchPattern.matcher(html);
			        while(m.find()){
			        	SearchResult result;
			        	String urlSuffix, gameName, downloads, parsedConsole;
			        	
			        	urlSuffix = m.group(1);
			        	parsedConsole = m.group(2);
			        	gameName = m.group(3);
			        	downloads = m.group(4);
			        	
			        	Console resultConsole = getConsole(parsedConsole);
			        	if(resultConsole==Console.UNKNOWN){ 
			        		continue;
			        	}
			        	
			        	currentItemTitle = gameName;
			        	publishProgress(currentItemTitle);
			        	
			        	result = new SearchResult(gameName, resultConsole, -1);
			        	result.setSubInfo1_Title("Downloads");
			        	result.setSubInfo1(downloads);
			        	result.setURLSuffix(urlSuffix);
			        	results.add(result);

			        }
					        
					
			        if(results.size()==0){
			        	results.add(new SearchResult("No results found",null, -1));
			        }	
					publishProgress("Done");
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}/* catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	*/	
			GlobalVars.setRecentQueryResults(results);
			return results;
	 }
	
	}

	@Override
	public boolean isPasswordNeededDuringRegistration() {
		return true;
	}


	@Override
	public ArrayList<Category> getAvailableCategories() {
		ArrayList<Category> ret = new ArrayList<Category>();
		for(Category cat : Category.values()){
			ret.add(cat);
		}
		ret.remove(Category.TOP100);
		ret.remove(Category.BIOS);
		return ret;
	}


	@Override
	public boolean isCaptchaRequired() {
		return false;
	}


	@Override
	public EULAcapsule getEULAcapsule(Activity activity) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getHost() {
		return null;
	}


	@Override
	public boolean isDirectDownload() {
		return true;
	}
	

}
