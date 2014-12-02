package com.romcessed.romsearch;

import com.romcessed.romsearch.searchproviders.AbstractConnector;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.searchproviders.DopeRomsConnector;
import com.romcessed.romsearch.searchproviders.EmuParadiseBIOSConnector;
import com.romcessed.romsearch.searchproviders.EmuParadiseConnector;
import com.romcessed.romsearch.searchproviders.RombayConnector;
import com.romcessed.romsearch.searchproviders.RomhackingConnector;
import com.romcessed.romsearch.searchproviders.RomulationConnector;

public enum SearchProvider {
	
	
	ROMBAY (R.drawable.rombay_logo, "Rombay", new RombayConnector(), 1), 
	DOPEROMS (R.drawable.doperoms_logo, "Dope Roms", new DopeRomsConnector(), 2),
	/*ROMULATION(R.drawable.romulation_logo, "Romulation", new RomulationConnector()),*/
	EMUPARADISE (R.drawable.emuparadise_logo, "Emuparadise", new EmuParadiseConnector(), 3),
	EMUPARADISE_BIOS (R.drawable.emuparadise_bios_logo, "Emuparadise Bios", new EmuParadiseBIOSConnector(), 9);
	
	//THESE ARE ACTUALLY REAL
	public static final int ID_ROMBAY = 1;
	public static final int ID_DOPEROMS = 2;
	public static final int ID_EMUPARADISE = 3;
	public static final int ID_EMUPARADISE_BIOS = 9;
	
	//THESE ARE NOT INCLUDED IN ENUM
	public static final int ID_ROMHACKING = 10;
	
	private Connector connector;
	private int drawable;
	private String properName;
	public final static String BUNDLE_TAG = SearchProvider.class.getSimpleName();
	
	SearchProvider(int drawable, String properName, Connector connector, int ConnectorID){
		this.drawable = drawable;
		this.properName = properName;
		this.setConnector(connector);
	}
	
	public static int getIDFromAbstractConnector(Connector connector){
		if(connector instanceof RombayConnector){
			return ID_ROMBAY;
		}
		if(connector instanceof EmuParadiseBIOSConnector){
			return ID_EMUPARADISE_BIOS;
		}
		if(connector instanceof EmuParadiseConnector){
			return ID_EMUPARADISE;
		}
		if(connector instanceof DopeRomsConnector){
			return ID_DOPEROMS;
		}
		if(connector instanceof RomhackingConnector){
			return ID_ROMHACKING;
		}
		return -1;
	}

	public int getDrawable() {
		return drawable;
	}

	public static Connector getConnectorFromID(int ID){
		switch(ID){
		case ID_ROMBAY:
			return new RombayConnector();
		case ID_DOPEROMS:
			return new DopeRomsConnector();
		case ID_EMUPARADISE:
			return new EmuParadiseConnector();
		case ID_EMUPARADISE_BIOS:
			return new EmuParadiseBIOSConnector();
		case ID_ROMHACKING:
			return new RomhackingConnector();
		default: //should never happen
			throw new IllegalStateException("Invalid Connector ID");
		}
	}
	
	public String getProperName() {
		return properName;
	}
	
	public String putExtra(){
		return this.toString();
	}
	
	public static SearchProvider getSearchProviderFromExtra(String searchProviderExtra){
		return SearchProvider.valueOf(SearchProvider.class, searchProviderExtra);
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public Connector getConnector() {
		return connector;
	}
	
}
