//
//  RNDelegate.swift
//  myRN
//
//  Created by Soul on 2021/9/28.
//

import React

@UIApplicationMain
class RNDelegate: UIResponder, UIApplicationDelegate {
  // 开启关闭调试
  let DEBUG: Bool = false
  // 默认模块
  let DEFAULT_MODULE = "Home"
  // 默认启动业务包
  let DEFAULT_BUNDLE = "bundle/home.buz.ios"
  var window: UIWindow?
  var bridge: RCTBridge!
  
  private func InitializeFlipper(_ application: UIApplication?) {
    let client = FlipperClient.shared()
    let layoutDescriptorMapper = SKDescriptorMapper()
    client?.add(FlipperKitLayoutPlugin(rootNode: application, with: layoutDescriptorMapper))
    client?.add(FKUserDefaultsPlugin(suiteName: nil))
    client?.add(FlipperKitReactPlugin())
    client?.add(FlipperKitNetworkPlugin(networkAdapter: SKIOSNetworkAdapter()))
    client?.start()
  }
  
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    #if FB_SONARKIT_ENABLED
      InitializeFlipper(application)
    #endif
    
    RCTDevSettingsSetEnabled(DEBUG)
    RCTDevLoadingView.setEnabled(DEBUG)
    
    var commonBundleUrl: URL
    
    if DEBUG {
      commonBundleUrl = RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index", fallbackResource: nil)
    } else {
      commonBundleUrl = Bundle.main.url(forResource: "bundle/common.ios", withExtension: "bundle")!
    }
    
    initDB()
    
    bridge = RCTBridge.init(bundleURL: commonBundleUrl, moduleProvider: nil, launchOptions: launchOptions)
    
    if !DEBUG {
      // 接收公共包加载完成的通知后才能加载业务包，否则会执行js报错
      NotificationCenter.default.addObserver(self, selector: #selector(initView), name: NSNotification.Name("RCTJavaScriptDidLoadNotification"), object: nil)
    } else {
      initView()
    }
    
    return true
  }
  
  @objc func initView() -> Void {
    do {
      if !DEBUG {
        // 移除通知监听，防止反复执行
        NotificationCenter.default.removeObserver(self)
        // 执行默认bundle包
        let homeBuzBundleUrl = Bundle.main.url(forResource: DEFAULT_BUNDLE, withExtension: "bundle")!
        let bundleData = try Data(contentsOf: homeBuzBundleUrl)
        self.bridge.batched.executeSourceCode(bundleData, sync: false)
      }
      
      let rootView = RCTRootView(bridge: self.bridge, moduleName: DEFAULT_MODULE, initialProperties: nil)

      if #available(iOS 13.0, *) {
        rootView.backgroundColor = UIColor.systemBackground
      } else {
        rootView.backgroundColor = UIColor.white
      }

      self.window = UIWindow(frame: UIScreen.main.bounds)
      let rootViewController = UIViewController()
      rootViewController.view = rootView
      self.window?.rootViewController = rootViewController
      self.window?.makeKeyAndVisible()
    } catch {
      print(error)
    }
  }
  
  func initDB() -> Void {
    let isInit: Bool = (Preferences.getValueByKey(key: StorageKey.INIT_DB) ?? false) as! Bool
    if (!isInit) {
      guard let path = Bundle.main.path(forResource: "bundle/appSetting", ofType: "json") else { return }
      let localData = NSData.init(contentsOfFile: path)! as Data
      do {
        let setting = try JSONDecoder().decode(SettingModel.self, from: localData)
        for (key, value) in setting.components {
          let componentModel: ComponentModel = ComponentModel.init(
            ComponentName: value.componentName,
            BundleName: key,
            Version: 0,
            Hash: value.hash,
            FilePath: "assets://bundle/\(key)",
            PublishTime: setting.timestamp,
            InstallTime: Int64(Date().timeIntervalSince1970 * 1000)
          )
          RNDBHelper.manager.insertRow(row: componentModel)
        }
        Preferences.storageKV(key: StorageKey.INIT_DB, value: true)
      } catch {
        print("InitDB failure")
      }
    }
  }
  
}
