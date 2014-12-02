package com.romcessed.romsearch.searchproviders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.EULAcapsule;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.ServiceCommunicableActivity;
import com.romcessed.romsearch.tools.GlobalVars;

public class DopeRomsConnector extends AbstractConnector {

	

	public DopeRomsConnector() {
		super();
		TAG="DopeRomsConnector";
	}


	@Override
	public void customizeWebView(final ServiceCommunicableActivity activity, WebView webview,
			SearchResult mRom) {
			SharedPreferences settings = activity.getSharedPreferences("FIRSTASKS", 0);
			if(!settings.getBoolean("WEBVIEWINFO_DOPEROMS", false)){
				final AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
		        builder1.setTitle("Long click and hold!");
		        builder1.setCancelable(false);
		        builder1.setPositiveButton("Will Do", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		                SharedPreferences settings = activity.getSharedPreferences("FIRSTASKS", 0);
		        	    	SharedPreferences.Editor editor = settings.edit();
		        	    	editor.putBoolean("WEBVIEWINFO_DOPEROMS", true);
		        	    	editor.commit();
		        	    }
		        });
		        builder1.setMessage("To download a file on DopeROMS, locate the download link on the page. When you try to click it, it will quickly say download failed.\n\nHOW-TO:Click and hold the link until the context menu appears\nSelect \"Save Link\"\n\n**IF THAT DOESN'T WORK, DopeROMS' servers are being overloaded!");
		        builder1.show();
			}
	}




	@Override
	public boolean isSearchSupported() {
		return true;
	}

	
	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> search(Context context, String query, Console console) throws ExecutionException, InterruptedException {
		return new SearchForRomsTask(context, query, console);
	}


	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> browse(Context context, Category category, Console console, int page) throws ExecutionException, InterruptedException {
		return new BrowseForRomsTask(context, category, console, page);
	}


	@Override
	public ArrayList<Console> getAvailableConsoles(Context context) {
		ArrayList<Console> al = new ArrayList<Console>();		
		al.add(Console.ALL);
		al.add(Console.PSX);
		al.add(Console.MAME);
		al.add(Console.GAMEBOYADVANCE);
		al.add(Console.N64);
		al.add(Console.NES);
		al.add(Console.SNES);
		al.add(Console.GENESIS);
		return al;
	}

	
	public Console getConsole(String s){
		Console console = null;
		if(s.equals("sony_playstation_psx")){
			console = Console.PSX;
		} else if(s.equals("mame")){
			console = Console.MAME;
		} else if(s.equals("gameboy_advance_gba")){
			console = Console.GAMEBOYADVANCE;
		} else if(s.equals("nintendo_64")){
			console = Console.N64;
		} else if(s.equals("nintendo_nes")){
			console = Console.NES;
		} else if(s.equals("super_nintendo_snes")){
			console = Console.SNES;
		} else if(s.equals("sega_genesis")){
			console = Console.GENESIS;
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
			consoleSearchString = "sony_playstation_psx";
			break;
		case NES:
			consoleSearchString = "nintendo_nes";
			break;
		case SNES:
			consoleSearchString = "super_nintendo_snes";
			break;
		case MAME:
			consoleSearchString="mame";
			break;
		case GENESIS:
			consoleSearchString = "sega_genesis";
			break;
		case GAMEBOYADVANCE:
			consoleSearchString="gameboy_advance_gba";
			break;
		case N64:
			consoleSearchString="nintendo_64";
		}
		return consoleSearchString;
	}
	

	public String getConsoleID(Console console) {
		switch(console){
		case GENESIS:
			return "11";
		case PSX:
			return "51";
		case SNES:
			return "67";
		case GAMEBOYADVANCE:
			return "114";
		case GAMEBOY:
			return "83";
		case MAME:
			return "97";
		case GAMEGEAR:
			return "72";
		case N64:
			return "23";
		case NES:
			return "90";
		default:
			return "0";
		}
	}




	@Override
	public int getLogoDrawableID() {
		return R.drawable.doperoms_logo;
	}

	@Override
	public boolean registerUser(String fullname, String username, String password, String email, boolean terms) throws UnsupportedEncodingException {
		return false;
	}

	@Override
	public boolean usernameAvailable(String username)
			throws ClientProtocolException, IOException {
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
	        progressDialog.setButton("Stop", new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SearchForRomsTask.this.cancel(true);
				}
			});
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
			i.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(DopeRomsConnector.this));
			i.setAction("com.romcessed.romsearch.action.LIST_ROMS");
			context.startActivity(i);
	     }

	     
	     
		@Override
		protected void onCancelled() {
			super.onCancelled();
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
					
					
					
					if(console==Console.ALL){
						console=null;
						searchPattern = Pattern.compile("<td.+?<a id=\"listing\".+?href=\\\".+?/(.+?)/((.+?).zip).+?/(\\d+)");
						String url = "http://www.doperoms.com/search.php?s=" + URLEncoder.encode(query);
						HttpGet searchGET = new HttpGet(url);
						String html = client.execute(searchGET, handler);
						
						Matcher m = searchPattern.matcher(html);
				        while(m.find() && !isCancelled()){
				        	
				        	parsedConsole = m.group(1);
				        	urlSuffix = m.group(2);
				        	gameName = m.group(3);
				        	
				        	downloads = "unknown";
				        	
				        	Console resultConsole = getConsole(parsedConsole);
				        	if(resultConsole==Console.UNKNOWN){ 
				        		continue;
				        	}
				        	
				        	currentItemTitle = gameName;
				        	publishProgress(currentItemTitle);
				        	
				        	////////////////////
				        	String baseURL = "http://doperoms.com/files/roms/" + getConsoleString(resultConsole) + "/" + URLEncoder.encode(gameName) + ".zip/";
				        	Pattern p = Pattern.compile("html/(\\d+)/");
							Matcher m2 = p.matcher(urlSuffix);
							String fileNumber = m.group(4);
							baseURL += fileNumber + "/" + getTitleWithoutExtraInfo(URLEncoder.encode(gameName)) + ".zip.html";
							String refferURL = "http://doperoms.com/roms/" + getConsoleString(resultConsole) + "/" + encodeRFC1738(gameName) + ".zip.html/" + fileNumber + "/" + encodeRFC1738(gameName) + ".zip.html";

				        	
				        	result = new SearchResult(gameName, resultConsole, SearchProvider.ID_DOPEROMS) ;
				        	result.setFileSize("unknown");
				        	result.setSubInfo1_Title("Downloads");
				        	result.setSubInfo1(downloads);
				        	//If we set a WebViewBaseURL then the URLSuffix will be used as the download link.
				        	result.setWebViewBaseURL(refferURL);
				        	result.setURLSuffix(baseURL);
				        	results.add(result);

				        }
				        Log.v(TAG, "Done finding matches");
						
					} else {

						searchPattern = Pattern.compile("\\\"(h.+?)\\\\\">(.+?).zip<");
						String url = "http://www.doperoms.com/ajaxsearchroms.php?data=" + URLEncoder.encode(query) + "&consoleid=" + getConsoleID(console) + "&_cba_request_id=0";
						HttpGet searchGET = new HttpGet(url);
						String html = client.execute(searchGET, handler);
						
						Matcher m = searchPattern.matcher(html);
				        while(m.find() && !isCancelled()){
				        	
				        	urlSuffix = m.group(1);
				        	gameName = m.group(2);
				        	downloads = "unknown";
				        	
				        	String baseURL = "http://doperoms.com/files/roms/" + getConsoleString(console) + "/" + URLEncoder.encode(gameName) + ".zip/";
				        	Pattern p = Pattern.compile("html/(\\d+)/");
							Matcher m2 = p.matcher(urlSuffix);
							String fileNumber = null;
							while(m2.find() && fileNumber==null){
								fileNumber = m2.group(1);
							}
							baseURL += fileNumber + "/" + getTitleWithoutExtraInfo(URLEncoder.encode(gameName)) + ".zip.html";
							String refferURL = "http://doperoms.com/roms/" + getConsoleString(console) + "/" + encodeRFC1738(gameName) + ".zip.html/" + fileNumber + "/" + encodeRFC1738(gameName) + ".zip.html";
				        	
				        	currentItemTitle = gameName;
				        	publishProgress(currentItemTitle);
				        	
				        	result = new SearchResult(gameName, console, SearchProvider.ID_DOPEROMS);
				        	result.setSubInfo1_Title("Downloads");
				        	result.setSubInfo1(downloads);
				        	result.setFileSize("unknown");
				        	result.setURLSuffix(baseURL);
				        	result.setWebViewBaseURL(refferURL);
				        	results.add(result);

				        }
					}  
					
			        if(results.size()==0){
			        	results.add(new SearchResult("No results found",console, SearchProvider.ID_DOPEROMS));
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
	
	private class BrowseForRomsTask extends AsyncTask<String, String, ArrayList<SearchResult>> {

		private final Context context;
		private ProgressDialog progressDialog;
		private Console console;
		private Category category;
		private int page;
		
		final Pattern searchPattern = Pattern.compile("<td.+?href=\"(.+?).html\".+name=\"(.+?).zip\"");
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
				try {
					HttpGet browseGet = new HttpGet("http://www.doperoms.com/roms/" +
							getConsoleString(console) + "/" + category.name() + "/" + ((page-1)*50) + ".html");
				
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
				        	gameName = m.group(2);
				        	
				        	
				        	String baseURL = "http://doperoms.com/files/roms/" + getConsoleString(console) + "/" + URLEncoder.encode(gameName) + ".zip/";
				        	Pattern p = Pattern.compile("html/(\\d+)/");
							Matcher m2 = p.matcher(urlSuffix);
							String fileNumber = null;
							while(m2.find()){
								fileNumber = m2.group(1);
							}
							baseURL += fileNumber + "/" + getTitleWithoutExtraInfo(URLEncoder.encode(gameName)) + ".zip.html";
							String refferURL = "http://doperoms.com/roms/" + getConsoleString(console) + "/" + encodeRFC1738(gameName) + ".zip.html/" + fileNumber + "/" + encodeRFC1738(gameName) + ".zip.html";
							
							
				        	fileSize = "unknown";
				        	downloads = "unknown";
				        	
				        	
				        	currentItemTitle = gameName;
				        	publishProgress(currentItemTitle);
				        	
				        	result = new SearchResult(gameName, console, SearchProvider.ID_DOPEROMS);
				        	result.setFileSize(fileSize);
				        	result.setSubInfo1_Title("Downloads");
				        	result.setSubInfo1(downloads);
				        	
				        	//If we set a WebViewBaseURL then the URLSuffix will be used as the download link.
				        	result.setWebViewBaseURL(refferURL);
				        	result.setURLSuffix(baseURL);
				        	
				        	
				        	results.add(result);
				        	Thread.sleep(5);
				        }
					        
					
			        if(results.size()==0){
			        	results.add(new SearchResult("No results found",null, SearchProvider.ID_DOPEROMS));
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
	
	public class myRI implements HttpRequestInterceptor
	{   
		String url;
		
		public myRI(String urlSuffix){
			url = urlSuffix;
		}
		
		@Override
		public void process(HttpRequest request, HttpContext context)
				throws HttpException, IOException {
			request.addHeader("Referer", "http://doperoms.com/files" + URLEncoder.encode(url));
			
		}
	};
	

	private String getTitleWithoutExtraInfo(String URLEncodedTitle){
		String ret = URLEncodedTitle.replaceAll("\\+%.+29", "+");
		ret = ret.replaceAll("\\+%.+5D", "");
		return ret;
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
	
	 /**
	   * Encode a string according to RFC 1738.
	   * <p/>
	   * <quote> "...Only alphanumerics [0-9a-zA-Z], the special characters "$-_.+!*'()," [not
	   * including the quotes - ed], and reserved characters used for their reserved purposes
	   * may be used unencoded within a URL."</quote>
	   * <p/>
	   * <ul> <li><p>The ASCII characters 'a' through 'z', 'A' through 'Z', and '0' through
	   * '9' remain the same.
	   * <p/>
	   * <li><p>The unreserved characters - _ . ! ~ * ' ( ) remain the same.
	   * <p/>
	   * <li><p>All other ASCII characters are converted into the 3-character string "%xy",
	   * where xy is the two-digit hexadecimal representation of the character code
	   * <p/>
	   * <li><p>All non-ASCII characters are encoded in two steps: first to a sequence of 2 or
	   * 3 bytes, using the UTF-8 algorithm; secondly each of these bytes is encoded as "%xx".
	   * </ul>
	   *
	   * @param s The string to be encoded
	   * @return The encoded string
	   */
	  public static String encodeRFC1738 (final String s) {
	    final StringBuffer sbuf = new StringBuffer();
	    final char[] sChars = s.toCharArray();
	    final int len = sChars.length;
	    for (int i = 0; i < len; i++)
	    {
	      final int ch = sChars[i];
	      if ('A' <= ch && ch <= 'Z')
	      {   // 'A'..'Z'
	        sbuf.append((char) ch);
	      }
	      else if ('a' <= ch && ch <= 'z')
	      { // 'a'..'z'
	        sbuf.append((char) ch);
	      }
	      else if ('0' <= ch && ch <= '9')
	      { // '0'..'9'
	        sbuf.append((char) ch);
	      }
	      else if (ch == '-' || ch == '_'   // unreserved
	              || ch == '.' || ch == '!'
	              || ch == '~' || ch == '*'
	              || ch == '\'' || ch == '('
	              || ch == ')')
	      {
	        sbuf.append((char) ch);
	      }
	      else if (ch <= 0x007f)
	      {   // other ASCII
	        sbuf.append(hex[ch]);
	      }
	      else if (ch <= 0x07FF)
	      {   // non-ASCII <= 0x7FF
	        sbuf.append(hex[0xc0 | (ch >> 6)]);
	        sbuf.append(hex[0x80 | (ch & 0x3F)]);
	      }
	      else
	      {         // 0x7FF < ch <= 0xFFFF
	        sbuf.append(hex[0xe0 | (ch >> 12)]);
	        sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
	        sbuf.append(hex[0x80 | (ch & 0x3F)]);
	      }
	    }
	    return sbuf.toString();
	  }

	  private static String encodeBytes (final byte[] s) {
	    final StringBuffer sbuf = new StringBuffer();
	    final int len = s.length;
	    for (int i = 0; i < len; i++)
	    {
	      final int ch = (s[i] & 0xff);
	      if ('A' <= ch && ch <= 'Z')
	      {   // 'A'..'Z'
	        sbuf.append((char) ch);
	      }
	      else if ('a' <= ch && ch <= 'z')
	      { // 'a'..'z'
	        sbuf.append((char) ch);
	      }
	      else if ('0' <= ch && ch <= '9')
	      { // '0'..'9'
	        sbuf.append((char) ch);
	      }
	      else if (ch == '-' || ch == '_'   // unreserved
	              || ch == '.' || ch == '!'
	              || ch == '~' || ch == '*'
	              || ch == '\'' || ch == '('
	              || ch == ')')
	      {
	        sbuf.append((char) ch);
	      }
	      else
	      {   // other ASCII
	        sbuf.append(hex[ch]);
	      }
	    }
	    return sbuf.toString();
	  }

	  public static String encode (final String s, final String encoding)
	          throws UnsupportedEncodingException
	  {
	    if ("utf-8".equalsIgnoreCase(encoding))
	    {
	      return encodeRFC1738(s);
	    }

	    return encodeBytes(s.getBytes(encoding));
	  }

	
	  private static final String[] hex = {
		    "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
		    "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F",
		    "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
		    "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F",
		    "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
		    "%28", "%29", "%2A", "%2B", "%2C", "%2D", "%2E", "%2F",
		    "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
		    "%38", "%39", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F",
		    "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
		    "%48", "%49", "%4A", "%4B", "%4C", "%4D", "%4E", "%4F",
		    "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
		    "%58", "%59", "%5A", "%5B", "%5C", "%5D", "%5E", "%5F",
		    "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
		    "%68", "%69", "%6A", "%6B", "%6C", "%6D", "%6E", "%6F",
		    "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
		    "%78", "%79", "%7A", "%7B", "%7C", "%7D", "%7E", "%7F",
		    "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
		    "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F",
		    "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
		    "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F",
		    "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7",
		    "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF",
		    "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7",
		    "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF",
		    "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7",
		    "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF",
		    "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7",
		    "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF",
		    "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7",
		    "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF",
		    "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7",
		    "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"
		  };


	@Override
	public EULAcapsule getEULAcapsule(Activity activity) {
		return null;
	}




	@Override
	public String getHost() {
		return "www.doperoms.com";
	}




	@Override
	public boolean isDirectDownload() {
		return false;
	}


}
