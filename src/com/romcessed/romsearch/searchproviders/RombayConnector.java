package com.romcessed.romsearch.searchproviders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.EULAcapsule;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.tools.GlobalVars;

public class RombayConnector extends AbstractConnector{
	

		/**
		 * Content-Length: 145 means username is valid
		 */
		private static final int CONTENT_LENGTH_USERNAME_OK = 145;
				

		
		public RombayConnector() {
			super();
			TAG="RombayConnector";
		}


		/**
		 * Checks to see if the submitted username is already taken
		 * @param username to check
		 * @return whether or not the username is available
		 * @throws IOException 
		 * @throws ClientProtocolException 
		 * @throws HttpException
		 * @throws IOException
		 */
		public boolean usernameAvailable(String username) throws ClientProtocolException, IOException {
			final HttpPost registerPOST = new HttpPost("http://www.rombay.com/includes/ajax/ajax.register.php");
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("c", "u"));
			formparams.add(new BasicNameValuePair("data", username));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, HTTP.UTF_8);
			registerPOST.setEntity(entity);
			
			String okUsername;
			HttpResponse response = client.execute(registerPOST);
			okUsername = EntityUtils.toString(response.getEntity());

			
			response.getEntity().consumeContent();
			GlobalVars.cleanClient();
						
			return (okUsername.contains("Your username is valid")? true : false);
		}

		//POST /register.html fullname=Ryan&username_login=cheezypoofs&email1=ccyh0a3c0b60i1bx%40mailcatch.com&terms=true&register=Register%21&a=reg2
		public boolean registerUser(String fullname, String username, String password, String email, boolean terms) throws UnsupportedEncodingException {
			HttpPost registerPOST = new HttpPost("http://www.rombay.com/register.html");
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("fullname", fullname));
			formparams.add(new BasicNameValuePair("username_login", username));
			formparams.add(new BasicNameValuePair("email1", email));
			formparams.add(new BasicNameValuePair("terms", Boolean.valueOf(terms).toString()));
			formparams.add(new BasicNameValuePair("register", "Register%21"));
			formparams.add(new BasicNameValuePair("a", "reg2"));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, HTTP.UTF_8);
			registerPOST.setEntity(entity);

			String html="";
			
			ResponseHandler<String> handler = new ResponseHandler<String>() {
			    public String handleResponse(
			            HttpResponse response) throws ClientProtocolException, IOException {
			    	 HttpEntity rEntity = response.getEntity(); 
				        
				        if (rEntity != null) {
				        	return EntityUtils.toString(rEntity);
				        } else {
				            return null;
				        }
				        
			    }
			};
			
			try {
				html = client.execute(registerPOST, handler);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			registerPOST.abort();
			GlobalVars.cleanClient();
			if(html.contains("your account has been created")){
				return true;
			}
			
			//TODO: Test to see the possible responses from the server.
			return false;
		}
		
		public Bitmap getCaptcha() throws ClientProtocolException, IOException, CaptchaNotRequiredException{
			Bitmap bmImg;
			
			HttpGet get = new HttpGet("http://www.rombay.com/php_captcha.php");
			ResponseHandler<Bitmap> handler = new ResponseHandler<Bitmap>() {
			    public Bitmap handleResponse(
			            HttpResponse response) throws ClientProtocolException, IOException {
			    	return BitmapFactory.decodeStream(response.getEntity().getContent());
			    }
			};
			
			bmImg = client.execute(get, handler);

			GlobalVars.cleanClient();
			return bmImg;
		}
		
		public AsyncTask<String, String, ArrayList<SearchResult>> search(Context context, String query, Console console) throws ExecutionException, InterruptedException{
			return new SearchForRomsTask(context, query, console);
		}
		
		public AsyncTask<String, String, ArrayList<SearchResult>> browse(Context context, Category category, Console console, int page) throws ExecutionException, InterruptedException{
			return new BrowseForRomsTask(context, category, console, page);
		}
		
		private class SearchForRomsTask extends AsyncTask<String, String, ArrayList<SearchResult>> {

			private final Context context;
			private ProgressDialog progressDialog;
			private Console console;
			private String query;
			
			final Pattern searchPattern = Pattern.compile("<td class=\"td\\w+?\"><a href=\"(\\S+)\">(.+)</a>" +
					"</td>[\r\n]+.+<t.+?>(.+)</td>[\r\n]+.+?<t.+?>(.+)</td>[\r\n]+.+<t.+?>(.+)</td>");
			
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
				i.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(RombayConnector.this));
				context.startActivity(i);
		     }

			@Override
			protected ArrayList<SearchResult> doInBackground(String... params) {
				ArrayList<SearchResult> results = new ArrayList<SearchResult>();
					try {
						HttpPost searchPOST = new HttpPost("http://www.rombay.com/search.html");
						List<NameValuePair> formparams = new ArrayList<NameValuePair>();
						formparams.add(new BasicNameValuePair("keywords", query));
						formparams.add(new BasicNameValuePair("search_type", "roms___" + getConsoleString(console)));
						formparams.add(new BasicNameValuePair("Search", "Search%21"));
						formparams.add(new BasicNameValuePair("a", "search2"));
						UrlEncodedFormEntity paramsEntity = new UrlEncodedFormEntity(formparams, HTTP.UTF_8);
						searchPOST.setEntity(paramsEntity);
						
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
						
						String html = client.execute(searchPOST, handler);
						
						searchPOST.abort();
						GlobalVars.cleanClient();
						
				        Matcher m = searchPattern.matcher(html);
				        while(m.find()){
				        	SearchResult result;
				        	String urlSuffix, gameName, parsedConsole, fileSize, downloads;
				        	
				        	urlSuffix = m.group(1);
				        	gameName = m.group(2);
				        	parsedConsole = m.group(3);
				        	fileSize = m.group(4);
				        	downloads = m.group(5);
				        	
				        	Console resultConsole = getConsole(parsedConsole);
				        	if(resultConsole==Console.UNKNOWN || resultConsole==Console.GAMEBOYADVANCE){ //Gameboy advance not supported!
				        		continue;
				        	}
				        	
				        	currentItemTitle = gameName;
				        	publishProgress(currentItemTitle);
				        	
				        	result = new SearchResult(gameName, resultConsole, SearchProvider.ID_ROMBAY);
				        	result.setFileSize(fileSize);
				        	result.setSubInfo1_Title("Downloads");
				        	result.setSubInfo1(downloads);
				        	result.setURLSuffix(urlSuffix);
				        	results.add(result);
				        	//Thread.sleep(5); //Slow it down
				        }
						        
						
				        if(results.size()==0){
				        	results.add(new SearchResult("No results found",null, SearchProvider.ID_ROMBAY));
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
			
			final Pattern searchPattern = Pattern.compile("<td.+?><a href=\"(.+?)\">(.+?)</a.</td>\\s*<td.*?>(.+?)</td>\\s*<td.*?>(.+?)</td>\\s*</tr>");
			
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
				ArrayList<SearchResult> results = new ArrayList<SearchResult>();
					try {
						HttpPost searchPOST = new HttpPost("http://www.rombay.com/rom_view.html");
						List<NameValuePair> formparams = new ArrayList<NameValuePair>();
						formparams.add(new BasicNameValuePair("page", String.valueOf(page)));
						formparams.add(new BasicNameValuePair("category", category.toString()));
						formparams.add(new BasicNameValuePair("console", getConsoleString(console))); //TODO: Change to Connector.getConsoleString(console);
						UrlEncodedFormEntity paramsEntity = new UrlEncodedFormEntity(formparams, HTTP.UTF_8);
						searchPOST.setEntity(paramsEntity);
						
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
						
						String html = client.execute(searchPOST, handler);
						
						 Matcher m = searchPattern.matcher(html);
					        while(m.find()){
					        	SearchResult result;
					        	String urlSuffix, gameName, fileSize, downloads;
					        	
					        	urlSuffix = m.group(1);
					        	gameName = m.group(2);
					        	fileSize = m.group(3);
					        	downloads = m.group(4);
					        	
					        	
					        	currentItemTitle = gameName;
					        	publishProgress(currentItemTitle);
					        	
					        	result = new SearchResult(gameName, console, SearchProvider.ID_ROMBAY);
					        	result.setFileSize(fileSize);
					        	result.setSubInfo1_Title("Downloads");
					        	result.setSubInfo1(downloads);
					        	result.setURLSuffix(urlSuffix);
					        	results.add(result);
					        	Thread.sleep(5);
					        }
						        
					        GlobalVars.cleanClient();
						
				        if(results.size()==0){
				        	results.add(new SearchResult("No results found",null, SearchProvider.ID_ROMBAY));
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
		
		
		public String getConsoleString(Console c){
			String consoleSearchString = null;
			switch(c){
			case ALL:
				consoleSearchString = "all";
				break;
			case GAMEBOY:
				consoleSearchString="gameboy";
				break;
			case NES:
				consoleSearchString="nintendo";
				break;
			case SNES:
				consoleSearchString="super_nintendo";
				break;
			case GAMEBOYADVANCE:
				consoleSearchString="GBA";
				break;
			case GENESIS:
				consoleSearchString="genesis";
				break;
			case MASTERSYSTEM:
				consoleSearchString="master_system";
				break;
			case GAMEGEAR:
				consoleSearchString="game_gear";
				break;
			case N64:
				consoleSearchString="nintendo_64";
				break;
			}
			return consoleSearchString;
		}
		
		/**
		 * Gets the {@link Console} from the website's string representation
		 * @param c
		 */
		public Console getConsole(String s){
			Console console = null;
			if(s.equals("Nintendo Gameboy Roms")){
				console = Console.GAMEBOY;
			} else if(s.equals("NES Nintendo Roms")){
				console = Console.NES;
			}else if(s.equals("SNES Roms")){
				console = Console.SNES;
			} else if(s.equals("Gameboy Advance Roms")){
				console = Console.GAMEBOYADVANCE;
			} else if(s.equals("Sega Genesis Roms")){
				console = Console.GENESIS;
			} else if(s.equals("Sega Master System Roms")){
				console = Console.MASTERSYSTEM;
			} else if(s.equals("Sega Game Gear Roms")){
				console = Console.GAMEGEAR;
			} else if(s.equals("N64 Roms")){
				console = Console.N64;
			} else {
				console = Console.UNKNOWN;
			}

			return console;
		}
		

		@Override
		public boolean authenticateUser(String username, String password) throws ClientProtocolException, IOException, AuthenticationNotRequiredException {
			//("cheezypoofs","yjsmbnks");
			//Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
			//client.getState().setCredentials(new AuthScope("http://www.rombay.com", 80), defaultcreds);

			HttpPost loginPOST = new HttpPost("http://www.rombay.com/sign_in.html");
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("username_login", username));
			formparams.add(new BasicNameValuePair("password", password));
			formparams.add(new BasicNameValuePair("login", "Sign+In%21"));
			formparams.add(new BasicNameValuePair("a", "login2"));
			formparams.add(new BasicNameValuePair("return_url", null));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, HTTP.DEFAULT_CONTENT_CHARSET);
			loginPOST.setEntity(entity);

			HttpResponse res = client.execute(loginPOST);
			HttpEntity ent = res.getEntity();
	        List<Cookie> cookies = client.getCookieStore().getCookies();
	        GlobalVars.cleanClient();
	        return isAuthenticated();
		}

		@Override
		public ArrayList<Console> getAvailableConsoles(Context context) {
			ArrayList<Console> al = new ArrayList<Console>();
			al.add(Console.ALL);
			al.add(Console.GAMEBOY);
			al.add(Console.NES);
			al.add(Console.SNES);
			al.add(Console.GENESIS);
			al.add(Console.MASTERSYSTEM);
			al.add(Console.GAMEGEAR);
			al.add(Console.N64);
			return al;
		}

		@Override
		public int getLogoDrawableID() {
			return R.drawable.rombay_logo;
		}

		@Override
		public boolean isSearchSupported() {
			return true;
		}

		@Override
		public boolean isAuthenticated() throws AuthenticationNotRequiredException {
			List<Cookie> cookies = client.getCookieStore().getCookies();
			for(Cookie c: cookies){
				Log.v(TAG, c.getName() + " " + c.getValue());
				if(c.getName().equals("session")){
					return true;
				}
			}
	        return false;
		}


		@Override
		public boolean isPasswordNeededDuringRegistration() {
			return false;
		}


		@Override
		public ArrayList<Category> getAvailableCategories() {
			ArrayList<Category> ret = new ArrayList<Category>();
			for(Category cat : Category.values()){
				ret.add(cat);
			}
			ret.remove(Category.NUMBERS);
			ret.remove(Category.BIOS);
			return ret;
		}


		@Override
		public boolean isCaptchaRequired() {
			return true;
		}


		@Override
		public EULAcapsule getEULAcapsule(Activity activity) {
			EULAcapsule e = new EULAcapsule(activity,"Rombay Terms of Use","Accept","Decline","eula_rombay",R.string.eula_rombay);
			return e;
		}


		@Override
		public String getHost() {
			return "www.rombay.com";
		}


		@Override
		public boolean isDirectDownload() {
			return true;
		}


}
