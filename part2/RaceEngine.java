package part2;

import java.util.*;

/**
 * Drives the typing-race simulation and exposes hooks the GUI can poll.
 * Each call to tick() advances all typists by one turn and fires listeners.
 */
public class RaceEngine {

    // Constants (mirrors TypingRace) 
    private static final double BASE_MISTYPE_CHANCE = 0.05;
    private static final int    SLIDE_BACK_AMOUNT   = 2;
    private static final int    BURNOUT_DURATION    = 8;

    // Passage & config 
    private final String   passage;
    private final int      passageLength;

    // Difficulty flags
    private final boolean autocorrectOn;
    private final boolean caffeineMode;
    private final boolean nightShift;

    // Typist state 
    private final List<Typist>       typists     = new ArrayList<>();
    private final List<TypistConfig> configs     = new ArrayList<>();
    private final List<Color2>       colors      = new ArrayList<>();   // lightweight record

    // Per-typist tracking
    private final Map<Typist, Integer> burnoutCounts    = new LinkedHashMap<>();
    private final Map<Typist, Integer> mistypeLastTen   = new LinkedHashMap<>();
    private final Map<Typist, Long>    finishTimestamp  = new LinkedHashMap<>();
    private final Map<Typist, Integer> finishTurn       = new LinkedHashMap<>();
    private final Map<Typist, Double>  accuracyAtStart  = new LinkedHashMap<>();

    // Race state 
    private int  turn        = 0;
    private boolean finished = false;
    private Typist  winner   = null;

    private long raceStartMs = 0;

    // Listeners
    public interface TickListener  { void onTick(int turn); }
    public interface FinishListener{ void onFinish(Typist winner, List<RaceRecord> records); }

    private final List<TickListener>   tickListeners   = new ArrayList<>();
    private final List<FinishListener> finishListeners = new ArrayList<>();

    // small record to hold colour alongside a typist
    public record Color2(java.awt.Color color) {}

    // Constructor 
    public RaceEngine(String passage,
                      boolean autocorrectOn,
                      boolean caffeineMode,
                      boolean nightShift) {
        this.passage       = passage;
        this.passageLength = passage.length();
        this.autocorrectOn = autocorrectOn;
        this.caffeineMode  = caffeineMode;
        this.nightShift    = nightShift;
    }

    // Setup 
    public void addTypist(TypistConfig cfg) {
        double acc = cfg.computeBaseAccuracy();
        if (nightShift) acc = Math.max(0, acc - 0.08);

        String sym = cfg.symbol.isEmpty() ? "?" : cfg.symbol;
        char   ch  = sym.codePointAt(0) > 0xFFFF
                     ? sym.charAt(0)   // surrogate pair – just use first char
                     : sym.charAt(0);

        Typist t = new Typist(ch, cfg.name, acc);
        typists.add(t);
        configs.add(cfg.copy());
        colors.add(new Color2(cfg.color));

        burnoutCounts.put(t, 0);
        mistypeLastTen.put(t, 0);
        accuracyAtStart.put(t, acc);
    }

    public void addTickListener  (TickListener l)   { tickListeners.add(l); }
    public void addFinishListener(FinishListener l) { finishListeners.add(l); }

    // Accessors for GUI 
    public List<Typist>       getTypists() { return Collections.unmodifiableList(typists); }
    public List<TypistConfig> getConfigs() { return Collections.unmodifiableList(configs); }
    public List<Color2>       getColors()  { return Collections.unmodifiableList(colors); }
    public String  getPassage()            { return passage; }
    public int     getPassageLength()      { return passageLength; }
    public int     getTurn()               { return turn; }
    public boolean isFinished()            { return finished; }
    public Typist  getWinner()             { return winner; }
    public int     getBurnoutCount(Typist t){ return burnoutCounts.getOrDefault(t, 0); }

    public java.awt.Color getColor(Typist t) {
        int idx = typists.indexOf(t);
        return idx >= 0 ? colors.get(idx).color() : java.awt.Color.WHITE;
    }

    // Main simulation step 
    public void start() {
        for (Typist t : typists) t.resetToStart();
        raceStartMs = System.currentTimeMillis();
        turn = 0;
        finished = false;
        winner   = null;
    }

    /**
     * Advances all typists one turn.  Call this from a Swing Timer.
     */
    public void tick() {
        if (finished) return;
        turn++;

        for (int i = 0; i < typists.size(); i++) {
            Typist t   = typists.get(i);
            TypistConfig cfg = configs.get(i);
            advanceTypist(t, cfg);
        }

        // Check for finish
        Typist firstDone = null;
        for (Typist t : typists) {
            if (t.getProgress() >= passageLength && !finishTurn.containsKey(t)) {
                finishTurn.put(t, turn);
                finishTimestamp.put(t, System.currentTimeMillis());
                if (firstDone == null) firstDone = t;
            }
        }
        if (firstDone != null && winner == null) {
            winner   = firstDone;
            finished = true;
            // small accuracy reward
            winner.setAccuracy(Math.min(1.0, winner.getAccuracy() + 0.01));
        }

        tickListeners.forEach(l -> l.onTick(turn));

        if (finished) {
            List<RaceRecord> records = buildRecords();
            finishListeners.forEach(l -> l.onFinish(winner, records));
        }
    }

    // Per-typist logic 
    private void advanceTypist(Typist t, TypistConfig cfg) {
        t.clearMistype();

        if (t.isBurntOut()) {
            t.recoverFromBurnout();
            return;
        }
        if (t.getProgress() >= passageLength) return;

        // Caffeine: first 10 turns → +0.10 acc, then +0.05 burnout risk
        double accBoost = 0;
        double burnoutExtra = 0;
        if (caffeineMode) {
            if (turn <= 10) accBoost = 0.10;
            else            burnoutExtra = 0.05;
        }

        // Energy drink: first half → +0.10, second half → -0.10
        if (cfg.energyDrink) {
            double halfway = passageLength / 2.0;
            if (t.getProgress() < halfway) accBoost += 0.10;
            else                           accBoost -= 0.10;
        }

        double effectiveAcc = Math.min(1.0, Math.max(0, t.getAccuracy() + accBoost));

        // Type or mistype
        if (Math.random() < effectiveAcc) {
            t.typeCharacter();
            t.setAccuracy(Math.min(1.0, t.getAccuracy() + 0.001)); // gradual improvement
        } else {
            double mistypeThresh = (1 - effectiveAcc)
                                   * (BASE_MISTYPE_CHANCE + cfg.computeMistypeMod());
            if (Math.random() < mistypeThresh) {
                int slideAmt = autocorrectOn
                    ? Math.max(1, SLIDE_BACK_AMOUNT / 2)
                    : SLIDE_BACK_AMOUNT;
                t.slideBack(slideAmt);
                mistypeLastTen.merge(t, 1, Integer::sum);
            }
        }

        if (t.getProgress() >= passageLength) return;

        // Burnout check
        double burnChance = 0.05 * t.getAccuracy() * t.getAccuracy()
                          + cfg.style.burnoutChanceMod
                          + burnoutExtra;
        if (Math.random() < burnChance) {
            int dur = BURNOUT_DURATION + cfg.computeBurnoutDurationMod();
            t.burnOut(Math.max(1, dur));
            burnoutCounts.merge(t, 1, Integer::sum);
        }
    }

    // Record building 
    private List<RaceRecord> buildRecords() {
        // Sort by finish turn (lower = better), then by progress descending
        List<Typist> sorted = new ArrayList<>(typists);
        sorted.sort((a, b) -> {
            Integer fa = finishTurn.get(a);
            Integer fb = finishTurn.get(b);
            if (fa != null && fb != null) return fa - fb;
            if (fa != null) return -1;
            if (fb != null) return  1;
            return b.getProgress() - a.getProgress();
        });

        long now = System.currentTimeMillis();
        List<RaceRecord> records = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Typist t      = sorted.get(i);
            int position  = i + 1;
            long elapsed  = (finishTimestamp.getOrDefault(t, now)) - raceStartMs;
            elapsed       = Math.max(1, elapsed);

            // WPM = (passageLength / 5) / (elapsed / 60000)
            double words  = passageLength / 5.0;
            double mins   = elapsed / 60_000.0;
            double wpm    = words / mins;

            // Accuracy% = successful chars / total chars attempted
            // Approximate: progress / (progress + mistypes*slideBack)
            int bo = burnoutCounts.getOrDefault(t, 0);
            double accPct = accuracyAtStart.getOrDefault(t, t.getAccuracy()) * 100.0;

            records.add(new RaceRecord(
                t.getName(), position, wpm, accPct, bo,
                accuracyAtStart.getOrDefault(t, t.getAccuracy()),
                t.getAccuracy(),
                passageLength, elapsed
            ));
        }
        return records;
    }
}
