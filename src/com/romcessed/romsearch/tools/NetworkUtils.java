package com.romcessed.romsearch.tools;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Environment;

import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.searchproviders.Connector;

public class NetworkUtils {

	public static boolean downForEveryone(Connector connector){
		DefaultHttpClient client = GlobalVars.getUniversalClient();
		HttpGet get = new HttpGet("http://downforeveryoneorjustme.com/" + connector.getHost());
		String html = "";
		try {
		HttpResponse r = client.execute(get);
		html = EntityUtils.toString(r.getEntity());
			r.getEntity().consumeContent();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(html.contains("It's just you")){
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Example:
	<code>
	String ftpAddress = "173.192.225.183";</br>
	String retrieveFromFTPFolder = "/2/PSX-PAL/Metal Gear Solid (Demo) (E) [SLED-01400].7z";
	</code>
	 * @param ftpAddress
	 * @param retrieveFromFTPFolder
	 */

	public static String replaceSquareBrackets(String input){
		input = input.replaceAll("\\[", URLEncoder.encode("["));
		input = input.replaceAll("\\]", URLEncoder.encode("]"));
		return input;
	}

}
