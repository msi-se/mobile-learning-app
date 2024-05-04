import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"
import { toast } from "sonner";
import Router from "next/router";
import { Course, FeedbackForm, FeedbackQuestion } from "./models";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function getBackendUrl() {
  const currentUrl = window.location.href;
  return currentUrl.includes("localhost") ? "http://localhost:8080" : `${currentUrl}/api`;
}

export async function hasValidJwtToken(): Promise<boolean> {
  const jwtToken = localStorage.getItem("jwtToken");
  if (!jwtToken) {
    return false;
  }

  // check if the token is still valid
  const BACKEND_URL = getBackendUrl();

  let checkResponse = await fetch(`${BACKEND_URL}/user/verify`, {
    method: "GET",
    headers: {
      "AUTHORIZATION": "Bearer " + jwtToken
    }
  });
  if (checkResponse.status !== 200) {
    // console.error(`Token is invalid. Status: ${checkResponse.status}`);
    // console.error(checkResponse);
    toast.error("Token is invalid. Please login again.");
    localStorage.removeItem("jwtToken");
    return false;
  }
  // console.log(`Token is still valid.`);
  // console.log(`JWT: ${jwtToken}`);
  return true;
}

export async function login(username: string, password: string): Promise<boolean> {
  // if (!username || !password) {
  //   console.error("Username and password are required.");
  //   toast.error("Username and password are required.");
  //   return false;
  // }
  // DEBUG: password is not required
  if (!username) {
    console.error("Username is required.");
    toast.error("Username is required.");
    return false;
  }

  const BACKEND_URL = getBackendUrl();

  let loginResponse = await fetch(`${BACKEND_URL}/user/login`, {
    method: "POST",
    headers: {
      "AUTHORIZATION": "Basic " + btoa(`${username}:${password}`)
    }
  });
  let jwt = await loginResponse.text();
  if (loginResponse.status !== 200) {
    // console.error(`Failed to login. Status: ${loginResponse.status}`);
    // console.error(loginResponse);
    toast.error("Failed to login. Please check your credentials.");
    return false;
  }
  localStorage.setItem("jwtToken", jwt);
  return true;
}

export async function fetchCourses(): Promise<Course[]> {
  const BACKEND_URL = getBackendUrl();
  const jwtToken = localStorage.getItem("jwtToken");
  if (!jwtToken) {
    console.error("No JWT token found.");
    return [];
  }

  let coursesResponse = await fetch(`${BACKEND_URL}/course`, {
    method: "GET",
    headers: {
      "AUTHORIZATION": "Bearer " + jwtToken
    }
  });
  let coursesRaw = await coursesResponse.json();
  if (coursesResponse.status !== 200) {
    console.error(`Failed to get courses. Status: ${coursesResponse.status}`);
    console.error(coursesResponse);
    toast.error("Failed to get courses. Please try again.");
    return [];
  }
  console.log(coursesRaw);
  let courses = coursesRaw.map((course: any) => {
    return {
      id: course.id,
      name: course.name,
      description: course.description,
      moodleCourseId: course.moodleCourseId,
      feedbackForms: course.feedbackForms.map((form: any) => { return { ...form, type: "Feedback" } }),
      quizForms: course.quizForms.map((form: any) => { return { ...form, type: "Quiz" } })
    };
  });
  console.log(courses);

  return courses;
}


export async function fetchCourse(id: string): Promise<Course | null> {

  let courses = await fetchCourses();
  let course = courses.find((c) => c.id === id);
  if (!course) {
    console.error(`Course with id ${id} not found.`);
    toast.error("Course not found.");
    return null;
  }
  return course;

}

export async function fetchFeedbackForm(courseId: string, formId: string): Promise<FeedbackForm | null> {
  
  // course/id/feedback/form/id/
  const BACKEND_URL = getBackendUrl();
  const jwtToken = localStorage.getItem("jwtToken");
  if (!jwtToken) {
    console.error("No JWT token found.");
    return null;
  }

  let feedbackFormResponse = await fetch(`${BACKEND_URL}/course/${courseId}/feedback/form/${formId}`, {
    method: "GET",
    headers: {
      "AUTHORIZATION": "Bearer " + jwtToken
    }
  });
  let feedbackForm = await feedbackFormResponse.json();
  if (feedbackFormResponse.status !== 200) {
    console.error(`Failed to get feedback form. Status: ${feedbackFormResponse.status}`);
    console.error(feedbackFormResponse);
    toast.error("Failed to get feedback form. Please try again.");
    return null;
  }
  console.log(feedbackForm);
  return {
    ...feedbackForm,
    questions: feedbackForm.questions.map((question: any) => {
      return question.questionContent
    })
  };
}

export async function fetchFeedbackQuestion(courseId: string, formId: string, questionId: string): Promise<FeedbackQuestion | null> {
  
  // course/id/feedback/form/id/question/id
  const BACKEND_URL = getBackendUrl();
  const jwtToken = localStorage.getItem("jwtToken");
  if (!jwtToken) {
    console.error("No JWT token found.");
    return null;
  }

  let feedbackQuestionResponse = await fetch(`${BACKEND_URL}/course/${courseId}/feedback/form/${formId}/question/${questionId}`, {
    method: "GET",
    headers: {
      "AUTHORIZATION": "Bearer " + jwtToken
    }
  });
  let feedbackQuestion = await feedbackQuestionResponse.json();
  if (feedbackQuestionResponse.status !== 200) {
    console.error(`Failed to get feedback question. Status: ${feedbackQuestionResponse.status}`);
    console.error(feedbackQuestionResponse);
    toast.error("Failed to get feedback question. Please try again.");
    return null;
  }
  console.log(feedbackQuestion);
  return feedbackQuestion;
}

// listCourses(); -> Name, Description

// getCourse(params.courseId); -> Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
// updateCourse(params.courseId, courseName, courseDescription, courseMoodleCourseId); -> Error | Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
// addCourse(courseName, courseDescription); -> Error | Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
// deleteCourse(params.courseId); -> Error | Success

// getFeedbackForm(params.courseId, params.formId); -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
// updateFeedbackForm(params.courseId, params.formId, feedbackformName, feedbackformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
// addFeedbackForm(params.courseId, feedbackformName, feedbackformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
// deleteFeedbackForm(params.courseId, params.formId) -> Error | Success

// getFeedbackQuestion(params.courseId, params.formId, params.questionId); -> Error | Name, Description, Type, Options, RangeLow, RangeHigh
// updateFeedbackQuestion(params.courseId, params.formId, params.questionId, feedbackQuestion?.name, feedbackQuestion?.description, feedbackQuestion?.type, feedbackQuestion?.options, feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name, Description, Type, Options, RangeLow, RangeHigh
// addFeedbackQuestion(params.courseId, params.formId, feedbackQuestion?.name, feedbackQuestion?.description, feedbackQuestion?.type, feedbackQuestion?.options, feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name, Description, Type, Options, RangeLow, RangeHigh
// deleteFeedbackQuestion(params.courseId, params.formId, params.questionId) -> Error | Success
