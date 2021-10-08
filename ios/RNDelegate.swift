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
  public static let DEBUG: Bool = true
  // 默认模块
  let DEFAULT_MODULE = "Home"
  // 默认启动业务包
  let DEFAULT_BUNDLE = "bundle/home.buz.ios.bundle"
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
    
    RCTDevSettingsSetEnabled(RNDelegate.DEBUG)
    RCTDevLoadingView.setEnabled(RNDelegate.DEBUG)
    
    var commonBundleUrl: URL
    
    if RNDelegate.DEBUG {
      commonBundleUrl = RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index", fallbackResource: nil)
    } else {
      commonBundleUrl = Bundle.main.url(forResource: "bundle/common.ios", withExtension: "bundle")!
    }
    
    initDB()
    
    bridge = RCTBridge.init(bundleURL: commonBundleUrl, moduleProvider: nil, launchOptions: launchOptions)
    RNBundleLoader.setBridge(bridge)
    
    if !RNDelegate.DEBUG {
      // 接收公共包加载完成的通知后才能加载业务包，否则会执行js报错
      NotificationCenter.default.addObserver(self, selector: #selector(initView), name: NSNotification.Name("RCTJavaScriptDidLoadNotification"), object: nil)
    } else {
      initView()
    }
    
    return true
  }
  
  func applicationDidBecomeActive(_ application: UIApplication) {
    becomeActive()
  }
  
  func becomeActive() -> Void {
    DispatchQueue.global().async {
      DispatchQueue.main.async {
        let onSuccess = {(data: Any) -> Void in
        }
        let onError = {(errorMsg: String) -> Void in
        }
        RNBridge.checkUpdate(onSuccess: onSuccess, onError: onError)
      }
    }
  }
  
  @objc func initView() -> Void {
    if !RNDelegate.DEBUG {
      // 移除通知监听，防止反复执行
      NotificationCenter.default.removeObserver(self)
      // 执行默认bundle包
      RNBundleLoader.loadScriptFromAssets(filePath: DEFAULT_BUNDLE, isSync: false)
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
    let navContoller = UINavigationController.init(rootViewController: rootViewController)
    navContoller.setNavigationBarHidden(true, animated: false)
    self.window?.rootViewController = navContoller
    self.window?.makeKeyAndVisible()
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
