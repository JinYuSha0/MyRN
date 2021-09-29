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

}
