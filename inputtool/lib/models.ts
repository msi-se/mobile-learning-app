export type FeedbackForm = {
    id: string;
    key: string;
    name: string;
    description: string;
    questions: FeedbackQuestion[];
    type: "Feedback";
}

export type FeedbackQuestion = {
    id: string;
    key: string;
    name: string;
    description: string;
    type: "SLIDER"| "STARS"| "SINGLE_CHOICE"| "FULLTEXT"| "YES_NO";
    options?: string[];
    rangeLow?: string;
    rangeHigh?: string;
}

export type QuizForm = {
    id: string;
    key: string;
    name: string;
    description: string;
    questions: QuizQuestion[];
    type: "Quiz";
}

export type QuizQuestion = {
    id: string;
    key: string;
    name: string;
    description: string;
    type: "YES_NO"| "SINGLE_CHOICE"| "MULTIPLE_CHOICE"| "WORD_CLOUD"| "FULLTEXT";
    options?: string[];
    hasCorrectAnswers: boolean;
    correctAnswers?: string[];
}

export type Course = {
    id: string;
    key: string;
    name: string;
    description: string;
    moodleCourseId: string;
    feedbackForms: FeedbackForm[];
    quizForms: QuizForm[];
}