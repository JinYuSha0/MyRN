//
//  RNBridge.swift
//  myRN
//
//  Created by Soul on 2021/9/28.
//

import React

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
  
  @objc(openComponent:moduleName:)
  func openComponent(_ bundleName: String, moduleName: String) -> Void {
    let params: Dictionary<String, Any> = ["goBack": true]
    DispatchQueue.main.async {
      let controller: UIViewController = RNController(bundleName: bundleName, moduleName: moduleName, params: params)
      UIApplication.topNavigationController()?.pushViewController(controller, animated: true)
    }
  }
  
  @objc(goBack)
  func goBack() -> Void {
    DispatchQueue.main.async {
      UIApplication.topNavigationController()?.popViewController(animated: true)
    }
  }
  
  public static func sendEvent(eventName: String, eventData: Any?) -> Void {
    RNBridge.eventEmitter?.sendEvent(withName: eventName, body: RNConvert.convert(eventData))
  }
  
  public static func sendEventInner(eventName: String, eventData: Any?) -> Void {
    sendEvent(eventName: "\(PREFIX)_\(eventName)", eventData: eventData)
  }
  
}
