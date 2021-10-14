//
//  RNBridge.m
//  myRN
//
//  Created by Soul on 2021/9/29.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(RNBridge, RCTEventEmitter)

RCT_EXTERN_METHOD(registerEvent: (NSString)eventName resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getAllComponent: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(openComponent: (NSString)moduleName)

RCT_EXTERN_METHOD(goBack)

RCT_EXTERN_METHOD(checkUpdate: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

@end
