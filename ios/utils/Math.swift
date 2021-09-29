//
//  Math.swift
//  myRN
//
//  Created by Soul on 2021/9/29.
//

class Math: NSObject {
  
  static func randomString(_ length: Int) -> String {
    let letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return String((0..<length).map{ _ in letters.randomElement()! })
  }
  
}
