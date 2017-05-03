package com.yyh.autoplugin.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;

import java.lang.reflect.Method;

/**
 * 插件资源管理者
 */
public class AssetsManager {

    public static AssetManager createAssetManager(Context context, String apkPath){
        try{

            String dexPath = context.getFileStreamPath(apkPath).getAbsolutePath();//ApkUtils.DOWNLOAD_PATH + apkPath;//插件apk
          //  String dexPath2 = context.getApplicationContext().getPackageResourcePath();//宿主apk.

            //加载
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
          //  addAssetPathMethod.invoke(assetManager, dexPath2);
            addAssetPathMethod.invoke(assetManager, dexPath);

            //初始化其内部参数
            Method ensureStringBolcksMethod = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            ensureStringBolcksMethod.setAccessible(true);
            ensureStringBolcksMethod.invoke(assetManager);
            return assetManager;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Resources getBoundleResource(Context context, String apkPath){
        AssetManager assetManager = createAssetManager(context, apkPath);
        return new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
    }

}
