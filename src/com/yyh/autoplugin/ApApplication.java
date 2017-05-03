package com.yyh.autoplugin;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.yyh.autoplugin.hook.HookHelper;
import com.yyh.autoplugin.utils.AssetsManager;
import com.yyh.autoplugin.utils.ProcessesManager;

/**
 * Created by Administrator on 2017/3/20.
 */
public class ApApplication extends Application{

    public static Context hostContext;
    public static List<Context> pluginList = new ArrayList<Context>();

    private AssetManager assetManager;
    private Resources newResource;
    private Resources.Theme mTheme;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (ProcessesManager.isPlugin()){
            pluginList.add(base);
            String assetApk = "AssetApp.apk";
            //创建自己的Resouce
            assetManager = AssetsManager.createAssetManager(this, assetApk);
            Resources supResource = getResources();
            newResource = new Resources(assetManager, supResource.getDisplayMetrics(), supResource.getConfiguration());
            mTheme = newResource.newTheme();
            mTheme.setTo(super.getTheme());
        }else {
            hostContext = base;
            try {
                HookHelper.hookActivityManagerNative();
                HookHelper.hookActivityThreadHandler();
			} catch (Exception e) {
			}
        }

    }

    public static Context getContext(){
        return hostContext;
    }
}
