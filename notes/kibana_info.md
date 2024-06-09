# Kibana
## Basic Knowledge
Kibana kann Daten auf verschiedenste Art und Weise visualiseren, dabei wird Elasticsearch unter der Haube verwendet.

Kibana ist Open Source und beinhaltet eine GUI im Browser. Für optimale Nutzung kann der ELK-Stack in Betracht gezogen werden. Mit Elasticsearch zur Indexierung der Daten und dass sie durchsuchbar sind, Logstash als Pipeline und Kibana zur Visualisierung im Browser. Hierbei ist Logstash aber optional.

## Anwendungsfälle
- Logging
- Metrics (Kennzahlen, Leistungsdaten)
- Sicherheitsanalyse
- Businessanalyse

## Installationsarten
### Auf dem Server selbst gemanaged
- Voraussetzung ist Elasticsearch, was installiert werden muss
- Kibana ist als Service startbar

### Elastic Cloud
- in Verbindung mit AWS/Azure/Google Cloud

### Enterprise/Kubernetes
- benutze die gleiche Software wie bei Elastic Cloud,
aber man installiert es bei sich selbst 
 
## Anzeige vom Dashboard
- Port 5601


# Fazit zur Verwendung von Kibana
Durch Kibana in Verbindung mit Elasticsearch können Nutzungsdaten über die App über eine WebGUI dargestellt werden,
dies bringt aber in der aktuellen Phase des Projekts keinen nennenswerten Mehrwert, da die Benutzerzahl zu gering ist.
Außerdem würde die Verwendung von Elasticsearch auch erst Sinn ergeben, wenn weitaus mehr Daten vorhanden sind.
Wenn die App eine große Verwendung gefunden hat, dann kann natürlich der Einsatz der genannten Tools im Hinblick auf die Auswertung in Erwägung gezogen werden.
