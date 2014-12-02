package com.romcessed.romsearch;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchResult implements Serializable, Parcelable{
	
	/**
	 * 
	 */
	
	private String title;
	private String fileSize;
	private String subInfo1;
	private String URLSuffix;
	private Console console;
	private String cantDownloadMessage;
	private String webViewURL;
	private int ConnectorID;
	private String subInfo1_title; 
	
	public String getSubInfo1_Title(){
		return subInfo1_title;
	}
	
	public void setSubInfo1_Title(String newSub1_title){
		subInfo1_title = newSub1_title;
	}
	
	public String getTitle() {
		return title;
	}
	public String getFileSize() {
		return fileSize;
	}
	public String getSubInfo1() {
		return subInfo1;
	}
	public String getURLSuffix() {
		return URLSuffix;
	}

	public Console getConsole() {
		return console;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	public void setSubInfo1(String subInfo1) {
		this.subInfo1 = subInfo1;
	}
	public void setURLSuffix(String uRLSuffix) {
		URLSuffix = uRLSuffix;
	}
	public SearchResult(String title, Console console, int connectorID){
		this.title = title;
		this.console = console;
		this.ConnectorID = connectorID;
	}
	
	public SearchResult(SearchResult fromRom) {
		this.title = fromRom.title;
		this.subInfo1 = fromRom.subInfo1;
		this.subInfo1_title = fromRom.subInfo1_title;
		this.fileSize = fromRom.fileSize;
		this.URLSuffix = fromRom.URLSuffix;
		this.console = fromRom.console;
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof SearchResult)){
			return false;
		}
		SearchResult other = (SearchResult) o;
		return URLSuffix.equals(other.URLSuffix);
	}
	
	@Override
	public int hashCode() {
		return 31 * getURLSuffix().hashCode();
	}
	
	
	public String getCountryCode(){
		String title = this.title.toLowerCase();
		if(title.contains("(u)") ){
			return "United States";
		} else if(title.contains("(e)")){
			return "Europe";
		} else if(title.contains("(j)")){
			return "Japan";
		} else if(title.contains("(a)")){
			return "Asia or Australia";
		} else if(title.contains("(Unl)")){
			return "Unliscensed";
		} else {
			return "N/A";
		}
	}
	
	
	/**
	 * The first string of the returned array contains the title stripped of recognized GoodCodes. The remainder of the Array
	 * contains the details of the ROM provided by the GoodCodes. Also removes country codes.
	 * @return
	 */
	public ArrayList<String> getGoodCodes(){
		String modifiedTitle = title;
		ArrayList<String> goodCodes = new ArrayList<String>();

		//Remove country codes from modifiedTitle
		if(title.contains("(U)") ){
			modifiedTitle = modifiedTitle.replace("(U)", "");
		} else if(title.contains("(E)")){
			modifiedTitle = modifiedTitle.replace("(E)", "");
		} else if(title.contains("(J)")){
			modifiedTitle = modifiedTitle.replace("(J)", "");
		} else if(title.contains("(A)")){
			modifiedTitle = modifiedTitle.replace("(A)", "");
		} else if(title.contains("(Unl)")){
			modifiedTitle = modifiedTitle.replace("(Unl)", "");
		}
		
		//Get good codes
		if (title.contains("[C]")) {
			if (console == Console.GAMEBOY) {
				goodCodes.add("Gameboy Color Rom");
				modifiedTitle = modifiedTitle.replace("[C]", "");
			}
		}
		if (title.contains("[U]")) {
			if (console == Console.NES) {
				goodCodes.add("Universal NES format");
				modifiedTitle = modifiedTitle.replace("[U]", "");
			}
		}
		if (title.contains("(PRG0)")) {
			goodCodes.add("Program Revision 0");
			modifiedTitle = modifiedTitle.replace("(PRG0)", "");
		}
		if (title.contains("(PRG1)")) {
			goodCodes.add("Program Revision 1");
			modifiedTitle = modifiedTitle.replace("(PRG1)", "");
		}
		if (title.contains("[PRG0]")) {
			goodCodes.add("Program Revision 0");
			modifiedTitle = modifiedTitle.replace("[PRG0]", "");
		}
		if (title.contains("[PRG1]")) {
			goodCodes.add("Program Revision 1");
			modifiedTitle = modifiedTitle.replace("[PRG1]", "");
		}
		if (title.contains("[!]")) {
			goodCodes.add("Verified Good Dump");
			modifiedTitle = modifiedTitle.replace("[!]", "");
		}
		if (title.contains("[a]")) {
			goodCodes.add("Alternate Version");
			modifiedTitle = modifiedTitle.replace("[a]", "");
		}
		if (title.contains("[b]")) {
			goodCodes.add("Bad Dump");
			modifiedTitle = modifiedTitle.replace("[a]", "");
		}
		if (title.contains("[f]")) {
			goodCodes.add("Fixed Dump");
			modifiedTitle = modifiedTitle.replace("[a]", "");
		}
		if (title.contains("[h]")) {
			goodCodes.add("Hacked Rom");
			modifiedTitle = modifiedTitle.replace("[a]", "");
		}
		if (title.contains("[p]")) {
			goodCodes.add("Pirated Version");
			modifiedTitle = modifiedTitle.replace("[a]", "");
		}
		if (title.contains("[t]")) {
			goodCodes.add("Trained\\Cheat Version");
			modifiedTitle = modifiedTitle.replace("[a]", "");
		}
		modifiedTitle = modifiedTitle.replaceAll("\\[.+?\\]", "");
		modifiedTitle = modifiedTitle.replaceAll("\\(.+?\\)", "");
		Pattern p = Pattern.compile("^(.+?)[^\\S]*$");
		Matcher m = p.matcher(modifiedTitle);
		String concatenatedTitle ="";
		while(m.find()){
			concatenatedTitle += m.group(1) + " "; 
		}
		modifiedTitle = concatenatedTitle.trim();
		
		goodCodes.add(0, modifiedTitle); //Add modified title to front
		return goodCodes;
	}
	public void setLegal(String legal) {
		this.cantDownloadMessage = legal;
	}
	public String getLegal() {
		return cantDownloadMessage;
	}
	public void setWebViewBaseURL(String webViewURL) {
		this.webViewURL = webViewURL;
	}
	public String getWebViewBaseURL() {
		return webViewURL;
	}
	public int getConnectorID() {
		return ConnectorID;
	}

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
		out.writeString(title);
		out.writeString(fileSize);
		out.writeString(subInfo1);
		out.writeString(subInfo1_title);
		out.writeString(URLSuffix);
		out.writeString(console.name());
		out.writeString(cantDownloadMessage);
		out.writeString(webViewURL);
		out.writeInt(ConnectorID);
    }

    public static final Parcelable.Creator<SearchResult> CREATOR
            = new Parcelable.Creator<SearchResult>() {
        public SearchResult createFromParcel(Parcel in) {
            return new SearchResult(in);
        }

        public SearchResult[] newArray(int size) {
            return new SearchResult[size];
        }
    };
    
    private SearchResult(Parcel in) {
		this.title = in.readString();
		this.fileSize = in.readString();
		this.subInfo1 = in.readString();
		this.subInfo1_title = in.readString();;
		this.URLSuffix = in.readString();
		this.console = Console.valueOf(in.readString());
		this.cantDownloadMessage = in.readString();
		this.webViewURL = in.readString();
		this.ConnectorID = in.readInt();
    }

	
	
	
}
