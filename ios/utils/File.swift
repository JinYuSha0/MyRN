//
//  File.swift
//  myRN
//
//  Created by Soul on 2021/10/9.
//

class File: NSObject {
  
  public static let manager = FileManager.default
  
  public static let documentURL = manager.urls(for: .applicationDirectory, in: .userDomainMask).first!
  
  public static func isExists(path: String) -> (isExists: Bool, isDirectory: ObjCBool) {
    var isDirectory = ObjCBool(false)
    let isExists = manager.fileExists(atPath: path, isDirectory: &isDirectory)
    return (isExists, isDirectory)
  }
  
  public static func createDirectory(dirname: String, withIntermediateDirectories: Bool = true) -> String? {
    if !isExists(path: "\(documentURL.path)/\(dirname)").isExists {
      do {
        try manager.createDirectory(at: documentURL.appendingPathComponent(dirname), withIntermediateDirectories: withIntermediateDirectories, attributes: nil)
      } catch {
        print(error)
        return nil
      }
    }
    return "\(documentURL.path)/\(dirname)"
  }
  
}
