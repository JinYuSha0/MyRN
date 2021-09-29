//
//  RNDelegate.swift
//  myRN
//
//  Created by Soul on 2021/9/28.
//


@UIApplicationMain
class RNDelegate: UIResponder, UIApplicationDelegate, RCTBridgeDelegate {
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

    bridge = RCTBridge(delegate: self, launchOptions: launchOptions)
    let rootView = RCTRootView(bridge: bridge, moduleName: "Home", initialProperties: nil)

    if #available(iOS 13.0, *) {
      rootView.backgroundColor = UIColor.systemBackground
    } else {
      rootView.backgroundColor = UIColor.white
    }

    window = UIWindow(frame: UIScreen.main.bounds)
    let rootViewController = UIViewController()
    rootViewController.view = rootView
    window?.rootViewController = rootViewController
    window?.makeKeyAndVisible()
    initDB()
    return true
  }

  func sourceURL(for bridge: RCTBridge?) -> URL? {
    #if DEBUG
      return RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index", fallbackResource: nil)
    #else
      return Bundle.main.url(forResource: "main", withExtension: "jsbundle")
    #endif
  }
  
  func initDB() -> Void {
    let isInit: Bool = (Preferences.getValueByKey(key: StorageKey.INIT_DB) ?? false) as! Bool
    if (!isInit) {
      guard let path = Bundle.main.path(forResource: "bundle/appSetting", ofType: "json") else { return }
      let localData = NSData.init(contentsOfFile: path)! as Data
      do {
        let setting = try JSONDecoder().decode(SettingModel.self, from: localData)
        for (key, value) in setting.components {
          let bundlePath: String = Bundle.main.path(forResource: "bundle/\(key)", ofType: "") ?? ""
          let componentModel: ComponentModel = ComponentModel.init(
            ComponentName: value.componentName,
            BundleName: key,
            Version: 0,
            Hash: value.hash,
            Filepath: bundlePath,
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
    RNDBHelper.manager.selectAll()
  }
}
