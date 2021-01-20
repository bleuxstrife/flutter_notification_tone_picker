import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_notif_tone_picker/flutter_notif_tone_picker.dart';
import 'package:flutter_notif_tone_picker/model/notification_tone.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
 NotificationTone notificationTone;
 bool isStart = true;
  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    notificationTone = NotificationTone();
  }

  Future<void> retrieveLostdata() async {
    NotificationTone lostData = await FlutterNotifTonePicker.retrieveLostData();
    if(lostData!=null){
      notificationTone = lostData;
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Builder(builder: (context){
          retrieveLostdata();
          return Center(
            child: Column(children: [
              Text("ToneName : ${notificationTone?.toneName ?? null}"),
              Text("uriString : ${notificationTone?.uriString ?? null}"),
              Container(margin: EdgeInsets.only(top: 16.0), child: FlatButton(child: Text("Press"), onPressed: () async {
                notificationTone = await FlutterNotifTonePicker.pickTone(uriString: notificationTone?.uriString);
                setState(() {});
              },))
            ]),
          );
        },),
      ),
    );
  }
}
