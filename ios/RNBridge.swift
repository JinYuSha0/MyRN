//
//  RNBridge.swift
//  myRN
//
//  Created by Soul on 2021/9/28.
//

import React
import Alamofire
import SSZipArchive

@objc(RNBridge)
class RNBridge: RCTEventEmitter {
  
  private static var eventEmitter: RCTEventEmitter?
  private static let PREFIX = Math.randomString(6)
  private static var registeredSupportEvents = [String]()
  
  override init() {
    super.init()
    RNBridge.eventEmitter = self
  }

  override func constantsToExport() -> [AnyHashable : Any]! {
    return ["model": UIDevice.modelName, "prefix": RNBridge.PREFIX + "_"]
  }
  
  override class func requiresMainQueueSetup() -> Bool {
    return false
  }
  
  override func supportedEvents() -> [String]! {
    return RNBridge.registeredSupportEvents
  }

  @objc(getAllComponent:rejecter:)
  func getAllComponent(_ resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    let components = RNDBHelper.manager.selectAll()
    resolve(RNConvert.convert(components))
  }
  
  @objc(registerEvent:resolver:rejecter:)
  func registerEvent(_ eventName: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    if (!RNBridge.registeredSupportEvents.contains(eventName)) {
      RNBridge.registeredSupportEvents.append(eventName)
    }
    resolve(true)
  }
  
  @objc(openComponent:)
  func openComponent(_ moduleName: String) -> Void {
    let params: Dictionary<String, Any> = ["goBack": true]
    DispatchQueue.main.async {
      let controller: UIViewController = RNController(moduleName: moduleName, params: params)
      UIApplication.topNavigationController()?.pushViewController(controller, animated: true)
    }
  }
  
  @objc(goBack)
  func goBack() -> Void {
    DispatchQueue.main.async {
      let popController =  UIApplication.topNavigationController()?.popViewController(animated: true)
      RNController.removeController(controller: popController as? RNController)
    }
  }
  
  @objc(checkUpdate:rejecter:)
  func checkUpdate(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
    func onSuccess(_ data: CheckUpdateModel) -> Void {
      resolve(RNConvert.convert(data))
    }
    func onError(_ errorMsg: String) -> Void {
      reject(nil, errorMsg, nil)
    }
    RNBridge.checkUpdate(onSuccess: onSuccess, onError: onError)
  }
  
  public static func checkUpdate(
    onSuccess: @escaping ((_ data: CheckUpdateModel) -> Void),
    onError: @escaping ((_ errorMsg: String) -> Void)
  ) {
    let componentsMap = RNDBHelper.manager.selectAllMap()
    let common = componentsMap["common"]
    RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_START, eventData: nil)
    AF.request("http://192.168.8.99:3000/rn/checkUpdate", method: .get, parameters: ["platform": "ios", "commonHash": (common?.Hash ?? "") ]).responseData { response in
      switch response.result {
        case .success(let value):
          do {
            let result: CheckUpdateModel = try JSONDecoder().decode(CheckUpdateModel.self, from: value )
            RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_SUCCESS, eventData: result)
            if result.success {
              onSuccess(result)
              for component in result.data {
                let oldComponent = componentsMap[component.componentName]
                if oldComponent == nil || (component.version > oldComponent?.Version ?? 0 && component.hash != oldComponent?.Hash) {
                  RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_NEWS, eventData: component)
                  let dest: DownloadRequest.Destination = { _, _ in
                    let fileURL = File.documentURL.appendingPathComponent("\(component.componentName)-\(component.hash).zip")
                    return (fileURL, [.removePreviousFile, .createIntermediateDirectories])
                  }
                  AF.download(component.downloadUrl, interceptor: nil, to: dest).downloadProgress(closure: { (progress) in
                    RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_PROGRESS, eventData: ["componentName": component.componentName, "progress": progress.fractionCompleted])
                  }).responseData { (res) in
                    switch res.result {
                      case .success:
                        let unzipPath = File.createDirectory(dirname: "buzBundle")
                        func completionHandler(path: String, success: Bool, error: Error?) {
                          if success {
                            setupComponent(componentDir: "\(unzipPath!)/\(component.hash)", version: component.version)
                            RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_NEWS_SUCCESS, eventData: component)
                          } else {
                            print(error!)
                          }
                        }
                        if unzipPath != nil {
                          SSZipArchive.unzipFile(atPath: res.fileURL!.path, toDestination: unzipPath!, progressHandler: nil, completionHandler: completionHandler)
                        }
                        break
                      case .failure:
                        RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_NEWS_FAILURE, eventData: res.error?.localizedDescription)
                        break
                    }
                  }
                }
              }
            } else {
              onError(result.message!)
              RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_FAILURE, eventData: result.message!)
            }
          } catch {
            onError(error.localizedDescription)
            RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_FAILURE, eventData: error.localizedDescription)
          }
          break
        case .failure(let error):
          let errorMsg = error.errorDescription ?? "Request unknow error"
          onError(errorMsg)
          RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_FAILURE, eventData: errorMsg)
          break
      }
    }
  }
  
  private static func setupComponent(componentDir: String, version: Int) {
    do {
      let settingData = try Data(contentsOf: URL(fileURLWithPath: "\(componentDir)/setting.json"))
      let setting: BuzSettingModel = try JSONDecoder().decode(BuzSettingModel.self, from: settingData)
      var bundleFilePath = "\(componentDir)/\(setting.bundleName)"
      if File.isExists(path: bundleFilePath).isExists {
        let buzBundlePath = File.createDirectory(dirname: "buzBundle")!
        let range = buzBundlePath.startIndex...buzBundlePath.endIndex
        bundleFilePath.replaceSubrange(range, with: "file://")
        let componentModel: ComponentModel = ComponentModel.init(
          ComponentName: setting.componentName,
          BundleName: setting.bundleName,
          Version: version,
          Hash: setting.hash,
          FilePath: bundleFilePath,
          PublishTime: setting.timestamp,
          InstallTime: Int64(Date().timeIntervalSince1970 * 1000)
        )
        RNDBHelper.manager.insertRow(row: componentModel)
        RNBridge.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_NEWS_APPLY, eventData: setting.componentName)
        if !RNController.isExistsModule(moduleName: setting.componentName) {
          RNBundleLoader.load(bundleFilePath)
        }
      }
    } catch {
      print(error)
    }
  }
  
  public static func sendEvent(eventName: String, eventData: Any?) -> Void {
    if !RNBridge.registeredSupportEvents.contains(eventName) {
      RNBridge.registeredSupportEvents.append(eventName)
    }
    RNBridge.eventEmitter?.sendEvent(withName: eventName, body: RNConvert.convert(eventData))
  }
  
  public static func sendEventInner(eventName: String, eventData: Any?) -> Void {
    sendEvent(eventName: "\(PREFIX)_\(eventName)", eventData: eventData)
  }
  
}
