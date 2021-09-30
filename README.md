# RN拆包框架

## 打包命令
- android `yarn rn-multi-bundle`
- ios     `yarn rn-multi-bundle -p ios`

## 拆包注意项
-  AppRegistry.registerComponent注册的Component必须由@registry目录导入否则会报错

---

## 开启调试模式方法

> 将RNApplication.java第108行isDebug参数置为true \
> ```return getReactNativeHost(false,RNApplication.this, null);```

---

## 待补充的feature
- [ ] ios原生
- [ ] assets diff