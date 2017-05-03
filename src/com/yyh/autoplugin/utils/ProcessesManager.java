package com.yyh.autoplugin.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Process;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件进程管理
 * Created by Administrator on 2017/1/23.
 */
public class ProcessesManager {

    public static boolean plugin = false;

    public static boolean isPlugin() {
        return plugin;
    }

    public static void setPlugin(boolean plugin) {
        ProcessesManager.plugin = plugin;
    }

    private static List<String> sProcessList = new ArrayList<String>();

    public static List<String> getProcessList(Context context){
        initProcessList(context);
        return sProcessList;
    }
    private static String getCurrentProcessName(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = activityManager.getRunningAppProcesses();

        if (infos == null){
            return null;
        }

        for(ActivityManager.RunningAppProcessInfo info:infos){
            if (info.pid == Process.myPid()){
                return info.processName;
            }
        }
        return null;
    }

    private static void initProcessList(Context context){
        try {
            if (sProcessList.size() > 0) {
                return;
            }

            sProcessList.add(context.getPackageName());
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS);
            if(packageInfo.receivers!=null){
                for(ActivityInfo info:packageInfo.receivers){
                    if (!sProcessList.contains(info.processName)){
                        sProcessList.add(info.processName);
                    }
                }
            }

            if (packageInfo.providers!=null){
                for (ProviderInfo info:packageInfo.providers){
                    if (!sProcessList.contains(info.processName)&&info.processName!=null && info.authority!=null){
                        sProcessList.add(info.processName);
                    }
                }
            }

            if (packageInfo.services!=null){
                for (ServiceInfo info:packageInfo.services){
                    if (!sProcessList.contains(info.processName)&&info.processName!=null&&info.name!=null){
                        sProcessList.add(info.processName);
                    }
                }
            }

            if (packageInfo.activities !=null){
                for (ActivityInfo info:packageInfo.activities){
                    if (!sProcessList.contains(info.processName)&& info.processName!=null && info.name!=null){
                        sProcessList.add(info.processName);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isPluginProcess(Context context){
        String currentProcessName = getCurrentProcessName(context);
        if (TextUtils.equals(currentProcessName,context.getPackageName())){
            return false;
        }

        initProcessList(context);

        return !sProcessList.contains(currentProcessName);
    }


    public static void killAllProcess(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        am.killBackgroundProcesses("com.example.testapp");

        for(ActivityManager.RunningAppProcessInfo info : infos){
            Process.killProcess(info.pid);
        }
    }


}
