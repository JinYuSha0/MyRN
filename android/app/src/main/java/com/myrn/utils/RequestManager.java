package com.myrn.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.google.gson.Gson;
import com.myrn.BuildConfig;


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

    public <T, E> Call Get(String actionUrl, HashMap<String, String> paramsMap, RequestCallBack<T, E> callBack) {
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

    private <T, E> Call requestGetByAsync(String actionUrl, HashMap<String, String> paramsMap, final RequestCallBack<T, E> callBack) {
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
                    failureCallBack(null,e, callBack);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        T res = new Gson().fromJson(response.body().string(), callBack.getType(false));
                        successCallBack(res, callBack);
                    } else {
                        E res = new Gson().fromJson(response.body().string(), callBack.getType(true));
                        failureCallBack(res,null, callBack);
                    }
                }
            });
            return call;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private <T, E> void successCallBack(final T result, final RequestCallBack<T, E> callBack) {
        mOkHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onSuccess(result);
                }
            }
        });
    }

    private <T, E> void failureCallBack(final E result, Exception exception, final RequestCallBack<T, E> callBack) {
        mOkHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onFailure(result, exception);
                }
            }
        });
    }


    public interface RequestCallBack<T, E> {
        void onSuccess(T result);
        void onFailure(E result, Exception exception);
        Type getType(Boolean isFailure);
    }
}
