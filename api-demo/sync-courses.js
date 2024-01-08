// ###### CONFIG ######

const coursesFile = "./courses.json";

async function printJavaException(response) {
    // console.log(response);
    let javaException = await response.json();
    let message = javaException.details.split(", ")[1] || javaException.message;
    console.error(`Backend Exception: ${message}`);
}

async function main() {

    // use dotenv to load environment variables from the ".env" file
    require("dotenv").config();

    // ##### Constants #####
    const BACKEND_URL = process.env.BACKEND_URL || "http://localhost:8080";
    const HTWG_USERNAME = process.env.HTWG_USERNAME;
    const HTWG_PASSWORD = process.env.HTWG_PASSWORD;

    console.log(`Using the following environment variables:`);
    console.log(`BACKEND_URL: "${BACKEND_URL}"`);
    console.log(`HTWG_USERNAME: "${HTWG_USERNAME}"`);
    console.log(`HTWG_PASSWORD: "${HTWG_PASSWORD}"`);

    // ##### Login #####
    let loginResponse = await fetch(`${BACKEND_URL}/user/login`, {
        method: "POST",
        headers: {
            "AUTHORIZATION": "Basic " + btoa(`${HTWG_USERNAME}:${HTWG_PASSWORD}`)
        }
    });

    // evaluate the response
    let jwt = await loginResponse.text();
    if (loginResponse.status !== 200) {
        console.error(`Failed to login. Status: ${loginResponse.status}`);
        await printJavaException(loginResponse);
        return;
    }
    console.log(`Successfully logged in.`);
    // console.log(`JWT: ${jwt}`);

    // ##### Courses #####
    // print out all courses
    let coursesResponse = await fetch(`${BACKEND_URL}/course`, {
        method: "GET",
        headers: {
            "AUTHORIZATION": `Bearer ${jwt}`
        }
    });

    // evaluate the response
    if (coursesResponse.status !== 200) {
        console.error(`Failed to get courses. Status: ${coursesResponse.status}`);
        await printJavaException(coursesResponse);
        return;
    }
    let existingCourses = await coursesResponse.json();
    console.log(`Existing courses on the server:`);
    console.table(existingCourses.map(course => { return { name: course.name, key: course.key }; }));

    // sync the courses with the json file
    let courses = null;
    try { courses = require(coursesFile); } catch (error) { }
    if (!courses) {
        console.error(`Failed to load courses from "${coursesFile}".`);
        return;
    }

    console.log(`Syncing courses with "${coursesFile}"...`);
    courses.forEach(element => { console.log(element.name); });

    let createCourseResponse = await fetch(`${BACKEND_URL}/public/courses`, {
        method: "PATCH",
        headers: {
            "AUTHORIZATION": `Bearer ${jwt}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify(courses)
    });

    // evaluate the response
    if (createCourseResponse.status !== 200) {
        await printJavaException(createCourseResponse);
        return;
    }
    let createdCourses = await createCourseResponse.json();
    console.log(`Successfully synced courses.`);
    console.table(createdCourses.map(course => { return { name: course.name, key: course.key }; }));
}


main();