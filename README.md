# RN拆包框架

## 打包命令
- android `yarn rn-multi-bundle`
- ios     `yarn rn-multi-bundle -p ios`

## 打增量业务包命令
- android `yarn rn-multi-bundle -u`
- ios     `yarn rn-multi-bundle -p ios -u`

## 拆包注意项
-  AppRegistry.registerComponent注册的Component必须由@registry目录导入否则会报错

## 开启调试模式方法
- android: 将RNApplication.java第108行isDebug参数置为true
- ios: 将RNDelegate.swift第14行DEBUG参数设为true

## 待补充的feature
- [ ] assets diff
- [ ] 增量业务包执行报错回滚