package com.romcessed.romsearch;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.romcessed.romsearch.activities.RomDetailActivity;
import com.romcessed.romsearch.searchproviders.DopeRomsConnector.myRI;
import com.romcessed.romsearch.tools.GlobalVars;
import com.romcessed.romsearch.tools.NetworkUtils;
import com.romcessed.romsearch.tools.NostalgiaTools;
import com.romcessed.romsearch.tools.ScreenShotTools;
import com.romcessed.romsearch.tools.ZipTools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

public class DownloadService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return comBinder;
	}
	
	private final ServiceCom.Stub comBinder = new ServiceCom.Stub(){

		@Override
		public boolean downloadHttp(SearchResult searchResult, int searchProviderID, String captcha) throws RemoteException {
			downloadHttpUsingSearchProvider(searchProviderID, searchResult, captcha);
			return true;
		}

		@Override
		public boolean downloadFtp(int searchProviderID, String completePathToFile, SearchResult searchResult) throws RemoteException {
			downloadFtpUsingSearchProvider(searchProviderID, searchResult, completePathToFile);
			return true;
		}
		
	};
	public RemoteViews contentView;
	public Notification notification;
	public NotificationManager mNotificationManager;

    @Override
    public void onStart(Intent intent, int startId) {
          super.onStart(intent, startId);
    }

    private String safeFileName(String input){
    	return input.replaceAll("[^\\w\\d]", "_");
    }

    private void downloadFtpUsingSearchProvider(int searchProviderID, SearchResult searchResult, String completePathToFile){
		AsyncTask<String, String, Uri> downloader;
		switch(searchProviderID){
		case SearchProvider.ID_EMUPARADISE:
			downloader = new EMUPARADISEDownloadROMTask(this, searchResult, completePathToFile);
			break;
		default:
			downloader = null;
			throw new IllegalStateException("Invalid searchProviderID or SearchProviderID doesn't support FTP");
		}
		downloader.execute("");
	}


    
    

	private void downloadHttpUsingSearchProvider(int searchProviderID, SearchResult searchResult, String captcha){
		AsyncTask<String, String, Uri> downloader = null;
		switch(searchProviderID){
		case SearchProvider.ID_ROMBAY:
			downloader = new ROMBAYDownloadROMTask(this, searchResult, captcha);
			break;
		case SearchProvider.ID_EMUPARADISE_BIOS:
			downloader = new EMUPARADISE_BIOSDownloadROMTask(this, searchResult, captcha);
			break;
		case SearchProvider.ID_ROMHACKING:
			downloader = new ROMHACKING_DownloadROMTask(this, searchResult, captcha);
			break;
		case SearchProvider.ID_DOPEROMS:
			
			break;
		default:
			throw new IllegalStateException("Invalid searchProviderID or SearchProviderID doesn't support HTTP");
		}
			//RomDetailActivity.this, mRom, et_CaptchaEntry.getText().toString());
		downloader.execute("");
	}

	
	
	
private class EMUPARADISEDownloadROMTask extends AsyncTask<String, String, Uri> {
		
		private final Context context;
		private SearchResult searchResult;
		
		private String completePathToFile;
		
		int fileSize;
		private File saveDirectory;
		private String fileName;
		ArrayList<String> zipEntryDump;
		int kilobytesDownloaded;
		
		private int romIdentifier;
		private boolean extracted;
		private String zipFilePath;
		private FTPClient FTPclient;
		
		
		public EMUPARADISEDownloadROMTask(Context context, SearchResult searchResult, String completePathToFile){
			super();
			this.context = context;
			this.searchResult = searchResult;
			this.completePathToFile = completePathToFile;
		}
		
	     @Override
		protected void onPreExecute() {
	    	 
			Intent i = new Intent(context, RomDetailActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setClass(context, RomDetailActivity.class);
			i.putExtra(RomDetailActivity.BUNDLE_TAG, (Serializable) searchResult);

			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			
			contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
			    
			fileSize = Integer.valueOf(searchResult.getFileSize());
			contentView.setProgressBar(R.id.stat_progress, fileSize, kilobytesDownloaded, true);
			
			contentView.setImageViewResource(R.id.stat_icon, searchResult.getConsole().getDrawable());
			contentView.setTextViewText(R.id.stat_text, "Starting download...");
			notification = new Notification();
			notification.icon = R.drawable.download_icon_levels;
			notification.iconLevel = 0;
			notification.flags = notification.FLAG_ONGOING_EVENT;
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			
			romIdentifier = (searchResult.getURLSuffix().length() + fileSize) * ((fileSize % 2) + 1) * 3;
			
			mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(romIdentifier, notification);
		}
	
	
		protected void onProgressUpdate(String... progress) {
			if(progress!=null){
				contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + "\n" + progress[0] + (((double)((double) kilobytesDownloaded / fileSize))* 100) + "%");
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
			if(result==null){
				return;
			}
			String romFileName = extracted ? zipEntryDump.get(0) : fileName; 
			contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + "\n"+ (extracted ? "Extraction complete" : "Download complete"));
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
			notification.flags = notification.FLAG_AUTO_CANCEL;
			notification.sound = NostalgiaTools.getRandomDownloadFinishedClipUri();
			mNotificationManager.notify(romIdentifier, notification);
	
	     }
		
	     
		@Override
		protected synchronized Uri doInBackground(String... params) {
			publishProgress("Downloading...");	
			
			
			 String userName="anonymous";
			  String passWord = "hello@hello.com";
			  
			Pattern p = Pattern.compile("ftp://(.+?)(/.+/(.+))");
			Matcher m = p.matcher(completePathToFile);
			m.find();

			  String hostPart = m.group(1);
			  String port = null;
			  if(hostPart.contains(":")){
				  port = hostPart.substring(hostPart.indexOf(":") + 1);
				  hostPart = hostPart.substring(0, hostPart.indexOf(":"));
			  }
			  String pathExcludingHostIncludingFirstSlash= URLDecoder.decode(m.group(2));
			  fileName = URLDecoder.decode(m.group(3));

			  FileOutputStream fos = null;
			  java.io.BufferedOutputStream bout = null;
			  InputStream myFileStream = null;
			  String tempFileForZip = Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVars.getSaveLocation(context, searchResult.getConsole()) + fileName;
			  int retries = 0;
			  
		try {
			   FTPclient = new FTPClient();

			  while (myFileStream==null && retries < 30){
			  try{
				  
				boolean mkdirresult = (new File(tempFileForZip)).getParentFile().mkdirs();
				fos = new FileOutputStream(new File(tempFileForZip));
				bout = new BufferedOutputStream(fos,1024);
				
				   FTPclient.connect(hostPart, port==null ? 21 : Integer.valueOf(port));
				   FTPclient.setListHiddenFiles(true);
				   FTPclient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
				   FTPclient.setFileType(FTP.BINARY_FILE_TYPE);
				   FTPclient.setFileType(2);
				   FTPclient.pasv();
				   
				   boolean loggedIn = FTPclient.login(userName, passWord);
				   	myFileStream = FTPclient.retrieveFileStream(pathExcludingHostIncludingFirstSlash);
				   	DataInputStream instream = new DataInputStream(myFileStream);
				   	
			  } catch (Exception e){
					retries++;
					//FTPclient.getReply();
					//Log.v("DownloadService", FTPclient.getReplyStrings().toString());
					//Log.v("DownloadService", FTPclient.getStatus());
					publishProgress("Error["+ e.getMessage() + "]" + "[" + retries +  "]...");	
					if(FTPclient.isConnected()){
						//FTPclient.disconnect();
						}
					if(fos!=null){fos.close();}
					 if(bout!=null){bout.close();}
					 if(myFileStream!=null){myFileStream.close();}
			  } finally {}
			  Thread.sleep(1000);
			  }
			  
			  if(myFileStream==null){
				  throw new RuntimeException("Could not connect, try again");
			  }
			
		        int updateCounter = 0;
		        int kbDownloaded = 0;
			      byte data[] = new byte[1024];
			      int x = 0;
			      while((x=myFileStream.read(data,0,1024))>=0){
			    	  bout.write(data,0,x);
			    	  bout.flush();
			    	  kbDownloaded++;
			    	  updateCounter++;
			    	  if(updateCounter==16){
		            	kilobytesDownloaded=kbDownloaded;
		            	publishProgress("Downloading...");
		            	updateCounter=0;
		            	Thread.yield();
		            }
			    	  //Thread.sleep(50);
			      }
			      
			      bout.close();
			      myFileStream.close();
			      
			      publishProgress("Completing Download...");
			      boolean complete = FTPclient.completePendingCommand();
			      if(!complete){
			    	  FTPclient.logout();
			    	  FTPclient.disconnect();
			          Log.e("DownloadService","File transfer failed.");
			      }
				

			      
			zipFilePath = tempFileForZip;
			
			//Change to indeterminate
			//Change to indeterminate
			kilobytesDownloaded = fileSize;
			saveDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVars.getSaveLocation(context, searchResult.getConsole()));
			if(fileName.endsWith(".zip") && fileSize < 2048){
				publishProgress("Extracting...");
				
				//TODO: Preferences for save directory
				zipEntryDump = new ArrayList<String>();
				ZipTools.unzipArchive(new File(zipFilePath), saveDirectory, zipEntryDump);
				boolean deleted = new File(tempFileForZip).delete();
				extracted=true;
			}

				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (Exception e) {
					setFailureNotification(romIdentifier, searchResult, e);
					   if (FTPclient.isConnected()) {
						    try {
						     FTPclient.logout();  
						     FTPclient.disconnect();  
						    } catch (IOException f) {
						     // do nothing
						    }
						   }
					   return null;
						  }
						  finally{
						   if (FTPclient.isConnected())
						            {
						                try
						                {
						                 FTPclient.logout();
						                 FTPclient.disconnect();
						                }
						                catch (IOException f)
						                {
						                }
						            }
						  } 
	
			return Uri.fromFile(saveDirectory);
	 }
	
	}
	
private void setFailureNotification(int romIdentifier, SearchResult searchResult, Exception e){
	notification.icon = R.drawable.x;
	contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + " failed:\n"+ e.getMessage());
	contentView.setProgressBar(R.id.stat_progress, 100, 0, false);
	notification.contentView = contentView;
	notification.sound = NostalgiaTools.getSoundURI(R.raw.linkdies);
	notification.flags = notification.FLAG_AUTO_CANCEL;
	mNotificationManager.notify(romIdentifier, notification);
}
	
	private class EMUPARADISE_BIOSDownloadROMTask extends AsyncTask<String, String, Uri> {
		
		private final Context context;
		private ProgressBar progressBar;
		private SearchResult searchResult;
		private String captcha;
		
		int fileSize;
		private File saveDirectory;
		private String fileName;
		ArrayList<String> zipEntryDump;
		int kilobytesDownloaded;
		
		private int romIdentifier;
		private DefaultHttpClient client;
		private boolean extracted;
		private String zipFilePath;
		
		
		public EMUPARADISE_BIOSDownloadROMTask(Context context, SearchResult searchResult, String captcha){
			super();
			this.context = context;
			this.searchResult = searchResult;
			this.captcha = captcha;
			client = GlobalVars.getUniversalClient();
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
			
			romIdentifier = (searchResult.getURLSuffix().length() + fileSize) * ((fileSize % 2) + 1) * 3;
			
			mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(romIdentifier, notification);
		}
	
	
		protected void onProgressUpdate(String... progress) {
			if(progress!=null){
				contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + "\n" + progress[0] + ((kilobytesDownloaded / fileSize)* 100) + "%");
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
			String romFileName = extracted ? zipEntryDump.get(0) : fileName; 
			contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + "\n"+ (extracted ? "Extraction complete" : "Download complete"));
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
			notification.flags = notification.FLAG_AUTO_CANCEL;
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
				String tempFileForZip = Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVars.getSaveLocation(context, searchResult.getConsole()) + fileName;
				boolean mkdirresult = (new File(tempFileForZip)).getParentFile().mkdirs();
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
		            	publishProgress("Downloading...");
		            	updateCounter=0;
		            	Thread.yield();
		            }
		            
		        }
	
				
			zipFilePath = tempFileForZip;
			
			//Change to indeterminate
			//Change to indeterminate
			kilobytesDownloaded = fileSize;
			saveDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVars.getSaveLocation(context, searchResult.getConsole()));
			if(fileName.endsWith(".zip")){
				publishProgress("Extracting...");
				
				//TODO: Preferences for save directory
				zipEntryDump = new ArrayList<String>();
				ZipTools.unzipArchive(new File(zipFilePath), saveDirectory, zipEntryDump);
				boolean deleted = new File(tempFileForZip).delete();
				extracted=true;
			}
			manualResponse.getEntity().consumeContent();
				} catch (Exception e) {
					setFailureNotification(romIdentifier, searchResult, e);
				} finally {
				
				}
	
			return Uri.fromFile(saveDirectory);
	 }
	
	}
	
	
	
	
	
	private class ROMBAYDownloadROMTask extends AsyncTask<String, String, Uri> {

		private final Context context;
		private ProgressBar progressBar;
		private SearchResult searchResult;
		private String captcha;
		private boolean extracted = false;
		
		private DefaultHttpClient client;
		
		int fileSize;
		private File saveDirectory;
		private String fileName;
		ArrayList<String> zipEntryDump;
		int kilobytesDownloaded;
		
		final Pattern searchPattern = Pattern.compile("<td class=\"td\\w+?\"><a href=\"(\\S+)\">(.+)</a>" +
				"</td>[\r\n]+.+<t.+?>(.+)</td>[\r\n]+.+?<t.+?>(.+)</td>[\r\n]+.+<t.+?>(.+)</td>");
		private int romIdentifier;
		private String zipFilePath;
		
		
		public ROMBAYDownloadROMTask(Context context, SearchResult searchResult, String captcha){
			super();
			this.context = context;
			this.searchResult = searchResult;
			this.captcha = captcha;
			client = GlobalVars.getUniversalClient();
		}
		
	     @Override
		protected void onPreExecute() {
	    	 
			Intent i = new Intent(context, RomDetailActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setClass(context, RomDetailActivity.class);
			i.putExtra(RomDetailActivity.BUNDLE_TAG, (Serializable) searchResult);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			
			contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
			    
			fileSize = Integer.valueOf(searchResult.getFileSize().replaceAll("\\D", ""));
			contentView.setProgressBar(R.id.stat_progress, fileSize, 0, false);
			
			contentView.setImageViewResource(R.id.stat_icon, searchResult.getConsole().getDrawable());
			contentView.setTextViewText(R.id.stat_text, "Starting download...");
			notification = new Notification();
			notification.icon = R.drawable.download_icon_levels;
			notification.iconLevel = 0;
			notification.flags = notification.FLAG_ONGOING_EVENT;
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			
			romIdentifier = (searchResult.getURLSuffix().length() + fileSize) * ((fileSize % 2) + 1) * 3;
			
			mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(romIdentifier, notification);
		}


		protected void onProgressUpdate(String... progress) {
			if(progress!=null){
				contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + " " + "\n" + progress[0] + ((kilobytesDownloaded / fileSize)* 100) + "%");
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
			if(result==null){
				return;
			}
			String romFileName = extracted ? zipEntryDump.get(0) : fileName; 
			contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + "\n"+ (extracted ? "Extraction complete" : "Download complete"));
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
			notification.flags = notification.FLAG_AUTO_CANCEL;
			notification.sound = NostalgiaTools.getRandomDownloadFinishedClipUri();
			mNotificationManager.notify(romIdentifier, notification);

	     }
		
	     
		@Override
		protected Uri doInBackground(String... params) {
			publishProgress("Downloading...");	
			try {
					HttpPost searchPOST = new HttpPost("http://www.rombay.com/" + searchResult.getURLSuffix());
					List<NameValuePair> formparams = new ArrayList<NameValuePair>();
					formparams.add(new BasicNameValuePair("number", captcha));
					formparams.add(new BasicNameValuePair("Download", "Download%21"));
					formparams.add(new BasicNameValuePair("a", "download2"));
					UrlEncodedFormEntity paramsEntity = new UrlEncodedFormEntity(formparams, HTTP.UTF_8);
					searchPOST.setEntity(paramsEntity);


			HttpResponse manualResponse = client.execute(searchPOST);
			
			
			Header fileNameHeader = manualResponse.getFirstHeader("Content-Disposition");
			Pattern p = Pattern.compile("filename=\"(.+?)\"");
			if(fileNameHeader==null){
				throw new Exception("Invalid captcha");
			}
			Matcher m = p.matcher(fileNameHeader.getValue());

			if (m.find()) {
				fileName = m.group(1);
				String tempFileForZip = Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVars.getSaveLocation(context, searchResult.getConsole()) + fileName;
				boolean mkdirresult = (new File(tempFileForZip)).getParentFile().mkdirs();
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
		            	publishProgress("Downloading...");
		            	updateCounter=0;
		            	Thread.yield();
		            }
		        }

				
				zipFilePath = tempFileForZip;
				
				//Change to indeterminate
				kilobytesDownloaded = fileSize;
				saveDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVars.getSaveLocation(context, searchResult.getConsole()));
				if(fileName.endsWith(".zip")){
					publishProgress("Extracting...");
					
					//TODO: Preferences for save directory
					zipEntryDump = new ArrayList<String>();
					ZipTools.unzipArchive(new File(zipFilePath), saveDirectory, zipEntryDump);
					boolean deleted = new File(tempFileForZip).delete();
					extracted=true;
				}
			}
			manualResponse.getEntity().consumeContent();
			GlobalVars.cleanClient();
				} catch (Exception e){
					setFailureNotification(romIdentifier, searchResult, e);
					return null;
				} finally {
				
				}

			return Uri.fromFile(saveDirectory);
	 }
	
	}
	
	private class ROMHACKING_DownloadROMTask extends AsyncTask<String, String, Uri> {
		
		private final Context context;
		private ProgressBar progressBar;
		private SearchResult searchResult;
		private String captcha;
		
		double fileSize;
		private File saveDirectory;
		private String fileName;
		ArrayList<String> zipEntryDump;
		int kilobytesDownloaded;
		
		private int romIdentifier;
		private DefaultHttpClient client;
		private boolean extracted;
		private String zipFilePath;
		
		
		public ROMHACKING_DownloadROMTask(Context context, SearchResult searchResult, String captcha){
			super();
			this.context = context;
			this.searchResult = searchResult;
			this.captcha = captcha;
			client = GlobalVars.getUniversalClient();
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
			contentView.setProgressBar(R.id.stat_progress, (int) fileSize, kilobytesDownloaded, true);
			
			contentView.setImageViewResource(R.id.stat_icon, searchResult.getConsole().getDrawable());
			contentView.setTextViewText(R.id.stat_text, "Starting download...");
			notification = new Notification();
			notification.icon = R.drawable.download_icon_levels;
			notification.iconLevel = 0;
			notification.flags = notification.FLAG_ONGOING_EVENT;
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			
			romIdentifier = (int) ((searchResult.getURLSuffix().length() + fileSize) * ((fileSize % 2) + 1) * 3);
			
			mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(romIdentifier, notification);
		}
	
	
		protected void onProgressUpdate(String... progress) {
			if(progress!=null){
				contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + "\n" + progress[0] + ((kilobytesDownloaded / fileSize)* 100) + "%");
			}
			
			if (notification.iconLevel < 6) {
				notification.iconLevel++;
			} else {
				notification.iconLevel = 0;
			}
	
			if(kilobytesDownloaded < fileSize){
	        	 contentView.setProgressBar(R.id.stat_progress, (int) fileSize, kilobytesDownloaded, false);
	         } else {
	        	 contentView.setProgressBar(R.id.stat_progress, (int) fileSize, kilobytesDownloaded, true);
	         }
			
			mNotificationManager.notify(romIdentifier, notification);
			
	     }
		
		@Override
	     protected void onPostExecute(Uri result) {
			String romFileName = extracted ? zipEntryDump.get(0) : fileName; 
			contentView.setTextViewText(R.id.stat_text, searchResult.getTitle() + "\n"+ (extracted ? "Extraction complete" : "Download complete"));
			contentView.setProgressBar(R.id.stat_progress, 100, 100, false);
			Intent i = new Intent();
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setClass(context, RomDetailActivity.class);
			i.putExtra(RomDetailActivity.BUNDLE_TAG, (Serializable) searchResult);
			i.putExtra("ROMID", romIdentifier);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			notification.contentIntent = contentIntent;
			notification.icon = R.drawable.done;
			notification.flags = notification.FLAG_AUTO_CANCEL;
			notification.sound = NostalgiaTools.getRandomDownloadFinishedClipUri();
			mNotificationManager.notify(romIdentifier, notification);
	
	     }
		
	     
		@Override
		protected Uri doInBackground(String... params) {
			String referer_url = null;
			
			publishProgress("Downloading...");	
			try {
			//TODO move this to Download Handler so it happens when click download, not search results
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
			
				Pattern searchPattern = Pattern.compile("<td><a hre.+?</td>.+?<td>.+?</.+?<t.+?td>.+?td><a href=\"(.+?)\"", Pattern.DOTALL);
				String url = searchResult.getURLSuffix();
				HttpGet searchGET = new HttpGet(url);
				String html = client.execute(searchGET, handler);
				
				referer_url = url;
        	
				Matcher m = searchPattern.matcher(html);
				
				m.find();
				String url_suffix = m.group(1).replace(" ", "%20");
				searchResult.setURLSuffix(url_suffix);
				
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			try {
				HttpGet downloadLinkGET = new HttpGet(NetworkUtils.replaceSquareBrackets(searchResult.getURLSuffix()));
				downloadLinkGET.addHeader("Referer",referer_url);
				HttpResponse manualResponse = client.execute(downloadLinkGET);
			
			
			fileSize = Integer.valueOf(manualResponse.getFirstHeader("Content-Length").getValue());
			fileSize = Math.ceil((fileSize / 1024));
			publishProgress("Downloading...");
			
			Pattern p = Pattern.compile("filename=\"(.+?)\"");

				fileName = searchResult.getURLSuffix();
				fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
				
				String tempFileForZip = Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVars.getSaveLocation(context, searchResult.getConsole()) + safeFileName(searchResult.getTitle()) + "/" + fileName;
				boolean mkdirresult = (new File(tempFileForZip)).getParentFile().mkdirs();
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
		            	kilobytesDownloaded=(bytesDownloaded / 1024);
		            	publishProgress("Downloading...");
		            	updateCounter=0;
		            	Thread.yield();
		            }
		        }
	
				
			zipFilePath = tempFileForZip;
			
			//Change to indeterminate
			//Change to indeterminate
			kilobytesDownloaded = (int) fileSize;
			saveDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVars.getSaveLocation(context, searchResult.getConsole()) + safeFileName(searchResult.getTitle()));
			if(fileName.endsWith(".zip")){
				publishProgress("Extracting...");
				
				//TODO: Preferences for save directory
				zipEntryDump = new ArrayList<String>();
				ZipTools.unzipArchive(new File(zipFilePath), saveDirectory, zipEntryDump);
				boolean deleted = new File(tempFileForZip).delete();
				extracted=true;
			}
			manualResponse.getEntity().consumeContent();
				} catch (Exception e) {
					setFailureNotification(romIdentifier, searchResult, e);
				} finally {
				
				}
	
			return Uri.fromFile(saveDirectory);
	 }
	
	}
	
	
}
