package com.romcessed.romsearch.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;

import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.R.id;
import com.romcessed.romsearch.R.layout;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.searchproviders.RombayConnector;
import com.romcessed.romsearch.searchproviders.RomulationConnector;
import com.romcessed.romsearch.searchproviders.Connector.AuthenticationNotRequiredException;
import com.romcessed.romsearch.searchproviders.Connector.CaptchaNotRequiredException;
import com.romcessed.romsearch.tools.GlobalVars;
import com.romcessed.romsearch.tools.ScreenShotTools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.widget.RelativeLayout.LayoutParams;

public class TestActivity extends Activity {
	private static final String TAG = "TestActivity";
	Button bt1, bt2;
	RombayConnector rc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imgtestlayout);
		bt1 = (Button)findViewById(R.id.tsta_btn);
		bt2 = (Button)findViewById(R.id.tsta_btn2);

		GlobalVars.initializeUniversalClient();
		
		
	}
	
	
	
}
