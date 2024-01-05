// ###### CONFIG ######

// fill in the following variables with the correct file paths (empty strings -> not needed)
const courseFile = "./data/course-1.json";
const feedbackFormFile = "./data/feedback-form-1.json";
const quizFormFile = "./data/quiz-form-1.json";

async function printJavaException(response) {
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
    let courses = await coursesResponse.json();
    let outputCourses = courses.map(course => { return { name: course.name, id: course.id } });
    console.log(`Courses:`);
    console.table(outputCourses);

    // create a new course from the json file
    let course = null;
    try { course = require(courseFile); } catch (error) {}
    if (course) {
        console.log(`Creating course: ${course.name}`);
        let createCourseResponse = await fetch(`${BACKEND_URL}/public/course`, {
            method: "POST",
            headers: {
                "AUTHORIZATION": `Bearer ${jwt}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(course)
        });

        // evaluate the response
        if (createCourseResponse.status !== 200) {
            console.error(`Failed to create course: ${course.name}`);
            await printJavaException(createCourseResponse);
            return;
        }
        let createdCourse = await createCourseResponse.json();
        console.log(`Created course:`, createdCourse);
    }

    // ##### Feedback Forms #####
    // create a new feedback form from the json file
    let feedbackForm = null;
    try { feedbackForm = require(feedbackFormFile); } catch (error) {}
    if (feedbackForm) {
        console.log(`Creating feedback form: ${feedbackForm.name}`);
        let createFeedbackFormResponse = await fetch(`${BACKEND_URL}/public/course/feedback/form`, {
            method: "POST",
            headers: {
                "AUTHORIZATION": `Bearer ${jwt}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(feedbackForm)
        });

        // evaluate the response
        if (createFeedbackFormResponse.status !== 200) {
            console.error(`Failed to create feedback form: ${feedbackForm.name}`);
            await printJavaException(createFeedbackFormResponse);
            return;
        }
        let createdFeedbackForm = await createFeedbackFormResponse.json();
        console.log(`Created feedback form:`, createdFeedbackForm);
    }

    // ##### Quiz Forms #####
    // create a new quiz form from the json file
    let quizForm1 = null;
    try { quizForm1 = require(quizFormFile); } catch (error) {}
    if (quizForm1) {
        console.log(`Creating quiz form: ${quizForm1.name}`);
        let createQuizFormResponse = await fetch(`${BACKEND_URL}/public/course/quiz/form`, {
            method: "POST",
            headers: {
                "AUTHORIZATION": `Bearer ${jwt}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(quizForm1)
        });

        // evaluate the response
        if (createQuizFormResponse.status !== 200) {
            console.error(`Failed to create quiz form: ${quizForm1.name}`);
            await printJavaException(createQuizFormResponse);
            return;
        }
        let createdQuizForm = await createQuizFormResponse.json();
        console.log(`Created quiz form:`, createdQuizForm);
    }

    console.log(`Successfully finished.`);
}


main();