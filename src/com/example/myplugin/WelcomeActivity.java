package com.example.myplugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WelcomeActivity extends Activity {

    private Button btn_frist;
    private Button btn_second;
	private Button btn_third;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initView();
        initEvent();
    }

	private void initEvent() {
        btn_frist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, FunTestActivity.class));
            }
        });

        btn_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, MulAppActivity.class));
            }
        });
        
        btn_third.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(WelcomeActivity.this,ThirdAppActivity.class));
			}
		});
    }

    private void initView() {
        btn_frist = (Button) findViewById(R.id.btn_first);
        btn_second = (Button) findViewById(R.id.btn_second);
        btn_third = (Button) findViewById(R.id.btn_third);
    }
}
