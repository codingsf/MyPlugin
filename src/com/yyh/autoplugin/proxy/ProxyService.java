package com.yyh.autoplugin.proxy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.yyh.autoplugin.utils.ServicesManager;

public class ProxyService extends Service {
	private static final String TAG = ProxyService.class.getName();
	
	@Override
	public void onCreate() {
		Log.e("TAG","proxy: onCreate()");
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.e("TAG","proxy: onStart()");
		//分发Service
		ServicesManager.getInstance().onStart(intent,startId);
		super.onStart(intent, startId);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("TAG","proxy: onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
