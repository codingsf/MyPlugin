package com.yyh.autoplugin.parser;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yyh.autoplugin.utils.ApkOperator;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

public class PluginManfiestParser {
	
	public static String getApkPackageName(File apkFile){

		try{
			Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
			Class<?> packageParser$packageClass = Class.forName("android.content.pm.PackageParser$Package");
			Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class,int.class);
			Object packageParseObj = packageParserClass.newInstance();
			Object packageObj = parsePackageMethod.invoke(packageParseObj, apkFile,PackageManager.GET_ACTIVITIES);
			Field packageNameField = packageParser$packageClass.getDeclaredField("packageName");
			packageNameField.setAccessible(true);
			String packageName = (String) packageNameField.get(packageObj);
			return packageName;
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * 获取apk文件对于的启动Activity页
	 * @param apkFile
	 * @return
	 */
	public static Map<ActivityInfo, List<? extends IntentFilter>> getApkActivityInfos(File apkFile){
		Map<ActivityInfo, List<? extends IntentFilter>> mActivitiesMap = new HashMap<ActivityInfo, List<? extends IntentFilter>>();

		try {
			Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
			Class<?> pakcageParser$packageClass = Class.forName("android.content.pm.PackageParser$Package");
			Class<?> packageParser$componentClass = Class.forName("android.content.pm.PackageParser$Component");
			Class<?> userHandleClass = Class.forName("android.os.UserHandle");
			Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
			Class<?> packageParser$activityClass = Class.forName("android.content.pm.PackageParser$Activity");
			Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class,int.class);
			Method generateActivityInfoMethod = packageParserClass.getDeclaredMethod("generateActivityInfo", packageParser$activityClass,int.class,packageUserStateClass,int.class);
			
			//参数1：activity
			Object packageParseObj = packageParserClass.newInstance();
			Field activitiesField = pakcageParser$packageClass.getDeclaredField("activities");
			Object packageObj = parsePackageMethod.invoke(packageParseObj, apkFile,PackageManager.GET_ACTIVITIES);
			List acitivities = (List) activitiesField.get(packageObj);
			
			//参数2：0
			//参数3：userstate
			Object packageUserStateObj = packageUserStateClass.newInstance();
			
			//参数4：userId;
			Method getCallingUserIdMethod = userHandleClass.getDeclaredMethod("getCallingUserId");
			int userId = (Integer) getCallingUserIdMethod.invoke(null);
			
			
			Field intentsField = packageParser$componentClass.getDeclaredField("intents");
			
			for(Object activity:acitivities){
				ActivityInfo info = (ActivityInfo) generateActivityInfoMethod.invoke(packageParseObj, activity,0,packageUserStateObj,userId);
				List<? extends IntentFilter> filters = (List<? extends IntentFilter>) intentsField.get(activity);
				mActivitiesMap.put(info, filters);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return mActivitiesMap;
	}
}
