# User-Stories

## Allgemein

### App-Verfügbarkeit
Als Benutzer möchte ich auf die Lern-App über mein Smartphone und über den Browser zugreifen können.

#### Akzeptanzkriterien
- Die App ist über den Browser erreichbar mit einer Subdomain der HTWG (z.B.: `lernapp.htwg-konstanz.de`)
- Die App ist über den Apple App Store und den Google Play Store erreichbar
- Die App ist über den Browser und die App mit dem gleichen Account nutzbar
- Die App hat die gleichen Funktionen auf dem Browser und der App

### UI/UX
Als Benutzer möchte ich eine benutzerfreundliche Oberfläche haben, um die App einfach und intuitiv zu bedienen.

#### Akzeptanzkriterien
- Umsetzung von UX-Design Prinzipien
  - Material UI (funktioniert nativ mit Flutter)
- Corporate Identity der HTWG wird im App Design umgesetzt
- klare Appstruktur & einfache Navigationsmöglichkeiten

### Sicherheits- und Datenschutzbestimmungen
Als Benutzer der Applikation möchte ich sicherstellen, dass meine Daten und meine Privatsphäre geschützt sind, indem Sicherheits- und Datenschutzbestimmungen eingehalten werden.

### Account- & Profilverwaltung
Als Studierender und als Dozent möchte ich einen Account bei der App haben.
Als Basis soll mein Hochschulaccount verwendet werden (sodass kein neues Passwort erstellt werden muss).

#### Akzeptanzkriterien
- der Login in der App ist mit dem HTWG-Account möglich

### FAQ & Support
Als Benutzer möchte ich als Hilfestellung bei bestimmten Fragen, Antworten im FAQ-Bereich finden.
Bei weiteren Fragen kann ich den Support kontaktieren.

#### Akzeptanzkriterien
- Der FAQ-Bereich ist in der App verfügbar.
- Eine Support-E-Mail-Adresse ist in der App verfügbar.
- Das FAQ soll wichtige und häufig gestellte Fragen beantworten.

## Feedback-System

### Dozent: Feedback-Möglichkeit erstellen
Als Dozent möchte ich einen Feedback-Bogen erstellen können, um Feedback von den Studierenden zu Lehrveranstaltungen zu erhalten.

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
- Per Handy-Kamera lässt sich ein QR-Code scannen bzw. ein Zugangscode eingeben, um so einen Zugang zum Feedbackbogen zu erhalten
- die Elemente des Fragebogens lassen sich wie vom Ersteller gedacht steuern und schließlich fertigstellen mit einem Knopf "Feedback absenden"
- Der User bekommt eine Bestätigung/Fehlermeldung, wenn das Feedback abgesendet wurde

### Dozent: Feedback einsehen
Als Dozent möchte ich das erhaltene Feedback zu meinen Lehrveranstaltungen einsehen können, um mögliche Verbesserungen vorzunehmen.

#### Akzeptanzkriterien
- Der Dozent kann eine Übersicht über alle erstellten Feedback-Bögen einsehen.
- Er kann einen Feedback-Bogen auswählen und die Ergebnisse einsehen.
- Die Ergebnisse werden anonymisiert und wo möglich kummulierte angezeigt.


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

## Drittanbieter API-Anbindung

### Eigener Studenplan einsehen
Als Student möchte ich meinen eigenen Studienplan einsehen, um meine Kurse, Zeiten und Termine zu organisieren.
  
### Speiseplan Mensa HTWG einsehen
Als Student möchte ich den Speiseplan der Mensa HTWG einsehen, um zu entscheiden, was ich zum Mittagessen möchte.

### Benachrichtigungen über wichtige Termine/Fristen
Als Student möchte ich Benachrichtigungen über wichtige Termine/Fristen erhalten, um sicherzustellen, dass ich keine wichtigen Termine oder Fristen verpasse.

### Quiz und Feedback anhand der Moodle Kurse Filtern
Als Student möchte ich Quiz und Feedbacks basierend auf meinen Moodle-Kursen filtern können, damit ich relevante Informationen schnell und effizient finden kann. Somit habe ich alle Quiz und Feedbacks von meinen belegten Kursen im Überblick.

Beispiel: Live Quiz für Vorlesung (mit Moodle-Integration)
- Vorbereitung
  - Dozent wählt gewünschten Moodle Kurs aus, falls es einen gibt (optional)
  - Dozent erstellt Quiz mit mehreren Fragen (+ Antworten)
- In der Vorlesung
  - Dozent schält Quiz Live
    - Alle Studenten, welche in dem Moodle Kurs eingeschrieben sind bekommen eine Benachrichtigung (falls gewünscht)
    - In der App wird das Live Quiz angezeigt und ggf. vorhergehoben.
    - Mit Klick auf die Benachrichtigung oder einer Schaltfläche in der App kommt man zu dem Quiz
    - QR Code und/oder Zahlencode wird als Fallback angezeigt. Über diesen Code kann ebenfalls an dem Live Quiz teilgenommen werden. Falls kein Moodle Kurs existiert ist das die einzige Option.
  - Live Quiz wird durchgeführt:
    - Dozent kann Frage für Frage durchschalten
    - Anonyme Live Auswertung

Quiz außerhalb der Vorlesungen und Feedback können auch mit Moodle Kursen Verknüpft werden.

Vorteile:
- Sehr einfache Bedienung und schnelles Beitreten/Teilnehmen an Quiz und Feedback für die Studierenden
- Schnelleres Beitreten für Live Quiz = Weniger Vorlesungszeit wird in Anspruch genommen
- Übersichtliche Organisation der Quiz/Feedbacks in Moodle Kursen für die Dozierenden

