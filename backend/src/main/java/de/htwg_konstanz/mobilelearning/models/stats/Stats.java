package de.htwg_konstanz.mobilelearning.models.stats;
public class Stats {
    
    public GlobalStats globalStats;
    public UserStats userStats;

    public Stats() {
    }

    public Stats(GlobalStats globalStats, UserStats userStats) {
        this.globalStats = globalStats;
        this.userStats = userStats;
    }
}
