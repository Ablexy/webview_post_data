package com.lxr.postdata.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class WriteHandlingWebViewClient extends WebViewClient {

    private String TAG = "WriteHandlingWebViewClient";

    private final String MARKER = "AJAXINTERCEPT";

    /**
     * 请求参数Map集
     */
    private Map<String, String> ajaxRequestContents = new HashMap<>();

    public WriteHandlingWebViewClient(WebView webView) {
        AjaxInterceptJavascriptInterface ajaxInterface = new AjaxInterceptJavascriptInterface(this);
        webView.addJavascriptInterface(ajaxInterface, "interception");
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Log.i(TAG,"onPageStarted----");
    }

    /*
     ** This here is the "fixed" shouldInterceptRequest method that you should override.
     ** It receives a WriteHandlingWebResourceRequest instead of a WebResourceRequest.
     */
    public WebResourceResponse shouldInterceptRequest(final WebView view, WriteHandlingWebResourceRequest request) {
        OkHttpClient client = new OkHttpClient();
        try {
            // Our implementation just parses the response and visualizes it. It does not properly handle
            // redirects or HTTP errors at the moment. It only serves as a demo for intercepting POST requests
            // as a starting point for supporting multiple types of HTTP requests in a full fletched browser

            // Construct request

//            HttpURLConnection conn = client.open(new URL(request.getUrl().toString()));
            URL url = new URL(request.getUrl().toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            conn.setRequestMethod(request.getMethod());
//            conn.setDoOutput(true);//向链接写入数据
//            conn.setDoInput(true);
            if ("POST".equals(request.getMethod())) {
                if (!conn.getDoOutput()){
                    conn.setDoOutput(true);
                }
                OutputStream os = conn.getOutputStream();
                try {
                    //将要post的传参的数据写入输出流
                    if (request.getAjaxData()!=null){
                        os.write(request.getAjaxData().getBytes("UTF-8"));
                        os.flush();//用于网络传输，需要强制刷新的缓存区，否则输出的数据只停留在缓冲区中，而无法进行网络传输
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                os.close();
            }

            // Read input
            String charset = conn.getContentEncoding() != null ? conn.getContentEncoding() : Charset.defaultCharset().displayName();
            String mime = conn.getContentType();
            if (conn.getInputStream()!=null){
                byte[] pageContents = Utils.consumeInputStream(conn.getInputStream());
                String res = new String(pageContents,"utf-8");;
                // Convert the contents and return
                InputStream isContents = new ByteArrayInputStream(pageContents);

                return new WebResourceResponse(mime, charset, isContents);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * 拦截了webview中的所有请求
     *
     * @param view
     * @param request
     * @return
     */
    @Override
    public final WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {
        Log.i(TAG,"shouldInterceptRequest----"+request.getUrl());
        String requestBody = null;
        Uri uri = request.getUrl();

        // 判断是否为Ajax请求（只要链接中包含AJAXINTERCEPT即是）
        if (isAjaxRequest(request)) {
            // 获取post请求参数
            requestBody = getRequestBody(request);
            // 获取原链接
            uri = getOriginalRequestUri(request, MARKER);
        }

        // 重新构造请求，并获取response
        WebResourceResponse webResourceResponse = shouldInterceptRequest(view, new WriteHandlingWebResourceRequest(request, requestBody, uri));
        if (webResourceResponse == null) {
            return webResourceResponse;
        } else {
            return injectIntercept(webResourceResponse, view.getContext());
        }

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Log.i(TAG,"shouldOverrideUrlLoading--request--"+request.getUrl().toString());
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i(TAG,"shouldOverrideUrlLoading--url--"+url);
        return super.shouldOverrideUrlLoading(view, url);
    }

    void addAjaxRequest(String id, String body) {
        ajaxRequestContents.put(id, body);
    }

    /**
     * 获取post请求参数
     *
     * @param request
     * @return
     */
    private String getRequestBody(WebResourceRequest request) {
        String requestID = getAjaxRequestID(request);
        return getAjaxRequestBodyByID(requestID);
    }

    /**
     * 判断是否为Ajax请求
     *
     * @param request
     * @return
     */
    private boolean isAjaxRequest(WebResourceRequest request) {
        return request.getUrl().toString().contains(MARKER);
    }

    private String[] getUrlSegments(WebResourceRequest request, String divider) {
        String urlString = request.getUrl().toString();
        return urlString.split(divider);
    }

    /**
     * 获取请求的id
     *
     * @param request
     * @return
     */
    private String getAjaxRequestID(WebResourceRequest request) {
        return getUrlSegments(request, MARKER)[1];
    }

    /**
     * 获取原链接
     *
     * @param request
     * @param marker
     * @return
     */
    private Uri getOriginalRequestUri(WebResourceRequest request, String marker) {
        String urlString = getUrlSegments(request, marker)[0];
        return Uri.parse(urlString);
    }

    /**
     * 通过请求id获取请求参数
     *
     * @param requestID
     * @return
     */
    private String getAjaxRequestBodyByID(String requestID) {
        String body = ajaxRequestContents.get(requestID);
        ajaxRequestContents.remove(requestID);
        return body;
    }

    /**
     * 如果请求是网页，则html注入
     *
     * @param response
     * @param context
     * @return
     */
    private WebResourceResponse injectIntercept(WebResourceResponse response, Context context) {
        String encoding = response.getEncoding();
        String mime = response.getMimeType();

        // WebResourceResponse的mime必须为"text/html",不能是"text/html; charset=utf-8"
        if (mime.contains("text/html")) {
            mime = "text/html";
        }

        InputStream responseData = response.getData();
        InputStream injectedResponseData = injectInterceptToStream(
                context,
                responseData,
                mime,
                encoding
        );
        return new WebResourceResponse(mime, encoding, injectedResponseData);
    }

    /**
     * 如果请求是网页，则html注入
     *
     * @param context
     * @param is
     * @param mime
     * @param charset
     * @return
     */
    private InputStream injectInterceptToStream(Context context, InputStream is, String mime, String charset) {
        try {
            byte[] pageContents = Utils.consumeInputStream(is);
            if (mime.contains("text/html")) {
                pageContents = AjaxInterceptJavascriptInterface
                        .enableIntercept(context, pageContents)
                        .getBytes(charset);
            }

            return new ByteArrayInputStream(pageContents);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}