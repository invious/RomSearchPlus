package com.romcessed.romsearch.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;

import com.romcessed.romsearch.R;
import com.romcessed.romsearch.SearchProvider;
import com.romcessed.romsearch.R.drawable;
import com.romcessed.romsearch.R.id;
import com.romcessed.romsearch.R.layout;
import com.romcessed.romsearch.searchproviders.Connector;
import com.romcessed.romsearch.searchproviders.Connector.UsernameVerificationNotAvaiableException;
import com.romcessed.romsearch.tools.GlobalVars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class RegisterActivity extends Activity {

	public static final int RESULT_REQUEST_CODE=1;
	public static final String TAG_REGISTERED = "REGISTERED";
	public static final String TAG_USERNAME = "USERNAME";
	public static final String TAG_PASSWORD = "PASSWORD";
	
	Connector connector;
	boolean validUsername;
	EditText usernameEditText;
	EditText emailEditText;
	EditText nameEditText;
	ImageView usernameStatusImageView;
	ImageView passwordStatusImageView;
	Button registerButton, cancelButton;
	EntryStatus usernameEntry, passwordEntry;
	boolean registering;
	private EditText password1EditText;
	private EditText password2EditText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        connector = SearchProvider.getConnectorFromID(getIntent().getIntExtra("CONNECTOR", -1));
		
		if(connector.isPasswordNeededDuringRegistration()){
			super.setContentView(R.layout.registration_view);
			password1EditText = (EditText)findViewById(R.id.reg_et_password);
			password1EditText.addTextChangedListener(registerButtonWatcher);
			password2EditText = (EditText)findViewById(R.id.reg_et_password2);
			password2EditText.addTextChangedListener(registerButtonWatcher);
			passwordStatusImageView = (ImageView)findViewById(R.id.reg_iv_entry_status_passmatch);
			passwordEntry = new EntryStatus(RegisterActivity.this, passwordStatusImageView);
			passwordEntry.setStatus(Status.UNKNOWN);
		} else {
			super.setContentView(R.layout.registration_view_nopass);
		}
		
		
		usernameStatusImageView = (ImageView)findViewById(R.id.reg_iv_entry_status_uservalid);
		usernameEntry = new EntryStatus(RegisterActivity.this, usernameStatusImageView);
		usernameEntry.setStatus(Status.UNKNOWN);
		
		usernameEditText = (EditText)findViewById(R.id.reg_et_user);
		usernameEditText.addTextChangedListener(usernameWatcher);
		usernameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus && usernameEntry.getStatus()!=Status.VALID){
					handler.sendEmptyMessageDelayed(2, 100);
				}
				
			}
		});
		nameEditText = (EditText)findViewById(R.id.reg_et_fullname);
		emailEditText = (EditText)findViewById(R.id.reg_et_email);
		emailEditText.addTextChangedListener(registerButtonWatcher);
		
		registerButton = (Button)findViewById(R.id.reg_btn_register);
		cancelButton = (Button)findViewById(R.id.reg_btn_cancel);
		
		registerButton.setOnClickListener(registerButtonListener);
		registerButton.setEnabled(false);
		cancelButton.setOnClickListener(cancelButtonListener);
		
		
	}
	
	private EditText getPassword1() throws IllegalAccessException {
		if (password1EditText==null)
			throw new IllegalAccessException();
		return password1EditText;
		}
	private EditText getPassword2() throws IllegalAccessException {
		if (password2EditText==null)
			throw new IllegalAccessException();
		return password2EditText;
		}
	private EntryStatus getPassEntry() throws IllegalAccessException {
		if (passwordEntry==null)
			throw new IllegalAccessException();
		return passwordEntry;
		}
	private String getPassword() throws IllegalAccessException {
		if (password1EditText==null)
			throw new IllegalAccessException();
		return password1EditText.getText().toString();
		}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        if(registering){
	        	setResult(RESULT_CANCELED);
	        	return true;
	        }
	    }
	    return super.onKeyDown(keyCode, event);
	}

	// Instantiating the Handler associated with the main thread.
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {  
		    switch(msg.what) {
		    case 1:
				usernameEntry.setStatus(validUsername);
				manageRegisterButton();
		    	break;
		    case 2:
		    	postDelayed(checkUsernameThread, 700);
		    	break;
		    }
		}
	};
	
	
	public void enableAllFields(boolean enabled){
		usernameEditText.setEnabled(enabled);

		emailEditText.setEnabled(enabled);
		cancelButton.setEnabled(enabled);
		registerButton.setEnabled(enabled);
	}
	
	private OnClickListener cancelButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			Toast.makeText(RegisterActivity.this, "Registration cancelled", Toast.LENGTH_SHORT);
			finish();
		}
	};
	
	private OnClickListener registerButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
				usernameEditText.setEnabled(false);
				emailEditText.setEnabled(false);
				nameEditText.setEnabled(false);
				usernameEditText.setEnabled(false);
				registerButton.setEnabled(false);
				cancelButton.setEnabled(false);
				registering=true;
				try {
					boolean registered = connector.registerUser(nameEditText.getText().toString(), usernameEditText.getText().toString(),null, emailEditText.getText().toString(), true);
					if(registered){
						Intent i = new Intent();
						i.putExtra(TAG_REGISTERED, true);
						i.putExtra(TAG_USERNAME, usernameEditText.getText().toString());
			            setResult(RESULT_OK, i);
					} else {
						setResult(RESULT_CANCELED);
					}
		            finish();
				} catch (UnsupportedEncodingException e) {
					
					e.printStackTrace();
					Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG);
				}
			
		}
	};
	
	
	
	private void manageRegisterButton(){
		registerButton.setEnabled(usernameEntry.getStatus()==Status.VALID);
	}
	
	Thread checkUsernameThread = new Thread(){
		@Override
		public void run() {
			try {
				validUsername = connector.usernameAvailable(usernameEditText.getText().toString());
				handler.sendEmptyMessage(1);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UsernameVerificationNotAvaiableException e) {
				validUsername=true;
				handler.sendEmptyMessage(1);
			}
		}
		
	};
	
	private TextWatcher usernameWatcher = new TextWatcher() {	
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			usernameEntry.setStatus(Status.UNKNOWN);
		}
		
		@Override
		public void afterTextChanged(Editable s) {

		}
	};
	
	private TextWatcher registerButtonWatcher = new TextWatcher() {	
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			try {
				boolean passwordsMatch = getPassword1().getText().toString().equals(getPassword2().getText().toString());
				registerButton.setEnabled(emailEditText.getText().toString().matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$") && 
						passwordsMatch);
				getPassEntry().setStatus(passwordsMatch);
			} catch (IllegalAccessException e) {
				registerButton.setEnabled(emailEditText.getText().toString().matches("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$"));
			}
			
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	
	enum Status {UNKNOWN, VALID, INVALID};
	
	public class EntryStatus {
		
		Context context;
		ImageView statusIcon;
		Status status;
		
		public Status getStatus(){
			return status;
		}
		
		public EntryStatus(Context context, ImageView statusImageView){
			this.context = context;
			this.statusIcon = statusImageView;
		}
		
		public void setStatus(boolean valid){
			if(valid){
				setStatus(Status.VALID);
			} else {
				setStatus(Status.INVALID);
			}
		}
		
		public void setStatus(Status status){
			this.status = status;
			switch(status){
			case VALID:	
				statusIcon.setImageResource(R.drawable.check);
				break;
			case INVALID:
				statusIcon.setImageResource(R.drawable.x);
				break;
			default:
				statusIcon.setImageResource(R.drawable.question);
				break;
			}
		}
	}

}
