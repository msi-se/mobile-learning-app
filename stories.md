# User-Stories

## Allgemein

### Einfache App erstellen
Als Benutzer möchte ich auf die Lern-App über mein Smartphone und über den Browser zugreifen können.

### UI/UX
Als Benutzer möchte ich eine benutzerfreundliche Oberfläche haben, um die App einfach und intuitiv zu bedienen.

#### Akzeptanzkriterien
- Umsetzung von UX-Design Prinzipien (Konsistente Userexperience, Reaktionsfreudige Oberfläche, tbd, ...)
- Corporate Identity der HTWG wird im App Design umgesetzt
- Klare Appstruktur & einfache Navigationsmöglichkeiten

### Sicherheits- und Datenschutzbestimmungen
Als Benutzer möchte ich sicherstellen, dass meine Daten und meine Privatsphäre geschützt sind, indem Sicherheits- und Datenschutzbestimmungen eingehalten werden.

### Account- & Profilverwaltung
Als Benutzer möchte ich mein Account/Profil einsehen können und Änderungen vornehmen können.

### FAQ & Support
Als Benutzer möchte ich als Hilfestellung bei bestimmten Fragen, Antworten im FAQ-Bereich finden. Bei weiteren Fragen kann ich den Support kontaktieren.


## Feedback-System

### Dozent: Feedback-Möglichkeit erstellen
Als Dozent möchte ich das erhaltene Feedback zu meinen Lehrveranstaltungen einsehen können, um mögliche Verbesserungen vorzunehmen.
  
#### Akzeptanzkriterien
- Es besteht die Möglichkeit zu jeder Veranstaltung ein Feedback-Kanal zu erstellen (z.B.: "Diskrete Mathematik WS23/34").
- Darin lassen sich Feedback-Bögen vorbereiten (z.B.: "Feedback zum ersten Monat der Veranstaltung").
  - Die Bögen können aus verschiedenen Elementen aufgebaut werden:
    - Regler (1-10)
    - Sterne (1-5)
    - Multiple-/Single-Choice
    - Feedback-Matrix (Bsp: "Tippe auf eine Position auf der Matrix, die folgende Dimensionen repräsentiert: Kurzweilig - Langweilig, Einfach - Schwierig")
    - Emoji-Auswahl
    - Volltext-Eingabe
  - Bei den Elementen kann ausgewählt werden, ob sie Pflicht sind oder optional.
- Der Ersteller kann User angeben, die das Feedback einsehen können (z.B.: andere Dozenten, die die Veranstaltung mit ihm halten).
- Der Ersteller kann einen QR-Code/Zugangscode generieren, den Studierende scannen können, um das Feedback zu geben. TODO: check, vlt mit Moodle Integration? (Jeder, der in Kurs eingeschrieben ist, kann App aufrufen und sieht dort den Feedback-Kanal)
  
### Student: Anonymes Feedback geben
Als Studierender möchte ich die Möglichkeit haben, anonymes Feedback zu einer Lehrveranstaltung zu geben, um meine Meinung zu äußern.

#### Akzeptanzkriterien
- per Handy-Kamera lässt sich ein QR-Code scannen bzw. ein Zugangscode eingeben, um so einen Zugang zum Quiz zu erhalten
- die Elemente des Fragebogens lassen sich wie vom Ersteller gedacht steuern  und schließlich fertigstellen mit einem Knopf "Feedback absenden"
- Der User bekommt eine Bestätigung/Fehlermeldung wenn das Feedback abgesendet wurde

### Dozent: Feedback einsehen
Als Dozent möchte ich das erhaltene Feedback zu meinen Lehrveranstaltungen einsehen können, um mögliche Verbesserungen vorzunehmen.

#### Akzeptanzkriterien
- Nur der Ersteller und vom Ersteller ernannten Personen können das Feedback eines Kanals einsehen.
- Feedback ist sortiert nach Lehrveranstaltung & Veranstaltungsdatum
- Feedback kann gefiltert und sortiert werden
- Dozent kann Feedbackinhalte markieren/vormerken um sie für die Umsetzung in der Vorlesung leichter zu finden
- Der Dozent hat Analyse Möglichkeiten um Trends & Muster im Feedback zu erkennen
- Feedback mit ähnlichen Inhalten wird gruppiert?

## Quiz

### Dozent: Quiz erstellen
Als Dozent möchte ich ein Quiz erstellen, um das Verständnis meiner Studenten für den Kursinhalt zu bewerten.

### Dozent: Quiz anonyme Auswertung einsehen
Als Dozent möchte ich eine anonyme Auswertung des Quiz einsehen, um die Gesamtleistung der Klasse ohne Vorurteile zu verstehen.

### Student: Quiz teilnehmen
Als Student möchte ich am Quiz teilnehmen, um mein Verständnis des Kursinhalts zu testen.

### Student: Quiz eigene Auswertung einsehen
Als Student möchte ich meine eigene Auswertung des Quiz einsehen, um meine Leistung und die Bereiche, in denen ich mich verbessern muss, zu verstehen.

### Dozent: Live Quiz durchführen
Als Dozent möchte ich ein Live-Quiz durchführen, um die Studenten in Echtzeit zu engagieren und ihr Verständnis sofort zu bewerten.

### Student: Live Quiz teilnehmen
Als Student möchte ich am Live-Quiz teilnehmen, um ein interaktives Lern-Erlebnis und sofortiges Feedback zu haben.

<!-- ### Story 1
  Als Studierender möchte ich interaktive Quizspiele zu verschiedenen Lerninhalten spielen können, um mein Wissen zu überprüfen und zu vertiefen.

### Story 2
  Als Studierender möchte ich personalisierte Quizspiele spielen können, um meinen individuellen Lernfortschritt zu verfolgen.

### Story 3
  Als Dozent möchte ich Quizfragen erstellen und verwalten können, um den Lernprozess zu unterstützen. -->


## Drittanbieter API-Anbindung

### Eigener Studenplan einsehen
Als Student möchte ich meinen eigenen Studienplan einsehen, um meine Kurse, Zeiten und Termine zu organisieren.
  
### Speiseplan Mensa HTWG einsehen
Als Student möchte ich den Speiseplan der Mensa HTWG einsehen, um zu entscheiden, was ich zum Mittagessen möchte.

### Benachrichtigungen über wichtige Termine/Fristen
Als Student möchte ich Benachrichtigungen über wichtige Termine/Fristen erhalten, um sicherzustellen, dass ich keine wichtigen Termine oder Fristen verpasse.

<!-- ### Story 1
  Als Studierender möchte ich auf externe Lernressourcen zugreifen können, die über APIs in die App integriert sind, um mein Wissen zu erweitern.

### Story 2
  Als Hochschuladministrator möchte ich die Möglichkeit haben, Drittanbieter-Integrationen zu verwalten und zu überwachen. -->

