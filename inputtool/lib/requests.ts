import { Course, FeedbackForm, FeedbackQuestion } from "./models";
import { toast } from "sonner";
import { getBackendUrl } from "./utils";

// GET /maint/courses
// listCourses(); -> Name, Description
export async function listCourses(): Promise<Course[]> {
    const BACKEND_URL = getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let courseResponse = await fetch(`${BACKEND_URL}/maint/courses`, {
        method: "GET",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    let courses = await courseResponse.json();
    if (courseResponse.status !== 200) {
        toast.error(`Failed to get courses. Please try again. Status: ${courseResponse.status}`);
        return [];
    }
    return courses;
}

// GET /maint/course/${courseId}
// getCourse(params.courseId); -> Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
export async function fetchCourse(courseId: string): Promise<Course | null> {
    const BACKEND_URL = getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let courseResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}`, {
        method: "GET",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken }
    });
    let course = await courseResponse.json();
    if (courseResponse.status !== 200) {
        toast.error(`Failed to get course. Please try again. Status: ${courseResponse.status}`);
        return null;
    }
    return course;
}

// PUT /maint/course/${courseId} ({String name, String description, String moodleCourseId})
// updateCourse(params.courseId, courseName, courseDescription, courseMoodleCourseId); -> Error | Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
export async function updateCourse(courseId: string, courseName: string, courseDescription: string, courseMoodleCourseId: string): Promise<Course | null> {
    const BACKEND_URL = getBackendUrl();
    const jwtToken = localStorage.getItem("jwtToken");
    let courseResponse = await fetch(`${BACKEND_URL}/maint/course/${courseId}`, {
        method: "PUT",
        headers: { "AUTHORIZATION": "Bearer " + jwtToken, "Content-Type": "application/json" },
        body: JSON.stringify({ name: courseName, description: courseDescription, moodleCourseId: courseMoodleCourseId })
    });
    let course = await courseResponse.json();
    if (courseResponse.status !== 200) {
        toast.error(`Failed to update course. Please try again. Status: ${courseResponse.status}`);
        return null;
    }
    return course;
}

// POST /maint/course ({String name, String description, String moodleCourseId})
// addCourse(courseName, courseDescription); -> Error | Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)

// DELETE /maint/course/${courseId}
// deleteCourse(params.courseId); -> Error | Success

// GET /maint/course/${courseId}/feedback/form/${formId}
// getFeedbackForm(params.courseId, params.formId); -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)

// PUT /maint/course/${courseId}/feedback/form/${formId} ({String name, String description})
// updateFeedbackForm(params.courseId, params.formId, feedbackformName, feedbackformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)

// POST /maint/course/${courseId}/feedback/form ({String name, String description})
// addFeedbackForm(params.courseId, feedbackformName, feedbackformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)

// DELETE /maint/course/${courseId}/feedback/form/${formId}
// deleteFeedbackForm(params.courseId, params.formId) -> Error | Success

// GET /maint/course/${courseId}/feedback/form/${formId}/question/${questionId}
// getFeedbackQuestion(params.courseId, params.formId, params.questionId); -> Error | Name, Description, Type, Options, RangeLow, RangeHigh

// PUT /maint/course/${courseId}/feedback/form/${formId}/question/${questionId} ({String name, String description, String type, String[] options, int rangeLow, int rangeHigh})
// updateFeedbackQuestion(params.courseId, params.formId, params.questionId, feedbackQuestion?.name, feedbackQuestion?.description, feedbackQuestion?.type, feedbackQuestion?.options, feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name, Description, Type, Options, RangeLow, RangeHigh

// POST /maint/course/${courseId}/feedback/form/${formId}/question ({String name, String description, String type, String[] options, int rangeLow, int rangeHigh})
// addFeedbackQuestion(params.courseId, params.formId, feedbackQuestion?.name, feedbackQuestion?.description, feedbackQuestion?.type, feedbackQuestion?.options, feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name, Description, Type, Options, RangeLow, RangeHigh

// DELETE /maint/course/${courseId}/feedback/form/${formId}/question/${questionId}
// deleteFeedbackQuestion(params.courseId, params.formId, params.questionId) -> Error | Success
