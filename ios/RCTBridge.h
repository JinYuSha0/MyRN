//
//  RCTBridge.h
//  myRN
//
//  Created by 邵瑾瑜 on 2021/10/1.
//

#import <Foundation/Foundation.h>

@interface RCTBridge (RnLoadJS) // RN private class, exposes his interface here.

- (void)executeSourceCode:(NSData *)sourceCode sync:(BOOL)sync;

@end
