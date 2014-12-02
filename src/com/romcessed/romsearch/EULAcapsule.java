package com.romcessed.romsearch;

import android.app.Activity;

public class EULAcapsule {
	
	Activity activity;
	String EULATitle;
	String buttonAccept;
	String buttonRefuse;
	final String EULAIdentifier;
	int EulaXMLString;
	
	public EULAcapsule(Activity activity, String EULATitle, String buttonAccept, String buttonRefuse, String EULAIdentifier, int EulaXMLString){
		this.activity = activity;
		this.EULATitle = EULATitle;
		this.buttonAccept = buttonAccept;
		this.buttonRefuse = buttonRefuse;
		this.EULAIdentifier = EULAIdentifier;
		this.EulaXMLString = EulaXMLString;
	}

	public Activity getActivity() {
		return activity;
	}

	public String getEULATitle() {
		return EULATitle;
	}

	public String getButtonAccept() {
		return buttonAccept;
	}

	public String getButtonRefuse() {
		return buttonRefuse;
	}

	public String getEULAIdentifier() {
		return EULAIdentifier;
	}

	public int getEulaXMLString() {
		return EulaXMLString;
	}
	
	
	
}