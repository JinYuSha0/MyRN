//
//  CheckUpdateModel.swift
//  myRN
//
//  Created by Soul on 2021/10/8.
//

class CheckUpdateModel: Codable {
  
  var code: Int
  var success: Bool
  var message: String?
  var data: [NewComponent]
  
  class NewComponent: Codable {
    var version: Int
    var hash: String
    var commonHash: String
    var isCommon: Bool
    var componentName: String
    var downloadUrl: String
    var buildTime: Int64
  }
  
}
