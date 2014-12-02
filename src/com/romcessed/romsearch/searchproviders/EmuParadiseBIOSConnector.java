package com.romcessed.romsearch.searchproviders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import android.widget.ProgressBar;
import android.widget.RemoteViews;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.EULAcapsule;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.activities.RomDetailActivity;
import com.romcessed.romsearch.tools.GlobalVars;
import com.romcessed.romsearch.tools.NostalgiaTools;
import com.romcessed.romsearch.tools.ZipTools;

public class EmuParadiseBIOSConnector extends AbstractConnector {

	
	
	public EmuParadiseBIOSConnector() {
		super();
		TAG="EmuParadiseConnector";
	}

	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> browse(Context context, Category category, Console console, int page)throws ExecutionException, InterruptedException {
		return new BrowseForRomsTask(context, category, console, page);
	}
	
	@Override
	public AsyncTask<String, String, ArrayList<SearchResult>> search(Context context, String query, Console console)throws ExecutionException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class DownloadROMTask extends AsyncTask<String, String, Uri> {
		
		private final Context context;
		private ProgressBar progressBar;
		private SearchResult searchResult;
		private NotificationManager mNotificationManager;
		private Notification notification;
		private RemoteViews contentView;
		private String captcha;
		
		int fileSize;
		private File saveDirectory;
		private String fileName;
		ArrayList<String> zipEntryDump;
		int kilobytesDownloaded;
		
		private int romIdentifier;
		
		
		public DownloadROMTask(Context context, SearchResult searchResult, String captcha){
			super();
			this.context = context;
			this.searchResult = searchResult;
			this.captcha = captcha;
		}
		
	     @Override
		protected void onPreExecute() {
	    	 
			Intent i = new Intent(context, RomDetailActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setClass(context, RomDetailActivity.class);
			i.putExtra(RomDetailActivity.BUNDLE_TAG, (Serializable) searchResult);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			
			contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
			    
			fileSize = Integer.MAX_VALUE;
			contentView.setProgressBar(R.id.stat_progress, fileSize, kilobytesDownloaded, true);
			
			contentView.setImageViewResource(R.id.stat_icon, searchResult.getConsole().getDrawable());
			contentView.setTextViewText(R.id.stat_text, "Starting download...");
			notification = new Notification();
			notification.icon = R.drawable.download_icon_levels;
			notification.iconLevel = 0;
			notification.flags = notification.FLAG_ONGOING_EVENT;
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			
			romIdentifier = searchResult.getURLSuffix().length() * fileSize;
			
			mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(romIdentifier, notification);
		}
	
	
		protected void onProgressUpdate(String... progress) {
			if(progress!=null){
				contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + " " + progress[0]);
			}
			
			if (notification.iconLevel < 6) {
				notification.iconLevel++;
			} else {
				notification.iconLevel = 0;
			}
	
			if(kilobytesDownloaded < fileSize){
	        	 contentView.setProgressBar(R.id.stat_progress, fileSize, kilobytesDownloaded, false);
	         } else {
	        	 contentView.setProgressBar(R.id.stat_progress, fileSize, kilobytesDownloaded, true);
	         }
			
			mNotificationManager.notify(romIdentifier, notification);
			
	     }
		
		@Override
	     protected void onPostExecute(Uri result) {
			String romFileName = zipEntryDump.get(0); 
			contentView.setTextViewText(R.id.stat_text, "Extraction complete");
			contentView.setProgressBar(R.id.stat_progress, 100, 100, false);
			Intent i = new Intent();
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setClass(context, RomDetailActivity.class);
			i.putExtra(RomDetailActivity.BUNDLE_TAG, (Serializable) searchResult);
			i.putExtra("ROMFILE", saveDirectory.getAbsolutePath() + "/" + romFileName);
			i.putExtra("ROMID", romIdentifier);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			notification.contentIntent = contentIntent;
			notification.icon = R.drawable.done;
			notification.sound = NostalgiaTools.getRandomDownloadFinishedClipUri();
			mNotificationManager.notify(romIdentifier, notification);
	
	     }
		
	     
		@Override
		protected Uri doInBackground(String... params) {
			publishProgress("Downloading...");	
			try {
					HttpGet biosGET = new HttpGet("http://www.emuparadise.org/biosfiles/" + searchResult.getURLSuffix());

			HttpResponse manualResponse = client.execute(biosGET);
			
			
			fileSize = Integer.valueOf(manualResponse.getFirstHeader("Content-Length").getValue());
			fileSize = (fileSize / 1024);
			publishProgress("Downloading...");
			
			Pattern p = Pattern.compile("filename=\"(.+?)\"");

				fileName = searchResult.getURLSuffix();
				File cacheDir = context.getCacheDir();
				String tempFileForZip = cacheDir.getAbsolutePath() + "/" + fileName;
				FileOutputStream fos = new FileOutputStream(tempFileForZip);
		        
				InputStream instream = manualResponse.getEntity().getContent();
		        int l;
		        byte[] tmp = new byte[2048];
		        int updateCounter = 0;
		        int bytesDownloaded = 0;
		        while ((l = instream.read(tmp)) != -1) {
		            fos.write(tmp, 0, l);
		            bytesDownloaded+=2048;
		            updateCounter++;
		            if(updateCounter==3){
		            	kilobytesDownloaded=(bytesDownloaded / 1000);
		            	publishProgress((String[])null);
		            	updateCounter=0;
		            }
		        }
	
				
			String zipFilePath = tempFileForZip;
			
			//Change to indeterminate
			kilobytesDownloaded = fileSize;
			publishProgress("Extracting...");
			
			//TODO: Preferences for save directory
			saveDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVars.getSaveLocation(context, searchResult.getConsole()));
			zipEntryDump = new ArrayList<String>();
			ZipTools.unzipArchive(new File(zipFilePath), saveDirectory, zipEntryDump);
			
			
			manualResponse.getEntity().consumeContent();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
				
				}
	
			return Uri.fromFile(saveDirectory);
	 }
	
	}
	
	
	private class BrowseForRomsTask extends AsyncTask<String, String, ArrayList<SearchResult>> {

		private final Context context;
		private ProgressDialog progressDialog;
		private Console console;
		private Category category;
		private int page;
		
		final Pattern searchPattern = Pattern.compile("<td.+?href=\"(.+?)\"");
		
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
	        progressDialog.setTitle("Page 1 of " + console.getProperName() + category.toString());
	        progressDialog.setMessage("Retrieving...");
	        progressDialog.show();
		}


		protected void onProgressUpdate(String... progress) {
	         progressDialog.setMessage(progress[0]);
	     }

	     protected void onPostExecute(ArrayList<SearchResult> result) {
	    	 String resultString = (page==1) ? "Found ": "Added ";
	    	 progressDialog.setMessage(resultString + result.size() + "BIOSes");
	    	 progressDialog.dismiss();
	     }

		@Override
		protected ArrayList<SearchResult> doInBackground(String... params) {
			ArrayList<SearchResult> results = new ArrayList<SearchResult>();
				try {
					HttpGet browseGet = new HttpGet("http://www.emuparadise.org/biosfiles/bios.html");
				
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
				        	gameName = m.group(1);
				        	fileSize = "unknown";
				        	downloads = "unknown";
				        					        	
				        	currentItemTitle = gameName;
				        	publishProgress(currentItemTitle);
				        	
				        	result = new SearchResult(gameName, console, SearchProvider.ID_EMUPARADISE_BIOS);
				        	result.setFileSize(fileSize);
				        	result.setSubInfo1_Title("Downloads");
				        	result.setSubInfo1(downloads);
				        	result.setURLSuffix(urlSuffix);
				        	results.add(result);
				        	Thread.sleep(5);
				        }
					        
					
			        if(results.size()==0){
			        	results.add(new SearchResult("No results found",null, SearchProvider.ID_EMUPARADISE_BIOS));
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
	public Console getConsole(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConsoleString(Console c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLogoDrawableID() {
		return R.drawable.emuparadise_bios_logo;
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
		al.add(Console.BIOS);
		return al;
	}

	@Override
	public ArrayList<Category> getAvailableCategories() {
		ArrayList<Category> ret = new ArrayList<Category>();
		ret.add(Category.BIOS);
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
		return "www.emuparadise.org";
	}

	@Override
	public boolean isDirectDownload() {
		return true;
	}

}
