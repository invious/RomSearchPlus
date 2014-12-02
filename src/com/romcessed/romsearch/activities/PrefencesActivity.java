package com.romcessed.romsearch.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.romcessed.romsearch.R;

public class PrefencesActivity extends PreferenceActivity {
    
	private Preference prefPSX;
	private Preference prefNES;
	private Preference prefSNES;
	private Preference prefGBA;
	private Preference prefGBC;
	
	private Preference prefAndroZip;
	
	private Preference prefScreenshotsCheck;
	private Preference prefSaveLocationCheck;
	private Preference prefSaveLocationEditText;
	private Preference prefUNecm;
	private Preference prefSevenZip;
	private Preference prefIPS;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        prefPSX= (Preference) findPreference("psx4droid");
        prefNES = (Preference) findPreference("nesnoid");
        prefSNES = (Preference) findPreference("snesoid");
        prefGBA= (Preference) findPreference("gameboid");
        prefGBC = (Preference) findPreference("gbcoid");
        prefUNecm = (Preference)findPreference("unecm");
        prefAndroZip = (Preference)findPreference("androzip");
        prefSevenZip = (Preference)findPreference("sevenzip");
        prefIPS = (Preference)findPreference("ips");
        
        setMarketLinkClickListener("com.androidemu.gbc", prefGBC);
        setMarketLinkClickListener("com.androidemu.gba", prefGBA);
        setMarketLinkClickListener("com.androidemu.nes", prefNES);
        setMarketLinkClickListener("com.androidemu.snes", prefSNES);
        setMarketLinkClickListener("com.zodttd.psx", prefPSX);
        setMarketLinkClickListener("com.romcessed.unecm", prefUNecm);
        setMarketLinkClickListener("com.romcessed.ips", prefIPS);
        
        setMarketLinkClickListener("com.agilesoftresource", prefAndroZip);
        setMarketLinkClickListener("com.hagia.sevenzip", prefSevenZip);
        
        prefScreenshotsCheck = (Preference) findPreference("uss");
        
        prefSaveLocationCheck = (Preference) findPreference("save_loc_check");
        prefSaveLocationEditText = (Preference) findPreference("save_loc_et");
        
       
    }

	private void setMarketLinkClickListener(final String marketUriString, Preference p) {
		OnPreferenceClickListener ret = new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
            	Intent i = new Intent();
    			i.setAction(android.content.Intent.ACTION_VIEW);
    			i.setData(Uri.parse("market://details?id=" + marketUriString));
    			startActivity(i);
            	return true;
            }
		};
		p.setOnPreferenceClickListener(ret);
	}
    
	
}