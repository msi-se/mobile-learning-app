package de.htwg_konstanz.mobilelearning.repositories;

import de.htwg_konstanz.mobilelearning.models.stats.GlobalStats;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GlobalStatsRepository implements PanacheMongoRepository<GlobalStats> {
    public GlobalStats getGlobalStats() {
        return findAll().firstResult();
    }
    
    public void updateGlobalStats(GlobalStats globalStats) {
        GlobalStats currentGlobalStats = getGlobalStats();
        if (currentGlobalStats == null) {
            persist(globalStats);
        } else {
            currentGlobalStats.setTotalUsers(globalStats.getTotalUsers());
            currentGlobalStats.setTotalCourses(globalStats.getTotalCourses());
            currentGlobalStats.setTotalFeedbackForms(globalStats.getTotalFeedbackForms());
            currentGlobalStats.setTotalQuizForms(globalStats.getTotalQuizForms());
            currentGlobalStats.setCompletedFeedbackForms(globalStats.getCompletedFeedbackForms());
            currentGlobalStats.setCompletedQuizForms(globalStats.getCompletedQuizForms());
            update(currentGlobalStats);
        }
    }
}
