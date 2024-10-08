package com.lxr.postdata.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSONObject;
import com.lxr.postdata.R;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initX5();
        Button button = findViewById(R.id.btn_jump);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InternetActivity.class);//appacce.yundasys.com
                //多页面容器离线测试，post请求拦截
                String url = "https://u-ydwt-pro.yundasys.com:35529/online/golden_finger/index.html#/goldenfinger?user_id=90150089&userid=90150089&userId=90150089&dbct_cd=0&site_code=0&companyNo=0&username=金亮";
                //测试h5绘制寄件码截图
//                String url = "http://kyweixin.yunda56.com/ky/view/shareSalesman.html?gh_oa=1734121002";
//                String url = "https://m.baidu.com";
                  //待办
//                String url = "http://10.172.248.119:31094/flowMenu/common?flowId=165024611233023";
                intent.putExtra("url", url);
                startActivity(intent);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //耗时逻辑
//                        try {
//                            getTaskId("","");
//                            getAppAccessToken();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
            }
        });
    }

    public String getAppAccessToken() throws IOException{
        String urlStr = "https://sit-pxapi.yundasys.com:443/gateway/interface";
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setUseCaches(false); // Post请求不能使用缓存
        connection.setDoInput(true);// 设置是否从HttpURLConnection输入，是否可以读数据，默认值为 true
        connection.setDoOutput(true);// 设置是否使用HttpURLConnection进行输出,写入数据，默认值为 false,设置false也可以写进去/？
        connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
        JSONObject  params = JSONObject.parseObject("{\n" +
                "    \"action\": \"yygserv.app.jsz.selectPolicyFlowCollect\",\n" +
                "    \"data\": {\n" +
                "        \"userId\": \"90150089\",\n" +
                "        \"flag\": \"2\",\n" +
                "        \"userRole\": \"2\",\n" +
                "        \"startTime\": \"2021-11-28 23:59:59\",\n" +
                "        \"endTime\": \"2022-11-28 00:00:00\",\n" +
                "        \"DateType\": 2\n" +
                "    },\n" +
                "    \"appid\": \"js6ueore28wdyjpy\",\n" +
                "    \"version\": \"V1.0\",\n" +
                "    \"req_time\": 1669620761862\n" +
                "}");
        connection.connect();

        // 得到请求的输出流对象
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8");
        writer.write(params.toString());
        writer.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
        StringBuffer document = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null){
            document.append(line);
        }
        reader.close();
//        InputStreamReader reader1 = new InputStreamReader(connection.getInputStream(),"utf-8");
//        int len = 0;
//        while ((len = reader1.read())!=-1){
//            document.append((char)len);
//        }
//        reader1.close();
        System.out.println(document);
        JSONObject json =JSONObject.parseObject(document.toString());
        String success=json.getString("success");
        System.out.println("success "+success);
        return success;
    }



    public String getTaskId(String accessToken, String json) throws IOException{
        json = "{\n" +
                "    \"action\": \"yygserv.app.jsz.selectPolicyFlowCollect\",\n" +
                "    \"data\": {\n" +
                "        \"userId\": \"90150089\",\n" +
                "        \"flag\": \"2\",\n" +
                "        \"userRole\": \"2\",\n" +
                "        \"startTime\": \"2021-11-28 23:59:59\",\n" +
                "        \"endTime\": \"2022-11-28 00:00:00\",\n" +
                "        \"DateType\": 2\n" +
                "    },\n" +
                "    \"appid\": \"js6ueore28wdyjpy\",\n" +
                "    \"version\": \"V1.0\",\n" +
                "    \"req_time\": 1669620761862\n" +
                "}";
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
        String url = "https://sit-pxapi.yundasys.com:443/gateway/interface";
        //String json="{\"create_time_range\": {\"end\": \"1651398061\",\"start\": \"1648806061\"},\"keywords\": [\"关键词1\"],\"owner_ids\": [\"8d96149a\"],\"task_name\": \"任务名称\"}";
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);

        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(url)
                .addHeader("Authorization","Bearer "+accessToken)
                .addHeader("Content-Type","application/json; charset=utf-8")
                .post(body)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        ResponseBody data = response.body();
        JSONObject obj =JSONObject.parseObject(data.string());
        String taskId = obj.getJSONObject("body").getString("status");
        System.out.println("taskId:"+taskId);
        return taskId;
    }


    private void initX5(){
        //在初始化前可配置允许移动网络下载内核
        QbSdk.setDownloadWithoutWifi(true);
        //设置开启优化方案+service
        // 在调用TBS初始化、创建WebView之前进行如下配置
        HashMap map = new HashMap();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
        QbSdk.initX5Environment(getApplicationContext(),  new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                // 内核初始化完成，可能为系统内核，也可能为系统内核
                Log.i("444","内核初始化完成");
            }

            /**
             * 预初始化结束
             * 由于X5内核体积较大，需要依赖网络动态下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
             * @param isX5 是否使用X5内核
             */
            @Override
            public void onViewInitFinished(boolean isX5) {
                Log.i("444","内核初始化完成-是X5吗？"+isX5);

            }
        });
    }



}
