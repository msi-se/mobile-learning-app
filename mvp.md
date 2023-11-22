# Minimal Viable Product (Sprint Goal 1)

## Stories

### App-Verfügbarkeit
Als Benutzer möchte ich auf die Lern-App über mein Smartphone und über den Browser zugreifen können.

#### Akzeptanzkriterien
- die App ist über den Browser erreichbar mit einer Subdomain der HTWG (z.B.: `lernapp.htwg-konstanz.de`)
- Hinweis: App Store und Play Store Verfügbarkeit ist nicht Teil des MVPs

### UI/UX
Als Benutzer möchte ich eine benutzerfreundliche Oberfläche haben, um die App einfach und intuitiv zu bedienen.

#### Akzeptanzkriterien
- Design in Material UI
- Corporate Identity der HTWG ist im App Design wiederzufinden
- klare Appstruktur & einfache Navigationsmöglichkeiten

### Account- & Profilverwaltung
Als Studierender und als Dozent möchte ich einen Account bei der App haben.
Als Basis soll mein Hochschulaccount verwendet werden (sodass kein neues Passwort erstellt werden muss).

#### Akzeptanzkriterien
- der Login in der App soll mit meinem Hochschulaccount möglich sein

## Feedback-Bogen erstellen
Als Dozent möchte ich einen Feedback-Bogen erstellen können, um Feedback von den Studierenden zu Lehrveranstaltungen zu erhalten.

#### Akzeptanzkriterien
- Es besteht die Möglichkeit zu jeder Veranstaltung ein Feedback-Kanal zu erstellen (z.B.: "Diskrete Mathematik WS23/34").
- Darin lassen sich Feedback-Bögen vorbereiten (z.B.: "Feedback zum ersten Monat der Veranstaltung").
  - Die Bögen können aus verschiedenen Elementen aufgebaut werden:
    - Regler (1-10)
    - Volltext-Eingabe
    - Hinweis: weitere Elemente kommen in späteren Sprints
  - Bei den Elementen kann ausgewählt werden, ob sie Pflicht sind oder optional.
- Der Ersteller kann einen QR-Code/Zugangscode generieren, den Studierende scannen können, um das Feedback zu geben.

## Feedback-Bogen ausfüllen
Als Studierender möchte ich die Möglichkeit haben, anonymes Feedback zu einer Lehrveranstaltung zu geben, um meine Meinung zu äußern.

#### Akzeptanzkriterien
- Per Handy-Kamera lässt sich ein QR-Code scannen bzw. ein Zugangscode eingeben, um so einen Zugang zum Feedbackbogen zu erhalten
- die Elemente des Fragebogens lassen sich wie vom Ersteller gedacht steuern und schließlich fertigstellen mit einem Knopf "Feedback absenden"
- Der User bekommt eine Bestätigung/Fehlermeldung wenn das Feedback abgesendet wurde

### Feedback-Bogen einsehen
Als Dozent möchte ich die Möglichkeit haben, meine Feedback-Bögen einzusehen, um die Meinung der Studierenden zu erfahren.

#### Akzeptanzkriterien
- Der Dozent kann eine Übersicht über alle erstellten Feedback-Bögen einsehen.
- Er kann einen Feedback-Bogen auswählen und die Ergebnisse einsehen.
- Die Ergebnisse werden anonymisiert und wo möglich kummulierte angezeigt.

# Sprint Goal

Das Ziel dieses Sprints ist es, eine grundlegende Version der Lern-App bereitzustellen, die es Benutzern ermöglicht, auf die Anwendung über den Browser und das Smartphone zuzugreifen. Die Benutzeroberfläche soll benutzerfreundlich gestaltet sein und das Corporate Identity der HTWG widerspiegeln. Darüber hinaus soll die Account- und Profilverwaltung implementiert werden, wobei der Login über den Hochschulaccount erfolgt.
Als erstes großes Feature der App soll die Erstellung von Feedback-Bögen für Lehrveranstaltungen implementiert werden. Dabei soll es Dozenten möglich sein, Feedback-Bögen zu erstellen, die dann von Studierenden ausgefüllt werden können.