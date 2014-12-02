package com.romcessed.romsearch.searchproviders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ObjectInputStream.GetField;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.EULAcapsule;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.ServiceCommunicableActivity;
import com.romcessed.romsearch.activities.SplashScreenActivity;
import com.romcessed.romsearch.tools.GlobalVars;



public class EmuParadiseConnector extends AbstractConnector{

	String pageHTML;
	String lastLoadedURL = "";
	
private String getDownloadButtonOnly(String url){
	HttpGet pageGet = new HttpGet(url);
	
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
	
	pageHTML = null;
	try {
		while (pageHTML==null){
			pageHTML = client.execute(pageGet, handler);
		}
	} catch (ClientProtocolException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
    	String displayHTML = null; 	
	
		Pattern pattern1 = Pattern.compile("<h2>Direct Down.+?</h2>(</div>)*(.+?)+?</a>", Pattern.DOTALL);
	    Matcher matcher1 = pattern1.matcher(pageHTML);
	    while(matcher1.find()){
	    	displayHTML = matcher1.group();
	    }
	    
	 	Pattern pattern2 = Pattern.compile("<h2>Download \\(FTP\\).+?</a>", Pattern.DOTALL);
	    Matcher matcher2 = pattern2.matcher(pageHTML);
	    while(matcher2.find()){
	    	displayHTML = matcher2.group();
	    }

	return displayHTML;
}
	
	@Override
	public void customizeWebView(final ServiceCommunicableActivity activity, final WebView webview, final SearchResult mRom) {
		mRom.setFileSize(getFileSize(mRom.getURLSuffix()));
		webview.getSettings().setJavaScriptEnabled(true);
		WebViewClient anchorWebViewClient = new WebViewClient()
	    {

	        @Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				String downloadButtonHTML = getDownloadButtonOnly(url);
				if(downloadButtonHTML!=null && !url.equals(lastLoadedURL)){
					lastLoadedURL = url;
					webview.loadDataWithBaseURL(url, downloadButtonHTML, null, "utf-8", url);
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

			}

			@Override
	        public boolean shouldOverrideUrlLoading(WebView view, String  url)
	        {	        		
	        	if ( url.contains("ftp:/")){
		        	try {
						activity.getServiceCom().downloadFtp(SearchProvider.ID_EMUPARADISE, url, mRom);
						activity.finish();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		return true;
	        	}
	        	return false;
	        }
	    };
	    webview.setWebViewClient(anchorWebViewClient);
	}

	private boolean AndFTPInstalled(Context context){
        boolean installed = false;
		List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);  
		for(int i=0;i<packs.size();i++) {  
			PackageInfo p = packs.get(i);  
			if(p.packageName.contains("lysesoft.andftp")){
				return true;
			}
		}
		return false;
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
			if(console==null){
				console=Console.ALL;
			}
			i.putExtra(Console.class.getSimpleName(), console.toString());
			i.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(EmuParadiseConnector.this));
			i.setAction("com.romcessed.romsearch.action.LIST_ROMS");
			context.startActivity(i);
	     }


		@Override
		protected ArrayList<SearchResult> doInBackground(String... params) {
			ArrayList<SearchResult> results = new ArrayList<SearchResult>();
				try {
					
		        	SearchResult result;
		        	String urlSuffix, gameName, downloads, parsedConsole;
					
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
					
						
						searchPattern = Pattern.compile(".+?href=\"(/(.+?)/.+?)\">(.+?)<.+?<br><b>System:</b>");
						String url = "http://www.emuparadise.org/roms/search.php?query=" + URLEncoder.encode(query) + "&sysid=" + getConsoleID(console);
						HttpGet searchGET = new HttpGet(url);
						String html = client.execute(searchGET, handler);
						
						Matcher m = searchPattern.matcher(html);
				        while(m.find()){
				        	
				        	parsedConsole = m.group(2);
				        	urlSuffix = m.group(1);
				        	gameName = m.group(3);
				        	
				        	urlSuffix="http://www.emuparadise.org" + urlSuffix;
				        	
				        	Console resultConsole = getConsole(parsedConsole);
				        	if(resultConsole==Console.UNKNOWN || !getAvailableConsoles(context).contains(resultConsole)){ 
				        		continue;
				        	}
				        	
				        	currentItemTitle = gameName;
				        	publishProgress(currentItemTitle);
				        	

				        	result = new SearchResult(gameName, resultConsole, SearchProvider.ID_EMUPARADISE) ;
				        	result.setFileSize("unknown");
				        	result.setSubInfo1_Title("Downloads");
				        	result.setSubInfo1("unknown");
				        	result.setWebViewBaseURL(url);
				        	result.setURLSuffix(urlSuffix);
				        	results.add(result);

				        }
				        Log.v(TAG, "Done finding matches");
						

					
			        if(results.size()==0){
			        	results.add(new SearchResult("No results found",console, SearchProvider.ID_EMUPARADISE));
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
	
    protected String getFileSize(String url){
		String fileSize = null;	
    	 HttpGet fileSizeGet = new HttpGet(url);
			
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
			try {
				html = client.execute(fileSizeGet, handler);
			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Pattern p = Pattern.compile("<h2>Direct Dow.+?a>.+?\\((.+?)\\)<br>", Pattern.DOTALL);
			Matcher m = p.matcher(html);
			m.find();
			fileSize = m.group(1);
			int val;
			if(fileSize.contains("M")){
				fileSize = fileSize.replace("M", "");
				Double dblVersion = Double.valueOf(fileSize);
				val = (int) (dblVersion * 1024);
				fileSize = String.valueOf(val);
			} else {
				fileSize = fileSize.replace("K", "");
				Double dblVersion = Double.valueOf(fileSize);
				val = (int) (dblVersion * 1);
				fileSize = String.valueOf(val);
			}
			
			return fileSize;
			
     }
	
	private class BrowseForRomsTask extends AsyncTask<String, String, ArrayList<SearchResult>> {

		private final Context context;
		private ProgressDialog progressDialog;
		private Console console;
		private Category category;
		private int page;
		
		final Pattern searchPattern = Pattern.compile("index title=\".+?\".+?href=\"(/(.+?)/.+?/(\\d+))\">(.+?)<", Pattern.DOTALL);
		private ArrayList<SearchResult> results;
		
		public BrowseForRomsTask(Context context, Category category, Console console, int page){
			super();
			this.context = context;
			this.console = console;
			this.category = category;
			this.page = page;
		}
		
	     @Override
		protected void onPreExecute() {
	        super.onPreExecute();
	    	 progressDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
	        progressDialog.setIcon(R.drawable.icon);
	        progressDialog.setCancelable(false);
	        progressDialog.setTitle("Page 1 of " + console.getProperName() + " roms beginning with " + category.toString());
	        progressDialog.setMessage("Retrieving...");
	        progressDialog.show();
		}

	     

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		protected void onProgressUpdate(String... progress) {
	         progressDialog.setMessage(progress[0]);
	     }

	     protected void onPostExecute(ArrayList<SearchResult> result) {
	    	 String resultString = (page==1) ? "Found ": "Added ";
	    	 progressDialog.setMessage(resultString + result.size() + "ROMs");
	    	 progressDialog.dismiss();
	     }


	     
		@Override
		protected ArrayList<SearchResult> doInBackground(String... params) {
			results = new ArrayList<SearchResult>();
			
			String browseURL = "http://www.emuparadise.org/" + getConsoleString(console) + "/Games-Starting-With-" + category.name() + "/" + getConsoleID(console);	
			try {
					HttpGet browseGet = new HttpGet(browseURL);
				
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
					
					String html = client.execute(browseGet, handler);
					
					 Matcher m = searchPattern.matcher(html);
				        while(m.find()){
				        	SearchResult result;
				        	String urlSuffix, gameName, fileSize, downloads;
				        	
				        	urlSuffix = m.group(1);
				        	gameName = m.group(4);
				        	String gameNumber = m.group(3);
				        	
				        	
				        	urlSuffix = "http://www.emuparadise.org/" + getConsoleString(console) + "/" + DopeRomsConnector.encodeRFC1738(gameName) + "/" + gameNumber;
				        	
				        	fileSize = "unknown";
				        	downloads = "unknown";
				        	
				        	
				        	currentItemTitle = gameName;
				        	publishProgress(currentItemTitle);
				        	
				        	result = new SearchResult(gameName, console, SearchProvider.ID_EMUPARADISE);
				        	result.setFileSize(fileSize);
				        	result.setSubInfo1_Title("Downloads");
				        	result.setSubInfo1(downloads);
				        	result.setWebViewBaseURL(browseURL);
				        	result.setURLSuffix(urlSuffix);
				        	results.add(result);
				        	Thread.sleep(5);
				        }
					        
					
			        if(results.size()==0){
			        	results.add(new SearchResult("No results found",null, SearchProvider.ID_EMUPARADISE));
			        }	
					publishProgress("Done");
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}		
			return results;
	 }
	
	}
	
	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> browse(Context context, Category category, Console console, int page)throws ExecutionException, InterruptedException {
		return new BrowseForRomsTask(context, category, console, page);
	}

	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> search(Context context, String query, Console console) throws ExecutionException, InterruptedException {
		return new SearchForRomsTask(context, query, console);
	}

	@Override
	public ArrayList<Category> getAvailableCategories() {
		ArrayList<Category> ret = new ArrayList<Category>();
		for(Category c: Category.values()){
			ret.add(c);
		}
		ret.remove(Category.BIOS);
		ret.remove(Category.TOP100);
		return ret;
	}

	public boolean extrasEnabled(Context context){
	      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	      return prefs.getBoolean("enableparadise", false);
	}
	
	@Override
	public ArrayList<Console> getAvailableConsoles(Context context) {
		ArrayList<Console> al = new ArrayList<Console>();
		al.add(Console.ALL);
		if(extrasEnabled(context)){
			al.add(Console.PSX);
		}
		al.add(Console.NES);
		if(extrasEnabled(context)){
			al.add(Console.GAMEBOYADVANCE);
		}
		al.add(Console.MAME);
		al.add(Console.GENESIS);
		if(extrasEnabled(context)){
			al.add(Console.N64);
		}
		return al;
	}

	@Override
	public boolean isSearchSupported() {
		return true;
	}
	
	public String getConsoleID(Console console) {
		switch(console){
		case ALL:
			return "0";
		case PSX:
			return "2";
		case MAME:
			return "7";
		case GAMEBOYADVANCE:
			return "31";
		case NES:
			return "13";
		case GENESIS:
			return "6";
		case N64:
			return "9";
		default:
			return "0";
		}
	}

	@Override
	public Console getConsole(String s) {
		Console console = null;
		if(s.equals("Sony_Playstation_ISOs")){
			console = Console.PSX;
		} else if(s.equals("Nintendo_Gameboy_Advance_ROMs")){
			console = Console.GAMEBOYADVANCE;
		} else if(s.equals("M.A.M.E._-_Multiple_Arcade_Machine_Emulator_ROMs")){
			console = Console.MAME;
		} else if(s.equals("Nintendo_Entertainment_System_ROMs")){
			console = Console.NES;
		} else if(s.equals("Sega_Genesis_-_Sega_Megadrive_ROMs")){
			console = Console.GENESIS;
		} else if(s.equals("Nintendo_64_ROMs")){
			console = Console.N64;
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
			consoleSearchString = "Sony_Playstation_ISOs";
			break;
		case MAME:
			consoleSearchString="M.A.M.E._-_Multiple_Arcade_Machine_Emulator_ROMs";
			break;
		case GAMEBOYADVANCE:
			consoleSearchString="Nintendo_Gameboy_Advance_ROMs";
			break;
		case NES:
			consoleSearchString="Nintendo_Entertainment_System_ROMs";
			break;
		case GENESIS:
			consoleSearchString="Sega_Genesis_-_Sega_Megadrive_ROMs";
			break;
		case N64:
			consoleSearchString="Nintendo_64_ROMs";
			break;
		}
		return consoleSearchString;
	}

	@Override
	public EULAcapsule getEULAcapsule(Activity activity) {
		return null;
	}

	@Override
	public String getHost() {
		return "www.emuparadise.org";
	}

	@Override
	public int getLogoDrawableID() {
		return R.drawable.emuparadise_logo;
	}

	@Override
	public boolean isCaptchaRequired() {
		return false;
	}

	@Override
	public boolean isPasswordNeededDuringRegistration() {
		return false;
	}

	@Override
	public boolean registerUser(String fullname, String username,
			String password, String email, boolean terms)
			throws UnsupportedEncodingException {
		return false;
	}

	@Override
	public boolean isDirectDownload() {
		return false;
	}

	
	
}
