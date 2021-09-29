//
//  RNController.swift
//  myRN
//
//  Created by Soul on 2021/9/29.
//

class RNController: UIViewController {
  var bundleName: String?
  var moduleName: String?
  var params: NSObject?
  
  func initWithURL(bundleName: String, moduleName: String, params: NSObject) -> Void {
    self.bundleName = bundleName
    self.moduleName = moduleName
    self.params = params
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
  }
  
  func initView() -> Void {
    
  }
  
}
