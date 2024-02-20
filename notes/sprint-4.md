# Sprint Review + Planing 20.02.2024

## Notizen aus letztem Meeting

- erster Einsatz durch Herr Schimkat Anfang April
  - bis dahin:
    - Feedback voll einsatzfähig
    - Quiz voll einsatzfähig
  - UI zum Erstellen erst später

## Sprintergebnis

- **Tests** erfolgreich eingerichtet und grob für alles mal Tests geschrieben (61 % Coverage)
  - war schwierig wegen den Websockets aber machbar
- **Deployment**
  - alles ist containerized
  - updaten geht jetzt mit einem Befehl auf dem Server
- **Moodle-Integration** erfolgreich implementiert
  - jeder Kurs ist jetzt einem Moodle-Kurs zugeordnet (per KursId)
  - Studenten werden automatisch den Kursen zugeordnet
- **Mensa-Plan** in App erfolgreich integriert (→ Datenspeicherung (nicht für jeden Aufruf ein Request an Seezeit))
- **Bugfixes**
- Socket-Timeout-Problem gelöst (musse in NGINX angepasst werden)
- AutoLogin
- Logout
- Slider verändert Farbe

## Abstimmung

- SSL auf Server
- Deployment App-/PlayStore

## Sprintplanning