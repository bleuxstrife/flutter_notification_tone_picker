#import "FlutterNotifTonePickerPlugin.h"
#if __has_include(<flutter_notif_tone_picker/flutter_notif_tone_picker-Swift.h>)
#import <flutter_notif_tone_picker/flutter_notif_tone_picker-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_notif_tone_picker-Swift.h"
#endif

@implementation FlutterNotifTonePickerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterNotifTonePickerPlugin registerWithRegistrar:registrar];
}
@end
