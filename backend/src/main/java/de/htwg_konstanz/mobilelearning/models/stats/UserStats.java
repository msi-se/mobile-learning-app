package de.htwg_konstanz.mobilelearning.models.stats;

public class UserStats {

    public Integer completedFeedbackForms = 0;
    public Integer completedQuizForms = 0;
    public Integer qainedQuizPoints = 0;
    public Double avgQuizPosition = 0.0;

    public UserStats() {
        this.completedFeedbackForms = 0;
        this.completedQuizForms = 0;
        this.qainedQuizPoints = 0;
        this.avgQuizPosition = 0.0;
    }

    public UserStats(Integer completedFeedbackForms, Integer completedQuizForms, Integer qainedQuizPoints, Double avgQuizPosition) {
        this.completedFeedbackForms = completedFeedbackForms;
        this.completedQuizForms = completedQuizForms;
        this.qainedQuizPoints = qainedQuizPoints;
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

    public Integer getQainedQuizPoints() {
        return this.qainedQuizPoints;
    }

    public void setQainedQuizPoints(Integer qainedQuizPoints) {
        this.qainedQuizPoints = qainedQuizPoints;
    }

    public Number incrementQainedQuizPoints(Integer qainedQuizPoints) {
        return this.qainedQuizPoints += qainedQuizPoints;
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
        this.incrementQainedQuizPoints(score);

    }
}
