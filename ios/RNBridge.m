//
//  RNBridge.m
//  myRN
//
//  Created by Soul on 2021/9/29.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(RNBridge, NSObject)

RCT_EXTERN_METHOD(getAllComponent: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

@end
