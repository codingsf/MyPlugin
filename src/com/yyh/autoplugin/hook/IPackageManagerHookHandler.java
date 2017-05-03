package com.yyh.autoplugin.hook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.content.pm.PackageInfo;
import android.util.Log;

/**
 * 
 */
public class IPackageManagerHookHandler implements InvocationHandler{

    private Object mBase;

    public IPackageManagerHookHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getPackageInfo")) {
            PackageInfo packageInfo = new PackageInfo();
            String packageName = packageInfo.packageName;
            Log.e("PackageInfo","packagename = " + packageName);
            return packageInfo;
        }
        return method.invoke(mBase, args);
    }
}
