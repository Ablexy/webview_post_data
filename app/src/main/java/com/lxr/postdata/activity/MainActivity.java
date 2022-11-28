package com.lxr.postdata.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.lxr.postdata.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn_jump);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InternetActivity.class);
                String url = "https://u-ydwt-pro.yundasys.com:35529/online/golden_finger/index.html#/goldenfinger?user_id=90150089&userid=90150089&userId=90150089&dbct_cd=0&site_code=0&companyNo=0&username=金亮";
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
    }

}
