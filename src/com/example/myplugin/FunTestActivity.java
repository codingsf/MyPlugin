package com.example.myplugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yyh.autoplugin.utils.ApkOperator;
import com.yyh.autoplugin.utils.AssetsManager;

public class FunTestActivity extends Activity {

    private static final Uri URI = Uri.parse("content://com.example.cpapp.CpContentProvider");
    static int count = 0;
    ImageView imageView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        
    }

    private void init() {
    	Button b = new Button(this);
        b.setText("Test AssetApp.apk");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ApkOperator.isAPKInstalled(FunTestActivity.this, getFileStreamPath("AssetApp.apk"))) {
    				ApkOperator.installApk(FunTestActivity.this, "AssetApp.apk");
    			}
                ApkOperator.launchApk(FunTestActivity.this, "AssetApp.apk");
            }
        });

        Button button_service = new Button(this);
        button_service.setText("start Service");
        button_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (!ApkOperator.isAPKInstalled(FunTestActivity.this, getFileStreamPath("ServiceApp.apk"))) {
					ApkOperator.installApk(getApplicationContext(), "ServiceApp.apk");
				}
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.serviceapp","com.example.serviceapp.Myservice"));
                startService(intent);
            }
        });

        Button button_service2 = new Button(this);
        button_service2.setText("stop Service");
        button_service2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (!ApkOperator.isAPKInstalled(FunTestActivity.this, getFileStreamPath("ServiceApp.apk"))) {
					ApkOperator.installApk(getApplicationContext(), "ServiceApp.apk");
				}
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.serviceapp","com.example.serviceapp.Myservice"));
                stopService(intent);
            }
        });

        Button button_receiver = new Button(this);
        button_receiver.setText("send broadcast");
        button_receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (!ApkOperator.isAPKInstalled(FunTestActivity.this, getFileStreamPath("ReceiverApp.apk"))) {
					ApkOperator.installApk(FunTestActivity.this, "ReceiverApp.apk");
				}
                Intent intent = new Intent();
                intent.setAction("com.example.receiverapp.MyReceiver");
                sendBroadcast(intent);
            }
        });

        Button query = new Button(this);
        query.setText("query");
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (!ApkOperator.isAPKInstalled(FunTestActivity.this, getFileStreamPath("CpApp.apk"))) {
					ApkOperator.installApk(getApplicationContext(), "CpApp.apk");
				}
                Cursor cursor = getContentResolver().query(URI, null, null, null, null);
                assert cursor !=null;
                StringBuffer result = new StringBuffer();
                while(cursor.moveToNext()){
                    int count = cursor.getColumnCount();
                    StringBuilder sb = new StringBuilder("column:");
                    for(int i = 0;i<count;i++){
                        sb.append(cursor.getString(i)+", ");
                    }
                    Log.d("TAG",sb.toString());
                    result.append(sb.toString());
                }
                Toast.makeText(FunTestActivity.this,"结果为： " + result.toString(), Toast.LENGTH_LONG).show();
            }
        });

        Button insert = new Button(this);
        insert.setText("insert");
        insert.setOnClickListener(new View.OnClickListener(	) {
            @Override
            public void onClick(View v) {
            	if (!ApkOperator.isAPKInstalled(FunTestActivity.this, getFileStreamPath("CpApp.apk"))) {
					ApkOperator.installApk(getApplicationContext(), "CpApp.apk");
				}
                ContentValues values = new ContentValues();
                values.put("name", "title"+count++);
                getContentResolver().insert(URI, values);
            }
        });

        imageView = new ImageView(this);
        imageView.setBackgroundColor(Color.argb(1,255,0,0));

        Button button_asset = new Button(this);
        button_asset.setText("assert");
        button_asset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (!ApkOperator.isAPKInstalled(FunTestActivity.this, getFileStreamPath("AssetApp.apk"))) {
					ApkOperator.installApk(FunTestActivity.this, "AssetApp.apk");
				}
                captureAsset();
            }
        });

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(b);
        ll.addView(button_service);
        ll.addView(button_service2);
        ll.addView(button_receiver);
        ll.addView(insert);
        ll.addView(query);
        ll.addView(button_asset);
        ll.addView(imageView);
        setContentView(ll);		
	}

	private void captureAsset() {
        Resources resources = AssetsManager.getBoundleResource(this, "AssetApp.apk");
        String packageName = "com.example.assetapp";
        Drawable drawable = resources.getDrawable(resources.getIdentifier("icon","drawable",packageName));
        imageView.setImageDrawable(drawable);
    }
}
