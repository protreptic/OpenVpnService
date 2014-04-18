package ru.magnat.android.service.openvpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class OpenVpnService extends VpnService {
	
	private static final String LOG_TAG = "OpenVpnService";
	
	public class LocalBinder extends Binder {
		
		public OpenVpnService getService() { 
			return OpenVpnService.this;
		}
		
	} 
	
	private LocalBinder mBinder = new LocalBinder(); 
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(LOG_TAG, "OpenVpnService's created");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d(LOG_TAG, "OpenVpnService's destroyed");
	}
	
	public void test() {
		Log.d(LOG_TAG, "test OK!");
	}
	
}
