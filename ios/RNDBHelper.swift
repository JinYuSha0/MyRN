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
      Filepath_column <- row.Filepath,
      PublishTime_column <- row.PublishTime,
      InstallTime_column <- row.InstallTime
    )
    if (try? getDB().run(insert)) != nil {
      debugPrint("Insert success")
    } else {
      debugPrint("Insert failure")
    }
  }
  
  func selectAll() -> [ComponentModel]? {
    let sql = "SELECT * FROM \(TableName) a WHERE Version = (SELECT MAX(b.Version) FROM \(TableName) b WHERE b.BundleName = a.BundleName) ORDER BY a.BundleName"
    let result = try? getDB().execute(sql)
    print(result)
    return nil
  }
  
}
