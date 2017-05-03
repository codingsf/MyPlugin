package com.example.myplugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.yyh.autoplugin.utils.ApkOperator;

/**
 */
public class ThirdAppActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button t = new Button(this);
        t.setText("launch mobilesafe");

        setContentView(t);

        Log.d("TAG", "context classloader: " + getApplicationContext().getClassLoader());
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                	if (!ApkOperator.isAPKInstalled(ThirdAppActivity.this, getFileStreamPath("mobilesafe.apk"))) {
						ApkOperator.installApk(ThirdAppActivity.this, "mobilesafe.apk");
					}
                    Intent t = new Intent();
                    t.setComponent(new ComponentName("com.example.mobilesafe", "com.example.activity.SplashActivity"));
                    startActivity(t);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
