
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import <React/RCTEventEmitter.h>
#else
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#endif

@interface RNTrustVisionRnsdkFramework : RCTEventEmitter <RCTBridgeModule>

@end
  
