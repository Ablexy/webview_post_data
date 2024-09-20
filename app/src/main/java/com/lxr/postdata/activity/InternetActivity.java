package com.lxr.postdata.activity;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.lxr.postdata.R;
import com.lxr.postdata.common.WriteHandlingWebViewClient;
import com.tencent.smtt.sdk.CookieSyncManager;

import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;

public class InternetActivity extends AppCompatActivity {
    private ImageView ivBack;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet);

        ivBack = findViewById(R.id.iv_back);
        webView = findViewById(R.id.web_view);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        String url = getIntent().getStringExtra("url");

        WebSettings settings = webView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setWebViewClient(new WriteHandlingWebViewClient(webView) {

        });
        webView.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        //TODO 遗留二级页面跳转问题

        //        如果是get请求，直接拦截url请求即可
//                        webView.setWebViewClient(new WebViewClient() {
//                                    @Override
//                                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                                        try {
//                                            URL url = new URL(request.getUrl().toString());
//                                        } catch (MalformedURLException e) {
//                                            e.printStackTrace();
//                                        }
//                                        Log.e("InternetActivity", request + "");
//                                        return super.shouldInterceptRequest(view, request);
//                                    }
//
//                                });

        webView.loadUrl(url);
    }

}
