package com.romcessed.romsearch.activities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.client.ClientProtocolException;

import com.romcessed.romsearch.Category;
import com.romcessed.romsearch.Console;
import com.romcessed.romsearch.EULA;
import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.SearchResult;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.searchproviders.RombayConnector;
import com.romcessed.romsearch.searchproviders.Connector.AuthenticationNotRequiredException;
import com.romcessed.romsearch.tools.GlobalVars;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginActivity extends Activity{

	private static final String BUNDLE_TAG = "LOGIN";
	public static final int LOGIN_FAILED = 2;
	public static final int CONNECTION_FAILED = 3;
	private static boolean savePassword = true;
	private Connector connector;
	EditText userET, passET;
	CheckBox savePass;
	ImageView providerLogo;
	
	ProgressDialog progressDialog;

	//** Called when activity first started. Initializes the components *//*
	public void onCreate(Bundle savedInstanceState){
		Log.i(BUNDLE_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);
		
        connector = SearchProvider.getConnectorFromID(getIntent().getIntExtra("CONNECTOR", -1));
		providerLogo = (ImageView)findViewById(R.id.ls_iv_logo);
		userET = (EditText) findViewById(R.id.ls_et_email);
		passET = (EditText) findViewById(R.id.ls_et_password);
		savePass = (CheckBox) findViewById(R.id.ls_cb_savepassword);
		
		//Customize
		providerLogo.setImageResource(connector.getLogoDrawableID());
		
		
	}

	
	public void onStart(){
		super.onStart();
		informAboutLogin();
		EULA.show(connector.getEULAcapsule(LoginActivity.this));
		//Get the email and password from a private preferences log
		//and log in if able to
		loadFromPreferences();
	}
	
	public void informAboutLogin(){
		SharedPreferences p = getSharedPreferences("FIRSTASKS", 0);
		if(!p.getBoolean("LOGIN", false)){
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("About Logging In");
	        builder.setCancelable(false);
	        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            SharedPreferences settings = getSharedPreferences("FIRSTASKS", 0);
	      	      SharedPreferences.Editor editor = settings.edit();
	      	      editor.putBoolean("LOGIN", true); //We asked
	      	      editor.commit();
	            }
	        });
	        builder.setMessage("\"Oh no, I have to register!\"\n\nIt only takes a second. And after that its lightning fast.\n\nIf you save your password, we'll skip the login screen.");
	        builder.show();
		}
	}


// Instantiating the Handler associated with the main thread.
	private Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {  
			Intent loginIntent = new Intent();
			loginIntent.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector));
			switch(msg.what) {
		    case 1:
		    	if(((Boolean) msg.obj).booleanValue()){
					connector.saveAuthenticationCredentials(LoginActivity.this, getUsername(), getPassword(), getPasswordSaveChecked());
		    		loginIntent.setClass(LoginActivity.this, ConsoleSelectionView.class);
		    		startActivity(loginIntent);
		    		finish();
		    	}
		    	break;
		    case 2: //Preauth delayed execute
		    		loginIntent.setClass(LoginActivity.this, ConsoleSelectionView.class);
		    		startActivity(loginIntent);
		    		finish();
		    	break;
		    }
		}
	};
	
	public void saveCheckOnClick(View v){
		savePassword = !savePassword;
	}
	
	public void loadFromPreferences(){
		SharedPreferences settings = getSharedPreferences(connector.TAG+"_CREDENTIALS", 0);
		String username = settings.getString("USERNAME", "");
		String password = settings.getString("PASSWORD", "");
		boolean savePassword = settings.getBoolean("SAVEPASSWORD", false);
		userET.setText(username);
		passET.setText(password);
		savePass.setChecked(savePassword);
		if(savePassword){
			try {
				connector.authenticateUser(username, password);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AuthenticationNotRequiredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void ls_btn_loginOnClick(View v){
		progressDialog = ProgressDialog.show(LoginActivity.this, "Log in", "Logging in...");
		Thread t = new Thread() {
	        public void run() {
	        	boolean loginResults = false;
				try {
					loginResults = connector.authenticateUser(userET.getText().toString(),
							passET.getText().toString());
				} catch (UnsupportedEncodingException e) {
					messageHandler.sendMessage(Message.obtain(messageHandler, 1, null));
					e.printStackTrace();
					return;
				} catch (ClientProtocolException e) {
					messageHandler.sendMessage(Message.obtain(messageHandler, 1, null));
					e.printStackTrace();
					return;
				} catch (IOException e) {
					messageHandler.sendMessage(Message.obtain(messageHandler, 1, null));
					e.printStackTrace();
					return;
				} catch (AuthenticationNotRequiredException e) {

				}
	        	messageHandler.sendMessage(Message.obtain(messageHandler, 1, new Boolean(loginResults))); 
	        }
	    };
	    t.start();
	}
	
	public void registerOnClick(View v){
		Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
		i.putExtra(RegisterActivity.TAG_USERNAME, userET.getText().toString());
        i.putExtra("CONNECTOR", SearchProvider.getIDFromAbstractConnector(connector));
		startActivityForResult(i, RegisterActivity.RESULT_REQUEST_CODE);
	}
	
	
	
	   protected void onActivityResult(int requestCode, int resultCode, Intent data){
	        // See which child activity is calling us back.
	        switch (requestCode) {
	            case RegisterActivity.RESULT_REQUEST_CODE:
	                if (resultCode == RESULT_CANCELED){
	                	Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show();
	                } else {
	                	String registeredUsername = data.getStringExtra(RegisterActivity.TAG_USERNAME);
	                	userET.setText(registeredUsername);
	                	Toast.makeText(LoginActivity.this, "Registered! Check email for confirmation and/or password", Toast.LENGTH_LONG).show();
	                }
	            default:
	                break;
	        }
	    }

		@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
			super.onSaveInstanceState(savedInstanceState);
			String username = userET.getText().toString();
			String password = passET.getText().toString();
			boolean savePassword = savePass.isChecked();
			
			savedInstanceState.putString("username", username);
			savedInstanceState.putString("password", password);
			savedInstanceState.putBoolean("checked", savePassword);
		}

		@Override
		public void onRestoreInstanceState(Bundle savedInstanceState) {
		  super.onRestoreInstanceState(savedInstanceState);
		  userET.setText(savedInstanceState.getString("username"));
		  passET.setText(savedInstanceState.getString("password"));
		  savePass.setChecked(savedInstanceState.getBoolean("checked"));
		}
	
	public void onPause(){
		Log.i(BUNDLE_TAG, "onPause");
		super.onPause();
	}

	
	public void onStop(){
		Log.i(BUNDLE_TAG, "onStop");
		super.onStop();
	}
	
	public void onResume(){
		Log.i(BUNDLE_TAG, "onResume");
		super.onResume();
	}
	
	public void onDestroy(){
		Log.i(BUNDLE_TAG, "onDestroy");
		super.onDestroy();
	}
	
	public void onRestart(){
		Log.i(BUNDLE_TAG, "onRestart");
		super.onRestart();
	}
	
	public String getUsername(){return userET.getText().toString();}
	public String getPassword(){return passET.getText().toString();}
	public Boolean getPasswordSaveChecked(){return savePass.isChecked();}
	
	
	public void onConfigurationChanged (Configuration newConfig){
		Log.i(BUNDLE_TAG, "onConfigChanged");
		super.onConfigurationChanged(newConfig);
	}
	
}