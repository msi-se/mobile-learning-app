package de.htwg_konstanz.mobilelearning.models.stats;

public class UserStats {

    public Integer completedFeedbackForms = 0;
    public Integer completedQuizForms = 0;
    public Integer gainedQuizPoints = 0;
    public Double avgQuizPosition = 0.0;

    public UserStats() {
        this.completedFeedbackForms = 0;
        this.completedQuizForms = 0;
        this.gainedQuizPoints = 0;
        this.avgQuizPosition = 0.0;
    }

    public UserStats(Integer completedFeedbackForms, Integer completedQuizForms, Integer gainedQuizPoints, Double avgQuizPosition) {
        this.completedFeedbackForms = completedFeedbackForms;
        this.completedQuizForms = completedQuizForms;
        this.gainedQuizPoints = gainedQuizPoints;
        this.avgQuizPosition = avgQuizPosition;
    }

    public Integer getCompletedFeedbackForms() {
        return this.completedFeedbackForms;
    }

    public void setCompletedFeedbackForms(Integer completedFeedbackForms) {
        this.completedFeedbackForms = completedFeedbackForms;
    }

    public Number incrementCompletedFeedbackForms() {
        return this.completedFeedbackForms++;
    }

    public Integer getCompletedQuizForms() {
        return this.completedQuizForms;
    }

    public void setCompletedQuizForms(Integer completedQuizForms) {
        this.completedQuizForms = completedQuizForms;
    }

    public Number incrementCompletedQuizForms() {
        return this.completedQuizForms++;
    }

    public Integer getGainedQuizPoints() {
        return this.gainedQuizPoints;
    }

    public void setGainedQuizPoints(Integer gainedQuizPoints) {
        this.gainedQuizPoints = gainedQuizPoints;
    }

    public Number incrementGainedQuizPoints(Integer gainedQuizPoints) {
        return this.gainedQuizPoints += gainedQuizPoints;
    }

    public Double getAvgQuizPosition() {
        return this.avgQuizPosition;
    }

    public void setAvgQuizPosition(Double avgQuizPosition) {
        this.avgQuizPosition = avgQuizPosition;
    }

    public Number updateAvgQuizPosition(Integer quizPosition) {

        // special case: first quiz
        if (this.completedQuizForms == 0) {
            this.avgQuizPosition = (double) quizPosition;
            return this.avgQuizPosition;
        }

        this.avgQuizPosition = (this.avgQuizPosition * this.completedQuizForms + quizPosition) / (this.completedQuizForms + 1);
        return this.avgQuizPosition;
    }

    public void doneQuiz(Integer quizPosition, Integer score) {

        this.updateAvgQuizPosition(quizPosition);
        this.incrementCompletedQuizForms();
        this.incrementGainedQuizPoints(score);

    }
}
