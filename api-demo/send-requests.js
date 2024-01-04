async function main() {

    const BACKEND_URL = "http://localhost:8080";

    // ##### Login #####
    let loginResponse = await fetch(`${BACKEND_URL}/user/login`, {
        method: 'POST',
        headers: {
            "AUTHORIZATION": "Basic " + btoa("Prof:")
        }
    });
    let jwt = await loginResponse.text();
    console.log(`Successfully logged in. JWT: ${jwt}`);

    // ##### Courses #####
    // print out all courses
    let coursesResponse = await fetch(`${BACKEND_URL}/course`, {
        method: 'GET',
        headers: {
            "AUTHORIZATION": `Bearer ${jwt}`
        }
    });
    let courses = await coursesResponse.json();
    let outputCourses = courses.map(course => { return { name: course.name, id: course.id } });
    console.log(`Courses:`);
    console.table(outputCourses);

    // create a new course from the "course-1.json" file
    // const course1 = require('./course-1.json');
    // console.log(`Creating course: ${course1.name}`);
    // let createCourseResponse = await fetch(`${BACKEND_URL}/public/course`, {
    //     method: 'POST',
    //     headers: {
    //         "AUTHORIZATION": `Bearer ${jwt}`,
    //         "Content-Type": "application/json"
    //     },
    //     body: JSON.stringify(course1)
    // });
    // let createdCourse = await createCourseResponse.json();
    // console.log(`Created course: ${JSON.stringify(createdCourse)}`);

    // ##### Feedback Forms #####
    // create a new feedback form from the "feedback-form-1.json" file
    const feedbackForm1 = require('./feedback-form-1.json');
    console.log(`Creating feedback form: ${feedbackForm1.name}`);
    let createFeedbackFormResponse = await fetch(`${BACKEND_URL}/public/course/feedback/form`, {
        method: 'POST',
        headers: {
            "AUTHORIZATION": `Bearer ${jwt}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify(feedbackForm1)
    });
    let createdFeedbackForm = await createFeedbackFormResponse.json();
    console.log(`Created feedback form: ${JSON.stringify(createdFeedbackForm)}`);

}


main();