class NotificationTone {
  final String uriPath;
  final String toneName;

  NotificationTone({this.uriPath, this.toneName});

  factory NotificationTone.fromMap(Map<dynamic, dynamic> map){
    return NotificationTone(
        uriPath: map["uriPath"] as String,
        toneName: map["toneName"] as String
    );
  }
}