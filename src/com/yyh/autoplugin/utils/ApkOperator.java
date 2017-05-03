package com.yyh.autoplugin.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.util.Log;

import com.yyh.autoplugin.ApApplication;
import com.yyh.autoplugin.exception.InstallException;
import com.yyh.autoplugin.hook.HostClassLoaderHookHelper;
import com.yyh.autoplugin.hook.MulClassLoaderHookHelper;
import com.yyh.autoplugin.parser.PluginManfiestParser;

/**
 * APK文件的安装、卸载、启动等功能
 * 
 * @author Administrator
 *
 */
public class ApkOperator {

	public static boolean installApk(Context context, String apkName) {
		try {
			AssetManager assets = context.getAssets();
			String[] list = assets.list("");
			if (!Arrays.asList(list).contains(apkName)) {
				throw new InstallException("AssetManager: file not exists");
			} else if (apkName.endsWith(".apk") || apkName.endsWith(".jar")) {
				// 提取apk文件。
				ApkUtils.extractAssets(context, apkName);
				// 构建插件类加载器。为启动插件，避免资源冲突。
				MulClassLoaderHookHelper.hookLoadedApkInActivityThread(context
						.getFileStreamPath(apkName));
				File apkFile = context.getFileStreamPath(apkName);
				File odexFile = context.getFileStreamPath(apkName.substring(0,
						apkName.lastIndexOf(".")) + ".odex");
				// 加载插件Service, Provider,Receiver到host中。
				ServicesManager.getInstance().preLoadServices(
						context.getFileStreamPath(apkName));
				ReceiversManager.preLoadReceiver(context, apkFile);
				HostClassLoaderHookHelper.patchClassLoader(
						context.getClassLoader(), apkFile, odexFile);
				ProvidersManager.installProviders(context,
						context.getFileStreamPath(apkName));
				return true;
			} else {
				throw new InstallException(
						"neither an apk file nor a jar file, can not be installed");
			}
		} catch (Exception e) {
			throw new InstallException(
					"installed exception, can't find .dex file");
		}
	}

	/**
	 * 启动APK
	 * 
	 * @param context
	 * @param apkFile
	 * @return
	 */
	public static boolean launchApk(Context context, String apkName) {
		File apkFile = context.getFileStreamPath(apkName);
		killPluginProcess(context);
		boolean success = false;
		Intent intent = new Intent();
		ActivityInfo launchActivityInfo = ActivitiesManager
				.getLaunchActivityInfo(apkFile);
		String packageName = launchActivityInfo.applicationInfo.packageName;
		String name = launchActivityInfo.name;
		ComponentName componentName = new ComponentName(packageName, name);
		if (componentName != null) {
			intent.setComponent(componentName);
			context.startActivity(intent);
			success = true;
			ProcessesManager.setPlugin(true);
			Log.e("TAG", "isplugin:	" + ProcessesManager.isPlugin());
		}
		return success;
	}

	private static void killPluginProcess(Context context) {
		List<Context> pluginList = ApApplication.pluginList;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		if (pluginList != null && !pluginList.isEmpty()) {
			for (Context c : pluginList) {
				am.killBackgroundProcesses(c.getPackageName());
			}
		}
	}

	public static boolean isAPKInstalled(Context context, File apkFile) {
		try {
			Class<?> activityThreadClass = Class
					.forName("android.app.ActivityThread");
			Method currentActivityThreadMethod = activityThreadClass
					.getDeclaredMethod("currentActivityThread");
			currentActivityThreadMethod.setAccessible(true);
			Object currentActivityThread = currentActivityThreadMethod
					.invoke(null);

			// 获取到 mPackages 这个静态成员变量, 这里缓存了dex包的信息
			Field mPackagesField = activityThreadClass
					.getDeclaredField("mPackages");
			mPackagesField.setAccessible(true);
			Map mPackages = (Map) mPackagesField.get(currentActivityThread);
			Set<String> keySet = mPackages.keySet();
			String packageName = PluginManfiestParser
					.getApkPackageName(apkFile);
			boolean extracted = ApkOperator.isApkExtracted(context,
					apkFile.getName());
			return extracted && keySet.contains(packageName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * @param context
	 * @param apkName
	 * @return
	 */
	private static boolean isApkExtracted(Context context, String apkName) {
		File parentFile = context.getFileStreamPath("");
		String[] list = parentFile.list();
		for (String name : list) {
			if (apkName.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static boolean uninstalledApk(Context context, String apkName) {
		if (isApkExtracted(context, apkName)) {
			boolean delete = context.getFileStreamPath(apkName).delete();
			return delete;
		}
		return false;
	}
}
