package de.htwg_konstanz.mobilelearning.services;

import org.eclipse.microprofile.jwt.JsonWebToken;

import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.stats.GlobalStats;
import de.htwg_konstanz.mobilelearning.models.stats.Stats;
import de.htwg_konstanz.mobilelearning.repositories.GlobalStatsRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/stats")
public class StatsService {

    @Inject
    GlobalStatsRepository globalStatsRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public Stats getStats() {

        // get the user
        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }

        // get the stats
        GlobalStats globalStats = globalStatsRepository.getGlobalStats();
        return new Stats(globalStats, user.getStats());
    }

    public Integer incrementTotalUsers() {
        GlobalStats globalStats = globalStatsRepository.getGlobalStats();
        Integer totalUsers = globalStats.getTotalUsers() + 1;
        globalStats.setTotalUsers(totalUsers);
        globalStatsRepository.updateGlobalStats(globalStats);
        return totalUsers;
    }

    public Integer incrementTotalCourses() {
        GlobalStats globalStats = globalStatsRepository.getGlobalStats();
        Integer totalCourses = globalStats.getTotalCourses() + 1;
        globalStats.setTotalCourses(totalCourses);
        globalStatsRepository.updateGlobalStats(globalStats);
        return totalCourses;
    }

    public Integer incrementTotalFeedbackForms() {
        GlobalStats globalStats = globalStatsRepository.getGlobalStats();
        Integer totalFeedbackForms = globalStats.getTotalFeedbackForms() + 1;
        globalStats.setTotalFeedbackForms(totalFeedbackForms);
        globalStatsRepository.updateGlobalStats(globalStats);
        return totalFeedbackForms;
    }

    public Integer incrementTotalQuizForms() {
        GlobalStats globalStats = globalStatsRepository.getGlobalStats();
        Integer totalQuizForms = globalStats.getTotalQuizForms() + 1;
        globalStats.setTotalQuizForms(totalQuizForms);
        globalStatsRepository.updateGlobalStats(globalStats);
        return totalQuizForms;
    }

    public Integer incrementCompletedFeedbackForms() {
        GlobalStats globalStats = globalStatsRepository.getGlobalStats();
        Integer completedFeedbackForms = globalStats.getCompletedFeedbackForms() + 1;
        globalStats.setCompletedFeedbackForms(completedFeedbackForms);
        globalStatsRepository.updateGlobalStats(globalStats);
        return completedFeedbackForms;
    }

    public Integer incrementCompletedQuizForms() {
        GlobalStats globalStats = globalStatsRepository.getGlobalStats();
        Integer completedQuizForms = globalStats.getCompletedQuizForms() + 1;
        globalStats.setCompletedQuizForms(completedQuizForms);
        globalStatsRepository.updateGlobalStats(globalStats);
        return completedQuizForms;
    }

    public GlobalStats getGlobalStats() {
        return globalStatsRepository.getGlobalStats();
    }

    public void resetGlobalStats() {
        globalStatsRepository.resetGlobalStats();
    }
}
