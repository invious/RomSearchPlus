package com.romcessed.romsearch.searchproviders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.EULAcapsule;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.activities.RomDetailActivity;
import com.romcessed.romsearch.tools.GlobalVars;
import com.romcessed.romsearch.tools.HtmlTools;
import com.romcessed.romsearch.tools.NostalgiaTools;
import com.romcessed.romsearch.tools.ZipTools;

public class RomhackingConnector extends AbstractConnector {

	
	
	public RomhackingConnector() {
		super();
		TAG="RomhackingConnector";
	}

	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> browse(Context context, Category category, Console console, int page)throws ExecutionException, InterruptedException {
		return null;
	}
	
	
	
	@Override
	public String getDescription(Context context, SearchResult searchResult) throws ExecutionException, InterruptedException {
		String description = null;
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
		
			Pattern searchPattern = Pattern.compile(">Description</span></div>(.+?)<a", Pattern.DOTALL);
			String url = searchResult.getURLSuffix();
			HttpGet searchGET = new HttpGet(url);
			String html;
			try {
				html = client.execute(searchGET, handler);
				Matcher m = searchPattern.matcher(html);
				if(m.find()){
					description = URLDecoder.decode(HtmlTools.decode((m.group(1).replaceAll("</p>", "\n")).replaceAll("<p>", "")));
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return description;
	}




	private class SearchForRomsTask extends AsyncTask<String, String, ArrayList<SearchResult>> {

		private final Context context;
		private ProgressDialog progressDialog;
		private Console console;
		private String query;
		Pattern searchPattern;
		
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
	        progressDialog.setTitle("Rom Hacks");
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
			if(console==null){
				console=Console.ALL;
			}
			i.putExtra(Console.class.getSimpleName(), console.toString());
			i.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(RomhackingConnector.this));
			i.setAction("com.romcessed.romsearch.action.LIST_ROMS");
			context.startActivity(i);
	     }

	     
	     @Override
			protected ArrayList<SearchResult> doInBackground(String... params) {
	    	 int curPage = 1;
				ArrayList<SearchResult> results = new ArrayList<SearchResult>();
					try {
						
			        	SearchResult result;
			        	String urlSuffix, gameName, parsedConsole, orig_game, hack_category;
						
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
						String html = null;
						while(true){
							searchPattern = Pattern.compile("<a.+?\"(.+?)\">(.+?)</.+?</a>.+?<td>(.+?)<.+?</td><td>(.+?)</td><td>.+?<td>(.+?)<");
							String url = "http://www.romhacking.net/?" + query + "&startpage=" + curPage;
							HttpGet searchGET = new HttpGet(url);
							html = client.execute(searchGET, handler);
							
							//Check to see if this page contains results
							if (html.contains("INVALID STARTPAGE VALUE"))
								break;
							
							Matcher m = searchPattern.matcher(html);
							int startIndex = html.indexOf(">Date</a>");
							m.region(startIndex, html.length());
					        while(m.find()){
					        	
					        	parsedConsole = m.group(4);
					        	urlSuffix = m.group(1);
					        	gameName = HtmlTools.decode(m.group(2));
					        	orig_game = HtmlTools.decode(m.group(3));
					        	hack_category = m.group(5);
					        	
					        	urlSuffix="http://www.romhacking.net" + urlSuffix;
					        	
					        	Console resultConsole = getConsole(parsedConsole);
					        	if(resultConsole==Console.UNKNOWN || !getAvailableConsoles(context).contains(resultConsole)){ 
					        		continue;
					        	}
					        	
					        	currentItemTitle = gameName;
					        	publishProgress(currentItemTitle);
					        	

					        	result = new SearchResult(gameName, resultConsole, SearchProvider.ID_ROMHACKING) ;
					        	result.setFileSize("unknown");
					        	result.setSubInfo1_Title("Info");
					        	result.setSubInfo1(orig_game + " - " + hack_category);
					        	result.setURLSuffix(urlSuffix);
					        	results.add(result);	
					        }
					        curPage++;
						}
					        Log.v(TAG, "Done finding matches");

							
				        if(results.size()==0){
				        	results.add(new SearchResult("No results found",console, SearchProvider.ID_ROMHACKING));
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
	public Console getConsole(String s) {
		if(s.equals("GB")){
			return Console.GAMEBOY;
		} else if (s.equals("GBA")){
			return Console.GAMEBOYADVANCE;
		} else if (s.equals("GEN")){
			return Console.GENESIS;
		} else {
			return Console.valueOf(s);
		} 
	}

	@Override
	public String getConsoleString(Console c) {
		return null;
	}

	@Override
	public int getLogoDrawableID() {
		return R.drawable.rom_hacking_logo;
	}


	@Override
	public boolean isPasswordNeededDuringRegistration() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean registerUser(String fullname, String username,
			String password, String email, boolean terms)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<Console> getAvailableConsoles(Context context) {
		ArrayList<Console> al = new ArrayList<Console>();
		al.add(Console.GENESIS);
		al.add(Console.PSX);
		al.add(Console.SNES);
		al.add(Console.GAMEBOYADVANCE);
		al.add(Console.GAMEBOY);
		al.add(Console.N64);
		al.add(Console.NES);
		return al;
	}

	@Override
	public ArrayList<Category> getAvailableCategories() {
		ArrayList<Category> ret = new ArrayList<Category>();
		return ret;
	}

	@Override
	public boolean isCaptchaRequired() {
		return false;
	}

	@Override
	public EULAcapsule getEULAcapsule(Activity activity) {
		return null;
	}

	@Override
	public String getHost() {
		return "www.romhacking.net";
	}

	@Override
	public boolean isDirectDownload() {
		return true;
	}
	
	@Override
	public boolean isSearchSupported() {
		return true;
	}

	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> search(
			Context context, String query, Console console)
			throws ExecutionException, InterruptedException {
		return new SearchForRomsTask(context, query, console);
	}

}
