
let baseUrl = "https://lsf.htwg-konstanz.de/qisserver"
let frontendUrl = "https://lsf.htwg-konstanz.de/qisserver/rds?state=wplan&act=show&show=plan&P.subc=plan&navigationPosition=functions%2CscheduleLoggedin&breadcrumb=schedule&topitem=functions&subitem=scheduleLoggedin"
let icalUrl = "https://lsf.htwg-konstanz.de/qisserver/rds?state=verpublish&status=transform&vmfile=no&termine=376382,380699,376777,380700,380701,380776,376385,376469,383933,380694,383932,381875&moduleCall=iCalendarPlan&publishConfFile=reports&publishSubDir=veranstaltung"

// async function main() {

//     let frontendResponse = await fetch(frontendUrl);
//     let frontendText = await frontendResponse.text();
//     console.log(frontendText);

// }

async function main() {

    // Dart:
    // final response = await http.post(
    //     Uri.parse("${getBackendUrl()}/user/login"),
    //     headers: {
    //       "Content-Type": "application/json",
    //       "AUTHORIZATION":
    //           "Basic ${base64Encode(utf8.encode('$username:$password'))}"
    //     },
    //   );

    // Javascript:
    let response = await fetch(`http://localhost:8080/user/login`, {
        method: 'POST',
        headers: {
            "AUTHORIZATION": "Basic " + btoa("Prof:")
        }
    });
    let jwt = await response.text();
    console.log(jwt);


}

main();