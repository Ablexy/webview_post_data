package com.lxr.postdata.activity;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.lxr.postdata.R;
import com.lxr.postdata.common.WebViewCaptureUtils;
import com.tencent.smtt.sdk.WebView;

import org.jsoup.helper.StringUtil;

public class WebviewCaptureActivity extends AppCompatActivity {
    private ImageView ivBack;
    private WebView mWebView;
    //动态申请sd卡读写权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.WebView.enableSlowWholeDocumentDraw();
        }
        setContentView(R.layout.activity_webview_capture);
        ivBack = findViewById(R.id.iv_back);
        mWebView = findViewById(R.id.web_view);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        String url = getIntent().getStringExtra("url");
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setDrawingCacheEnabled(true);
        mWebView.loadUrl(url);
        //申请存储权限
        verifyStoragePermissions(this);
        //网页截图保存照片
        findViewById(R.id.tv_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Bitmap bitmap = WebViewCaptureUtils.captureWebView(mWebView);
//                Bitmap bitmap = WebViewCaptureUtils.captureWebViewbyCache(mWebView);
                Bitmap bitmap = WebViewCaptureUtils.captureX5WebViewUnsharp(WebviewCaptureActivity.this,mWebView);
//                Bitmap bitmap =  WebViewCaptureUtils.captureWholePage(mWebView);
                String path = WebViewCaptureUtils.saveBitmpToLoca(bitmap, WebviewCaptureActivity.this);
                if (StringUtil.isBlank(path)){
                    Toast.makeText(WebviewCaptureActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(WebviewCaptureActivity.this,"保存成功",0).show();
                }
//                mWebView.destroyDrawingCache();
            }
        });
    }


    public  void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_EXTERNAL_STORAGE&&grantResults[0]==0){
            Toast.makeText(WebviewCaptureActivity.this,"申请权限成功",0).show();
        }else {
            Toast.makeText(WebviewCaptureActivity.this,"读写申请权限失败，请继续申请",0).show();
        }

    }
}