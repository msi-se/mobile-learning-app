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


## Elasticsearch
Daten können sehr schnell indexiert werden.