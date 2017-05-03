package com.yyh.autoplugin.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.yyh.autoplugin.parser.PluginManfiestParser;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;

public class ActivitiesManager {

    /**
     * 获取插件中的启动Activity
     * @return
     */
	public static ActivityInfo getLaunchActivityInfo(File apkFile){
		Map<ActivityInfo, List<? extends IntentFilter>> map = PluginManfiestParser.getApkActivityInfos(apkFile);
		
		boolean isMain = false;
		boolean isLaunch = false;;
		ActivityInfo activityInfo = null;
		for(ActivityInfo info:map.keySet()){
			List<? extends IntentFilter> list = map.get(info);
			for(IntentFilter filter:list){
				int countActions = filter.countActions();
				for(int i = 0;i<countActions;i++){
					if (filter.getAction(i).equals("android.intent.action.MAIN")) {
						isMain = true;
						break;
					}
				}
				
				int countCategories = filter.countCategories();
				for(int i = 0;i<countActions;i++){
					if (filter.getCategory(i).equals("android.intent.category.LAUNCHER")) {
						isLaunch = true;
						break;
					}
				}
				if (isLaunch&&isMain) {
					break;
				}
			}
			if (isLaunch&&isMain) {
				activityInfo = info;
				break;
			}
		}
		return activityInfo;
	}
}
