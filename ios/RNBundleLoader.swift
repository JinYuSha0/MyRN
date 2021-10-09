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
  
  static func loadScriptFromAssets(filePath: String, isSync: Bool = false) {
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
  
  static func loadScriptFromFile(filePath: String, isSync: Bool = false) {
    do {
      let bundleUrl = URL(fileURLWithPath: "\(File.createDirectory(dirname: "buzBundle")!)/\(filePath)")
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
    var filePath = BundleName
    let startIndex = filePath.startIndex
    if BundleName.starts(with: "assets://") {
      let endIndex = filePath.index(startIndex, offsetBy: 8)
      let range = startIndex...endIndex
      filePath.removeSubrange(range)
      loadScriptFromAssets(filePath: filePath, isSync: false)
    } else if BundleName.starts(with: "file://") {
      let endIndex = filePath.index(startIndex, offsetBy: 6)
      let range = startIndex...endIndex
      filePath.removeSubrange(range)
      loadScriptFromFile(filePath: filePath, isSync: false)
    }
  }
  
}
