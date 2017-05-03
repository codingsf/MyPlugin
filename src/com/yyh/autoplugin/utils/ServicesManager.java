package com.yyh.autoplugin.utils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.yyh.autoplugin.ApApplication;
import com.yyh.autoplugin.hook.HookHelper;
import com.yyh.autoplugin.proxy.ProxyService;
/**
 * 插件service管理器
 * @author Administrator
 *
 */
public class ServicesManager {
    private static volatile ServicesManager sIntance;
    
    private Map<String, Service> mServiceMap = new HashMap<String, Service>();
    private Map<ComponentName, ServiceInfo> mServiceInfoMap = new HashMap<ComponentName, ServiceInfo>();
	
    public synchronized static ServicesManager getInstance() {
		if (sIntance==null) {
			sIntance = new ServicesManager();
		}
		return sIntance;
	}

	public void preLoadServices(File apkFile) throws Exception {
		Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
		Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class,int.class);
		Object packageParser = packageParserClass.newInstance();
		Object packageObj = parsePackageMethod.invoke(packageParser, apkFile,PackageManager.GET_SERVICES);
		
		Field servicesField = packageObj.getClass().getDeclaredField("services");
		List services = (List) servicesField.get(packageObj);
		
		Class<?> packageParser$ServiceClass = Class.forName("android.content.pm.PackageParser$Service");
		Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
		Class<?> userHandleClass = Class.forName("android.os.UserHandle");
		
		Method getCallingUserIdMethod = userHandleClass.getDeclaredMethod("getCallingUserId");
		int userId = (Integer) getCallingUserIdMethod.invoke(null);
		
		Object defaultUserState = packageUserStateClass.newInstance();
		
		Method generateServiceInfoMethod = packageParserClass.getDeclaredMethod("generateServiceInfo", packageParser$ServiceClass,int.class,packageUserStateClass,int.class);
		
		for(Object service:services){
			ServiceInfo info = (ServiceInfo) generateServiceInfoMethod.invoke(packageParser, service,0,defaultUserState,userId);;
			mServiceInfoMap.put(new ComponentName(info.packageName,info.name), info);
		}
		
	}

	public void onStart(Intent intent, int startId) {
		if (intent == null){
			return;
		}
		Intent targetIntent = intent.getParcelableExtra(HookHelper.EXTRA_SERVICE_INTENT);
		//startService方法已被 拦截，将永远启动ProxyService
		//UPFServiceApplication.getContext().startService(targetIntent);
		
		ServiceInfo serviceInfo = selectPluginService(targetIntent);
		if (serviceInfo==null) {
			Log.e("TAG","cannot found service: "+targetIntent.getComponent());
			return;
		}
		try {
			if (!mServiceMap.containsKey(serviceInfo.name)) {
				//service还不存在，先创建Service;
				proxyCreateService(serviceInfo);
			}
			Log.e("TAG","startserVice");
			Service service = mServiceMap.get(serviceInfo.name);
			service.onStart(targetIntent, startId);
			//service.onStartCommand(targetIntent, 0, startId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 通过ActivityThread的handleCreateService(CreateServiceData)方法创建出Service对象。
	 * @param serviceInfo
	 * @throws Exception 
	 */
	private void proxyCreateService(ServiceInfo serviceInfo) throws Exception {
		IBinder token = new Binder();
		
		//创建参数：createServicddata对象。
		Class<?> activityThread$CreateServiceDataClass = Class.forName("android.app.ActivityThread$CreateServiceData");
		Constructor<?> constructor = activityThread$CreateServiceDataClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		Object createServiceData = constructor.newInstance();
		
        // 写入我们创建的createServiceData的token字段, ActivityThread的handleCreateService用这个作为key存储Service
		Field tokenField = activityThread$CreateServiceDataClass.getDeclaredField("token");
		tokenField.setAccessible(true);
		tokenField.set(createServiceData, token);
		
		//写入ServiceInfo info对象
		serviceInfo.applicationInfo.packageName = ApApplication.getContext().getPackageName();
		Field infoField = activityThread$CreateServiceDataClass.getDeclaredField("info");
		infoField.setAccessible(true);
		infoField.set(createServiceData, serviceInfo);
		
		//写入CompatInfo字段，获取默认配置。
		Class<?> compatibilityInfoClass = Class.forName("android.content.res.CompatibilityInfo");
		Field defaultCompatibilityField = compatibilityInfoClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
		Object defaultCompatibiliby = defaultCompatibilityField.get(null);
		Field compatInfofField = activityThread$CreateServiceDataClass.getDeclaredField("compatInfo");
		compatInfofField.setAccessible(true);
		compatInfofField.set(createServiceData, defaultCompatibiliby);
		
		Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
		Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
		Object currentActivityThread = currentActivityThreadMethod.invoke(null);
		
		Method handleCreateServiceMethod = activityThreadClass.getDeclaredMethod("handleCreateService", activityThread$CreateServiceDataClass);
		handleCreateServiceMethod.setAccessible(true);
		
		handleCreateServiceMethod.invoke(currentActivityThread, createServiceData);
       
		
		// handleCreateService创建出来的Service对象并没有返回, 而是存储在ActivityThread的mServices字段里面, 这里我们手动把它取出来
		Field mServiceField = activityThreadClass.getDeclaredField("mServices");
		mServiceField.setAccessible(true);
		Map mService = (Map) mServiceField.get(currentActivityThread);
		Service service = (Service) mService.get(token);

        // 获取到之后, 移除这个service, 我们只是借花献佛
		mService.remove(token);
		mServiceMap.put(serviceInfo.name, service);
	}

	public int stopService(Intent raw) {
		ServiceInfo serviceInfo = selectPluginService(raw);
		if (serviceInfo==null) {
			Log.e("TAG","cannot found Service"+raw.getComponent());
			return 0;
		}
		Service service = mServiceMap.get(serviceInfo.name);
		if (service==null) {
			return 0;
		}
		service.onDestroy();
		mServiceMap.remove(serviceInfo.name);
		
		if (mServiceMap.isEmpty()) {
			//没有Service后，代理Service不需要存在。
			Context appContext = ApApplication.getContext();
			appContext.stopService(new Intent().setComponent(new ComponentName(appContext.getPackageName(),ProxyService.class.getName())));
		}
		return 1;
	}
	
	private ServiceInfo selectPluginService(Intent pluginIntent){
		for(ComponentName componentName:mServiceInfoMap.keySet()){
			if (componentName.equals(pluginIntent.getComponent())) {
				return mServiceInfoMap.get(componentName);
			}
		}
		return null;
	}

}
