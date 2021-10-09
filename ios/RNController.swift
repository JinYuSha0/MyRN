//
//  RNController.swift
//  myRN
//
//  Created by Soul on 2021/9/29.
//

import React

class RNController: UIViewController {
  
  static private var controllerList = Set<RNController>()
  
  static public func removeController(controller: RNController?) {
    if controller != nil {
      RNController.controllerList.remove(controller!)
    }
  }
  
  static public func isExistsModule(moduleName: String) -> Bool {
    var result = false
    for controller in RNController.controllerList {
      if controller.moduleName == moduleName {
        result = true
        break
      }
    }
    return result
  }
  
  var bundleName: String
  var moduleName: String
  var params: Dictionary<String, Any>
  
  init(bundleName: String, moduleName: String, params: Dictionary<String, Any>) {
    self.bundleName = bundleName
    self.moduleName = moduleName
    self.params = params
    super.init(nibName:nil, bundle:nil)
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    RNController.controllerList.insert(self)
    initView()
  }
  
  func initView() -> Void {
    let result = RNDBHelper.manager.selectByBundleName(self.bundleName)!
    RNBundleLoader.load(result.FilePath)
    let rctBridge = RNBundleLoader.getBridge()
    let view = RCTRootView(bridge: rctBridge, moduleName: self.moduleName, initialProperties: self.params)
    self.view = view
  }
  
}
