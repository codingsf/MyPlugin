package com.yyh.autoplugin.proxy;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class ProxyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button b = new Button(this);
        b.setText("我是Stub");
        setContentView(b);
    }
}
