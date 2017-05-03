package com.yyh.autoplugin.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.yyh.autoplugin.utils.ApkUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/4/14.
 */
public class PluginInstrumentation extends Instrumentation {
    private String apkPath;
    private  Context mContext;
    public PluginInstrumentation(Context context, String apkPath) {
        this.apkPath = apkPath;
        this.mContext = context;
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        try {
            String className = activity.getComponentName().getClassName();
            String packageName1 = activity.getPackageName();
            Field mBaseField = Activity.class.getSuperclass().getSuperclass().getDeclaredField("mBase");
            mBaseField.setAccessible(true);
            Context mBase = (Context) mBaseField.get(activity);

            Class<?> contextImplClass = Class.forName("android.app.ContextImpl");
            Field mResourcesField = contextImplClass.getDeclaredField("mResources");
            mResourcesField.setAccessible(true);

            String dexPath = activity.getFileStreamPath(apkPath).getAbsolutePath();//ApkUtils.DOWNLOAD_PATH + apkPath;
           // String dexPath2 = mContext.getApplicationContext().getPackageResourcePath();

            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager, dexPath);
           // addAssetPathMethod.invoke(assetManager, dexPath2);

            Method ensureStringBlocksMethod = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocksMethod.setAccessible(true);
            ensureStringBlocksMethod.invoke(assetManager);

            Resources superRes = mContext.getResources();
            Resources resources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
            String packageName = "com.example.assetapp";
            int identifier = resources.getIdentifier("icon", "drawable", packageName);
            int drawable1 = resources.getIdentifier("", "drawable", packageName);
            int layout = resources.getIdentifier("activity_asset", "layout", packageName);
            XmlResourceParser layout1 = resources.getLayout(layout);
            View view = LayoutInflater.from(activity).inflate(layout, null);
            activity.setContentView(view);
            Drawable drawable = resources.getDrawable(resources.getIdentifier("icon","drawable",packageName));
            mResourcesField.set(mBase, resources);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.callActivityOnCreate(activity, icicle);

    }

}
