package com.yyh.autoplugin.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.util.Log;

/**
 * 插件内容提供器管理器
 * @author Administrator
 *
 */
public class ProvidersManager {

	/**
	 * 在进程内部安装provider，也就是调用ActivityThread.installContentProvider方法。
	 * @throws Exception 
	 */
	public static void installProviders(Context context, File apkFile) throws Exception {
		List<ProviderInfo> providerInfos = parseProviders(apkFile);
		for(ProviderInfo providerInfo:providerInfos){
			providerInfo.applicationInfo.packageName = context.getPackageName();
		}
		
		Log.d("TAG", providerInfos.toString());
		
		//安装
		Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
		Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
		Object currentAtivityThread = currentActivityThreadMethod.invoke(null);
		
		Method installContentProvidersmeMethod = activityThreadClass.getDeclaredMethod("installContentProviders", Context.class,List.class);
		installContentProvidersmeMethod.setAccessible(true);
		installContentProvidersmeMethod.invoke(currentAtivityThread, context,providerInfos);
	}

	public static final List<ProviderInfo> parseProviders(File apkFile) throws Exception{
		
		Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
		Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class,int.class	);
		Object packageParser = packageParserClass.newInstance();
		Object packageObj = parsePackageMethod.invoke(packageParser, apkFile,PackageManager.GET_PROVIDERS);
		/**
		 * 反射调用参数
		 */
		//第一个参数 PackageParser内部类，Provider,获取类名用于反射，获取对象用于invoke
		Class<?> packageParser$ProviderClass = Class.forName("android.content.pm.PackageParser$Provider");
		//第二个PackageUserState类名，
		Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
		
		//通过该方法获取每一个provider对应的ProviderInfo值
		Method generateProviderInfoMethod = packageParserClass.getDeclaredMethod("generateProviderInfo", packageParser$ProviderClass,int.class,packageUserStateClass,int.class);
		
		/**
		 * invoke调用参数
		 */
		//第一个参数，从providers字段中获取，Package类中public字段
		Field providersField = packageObj.getClass().getDeclaredField("providers");
		List providers = (List) providersField.get(packageObj);//
		//第二个参数flag设为0，全部获取，
		//第三个参数packageUserState
		Object packageUserState = packageUserStateClass.newInstance();
		//第四个参数id.
		Class<?> userHandleClass = Class.forName("android.os.UserHandle");
		Method getCallingUserIdMethod = userHandleClass.getDeclaredMethod("getCallingUserId");
		int userId = (Integer) getCallingUserIdMethod.invoke(null);
		
		//返回一个List<ProviderInfo>对象
		List<ProviderInfo> ret = new ArrayList<ProviderInfo>();
		for(Object provider : providers){
			ProviderInfo info = (ProviderInfo) generateProviderInfoMethod.invoke(packageParser, provider,0,packageUserState,userId);
			ret.add(info);
		}
		//获取ProviderInfo对象，参数：provider,flag,userstate,userid
		return ret;
	}
}
