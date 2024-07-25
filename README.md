# README

## Overview

This project is a cross-platform mobile application developed using Flutter for the frontend and Quarkus for the backend. 
## Features

- **Flutter Frontend**
  - Cross-platform mobile development (iOS, Android,Web)
  - Custom animations and UI components
  - Real-time data updates using WebSockets

- **Quarkus Backend**
  - REST API endpoints
  - MongoDB database
  - Authentication and authorization

## Prerequisites

### General

- Flutter SDK 3.22: [Installation Guide](https://flutter.dev/docs/get-started/install)
- Dart SDK
- Java JDK 21: [Installation Guide](https://adoptopenjdk.net/)
- MongoDB
- Android Studio
- HTWG VPN
- (Andoroid compileSdkVersion 34 required by url_launcher)

## Setup

### Frontend

1. Clone the repository
2. install dependencies

```bash
   cd ./frontend
   flutter pub get
```
3. env file
```bash
.env file in /frontend mit (Url for backend requests):
DOMAIN=connect.in.htwg-konstanz.de
```
4. run application
```bash
   flutter run
   oder VScode start button in lib/main.dart
```
- Login with HTWG account credentials (VPN connection required)
- Alternative: Auth bypass (no VPN required, Username: Student\<Zahl\> oder Prof\<Zahl\> Password:\<leave empty\>) e.g. Username: Student1


### Backend

1. Run app in Quarkus dev mode
```bash
cd ./backend
 mvn quarkus:dev
```
- application.properties content (public & private key for jwt sign/verification can be generated with openssl):
```bash
quarkus.http.cors=true
quarkus.http.cors.origins=*
quarkus.http.cors.headers=*
quarkus.http.cors.methods=*
quarkus.mongodb.connection-string=mongodb://localhost:27017
quarkus.mongodb.database=mobilelearning
quarkus.naming.enable-jndi=true

# Public verification key
mp.jwt.verify.publickey.location=publicKey.pem
quarkus.native.resources.includes=publicKey.pem

# Private signing key
smallrye.jwt.sign.key.location=privateKey.pem

quarkus.jacoco.excludes=**/helper/**/*
```
- Swagger UI: http://localhost:8080/q/swagger-ui/
- Mongodb express

## Main Features

### Homepage Tab
1. HTWG Quiz Stats (Globally & user specific)
2. Mensa Menu (Current Data from Seezeit API https://www.max-manager.de/daten-extern/seezeit/xml/mensa_htwg/speiseplan.xml)
   
### Kurse Tab
- See Courses that User is owner of (created by User)
- Courses that User is participant of
  - If course has moodle id number assigned to it. Course appears automatically
- Feedback/Quiz tab to join or moderate session
- Feedback
  - participant: send response with different elements (anonymized)
  - moderator: start/end session & see realtime results
- Quiz
  - Quiz lobby to wait till everyone joins
  - moderator: Start/end session, change questions, display answers & correct answer after closing question, scoreboard inbetween questions, final scoreboard (with throw animations on scoreboard)
- participant: join with random alias or username, send responses to questions
- Questions with limited time to answer for increased competition & score calculation based on fastest answer    

### Live Tab
- Join live session with code displayed in live session
- See current live session & join just by clicking on it (User only sees live session of courses that are associated with User)

### Profile Tab
- Logout
- Privacy Template Text (Abstimmung mit Marius Plakenhorn --> keinen finalen Text erhalten)

## Deployment

  - dev: http://loco.in.htwg-konstanz.de/
  - prod: https://connect.in.htwg-konstanz.de/
```bash
cd /home/loco/team-learning
docker compose up -d
```
- automatic TLS key management with lets encrypt

### frontend

#### build android bundle (for playstore)
- follow flutter guide: https://docs.flutter.dev/deployment/android
- requires signage key
```bash
flutter build appbundle
```
- Upload appbundle to playstore (Version code has to be incremented in pubspec)

## Rive
- email: mobilelearning.htwg@gmail.com
- Account for 
  - HTWG Mascot animations
  - Quiz/Feedback animations
  - Mensa Icons