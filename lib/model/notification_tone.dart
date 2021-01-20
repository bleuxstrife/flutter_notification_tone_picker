class NotificationTone {
  final String uriString;
  final String toneName;

  NotificationTone({this.uriString, this.toneName});

  factory NotificationTone.fromMap(Map<dynamic, dynamic> map){
    return NotificationTone(
        uriString: map["uriString"] as String,
        toneName: map["toneName"] as String
    );
  }
}