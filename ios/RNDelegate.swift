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

    bridge = RCTBridge.init(bundleURL: commonBundleUrl, moduleProvider: nil, launchOptions: launchOptions)
    
    initView()
    
    initDB()
    
    return true
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
        debugPrint("InitDB failure")
      }
    }
  }
  
  func initView() -> Void {
    let homeBuzBundleUrl = Bundle.main.url(forResource: "bundle/home.buz.ios", withExtension: "bundle")!
    let onComplete = {() -> Void in
      let rootView = RCTRootView(bridge: self.bridge, moduleName: "Home", initialProperties: nil)

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
    }
//    bridge.batched.loadAndExecuteSplitBundleURL(homeBuzBundleUrl, onError: nil, onComplete: onComplete)
  }
}
