package com.myrn.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myrn.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RequestManager {
    public enum RequestTypeEnum {GET,POST}
    private static RequestManager mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mOkHttpHandler;

    public static RequestManager getInstance(Context context) {
        RequestManager inst = mInstance;
        if (inst == null) {
            synchronized (RequestManager.class) {
                inst = mInstance;
                if (inst == null) {
                    inst = new RequestManager(context.getApplicationContext());
                    mInstance = inst;
                }
            }
        }
        return inst;
    }

    public RequestManager(Context context) {
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(10, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        //初始化Handler
        mOkHttpHandler = new Handler(context.getMainLooper());
    }

    public <T> Call Get(String actionUrl, HashMap<String, String> paramsMap, RequestCallBack<T> callBack) {
        return requestGetByAsync(actionUrl,paramsMap,callBack);
    }

    private Request.Builder addHeaders() {
        Request.Builder builder = new Request.Builder()
                .addHeader("Connection", "keep-alive")
                .addHeader("platform", "android")
                .addHeader("phoneModel", Build.MODEL)
                .addHeader("systemVersion", Build.VERSION.RELEASE)
                .addHeader("appVersion", BuildConfig.VERSION_NAME);
        return builder;
    }

    private <T> Call requestGetByAsync(String actionUrl, HashMap<String, String> paramsMap, final RequestCallBack<T> callBack) {
        StringBuilder tempParams = new StringBuilder();
        try {
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                pos++;
            }
            String requestUrl = String.format("%s%s?%s", BuildConfig.SERVER_HOST, actionUrl, tempParams.toString());
            final Request request = addHeaders().url(requestUrl).build();
            final Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    failureCallBack(new MyResponse<Object>(null,1001,false,e.getMessage()), callBack);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Type type = new TypeToken<MyResponse<T>>(){}.getType();
                        MyResponse<T> res = new Gson().fromJson(response.body().string(), type);
                        successCallBack(res, callBack);
                    } else {
                        Type type = new TypeToken<MyResponse<Object>>(){}.getType();
                        MyResponse<Object> res = new Gson().fromJson(response.body().string(), type);
                        failureCallBack(res,callBack);
                    }
                }
            });
            return call;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private <T> void successCallBack(final MyResponse<T> result, final RequestCallBack<T> callBack) {
        mOkHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onSuccess(result);
                }
            }
        });
    }

    private <T> void failureCallBack(final MyResponse<Object> result, final RequestCallBack<T> callBack) {
        mOkHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onFailure(result);
                }
            }
        });
    }


    public interface RequestCallBack<T> {
        void onSuccess(MyResponse<T> data);
        void onFailure(MyResponse<Object> error);
    }

    public class MyResponse<T> {
        public T data;
        public int code;
        public boolean success;
        public String message;

        public MyResponse(T data, int code, boolean success, String message) {
            this.data = data;
            this.code = code;
            this.success = success;
            this.message = message;
        }
    }
}
