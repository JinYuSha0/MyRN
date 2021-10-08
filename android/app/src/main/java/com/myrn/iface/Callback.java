package com.myrn.iface;

public interface Callback {
    void onSuccess(Object result);
    void onError(String errorMsg);
}
