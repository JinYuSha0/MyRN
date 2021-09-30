//
//  RNBridge.swift
//  myRN
//
//  Created by Soul on 2021/9/28.
//

@objc(RNBridge)
class RNBridge: NSObject {
  
  public static let PREFIX = Math.randomString(6)

  @objc
  func constantsToExport() -> [String: Any]! {
    return ["model": UIDevice.modelName, "prefix": RNBridge.PREFIX + "_"]
  }

  @objc(getAllComponent:rejecter:)
  func getAllComponent(_ resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    let components = RNDBHelper.manager.selectAll()
    let aaa = RNConvert.convert(components)
    resolve(aaa)
  }
  
}
