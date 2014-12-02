package com.romcessed.romsearch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class ServiceCommunicableActivity extends Activity {
	protected ServiceCom comService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent svc = new Intent(this.getApplicationContext(),
				DownloadService.class);
		this.startService(svc);
		bindService(svc, mConnection, Context.BIND_AUTO_CREATE);
	}

	protected ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			comService = ServiceCom.Stub.asInterface(service);
			ServiceCommunicableActivity.this.runOnUiThread(comServiceBoundEvent);
		}

		public void onServiceDisconnected(ComponentName name){

		}

	};

	final Runnable comServiceBoundEvent = new Runnable() {

		public void run() {

			onActivityReady();

		}

	};

	protected void onActivityReady() {
	
	}
	
	public ServiceCom getServiceCom(){
		return comService;
	}

}