class SessionData{
  String? userId;
  String? username;
}

SessionData session = SessionData();

void clearSessionData(){
  session = SessionData();
}
