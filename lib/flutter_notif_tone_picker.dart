
import 'dart:async';

import 'package:flutter/services.dart';

import 'model/notification_tone.dart';

class FlutterNotifTonePicker {
  static const MethodChannel _channel =
      const MethodChannel('dev.bleuxstrife.flutter_notif_tone_picker');


  static Future<NotificationTone>  pickTone({String uriString}) async {
    Map<dynamic, dynamic> result = await _channel.invokeMethod("changeTone",<String, dynamic>{
      'uriString': uriString,
    }) as Map;
    if (result == null) return null;
    NotificationTone data = NotificationTone.fromMap(result);
    return data;
  }

  static Future<NotificationTone> retrieveLostData() async{
    Map<dynamic, dynamic> result = await _channel.invokeMethod("retrieve") as Map;
    if (result == null) return null;
    NotificationTone data = NotificationTone.fromMap(result);
    return data;
  }
}
