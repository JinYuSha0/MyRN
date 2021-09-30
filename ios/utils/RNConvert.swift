//
//  RNConvert.swift
//  myRN
//
//  Created by Soul on 2021/9/30.
//

class RNConvert: NSObject {
  
  private static func isBaseType(_ value: Any?) -> Bool {
    if (value == nil) {
      return true
    }
    var isBase: Bool = true
    if (value is NSString) {}
    else if (value is NSNumber) {}
    else if (value is Bool) {}
    else if (value is NSArray) {
      isBase = false
    } else {
      isBase = false
    }
    return isBase
  }
  
  static func convertObj(_ obj: Any?) -> NSDictionary? {
    if obj == nil {
      return nil
    }
    let result: NSMutableDictionary = [:]
    let mirror = Mirror(reflecting: obj!)
    for property in mirror.children {
      let label = property.label!
      let value = property.value
      if let optionalValue = value as? AnyObject? {
        if optionalValue == nil {
          result[label] = NSNull.init()
          continue
        }
      }
      if (isBaseType(value)) {
        result[label] = value
      } else {
        if (value is NSArray) {
          result[label] = convertArray(value as! NSArray?)
        } else {
          result[label] = convertObj(value)
        }
      }
    }
    return result
  }
  
  static func convertArray(_ array: NSArray?) -> NSArray? {
    if array == nil {
      return nil
    }
    let result: NSMutableArray = []
    for element in array! {
      if let optionalValue = element as? AnyObject? {
        if optionalValue == nil {
          result.add(NSNull.init())
          continue
        }
      }
      if isBaseType(element) {
        result.add(element)
      } else {
        if (element is NSArray) {
          result.add(convertArray(element as! NSArray?)!)
        } else {
          result.add(convertObj(element)!)
        }
      }
    }
    return result
  }
  
  static func convert(_ obj: Any?) -> Any? {
    if isBaseType(obj) {
      return obj
    } else if (obj is NSArray) {
      return convertArray(obj as? NSArray)
    } else {
      return convertObj(obj!)
    }
  }
  
}
