package com.yyh.autoplugin.hook;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.yyh.autoplugin.ApApplication;
import com.yyh.autoplugin.proxy.ProxyService;
import com.yyh.autoplugin.proxy.ProxyActivity;
import com.yyh.autoplugin.utils.ServicesManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/3/20.
 */
public class IActivityManagerHandler implements InvocationHandler {
    private static final String TAG = "IActivityManagerHandler";

    Object mBase;

    public IActivityManagerHandler(Object base) {
        this.mBase = base;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (method.getName().equals("startService")) {
            Pair<Integer, Intent> integerIntentPair = foundFirstIntentOfArgs(args);
            String targetPackageName = integerIntentPair.second.getComponent().getPackageName();
            String stubPackage = ApApplication.getContext().getPackageName();
            if (!stubPackage.equals(targetPackageName)) {
            	 Intent newIntent = new Intent();
                 ComponentName componentName = new ComponentName(stubPackage, ProxyService.class.getName());
                 newIntent.setComponent(componentName);
                 newIntent.putExtra(HookHelper.EXTRA_SERVICE_INTENT, integerIntentPair.second);
                 args[integerIntentPair.first] = newIntent;
                 Log.e("TAG", "hook method startService success");
			}
            return method.invoke(mBase, args);
        }

        if ("stopService".equals(method.getName())) {
            Intent raw = foundFirstIntentOfArgs(args).second;
            if (!TextUtils.equals(ApApplication.getContext().getPackageName(), raw.getComponent().getPackageName())) {
                Log.e("TAG","hook method stopService success");
                return ServicesManager.getInstance().stopService(raw);
            }
        }


        if ("startActivity".equals(method.getName())) {
                Intent raw;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        break;
                    }
                }

                raw = (Intent) args[index];
                String hostPackageName = ApApplication.getContext().getPackageName();
                String targetPackageName = raw.getComponent().getPackageName();
                
                if (hostPackageName != null && !hostPackageName.equals(targetPackageName)) {
                    Intent newIntent = new Intent();
                    //Proxy Intent字段参数3：
                    ComponentName componentName = new ComponentName(hostPackageName, ProxyActivity.class.getName());
                    //ComponentName componentName = new ComponentName("com.example.autoplugintest", "com.example.autoplugintest.AActivity");
                    newIntent.setComponent(componentName);
                    newIntent.putExtra(HookHelper.EXTRA_ACTIVITY_INTENT, raw);
                    args[index] = newIntent;

                    Log.d(TAG, "hook success");
				}
            //return method.invoke(mBase, args);
        }

        return method.invoke(mBase, args);
    }

    private Pair<Integer, Intent> foundFirstIntentOfArgs(Object[] args) {
        int index = 0;
        for(int i = 0;i<args.length;i++){
            if (args[i] instanceof Intent ) {
                index = i;
                break;
            }
        }
        return new Pair<Integer, Intent>(index, (Intent) args[index]);
    }

}
