import { Course, FeedbackForm, FeedbackQuestion, QuizForm } from "./models";
import { toast } from "sonner";
import getBackendUrl from "@/lib/get-backend-url";

// GET /maint/courses
// listCourses(); -> Name, Description
export async function listCourses(): Promise<Course[]> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let courseResponse = await fetch(`${BACKEND_URL}/maint/courses`, {
        method: "GET",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (courseResponse.status !== 200) {
        toast.error(`Failed to get courses. Please try again. Status: ${courseResponse.status}`);
        return [];
    }
    let courses = await courseResponse.json();
    return courses;
}

// GET /maint/course/${courseId}
// getCourse(params.courseId); -> Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
export async function fetchCourse(courseId: string): Promise<Course | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let courseResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}`, {
        method: "GET",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (courseResponse.status !== 200) {
        toast.error(`Failed to get course. Please try again. Status: ${courseResponse.status}`);
        return null;
    }
    let course = await courseResponse.json();

    course.feedbackForms = course.feedbackForms.map((form: any) => { return { ...form, type: "Feedback" } });
    course.quizForms = course.quizForms.map((form: any) => { return { ...form, type: "Quiz" } });


    return course;
}

// PUT /maint/course/${courseId} ({String name, String description, String moodleCourseId})
// updateCourse(params.courseId, courseName, courseDescription, courseMoodleCourseId); -> Error | Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
export async function updateCourse(courseId: string, courseName: string, courseDescription: string, courseMoodleCourseId: string): Promise<Course | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let courseResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}`, {
        method: "PUT",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: courseName, description: courseDescription, moodleCourseId: courseMoodleCourseId })
    });
    if (courseResponse.status !== 200) {
        toast.error(`Failed to update course. Please try again. Status: ${courseResponse.status}`);
        return null;
    }
    let course = await courseResponse.json();
    return course;
}

// POST /maint/course ({String name, String description, String moodleCourseId})
// addCourse(courseName, courseDescription); -> Error | Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
export async function addCourse(courseName: string, courseDescription: string, courseMoodleCourseId: string): Promise<Course | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let courseResponse = await fetch(`${BACKEND_URL}/maint/course`, {
        method: "POST",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: courseName, description: courseDescription, moodleCourseId: courseMoodleCourseId })
    });
    if (courseResponse.status !== 200) {
        toast.error(`Failed to add course. Please try again. Status: ${courseResponse.status}`);
        return null;
    }
    let course = await courseResponse.json();
    return course;
}

// DELETE /maint/course/${courseId}
// deleteCourse(params.courseId); -> Error | Success
export async function deleteCourse(courseId: string): Promise<boolean> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let courseResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}`, {
        method: "DELETE",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (courseResponse.status !== 200) {
        toast.error(`Failed to delete course. Please try again. Status: ${courseResponse.status}`);
        return false;
    }
    return true;
}

// GET /maint/course/${courseId}/feedback/form/${formId}
// getFeedbackForm(params.courseId, params.formId); -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
export async function fetchFeedbackForm(courseId: string, formId: string): Promise<FeedbackForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form/${formId}`, {
        method: "GET",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (feedbackFormResponse.status !== 200) {
        toast.error(`Failed to get feedback form. Please try again. Status: ${feedbackFormResponse.status}`);
        return null;
    }
    let feedbackForm = await feedbackFormResponse.json();
    feedbackForm.questions = feedbackForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return feedbackForm;
}

// PUT /maint/course/${courseId}/feedback/form/${formId} ({String name, String description})
// updateFeedbackForm(params.courseId, params.formId, feedbackformName, feedbackformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
export async function updateFeedbackForm(courseId: string, formId: string, feedbackformName: string, feedbackformDescription: string): Promise<FeedbackForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form/${formId}`, {
        method: "PUT",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: feedbackformName, description: feedbackformDescription })
    });
    if (feedbackFormResponse.status !== 200) {
        toast.error(`Failed to update feedback form. Please try again. Status: ${feedbackFormResponse.status}`);
        return null;
    }
    let feedbackForm = await feedbackFormResponse.json();
    feedbackForm.questions = feedbackForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return feedbackForm;
}

// PUT /maint/course/${courseId}/feedback/form/${formId}/reorder ({String[] questionIds})
// reorderFeedbackFormQuestions(params.courseId, params.formId, questionIds) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
export async function reorderFeedbackFormQuestions(courseId: string, formId: string, questionIds: string[]): Promise<FeedbackForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form/${formId}/reorder`, {
        method: "PUT",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ questionIds })
    });
    if (feedbackFormResponse.status !== 200) {
        toast.error(`Failed to reorder feedback form questions. Please try again. Status: ${feedbackFormResponse.status}`);
        return null;
    }
    let feedbackForm = await feedbackFormResponse.json();
    feedbackForm.questions = feedbackForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return feedbackForm;
}

// POST /maint/course/${courseId}/feedback/form ({String name, String description})
// addFeedbackForm(params.courseId, feedbackformName, feedbackformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
export async function addFeedbackForm(courseId: string, feedbackformName: string, feedbackformDescription: string): Promise<FeedbackForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form`, {
        method: "POST",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: feedbackformName, description: feedbackformDescription })
    });
    if (feedbackFormResponse.status !== 200) {
        toast.error(`Failed to add feedback form. Please try again. Status: ${feedbackFormResponse.status}`);
        return null;
    }
    let feedbackForm = await feedbackFormResponse.json();
    feedbackForm.questions = feedbackForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return feedbackForm;
}

// POST /maint/course/${courseId}/feedback/form/${formId}/copy
// copyFeedbackForm(params.courseId, params.formId) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
export async function copyFeedbackForm(courseId: string, formId: string): Promise<FeedbackForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form/${formId}/copy`, {
        method: "POST",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (feedbackFormResponse.status !== 200) {
        toast.error(`Failed to copy feedback form. Please try again. Status: ${feedbackFormResponse.status}`);
        return null;
    }
    let feedbackForm = await feedbackFormResponse.json();
    feedbackForm.questions = feedbackForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return feedbackForm;
}

// DELETE /maint/course/${courseId}/feedback/form/${formId}
// deleteFeedbackForm(params.courseId, params.formId) -> Error | Success
export async function deleteFeedbackForm(courseId: string, formId: string): Promise<boolean> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form/${formId}`, {
        method: "DELETE",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (feedbackFormResponse.status !== 200) {
        toast.error(`Failed to delete feedback form. Please try again. Status: ${feedbackFormResponse.status}`);
        return false;
    }
    return true;
}

// GET /maint/course/${courseId}/feedback/form/${formId}/question/${questionId}
// getFeedbackQuestion(params.courseId, params.formId, params.questionId); -> Error | Name, Description, Type, Options, RangeLow, RangeHigh
export async function fetchFeedbackQuestion(courseId: string, formId: string, questionId: string): Promise<FeedbackQuestion | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackQuestionResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form/${formId}/question/${questionId}`, {
        method: "GET",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (feedbackQuestionResponse.status !== 200) {
        toast.error(`Failed to get feedback question. Please try again. Status: ${feedbackQuestionResponse.status}`);
        return null;
    }
    let feedbackQuestion = await feedbackQuestionResponse.json();
    feedbackQuestion = { ...feedbackQuestion.questionContent, id: feedbackQuestion.id };
    
    return feedbackQuestion;
}

// PUT /maint/course/${courseId}/feedback/form/${formId}/question/${questionId} ({String name, String description, String type, String[] options, int rangeLow, int rangeHigh})
// updateFeedbackQuestion(params.courseId, params.formId, params.questionId, feedbackQuestion?.name, feedbackQuestion?.description, feedbackQuestion?.type, feedbackQuestion?.options, feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name, Description, Type, Options, RangeLow, RangeHigh
export async function updateFeedbackQuestion(courseId: string, formId: string, questionId: string, feedbackQuestionName: string, feedbackQuestionDescription: string, feedbackQuestionType: string, feedbackQuestionOptions: string[], feedbackQuestionRangeLow: string, feedbackQuestionRangeHigh: string): Promise<FeedbackQuestion | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackQuestionResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form/${formId}/question/${questionId}`, {
        method: "PUT",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: feedbackQuestionName, description: feedbackQuestionDescription, type: feedbackQuestionType, options: feedbackQuestionOptions, rangeLow: feedbackQuestionRangeLow, rangeHigh: feedbackQuestionRangeHigh })
    });
    if (feedbackQuestionResponse.status !== 200) {
        toast.error(`Failed to update feedback question. Please try again. Status: ${feedbackQuestionResponse.status}`);
        return null;
    }
    let feedbackQuestion = await feedbackQuestionResponse.json();
    feedbackQuestion = { ...feedbackQuestion.questionContent, id: feedbackQuestion.id };

    return feedbackQuestion;
}

// POST /maint/course/${courseId}/feedback/form/${formId}/question ({String name, String description, String type, String[] options, int rangeLow, int rangeHigh})
// addFeedbackQuestion(params.courseId, params.formId, feedbackQuestion?.name, feedbackQuestion?.description, feedbackQuestion?.type, feedbackQuestion?.options, feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name, Description, Type, Options, RangeLow, RangeHigh
export async function addFeedbackQuestion(courseId: string, formId: string, feedbackQuestionName: string, feedbackQuestionDescription: string, feedbackQuestionType: string, feedbackQuestionOptions: string[], feedbackQuestionRangeLow: string, feedbackQuestionRangeHigh: string): Promise<FeedbackQuestion | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackQuestionResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form/${formId}/question`, {
        method: "POST",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: feedbackQuestionName, description: feedbackQuestionDescription, type: feedbackQuestionType, options: feedbackQuestionOptions, rangeLow: feedbackQuestionRangeLow, rangeHigh: feedbackQuestionRangeHigh })
    });
    if (feedbackQuestionResponse.status !== 200) {
        toast.error(`Failed to add feedback question. Please try again. Status: ${feedbackQuestionResponse.status}`);
        return null;
    }
    let feedbackQuestion = await feedbackQuestionResponse.json();
    feedbackQuestion = { ...feedbackQuestion.questionContent, id: feedbackQuestion.id };

    return feedbackQuestion;
}

// DELETE /maint/course/${courseId}/feedback/form/${formId}/question/${questionId}
// deleteFeedbackQuestion(params.courseId, params.formId, params.questionId) -> Error | Success
export async function deleteFeedbackQuestion(courseId: string, formId: string, questionId: string): Promise<boolean> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let feedbackQuestionResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/feedback/form/${formId}/question/${questionId}`, {
        method: "DELETE",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (feedbackQuestionResponse.status !== 200) {
        toast.error(`Failed to delete feedback question. Please try again. Status: ${feedbackQuestionResponse.status}`);
        return false;
    }
    return true;
}


// NOW THE SAME FOR QUIZ FORMS

// GET /maint/course/${courseId}/quiz/form/${formId}
// getQuizForm(params.courseId, params.formId); -> Error | Name, Description, Questions (Name, Description, Type, Options, HasCorrectAnswers, CorrectAnswers )
export async function fetchQuizForm(courseId: string, formId: string): Promise<QuizForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form/${formId}`, {
        method: "GET",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (quizFormResponse.status !== 200) {
        toast.error(`Failed to get quiz form. Please try again. Status: ${quizFormResponse.status}`);
        return null;
    }
    let quizForm = await quizFormResponse.json();
    quizForm.questions = quizForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return quizForm;
}

// PUT /maint/course/${courseId}/quiz/form/${formId} ({String name, String description})
// updateQuizForm(params.courseId, params.formId, quizformName, quizformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, HasCorrectAnswers, CorrectAnswers)
export async function updateQuizForm(courseId: string, formId: string, quizformName: string, quizformDescription: string): Promise<QuizForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form/${formId}`, {
        method: "PUT",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: quizformName, description: quizformDescription })
    });
    if (quizFormResponse.status !== 200) {
        toast.error(`Failed to update quiz form. Please try again. Status: ${quizFormResponse.status}`);
        return null;
    }
    let quizForm = await quizFormResponse.json();
    quizForm.questions = quizForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return quizForm;
}

// PUT /maint/course/${courseId}/quiz/form/${formId}/reorder ({String[] questionIds})
// reorderQuizFormQuestions(params.courseId, params.formId, questionIds) -> Error | Name, Description, Questions (Name, Description, Type, Options, HasCorrectAnswers, CorrectAnswers)
export async function reorderQuizFormQuestions(courseId: string, formId: string, questionIds: string[]): Promise<QuizForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form/${formId}/reorder`, {
        method: "PUT",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ questionIds })
    });
    if (quizFormResponse.status !== 200) {
        toast.error(`Failed to reorder quiz form questions. Please try again. Status: ${quizFormResponse.status}`);
        return null;
    }
    let quizForm = await quizFormResponse.json();
    quizForm.questions = quizForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return quizForm;
}

// POST /maint/course/${courseId}/quiz/form ({String name, String description})
// addQuizForm(params.courseId, quizformName, quizformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, HasCorrectAnswers, CorrectAnswe)
export async function addQuizForm(courseId: string, quizformName: string, quizformDescription: string): Promise<QuizForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form`, {
        method: "POST",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: quizformName, description: quizformDescription })
    });
    if (quizFormResponse.status !== 200) {
        toast.error(`Failed to add quiz form. Please try again. Status: ${quizFormResponse.status}`);
        return null;
    }
    let quizForm = await quizFormResponse.json();
    quizForm.questions = quizForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return quizForm;
}

// POST /maint/course/${courseId}/quiz/form/${formId}/copy
// copyQuizForm(params.courseId, params.formId) -> Error | Name, Description, Questions (Name, Description, Type, Options, HasCorrectAnswers, CorrectAnswers )
export async function copyQuizForm(courseId: string, formId: string): Promise<QuizForm | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form/${formId}/copy`, {
        method: "POST",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (quizFormResponse.status !== 200) {
        toast.error(`Failed to copy quiz form. Please try again. Status: ${quizFormResponse.status}`);
        return null;
    }
    let quizForm = await quizFormResponse.json();
    quizForm.questions = quizForm.questions.map((question: any) => { return {...question.questionContent, id: question.id} });
    return quizForm;
}

// DELETE /maint/course/${courseId}/quiz/form/${formId}
// deleteQuizForm(params.courseId, params.formId) -> Error | Success
export async function deleteQuizForm(courseId: string, formId: string): Promise<boolean> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizFormResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form/${formId}`, {
        method: "DELETE",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (quizFormResponse.status !== 200) {
        toast.error(`Failed to delete quiz form. Please try again. Status: ${quizFormResponse.status}`);
        return false;
    }
    return true;
}

// GET /maint/course/${courseId}/quiz/form/${formId}/question/${questionId}
// getQuizQuestion(params.courseId, params.formId, params.questionId); -> Error | Name, Description, Type, Options, CorrectAnswers, HasCorrectAnswers
export async function fetchQuizQuestion(courseId: string, formId: string, questionId: string): Promise<FeedbackQuestion | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizQuestionResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form/${formId}/question/${questionId}`, {
        method: "GET",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (quizQuestionResponse.status !== 200) {
        toast.error(`Failed to get quiz question. Please try again. Status: ${quizQuestionResponse.status}`);
        return null;
    }
    let quizQuestion = await quizQuestionResponse.json();
    quizQuestion = { ...quizQuestion.questionContent, id: quizQuestion.id };
    
    return quizQuestion;
}

// PUT /maint/course/${courseId}/quiz/form/${formId}/question/${questionId} ({String name, String description, String type, String[] options, boolean hasCorrectAnswers, String[] correctAnswers})
// updateQuizQuestion(params.courseId, params.formId, params.questionId, quizQuestion?.name, quizQuestion?.description, quizQuestion?.type, quizQuestion?.options, quizQuestion?.hasCorrectAnswers, quizQuestion?.correctAnswers) -> Error | Name, Description, Type, Options, CorrectAnswers, HasCorrectAnswers
export async function updateQuizQuestion(courseId: string, formId: string, questionId: string, quizQuestionName: string, quizQuestionDescription: string, quizQuestionType: string, quizQuestionOptions: string[], quizQuestionHasCorrectAnswers: boolean, quizQuestionCorrectAnswers: string[]): Promise<FeedbackQuestion | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizQuestionResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form/${formId}/question/${questionId}`, {
        method: "PUT",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: quizQuestionName, description: quizQuestionDescription, type: quizQuestionType, options: quizQuestionOptions, hasCorrectAnswers: quizQuestionHasCorrectAnswers, correctAnswers: quizQuestionCorrectAnswers })
    });
    if (quizQuestionResponse.status !== 200) {
        toast.error(`Failed to update quiz question. Please try again. Status: ${quizQuestionResponse.status}`);
        return null;
    }
    let quizQuestion = await quizQuestionResponse.json();
    quizQuestion = { ...quizQuestion.questionContent, id: quizQuestion.id };

    return quizQuestion;
}

// POST /maint/course/${courseId}/quiz/form/${formId}/question ({String name, String description, String type, String[] options, boolean hasCorrectAnswers, String[] correctAnswers})
// addQuizQuestion(params.courseId, params.formId, quizQuestion?.name, quizQuestion?.description, quizQuestion?.type, quizQuestion?.options, quizQuestion?.hasCorrectAnswers, quizQuestion?.correctAnswers) -> Error | Name, Description, Type, Options, CorrectAnswers, HasCorrectAnswers
export async function addQuizQuestion(courseId: string, formId: string, quizQuestionName: string, quizQuestionDescription: string, quizQuestionType: string, quizQuestionOptions: string[], quizQuestionHasCorrectAnswers: boolean, quizQuestionCorrectAnswers: string[]): Promise<FeedbackQuestion | null> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizQuestionResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form/${formId}/question`, {
        method: "POST",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: quizQuestionName, description: quizQuestionDescription, type: quizQuestionType, options: quizQuestionOptions, hasCorrectAnswers: quizQuestionHasCorrectAnswers, correctAnswers: quizQuestionCorrectAnswers })
    });
    if (quizQuestionResponse.status !== 200) {
        toast.error(`Failed to add quiz question. Please try again. Status: ${quizQuestionResponse.status}`);
        return null;
    }
    let quizQuestion = await quizQuestionResponse.json();
    quizQuestion = { ...quizQuestion.questionContent, id: quizQuestion.id };

    return quizQuestion;
}

// DELETE /maint/course/${courseId}/quiz/form/${formId}/question/${questionId}
// deleteQuizQuestion(params.courseId, params.formId, params.questionId) -> Error | Success
export async function deleteQuizQuestion(courseId: string, formId: string, questionId: string): Promise<boolean> {
    const BACKEND_URL = await getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let quizQuestionResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}/quiz/form/${formId}/question/${questionId}`, {
        method: "DELETE",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    if (quizQuestionResponse.status !== 200) {
        toast.error(`Failed to delete quiz question. Please try again. Status: ${quizQuestionResponse.status}`);
        return false;
    }
    return true;
}

