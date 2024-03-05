package de.htwg_konstanz.mobilelearning.models.stats;

import org.bson.types.ObjectId;

public class GlobalStats {
    public ObjectId id;
    public Integer totalUsers = 0;
    public Integer totalCourses = 0;
    public Integer totalFeedbackForms = 0;
    public Integer totalQuizForms = 0;
    public Integer completedFeedbackForms = 0;
    public Integer completedQuizForms = 0;

    public GlobalStats() {
    }

    public GlobalStats(Integer totalUsers, Integer totalCourses, Integer totalFeedbackForms, Integer totalQuizForms, Integer completedFeedbackForms, Integer completedQuizForms) {
        this.id = new ObjectId();
        this.totalUsers = totalUsers;
        this.totalCourses = totalCourses;
        this.totalFeedbackForms = totalFeedbackForms;
        this.totalQuizForms = totalQuizForms;
        this.completedFeedbackForms = completedFeedbackForms;
        this.completedQuizForms = completedQuizForms;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Integer getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(Integer totalCourses) {
        this.totalCourses = totalCourses;
    }

    public Integer getTotalFeedbackForms() {
        return totalFeedbackForms;
    }

    public void setTotalFeedbackForms(Integer totalFeedbackForms) {
        this.totalFeedbackForms = totalFeedbackForms;
    }

    public Integer getTotalQuizForms() {
        return totalQuizForms;
    }

    public void setTotalQuizForms(Integer totalQuizForms) {
        this.totalQuizForms = totalQuizForms;
    }

    public Integer getCompletedFeedbackForms() {
        return completedFeedbackForms;
    }

    public void setCompletedFeedbackForms(Integer completedFeedbackForms) {
        this.completedFeedbackForms = completedFeedbackForms;
    }

    public Integer getCompletedQuizForms() {
        return completedQuizForms;
    }

    public void setCompletedQuizForms(Integer completedQuizForms) {
        this.completedQuizForms = completedQuizForms;
    }

    @Override
    public String toString() {
        return "GlobalStats{" +
                "totalUsers=" + totalUsers +
                ", totalCourses=" + totalCourses +
                ", totalFeedbackForms=" + totalFeedbackForms +
                ", totalQuizForms=" + totalQuizForms +
                ", completedFeedbackForms=" + completedFeedbackForms +
                ", completedQuizForms=" + completedQuizForms +
                '}';
    }
}
