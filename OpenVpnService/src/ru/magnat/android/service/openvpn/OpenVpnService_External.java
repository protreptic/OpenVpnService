package ru.magnat.android.service.openvpn;

import ru.magnat.android.service.openvpn.OpenVpnService.LocalBinder;
import ru.magnat.android.service.openvpn.aidl.IOpenVpnService_External;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class OpenVpnService_External extends Service {

	private static final String LOG_TAG = "OpenVpnService_External";
	
	private IOpenVpnService_External.Stub mBinder = new IOpenVpnService_External.Stub() {
		
		@Override
		public void test() throws RemoteException {
			mOpenVpnService.test();
		}
		
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private OpenVpnService mOpenVpnService;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(LOG_TAG, "Connected to internal service");
			
            mOpenVpnService = ((LocalBinder) service).getService();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(LOG_TAG, "Disconnected from internal service");
			
			mOpenVpnService = null;
		}

	};
	
	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "OpenVpnService_External's created");
		
		bindService(new Intent(getBaseContext(), OpenVpnService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "OpenVpnService_External's destroyed");
		
		unbindService(mConnection); 
	}

}
