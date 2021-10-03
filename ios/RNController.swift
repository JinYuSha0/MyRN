//
//  RNController.swift
//  myRN
//
//  Created by Soul on 2021/9/29.
//

import React

class RNController: UIViewController {
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
