package com.example.myplugin;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yyh.autoplugin.utils.ApkOperator;

public class MulAppActivity extends Activity {

    private ListView lv_assets;
    private List<ApkItem> apkItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset);

        initView();
        initData();
        initAdapter();
    }

    private void initAdapter() {
        lv_assets.setAdapter(new MyAdapter(this));
    }

    private void initData() {
        apkItemList = new ArrayList<ApkItem>();
        try{
            AssetManager assetManager = getAssets();
            String[] list = assetManager.list("");
            if (list != null){
                for(String name : list){
                    if (name.endsWith(".apk")){
                    	boolean apkInstalled = ApkOperator.isAPKInstalled(this, getFileStreamPath(name));
                        ApkItem apkItem = new ApkItem(name, apkInstalled);
                        apkItemList.add(apkItem);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initView() {
        lv_assets = (ListView) findViewById(R.id.lv_assets);
    }

    private class MyAdapter extends BaseAdapter{

        private Context mContext;
        MyAdapter(Context context){
            this.mContext = context;
        }
        @Override
        public int getCount() {
            return apkItemList.size();
        }

        @Override
        public ApkItem getItem(int position) {
            return apkItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ApkItem apkItem = getItem(position);
            Holder holder = null;
            if (convertView == null){
                holder = new Holder();
                convertView = View.inflate(mContext, R.layout.asset_item, null);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                holder.btn_start = (Button) convertView.findViewById(R.id.btn_start);
                holder.btn_stop = (Button) convertView.findViewById(R.id.btn_stop);
                convertView.setTag(holder);
            }else{
                holder = (Holder) convertView.getTag();
            }
            changeText(apkItem, holder);
            addClickEvent(mContext, apkItem, holder);
            return convertView;
        }

        private void addClickEvent(final Context context, final ApkItem apkItem, final Holder holder) {
            holder.btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {                	
                	if (!apkItem.isInstalled()){
                        boolean install = ApkOperator.installApk(context, apkItem.getName());
                        if (install){
                            Toast.makeText(context, "安装apk成功", Toast.LENGTH_SHORT).show();
                            apkItem.setInstalled(true);
                            changeText(apkItem, holder);
                        }
                    }else{
                        try{
                            ApkOperator.launchApk(context,apkItem.getName());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
            
            holder.btn_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!apkItem.isInstalled()){
                        //删除应用
                    }else{
                        //卸载应用
                    	boolean uninstalledApk = ApkOperator.uninstalledApk(context, apkItem.getName());
                    	if (uninstalledApk) {
							Toast.makeText(context, "卸载成功", 1).show();
							apkItem.setInstalled(false);
							changeText(apkItem, holder);
						}else {
							Toast.makeText(context, "卸载成功", 1).show();
						}
                    }
                }
            });
        }

        private void changeText(ApkItem apkItem, Holder holder) {
            holder.tv_name.setText(apkItem.getName());
            if (apkItem.isInstalled()){
                holder.btn_start.setText("启动");
                holder.btn_stop.setText("卸载");
            }else{
                holder.btn_start.setText("安装");
                holder.btn_stop.setText("删除");
            }
        }

        class Holder{
            TextView tv_name;
            Button btn_start;
            Button btn_stop;
        }
    }
}
