//
//  RNBundleLoader.swift
//  myRN
//
//  Created by 邵瑾瑜 on 2021/10/3.
//

import React

class RNBundleLoader: NSObject {
  
  static private var rctBridge: RCTBridge? = nil
  static private var sLoadedBundle = Set<String>()
  
  static func setBridge(_ rctBridge: RCTBridge) {
    RNBundleLoader.rctBridge = rctBridge
  }
  
  static func getBridge() -> RCTBridge {
    return rctBridge!
  }
  
  static func loadScriptFromAssets(filePath: String, isSync: Bool) {
    do {
      let bundleUrl = Bundle.main.url(forResource: filePath, withExtension: "")!
      if !sLoadedBundle.contains(bundleUrl.path) {
        let bundleData = try Data(contentsOf: bundleUrl)
        rctBridge?.batched.executeSourceCode(bundleData, sync: isSync)
        sLoadedBundle.insert(bundleUrl.path)
      }
    } catch {
      print(error)
    }
  }
  
  static func load(_ BundleName: String) {
    if RNDelegate.DEBUG { return }
    if BundleName.starts(with: "assets://") {
      var filePath = BundleName
      let startIndex = filePath.startIndex
      let endIndex = filePath.index(startIndex, offsetBy: 8)
      let range = startIndex...endIndex
      filePath.removeSubrange(range)
      print(filePath)
      loadScriptFromAssets(filePath: filePath, isSync: false)
    } else if BundleName.starts(with: "file://") {
      
    }
  }
  
}
