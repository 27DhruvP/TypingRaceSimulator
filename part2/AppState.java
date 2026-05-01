package part2;

import java.util.*;

/**
 * Application-wide persistent state: history, leaderboard, badges, earnings.
 * Single instance passed between screens.
 */
public class AppState {

    // History
    private final List<RaceRecord> allRecords = new ArrayList<>();
    // per-name lists
    private final Map<String, List<RaceRecord>> historyByName = new LinkedHashMap<>();

    //Leaderboard (cumulative points)
    private final Map<String, Integer> cumulativePoints   = new LinkedHashMap<>();
    private final Map<String, Integer> consecutiveWins    = new LinkedHashMap<>();
    private final Map<String, Integer> racesWithoutBurnout= new LinkedHashMap<>();
    private final Map<String, Integer> totalRaces         = new LinkedHashMap<>();
    private final Map<String, Double>  personalBestWpm    = new LinkedHashMap<>();

    // Badges earned per typist
    private final Map<String, Set<String>> badges = new LinkedHashMap<>();

    //  Sponsor earnings
    private final Map<String, Integer> earnings  = new LinkedHashMap<>();
    private final Map<String, Integer> upgrades  = new LinkedHashMap<>();  // 0=none,1=keyboard,2=wrist

    // Singleton
    private static AppState instance;
    public  static AppState get() { if (instance == null) instance = new AppState(); return instance; }
    private AppState() {}

    // Ingestion
    public void recordRace(List<RaceRecord> records, List<TypistConfig> configs) {
        allRecords.addAll(records);

        Map<String, TypistConfig> cfgMap = new LinkedHashMap<>();
        for (TypistConfig c : configs) cfgMap.put(c.name, c);

        for (RaceRecord r : records) {
            historyByName.computeIfAbsent(r.typistName, k -> new ArrayList<>()).add(r);
            totalRaces.merge(r.typistName, 1, Integer::sum);

            // Personal best
            personalBestWpm.merge(r.typistName, r.wpm, Math::max);

            // Base points
            int pts = r.pointsEarned();

            // Sponsor bonus
            TypistConfig cfg = cfgMap.get(r.typistName);
            if (cfg != null && cfg.sponsor != TypistConfig.Sponsor.NONE) {
                pts += evaluateSponsor(r, cfg.sponsor, records);
            }

            cumulativePoints.merge(r.typistName, pts, Integer::sum);
            earnings.merge(r.typistName, pts * 10, Integer::sum);  // 10 coins per point

            // Consecutive wins
            if (r.finishPosition == 1) {
                consecutiveWins.merge(r.typistName, 1, Integer::sum);
            } else {
                consecutiveWins.put(r.typistName, 0);
            }

            // Races without burnout
            if (r.burnoutCount == 0) {
                racesWithoutBurnout.merge(r.typistName, 1, Integer::sum);
            } else {
                racesWithoutBurnout.put(r.typistName, 0);
            }

            awardBadges(r.typistName);
        }
    }

    private int evaluateSponsor(RaceRecord r, TypistConfig.Sponsor s, List<RaceRecord> allInRace) {
        return switch (s) {
            case KEYCORP      -> (r.burnoutCount == 0)              ? s.bonusPoints : 0;
            case SPEEDKEYS    -> (r.finishPosition <= 2)            ? s.bonusPoints : 0;
            case ACCURATETYPE -> (r.accuracyPercent > 80)           ? s.bonusPoints : 0;
            case IRONDESK     -> (r.burnoutCount == 0 && r.finishPosition == 1) ? s.bonusPoints : 0;
            default           -> 0;
        };
    }

    private void awardBadges(String name) {
        Set<String> b = badges.computeIfAbsent(name, k -> new LinkedHashSet<>());
        if (consecutiveWins.getOrDefault(name, 0) >= 3)         b.add("Speed Demon");
        if (racesWithoutBurnout.getOrDefault(name, 0) >= 5)     b.add("Iron Fingers");
        if (personalBestWpm.getOrDefault(name, 0.0) > 60)       b.add("Flash Typist");
        if (totalRaces.getOrDefault(name, 0) >= 10)              b.add("Veteran");
        if (cumulativePoints.getOrDefault(name, 0) >= 100)       b.add("Century Club");
    }

    // Queries
    public List<RaceRecord>  getHistory(String name) {
        return historyByName.getOrDefault(name, Collections.emptyList());
    }
    public List<RaceRecord>  getAllRecords() { return Collections.unmodifiableList(allRecords); }
    public int    getPoints(String name)    { return cumulativePoints.getOrDefault(name, 0); }
    public double getBestWpm(String name)   { return personalBestWpm.getOrDefault(name, 0.0); }
    public Set<String> getBadges(String name){ return badges.getOrDefault(name, Collections.emptySet()); }
    public int    getEarnings(String name)  { return earnings.getOrDefault(name, 0); }

    /** Sorted leaderboard entries: (name, points) highest first */
    public List<Map.Entry<String,Integer>> getLeaderboard() {
        List<Map.Entry<String,Integer>> entries = new ArrayList<>(cumulativePoints.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        return entries;
    }

    public void purchaseUpgrade(String name, int level) {
        int cost = level * 50;
        int current = earnings.getOrDefault(name, 0);
        if (current >= cost) {
            earnings.put(name, current - cost);
            upgrades.put(name, level);
        }
    }
    public int getUpgradeLevel(String name) { return upgrades.getOrDefault(name, 0); }
}
