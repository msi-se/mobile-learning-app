# Mobile Learning API Tool

Willkommen zum Mobile Learning App API Tool!

Dieses repository beinhaltet ein Skript, mit dem Sie **automatisch** Ihre Kurse der Mobile Learning App in Form von JSON-Dateien automatisch **hinzufügen, ändern und löschen** können. 

Die Datei
`courses.json` enthält zur Orientierung eine beispielhafte Struktur, die einfach ersetzt werden kann.

Forken Sie dieses Repository den Versionsverlauf Ihrer Kurse zu verwalten. 

## Setup

Das Ausführen des Skripts erfordert eine installierte Javasript Laufzeitumgebung z.B. **Node**

1. `npm install` 

2. Erstellen Sie eine `.env` Datei im Stammverzeichnis des Projekts und fügen Sie die folgenden Umgebungsvariablen hinzu (siehe .env.example):
```env
BACKEND_URL=http://localhost:8080
HTWG_USERNAME=<HTWG_Benutzername>
HTWG_PASSWORD=<HTWG_Passwort>
```
3. Kurse inkl. Feedback & Quizzes in `courses.json` anlegen

## Skript Ausführen

`node sync-courses.js`

## JSON Struktur in courses.json
 
- Kurs 1
  - Feedback-Formular 1
    - Frage 1
      - Antwortmöglichkeit 1 
      - Antwortmöglichkeit 2
    - Frage 2
      - ...
  - Feedback-Formular 2
    - ...
  - Quiz-Formular 1
    - Frage 1
      - Antwortmöglichkeit 1
- Kurs 2
  - ...

### Kurse sind JSON-Objekte bestehend aus: 

- `name`: Name des Kurses.
- `description`: Beschreibung des Kurses.
- `key`: Eindeutiger Schlüssel für den Kurs.
- `feedbackForms`: Array aus Feedback-Forumular Objekten, die mit dem Kurs verbunden sind.
- `quizForms`: Array aus Quiz-Formular Objekten, die mit dem Kurs verbunden sind.

### Feedback-Formulare sind JSON-Objekte bestehend aus:

- `name`: Name des Feedback-Formulars.
- `description`: Beschreibung des Feedback-Formulars.
- `key`: Eindeutiger Schlüssel für das Feedback-Formular.
- `questions`: Array aus Feedback-Frage Objekten, die im Feedback-Formular gestellt werden.

### Feedback Fragen sind JSON-Objekte bestehend aus:

- `name`: Name der Frage.
- `description`: Beschreibung der Frage.
- `type`: Typ der Frage.
- `options`: Array aus Strings mit Antwortmöglichkeiten, die für `SINGLE_CHOICE` Fragen zur Verfügung stehen.

Optionen für `type`: `SLIDER`, `SINGLE_CHOICE`

### Quiz-Formulare sind JSON-Objekte bestehend aus:

- `name`: Name des Quiz-Formulars.
- `description`: Beschreibung des Quiz-Formulars.
- `key`: Eindeutiger Schlüssel für das Quiz-Formular.
- `questions`: Array aus Quiz Frage Objekten, die im Quiz-Formular gestellt werden.

### Quiz Fragen sind JSON-Objekte bestehend aus:

- `name`: Name der Frage.
- `description`: Beschreibung der Frage.
- `type`: Typ der Frage
- `options`: Array aus Strings mit Antwortmöglichkeiten, die für `SINGLE_CHOICE` Fragen zur Verfügung stehen. 
- `correctAnswer`: richtige Antwort für die Frage.

Optionen für `type`: `SINGLE_CHOICE`, `YES_NO`



## Funktionsweise

Das Skript führt die folgenden Schritte aus:

1. Lädt die Umgebungsvariablen aus der `.env` Datei & führt Login durch.

2. Gibt aktuell existierende Kurse inklusive exisitierender Feedbacks & Quizzes aus

3. Wenn bereits Kurse existieren wird der aktuellste Stand der Kurse in `courses.json` gepullt

1. Neu hinzugefügte Kurse, Feedbacks, Quizzes & Antwortmöglichkeiten werden angelegt (z. B. auch neue Antwortmöglickeiten bei bestehenden Quizzes/Feedbacks). Führt Änderungen bei bestehenden Kursen, Feedbacks, Quizzes & Antwortmöglichkeiten durch (z. B. Änderungen von Beschreibung, Formulierung von Antwormöglichkeiten, etc.)