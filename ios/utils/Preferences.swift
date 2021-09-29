//
//  Preferences.swift
//  myRN
//
//  Created by Soul on 2021/9/29.
//

class Preferences: NSObject {
  private static let userDefault = UserDefaults.standard
  
  static func storageKV(key: String, value: Any) -> Void {
    Preferences.userDefault.setValue(value, forKey: key)
    Preferences.userDefault.synchronize()
  }
  
  static func getValueByKey(key: String) -> Any? {
    return Preferences.userDefault.object(forKey: key) ?? nil
  }
}
