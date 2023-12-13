
// # Sprint 2

// - Feedback
//   - Owner von Feedbackchannels
//   - speichern, wer schon abgestimmt hat
//   - QR-Codes
//   - Inkognito-Modus

// - Live-Quiz
//   - Single-Coice
//   - Multiple-Choice
//   - Student kann entscheiden, ob er anonym oder personalisiert teilnimmt
//     - wenn anaonymisiert → Student kann Pseudonamen eingeben
//   - Leaderboard
//   - moderatorgetrieben (Weiterklicken durch Profs)

// - für Feedback und Quiz erstellen kein UI zunächst, sondern per API/JSON input

// - Dashboard (**Mock-Daten**, nur Design)
//   - Grafiken für Prof und Studierende
//   - Wie viele Feedbacks habe ich gegeben?
//   - Wie viele Quizzes hab ich ausgefüllt?
//   - Essensplan
//   - Durchschnittliche Quiz-Position
//   - Wie häufig wurden Feedback-Sessions durchgeführt an der HTWG
//   - KudoCards

let userStories = [
    {
        summary: "Feedback: Owner von Feedbackchannels",
        description: `
        Als Dozent möchte ich nicht, das jeder Student meinen Feedbackform starten kann, sondern nur ich.

        - Feedbackchannels haben einen Owner
        - nur dieser kann Forms in Channels starten
        - nur dieser sieht die Übersicht der Feedbacks
        - nur dieser sieht die Ergebnisse der Feedbacks
        `,
        subtasks: [
            "Model: Ownerfeld zu Feedbackchannel hinzufügen",
            "Service: Änderung nur dem Owner erlauben",
            "Service: Ergebnisse nur dem Owner zeigen",
            "Frontend: UI auf Rolle des Users anpassen",
        ]
    },
    {
        summary: "Feedback: Nur ein Feedback pro Student",
        description: `
        Als Dozent möchte ich, dass ein Student nur einmal Feedback geben kann.

        - Studenten können nur einmal pro Form und Element Feedback geben
        `,
        subtasks: [
            "Model: FeedbackResult-Model um UserId erweitern",
            "Service: Feedback nur einmal pro User erlauben",
            "Frontend: Wenn abgestimmt, dann nicht mehr abstimmen lassen",
        ]
    },
    {
        summary: "Feedback: QR-Codes",
        description: `
        Als Dozent möchte ich, dass Studenten schnell und einfach per Scan eines QR-Codes an einem Feedback teilnehmen können.

        - QR-Codes werden automatisch generiert und angezeigt
        - QR-Codes können vom Studenten gescannt werden
        - Studenten werden automatisch zum Feedback weitergeleitet
        `,
        subtasks: [
            "Frontend: QR-Code anzeigen",
            "Frontend: QR-Code scannen und zum Feedback weiterleiten",
        ],
    },
    {
        summary: "Feedback: Inkognito-Modus",
        description: `
        Als Student möchte ich deutlich wissen, wann ich anonyme Funktionen nutze.

        - Studenten bekommen ein Inkognito-Icon angezeigt, wenn sie anonyme Funktionen nutzen
        `,
        subtasks: [
            "Frontend: Inkognito-Icon anzeigen",
        ],
    },
    {
        summary: "Feedback: Forms per API erstellen",
        description: `
        Als Dozent möchte ich Feedbackforms per API erstellen können.

        - Feedbackforms können per API erstellt werden (JSON)
        - Es gibt eine Dokumentation für die API
        `,
        subtasks: [
            "Service: Public API für Feedbackforms erstellen/erweitern",
            "Dokumentation: API-Dokumentation erstellen",
        ],
    },
    {
        summary: "Quiz: Generelle Funktionalität",
        description: `
        Als Dozent möchte ich, dass ich moderierte Quizze durchführen kann, an denen Studenten teilnehmen können.

        - Dozent kann Quizze moderieren (Frage für Frage weiterklicken)
        - Studenten können an Quizzes teilnehmen
        - Single-Choice
        - Multiple-Choice
        `,
        subtasks: [
            "Model: FeedbackChannel-Model in Course-Model umbenennen",
            "Model: QuizForm-Model erstellen",
        ],
    },
    {
        summary: "Quiz: Anonyme Teilnahme",
        description: `
        Als Student möchte ich anonym an einem Quiz teilnehmen können.

        - Studenten können entscheiden, ob sie anonym oder personalisiert teilnehmen
        - wenn anonym, dann können sie einen Pseudonamen eingeben
        `
    },
    {
        summary: "Quiz: Leaderboard",
        description: `
        Als Student möchte ich sehen, wie ich im Vergleich zu anderen Studenten bei einem Quiz abgeschnitten habe.

        - Studenten werden auf einem Leaderboard getrackt
        - Dozent sieht dies zwischen den Fragen und am Ende
        `
    },
    {
        summary: "Quiz: Quiz per API erstellen",
        description: `
        Als Student möchte ich sehen, wie ich im Vergleich zu anderen Studenten bei einem Quiz abgeschnitten habe.

        - Studenten werden auf einem Leaderboard getrackt
        - Dozent sieht dies zwischen den Fragen und am Ende
        `
    },
    {
        summary: "Dashboard: Grafiken und Platzhalter für persönliche Daten",
        description: `
        Als Benutzer möchte ich auf dem Dashboard Grafiken und interessante Informationen zu meinem Nutzungsverhalten und der HTWG sehen.

        - Grafiken für Prof und Studierende
        - Wie viele Feedbacks habe ich gegeben?
        - Wie viele Quizzes hab ich ausgefüllt?
        - Essensplan
        - Durchschnittliche Quiz-Position
        - Wie häufig wurden Feedback-Sessions durchgeführt an der HTWG
        - KudoCards
        `
    },
];


async function main() {

    const createMetaLink = "https://kp2.in.htwg-konstanz.de/jira7/rest/api/2/issue";

    for (let i = 0; i < userStories.length; i++) {

        let response = await fetch(createMetaLink, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Basic " + btoa("jo871bra:DasistderdritteCodeathtwg")
            },
            body: JSON.stringify({
                "fields": {
                    "project": {
                        "key": "TML"
                    },
                    "summary": userStories[i].summary,
                    "description": userStories[i].description,
                    "issuetype": {
                        "name": "Story"
                    },
                }
            })
        });
        let data = await response.status;
        console.log(data);

    }
}

// main();