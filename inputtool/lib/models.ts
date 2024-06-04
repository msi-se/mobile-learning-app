export type FeedbackForm = {
    id: string;
    key: string;
    name: string;
    description: string;
    questions: FeedbackQuestion[];
    type: "Feedback";
    lastModified: Date;
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
    lastModified: Date;
}

export type QuizQuestion = {
    id: string;
    key: string;
    name: string;
    description: string;
    type: "YES_NO"| "SINGLE_CHOICE"| "MULTIPLE_CHOICE" | "FULLTEXT";
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
    lastModified: Date;
}

export type FormResult = {
    id: string;
    userId: string;
    values: string[];
    hashedUserId: string;
    gainedPoints: number;
}