package com.yyh.autoplugin.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yyh.autoplugin.hook.PluginClassLoader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
/**
 * 插件广播接受者管理器
 * @author Administrator
 *
 */
public class ReceiversManager {
	public static Map<ActivityInfo, List<? extends IntentFilter>> sCache = new HashMap<ActivityInfo, List<? extends IntentFilter>>();

	/**
	 * 解析apk文件中的<receiver>并存储起来。
	 * @param apkFile
	 * @throws Exception 
	 */
	private static void parseReceivers(File apkFile) throws Exception{
		Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
		Class<?> packageParser$packageClass = Class.forName("android.content.pm.PackageParser$Package");
		Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
		Class<?> packageParser$activityClass = Class.forName("android.content.pm.PackageParser$Activity");
		Method generateActivityInfoMethod = packageParserClass.getDeclaredMethod("generateActivityInfo", packageParser$activityClass,int.class,packageUserStateClass,int.class);
		
		Object packageParserObj = packageParserClass.newInstance();
		//第一个参数，内部类，Activity,从字段receivers中循环获取
		Field receiversField = packageParser$packageClass.getDeclaredField("receivers");
		Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage",File.class,int.class);
		Object packageObj = parsePackageMethod.invoke(packageParserObj, apkFile,PackageManager.GET_RECEIVERS);
		//receiversField = packageObj.getClass().getDeclaredMethod("parsePackage", parameterTypes)
		List receivers = (List) receiversField.get(packageObj);
		
		//第二个参数0，
		//第三个参数：PackageUserState,
		Object PackageUserStateObj = packageUserStateClass.newInstance();
		
		//第国个参数，userId;
		Class<?> userHandleClass = Class.forName("android.os.UserHandle");
		Method getCallingUserIdMethod = userHandleClass.getDeclaredMethod("getCallingUserId");
		int userId = (Integer) getCallingUserIdMethod.invoke(null);
		
		
		Class<?> componentClass = Class.forName("android.content.pm.PackageParser$Component");
		Field intentsField = componentClass.getDeclaredField("intents");
		
		for(Object receiver: receivers){
			ActivityInfo info = (ActivityInfo) generateActivityInfoMethod.invoke(null, receiver,0,PackageUserStateObj,userId);
			List<? extends IntentFilter> filters = (List<? extends IntentFilter>) intentsField.get(receiver);
			sCache.put(info, filters);
		}
	}
	
	public static void preLoadReceiver(Context context,
			File apkFile) throws Exception {
		parseReceivers(apkFile);
		
		ClassLoader classLoader = null;
		for(ActivityInfo activityInfo:ReceiversManager.sCache.keySet()){
            Log.i("ReceiverHelper", "preload receiver:" + activityInfo.name);
            List<? extends IntentFilter> intentFilters = ReceiversManager.sCache.get(activityInfo);
            if (classLoader==null) {
				classLoader = PluginClassLoader.getPluginClassLoader(apkFile,activityInfo.packageName);
			}
            //f动态注册
            for(IntentFilter intentFilter:intentFilters){
            	BroadcastReceiver receiver = (BroadcastReceiver) classLoader.loadClass(activityInfo.name).newInstance();
            	context.registerReceiver(receiver, intentFilter);
            }
		}
		
	}

}
