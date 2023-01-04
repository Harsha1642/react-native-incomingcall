
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNIncomingcallSpec.h"

@interface Incomingcall : NSObject <NativeIncomingcallSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Incomingcall : NSObject <RCTBridgeModule>
#endif

@end
