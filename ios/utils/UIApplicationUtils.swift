//
//  UIApplicationUtils.swift
//  myRN
//
//  Created by 邵瑾瑜 on 2021/10/3.
//

extension UIApplication {
    
  class func topViewController(_ viewController: UIViewController? = UIApplication.shared.keyWindow?.rootViewController) -> UIViewController? {
    if let nav = viewController as? UINavigationController {
      return topViewController(nav.visibleViewController)
    }
    if let tab = viewController as? UITabBarController {
      if let selected = tab.selectedViewController {
        return topViewController(selected)
      }
    }
    if let presented = viewController?.presentedViewController {
      return topViewController(presented)
    }
    return viewController
  }
    
  class func topNavigationController(_ viewController: UIViewController? = UIApplication.shared.keyWindow?.rootViewController) -> UINavigationController? {
    if let nav = viewController as? UINavigationController {
      return nav
    }
    if let tab = viewController as? UITabBarController {
      if let selected = tab.selectedViewController {
        return selected.navigationController
      }
    }
    return viewController?.navigationController
  }
  
}
