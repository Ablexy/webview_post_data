package com.lxr.postdata.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.tencent.smtt.sdk.WebView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 类功能：
 * 类作者：able
 * 类日期: 2022/12/6
 **/
public class WebViewCaptureUtils {



    //普通Webview，包含（网页通过canvas绘制的二维码）的截图均不能显现
    /***
     * 普通webview二维码不正常；x5-二维码正常
     * 过期api，对webvierw进行截图 5.0一下版本
     * @param webView
     * @return 可以截取长图
     */
    public static Bitmap captureWebViewKitKat(WebView webView) {
        Picture picture = webView.capturePicture();
        int width = picture.getWidth();
        int height = picture.getHeight();
        if (width > 0 && height > 0) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            picture.draw(canvas);
            return bitmap;
        }
        return null;
    }


    /***
     * 一个界面中，重复截取图片，在每次截屏之前，都应该清除缓存,可以通过Bitmap.createBitmap( )方法处理图片-压缩等
     * @param webView
     * @return 截屏--当前界面显示等内容，非长图
     */
    public static Bitmap captureWebViewbyCache(WebView webView) {
        webView.setDrawingCacheEnabled(true);//设置能否缓存图片信息（drawing cache）
        webView.buildDrawingCache();//如果能够缓存图片，则创建图片缓存
        Bitmap bitmap = webView.getDrawingCache();//如果图片已经缓存，返回一个bitmap
        //webView.destroyDrawingCache();//释放缓存占用的资源--图片保存成功之后可调用
        return bitmap;
    }


    /**
     * 普通webview，x5二维码不正常
     * 5.0以上对webview做了优化，为了减少内存和提高性能，使用WebView加载网页时只绘制显示部分，只截取显示部分，
     * 不做处理，仍然使用本代码截图的话，就会出现只截到屏幕内显示的WebView内容，其它部分是空白的情况
     * 如果WebView实例被创建前加入代码，可全屏截图
     * setcontentView之前
     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
     *     android.webkit.WebView.enableSlowWholeDocumentDraw();
     * }
     * @param webView
     * @return 二维码没存上
     */
    public static Bitmap captureWebViewLollipop(WebView webView) {
        float scale = webView.getScale();
        int width = webView.getWidth();
        int height = (int) (webView.getContentHeight() * scale + 0.5);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);
        return bitmap;
    }

    //X5内核截图

    /**
     * 截图整个页面缩略图，就是不以屏幕上WebView的宽高截图，
     * 只是以WebView的contentWidth和contentHeight为宽高截图，所以截出来的图片会不怎么清晰
     * @param context
     * @param webView
     * @return 二维码正常。缩略图
     */
    public static Bitmap captureX5WebViewUnsharp(Context context, WebView webView) {
        if (webView == null) {
            return null;
        }
        if (context == null) {
            context = webView.getContext();
        }
        int width = webView.getContentWidth();
        int height = webView.getContentHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        webView.getX5WebViewExtension().snapshotWholePage(canvas, false, false);
        return bitmap;
    }

    /**
     * x5内核截图高清图，也可使用过期api
     * 有x5内核没有生效，并且Android版本是5.0及以上时，调用enableSlowWholeDocumentDraw()方便截取长图
     * if (!isX5Enabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
     *     android.webkit.WebView.enableSlowWholeDocumentDraw();
     * }
     */
    public static Bitmap captureWholePage(WebView webView) {
        try {
            Bitmap bitmap = captureWebViewKitKat(webView);
            /* 对拿到的bitmap根据需要进行处理 */
            return bitmap;
        } catch (OutOfMemoryError oom) {
            /* 对OOM做处理 */
            return null;
        }
    }



    /**
     *将Bitmap转成jpg保存到SD卡目录下
     * @param btImage
     */
    public static String  saveBitmpToLoca(Bitmap btImage, Context context){
        if (btImage==null) {
            Log.e("444", "path-null");
            return "";
        }
        if (Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED)) // 判断是否可以对SDcard进行操作
        {    // 获取SDCard指定目录下
            //
            String  sdCardDir = Environment.getExternalStorageDirectory()+ "/webviewCapture/";
            File dirFile  = new File(sdCardDir);  //目录转化成文件夹
            if (!dirFile .exists()) {              //如果不存在，那就建立这个文件夹
                dirFile .mkdirs();
            }                          //文件夹有啦，就可以保存图片啦
            File file = new File(sdCardDir,"AppQRcode.jpg");// 在SDcard的目录下创建图片文,以当前时间为其命名


            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(file);
                btImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                Log.e("444","path-"+file.getAbsolutePath());
                // 最后通知图库更新
                if(Build.VERSION.SDK_INT < 19) {
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));
                }else {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    context.sendBroadcast(intent);
                }
                return file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("444","path-null");
                return "";
            }finally {
                try {
                    if (out!=null) {
                        out.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            Log.e("444","path-null");
            return "";
        }
    }


    /**
     * 原生webview，二维码不能显示
     * @param mWebView
     * @return
     */
    public static Bitmap captureWebView(WebView mWebView) {
        // WebView 生成长图，也就是超过一屏的图片，代码中的 longImage 就是最后生成的长图
        mWebView.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        mWebView.layout(0, 0, mWebView.getMeasuredWidth(), mWebView.getMeasuredHeight());
        mWebView.setDrawingCacheEnabled(true);
        mWebView.buildDrawingCache();
        Bitmap longImage = Bitmap.createBitmap(mWebView.getMeasuredWidth(),
                mWebView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(longImage);    // 画布的宽高和 WebView 的网页保持一致
        Paint paint = new Paint();
        canvas.drawBitmap(longImage, 0, mWebView.getMeasuredHeight(), paint);
        mWebView.draw(canvas);
        return longImage;
    }
}
