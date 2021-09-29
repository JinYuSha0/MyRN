//
//  SettingModel.swift
//  myRN
//
//  Created by Soul on 2021/9/29.
//

class SettingModel: Codable {
  
  var components: [String: Component]
  var timestamp: Int64
  
  class Component: Codable {
    var hash: String
    var componentName: String?
  }
  
}
