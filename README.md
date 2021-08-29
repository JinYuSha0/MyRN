# RN拆包框架

## 拆包注意项
-  AppRegistry.registerComponent注册的Component必须由@registry目录导入否则会报错

---

## 开启调试模式方法

> 将RNApplication.java第108行isDebug参数置为true \
> ```return getReactNativeHost(false,RNApplication.this, null);```

---

## 待补充的feature
- [ ] 网络加载bundle包