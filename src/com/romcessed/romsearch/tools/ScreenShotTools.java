package com.romcessed.romsearch.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.R;

public class ScreenShotTools {

	public static final String BUNDLE_TAG = "BITMAPS";
	
	public static HashMap<Integer, TreeMap<String, String>> screenShotURLreference;
	public static DefaultHttpClient client;
	
	public ScreenShotTools(){
		client = GlobalVars.getUniversalClient();
	}
	
	public static void initializeScreenShotLibrary(Context c){
		//InputStream is;
			//is = c.getResources().openRawResource(R.raw.list);
			//ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(is));
		    //screenShotURLreference = (HashMap<Integer, TreeMap<String, String>>) ois.readObject();
		    //ois.close();
		client = GlobalVars.getUniversalClient();
		screenShotURLreference = getAllRomsFromInternet();
	}
	
	public static HashMap<Integer, TreeMap<String, String>> getAllRomsFromInternet(){
		String[] urls = null;
		HashMap<Integer, TreeMap<String, String>> allRoms = new HashMap<Integer, TreeMap<String,String>>();
		TreeMap<String, String> gameBoyCombined = new TreeMap<String, String>();
		urls = new String[]{"nes_b", "gb_b", "gba_b", "gbc_b", "genesis_b", "gg_b", "sms_b", "snes_b", "psx_b","n64_b"};
		for(String url : urls){
			int consoleOrdinal = getConsole(url).ordinal();
			if(url.equals("gb_b") || url.equals("gbc_b")){
				gameBoyCombined.putAll(parseOneConsoleList(url));
			} else {
				allRoms.put(getConsole(url).ordinal(), parseOneConsoleList(url));
			}
		}
		allRoms.put(Console.GAMEBOY.ordinal(), gameBoyCombined);
		return allRoms;
	}
	
	private static TreeMap<String, String> parseOneConsoleList(final String urlPart){
		URL fullURL = null;
		final TreeMap<String, String> returnTreeMap = new TreeMap<String, String>();
		try {
			fullURL = new URL("http://www.vgmuseum.com/" + urlPart + ".html");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpGet get = new HttpGet(fullURL.toString());
		HttpResponse cResponse = null;
		
		String pageContent = null;
		try {
			cResponse = client.execute(get);
			pageContent = EntityUtils.toString(cResponse.getEntity());
			cResponse.getEntity().consumeContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
    	final Pattern searchPattern = Pattern.compile("<li><a\\s.+?\"(.+?)\"((?=>)>(.+?)<|.+?>(.+?)<)");
    	Matcher m = searchPattern.matcher(pageContent);
        while(m.find()){
        	String RomName;
        	RomName = (m.group(4)!=null) ? m.group(4) : m.group(3);
        	String RomScreenShotURL = m.group(1);
        	String value;
        	value = "http://www.vgmuseum.com/" + RomScreenShotURL;
        	returnTreeMap.put(cleanTitle(RomName), value);
        }

        
		return returnTreeMap;
		
	}
	
	private static boolean matchRomTitles(String s1, String s2){
		//AbstractStringMetric Smetric = new Soundex();
		AbstractStringMetric Qmetric = new QGramsDistance();
		//float Ssimilarity = Smetric.getSimilarity(s1, s2);
		
		s1 = cleanTitle(s1);
		s2 = cleanTitle(s2);
		
		float Qsimilarity = Qmetric.getSimilarity(s1, s2);
		if(s1.contains(s2)){
			if(Qsimilarity >= .86){
				return true;
			}
		} else {
			if(Qsimilarity >= .88){
				return true;
			}
		}
		return false;
	}
	
	public static String findMatchingKey(Console console, String romTitle) throws NoRomTitleMatchFoundException, ScreenShotLibraryNotLoadedException{
		TreeMap<String, String> consoleSpecificTreeMap;
		try{
			consoleSpecificTreeMap = screenShotURLreference.get(console.ordinal());
		} catch (NullPointerException e){
			throw new ScreenShotLibraryNotLoadedException();
		}
		char firstLetter = romTitle.substring(0, 1).charAt(0);
		char nextLetter = firstLetter; nextLetter++;
		int howManyChar = romTitle.length()>=4 ? 4 : 2;
		String firstFive = romTitle.substring(0, 4).toLowerCase();
		SortedMap<String, String> mapStartingAtFirstLetter = consoleSpecificTreeMap.subMap(firstFive, String.valueOf(nextLetter).toLowerCase());
		Set<String> romTitles = mapStartingAtFirstLetter.keySet();
		for(String romT : romTitles){
			if(matchRomTitles(romT, romTitle)){
				return romT;
			}
		}
		throw new NoRomTitleMatchFoundException();
	}
	
	public static Console getConsole(String s){
		Console console = null;
		if(s.equals("gb_b") || s.equals("gbc_b")){
			console = Console.GAMEBOY;
		} else if(s.equals("nes_b")){
			console = Console.NES;
		}else if(s.equals("snes_b")){
			console = Console.SNES;
		} else if(s.equals("gba_b")){
			console = Console.GAMEBOYADVANCE;
		} else if(s.equals("genesis_b")){
			console = Console.GENESIS;
		} else if(s.equals("sms_b")){
			console = Console.MASTERSYSTEM;
		} else if(s.equals("gg_b")){
			console = Console.GAMEGEAR;
		} else if(s.equals("psx_b")){
			console = Console.PSX;
		} else if(s.equals("n64_b")){
			console = Console.N64;
		} else {
			console = Console.UNKNOWN;
		}

		return console;
	}
	
	public Bitmap getScreenshot(String url) throws ClientProtocolException, IOException{
		Bitmap bmImg;
		
		HttpGet get = new HttpGet(url);
		ResponseHandler<Bitmap> handler = new ResponseHandler<Bitmap>() {
		    public Bitmap handleResponse(
		            HttpResponse response) throws ClientProtocolException, IOException {
		    	return BitmapFactory.decodeStream(response.getEntity().getContent());
		    }
		};
		
		bmImg = client.execute(get, handler);
		return bmImg;
	}
	
	
	public static ArrayList<Bitmap> getScreenshots(String romTitle, Console console, Context c){
		ArrayList<Bitmap> returnAl = new ArrayList<Bitmap>();
		ScreenShotTools sst = new ScreenShotTools();
		String matchingKey;
		try {
			matchingKey = findMatchingKey(console, romTitle);
		} catch (NoRomTitleMatchFoundException e) {
			Resources res = c.getResources();
			returnAl.add(BitmapFactory.decodeResource(res, R.drawable.no_screen_shot));
			return returnAl;
		} catch (ScreenShotLibraryNotLoadedException e){
			Resources res = c.getResources();
			returnAl.add(BitmapFactory.decodeResource(res, R.drawable.no_screen_shot));
			return returnAl;
		}
		
		String foundRomPageURL = screenShotURLreference.get(console.ordinal()).get(matchingKey);
		
		HttpGet get = new HttpGet(foundRomPageURL);
		ResponseHandler<String> handler = new ResponseHandler<String>() {
		    public String handleResponse(
		            HttpResponse response) throws ClientProtocolException, IOException {
		        HttpEntity entity = response.getEntity();
		    	String html;
		    	if (entity != null) {
		        	html=EntityUtils.toString(entity);
		        	entity.consumeContent();
		    		return html;
		        } else {
		            return null;
		        }
		    }
		};
		
		String html = null;
		try {
			html = sst.client.execute(get, handler);
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String foundRomPageURLRoot = foundRomPageURL.substring(0, foundRomPageURL.lastIndexOf("/") + 1);
		
		Pattern p = Pattern.compile("<\\s*img [^\\>]*src\\s*=\\s*([\"\\\'])(.*?)\\1", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		int numScreenshots = 0;
		 while(m.find()){
	        if(numScreenshots==3){break;}	
			 String RomScreenShotImageURL = m.group(2);
	        	RomScreenShotImageURL = foundRomPageURLRoot + RomScreenShotImageURL;
	        	try {
					returnAl.add(sst.getScreenshot(RomScreenShotImageURL));
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				numScreenshots++;
	        }
		Log.v("ScreenShotTools", "Returning screenshots");
		return returnAl;
	}
	
	public static class NoRomTitleMatchFoundException extends Exception {

		@Override
		public String getMessage() {
			return "This rom was not found in the ROM Screenshot URL HashMap";
		}
		
	}
	
	public static class ScreenShotLibraryNotLoadedException extends Exception {

		@Override
		public String getMessage() {
			return "The screenshot URL Library hasn't finished loading";
		}
		
	}

	public static String cleanTitle(String nonStrictlyAlphaNumericTitle){
		Pattern searchPattern = Pattern.compile("[^\\w\\s']");
		Matcher m = searchPattern.matcher(nonStrictlyAlphaNumericTitle);
		String returnString = m.replaceAll(" ");
		returnString = returnString.toLowerCase().replace("the", "");
		returnString = returnString.replaceAll("\\s+", " ").trim();
		return returnString;
	}
	
	
}
