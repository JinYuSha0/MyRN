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

  @objc
  override func constantsToExport() -> [AnyHashable : Any]! {
    return ["model": UIDevice.modelName, "prefix": RNBridge.PREFIX + "_"]
  }
  
  @objc
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
  func openComponent(_ bundlePath: String, moduleName: String ) -> Void {
    print(bundlePath, moduleName)
  }
  
  public static func sendEvent(eventName: String, eventData: Any?) -> Void {
    RNBridge.eventEmitter?.sendEvent(withName: eventName, body: RNConvert.convert(eventData))
  }
  
  public static func sendEventInner(eventName: String, eventData: Any?) -> Void {
    sendEvent(eventName: "\(PREFIX)_\(eventName)", eventData: eventData)
  }
  
}
