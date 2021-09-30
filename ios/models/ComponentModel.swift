//
//  ComponentModel.swift
//  myRN
//
//  Created by Soul on 2021/9/29.
//

class ComponentModel: Codable {
  var ComponentName: String?
  var BundleName: String
  var Version: Int
  var Hash: String
  var FilePath: String
  var PublishTime: Int64
  var InstallTime: Int64
  
  init(ComponentName: String?, BundleName: String, Version: Int, Hash: String, FilePath: String, PublishTime: Int64, InstallTime: Int64) {
    self.ComponentName = ComponentName
    self.BundleName = BundleName
    self.Version = Version
    self.Hash = Hash
    self.FilePath = FilePath
    self.PublishTime = PublishTime
    self.InstallTime = InstallTime
  }
}
