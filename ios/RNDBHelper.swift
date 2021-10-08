//
//  RNDBHelper.swift
//  myRN
//
//  Created by Soul on 2021/9/29.
//

import SQLite

let BundleName_column = Expression<String>("BundleName")
let ComponentName_column = Expression<String?>("ComponentName")
let Version_column = Expression<Int>("Version")
let Hash_column = Expression<String>("Hash")
let Filepath_column = Expression<String>("Filepath")
let PublishTime_column = Expression<Int64>("PublishTime")
let InstallTime_column = Expression<Int64>("InstallTime")

class RNDBHelper: NSObject {
  
  static let manager = RNDBHelper()
  private let TableName = "bundle"
  private var db: Connection?
  private var table: Table?
  
  func getDB() -> Connection {
    if db == nil {
      let path = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!
      db = try! Connection("\(path)/db.sqlite3")
      db?.busyTimeout = 5.0
    }
    return db!
  }
  
  func getTable() -> Table {
    if table == nil {
      table = Table(TableName)
      try! getDB().run(
        table!.create(temporary: false, ifNotExists: true, withoutRowid: false, block: { (builder) in
          builder.column(BundleName_column)
          builder.column(ComponentName_column)
          builder.column(Version_column)
          builder.column(Hash_column, unique: true)
          builder.column(Filepath_column, unique: true)
          builder.column(PublishTime_column)
          builder.column(InstallTime_column)
          builder.primaryKey(BundleName_column, Version_column)
        })
      )
    }
    return table!
  }
  
  func insertRow(row: ComponentModel) -> Void {
    let insert = getTable().insert(
      BundleName_column <- row.BundleName,
      ComponentName_column <- row.ComponentName,
      Version_column <- row.Version,
      Hash_column <- row.Hash,
      Filepath_column <- row.FilePath,
      PublishTime_column <- row.PublishTime,
      InstallTime_column <- row.InstallTime
    )
    if (try? getDB().run(insert)) != nil {
      print("Insert success")
    } else {
      print("Insert failure")
    }
  }
  
  func selectAll() -> [ComponentModel] {
    var result: [ComponentModel] = [ComponentModel]()
    do {
      let sql = "SELECT ComponentName, BundleName, Version, Hash, Filepath, PublishTime, InstallTime FROM \(TableName) a WHERE Version = (SELECT MAX(b.Version) FROM \(TableName) b WHERE b.BundleName = a.BundleName) ORDER BY a.BundleName"
      for row in try getDB().prepare(sql) {
        result.append(
          ComponentModel.init(
            ComponentName: row[0] as? String,
            BundleName: row[1] as! String,
            Version: Int("\(row[2] ?? 0)")!,
            Hash: row[3] as! String,
            FilePath: row[4] as! String,
            PublishTime: row[5] as! Int64,
            InstallTime: row[6] as! Int64
          )
        )
      }
    } catch {
      print(error)
    }
    return result
  }
  
  func selectAllMap() -> [String: ComponentModel] {
    var result = [String: ComponentModel]()
    let components = RNDBHelper.manager.selectAll()
    for component in components {
      result[component.ComponentName ?? "common"] = component
    }
    return result
  }
  
  func selectByBundleName(_ BundleName: String) -> ComponentModel? {
    var result: ComponentModel? = nil
    do {
      let sql = "SELECT ComponentName, BundleName, Version, Hash, Filepath, PublishTime, InstallTime FROM \(TableName) WHERE BundleName = '\(BundleName)' ORDER BY Version DESC LIMIT 1;"
      for row in try getDB().prepare(sql) {
        result = ComponentModel.init(
          ComponentName: row[0] as? String,
          BundleName: row[1] as! String,
          Version: Int("\(row[2] ?? 0)")!,
          Hash: row[3] as! String,
          FilePath: row[4] as! String,
          PublishTime: row[5] as! Int64,
          InstallTime: row[6] as! Int64
        )
      }
    } catch {
      print(error)
    }
    return result
  }
  
}
