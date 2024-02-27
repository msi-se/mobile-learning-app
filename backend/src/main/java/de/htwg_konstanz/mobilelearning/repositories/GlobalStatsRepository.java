package de.htwg_konstanz.mobilelearning.repositories;

import de.htwg_konstanz.mobilelearning.models.stats.GlobalStats;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GlobalStatsRepository implements PanacheMongoRepository<GlobalStats> {
    public GlobalStats getGlobalStats() {

        GlobalStats globalStats = findAll().firstResult();

        if (globalStats == null) {
            globalStats = new GlobalStats();
            persist(globalStats);
        }

        return globalStats;
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

    public void resetGlobalStats() {
        GlobalStats globalStats = getGlobalStats();
        globalStats.setTotalUsers(0);
        globalStats.setTotalCourses(0);
        globalStats.setTotalFeedbackForms(0);
        globalStats.setTotalQuizForms(0);
        globalStats.setCompletedFeedbackForms(0);
        globalStats.setCompletedQuizForms(0);
        update(globalStats);
    }
}
