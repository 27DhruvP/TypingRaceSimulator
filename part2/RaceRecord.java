package part2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * Immutable record of a single typist's performance in one race.
 */
public class RaceRecord {
    public final String typistName;
    public final int    finishPosition;   // 1-based; 0 = DNF
    public final double wpm;
    public final double accuracyPercent;  // 0-100
    public final int    burnoutCount;
    public final double accuracyBefore;
    public final double accuracyAfter;
    public final int    passageLength;
    public final long   raceMillis;
    public final String timestamp;

    public RaceRecord(String typistName, int finishPosition, double wpm,
                      double accuracyPercent, int burnoutCount,
                      double accuracyBefore, double accuracyAfter,
                      int passageLength, long raceMillis) {
        this.typistName      = typistName;
        this.finishPosition  = finishPosition;
        this.wpm             = wpm;
        this.accuracyPercent = accuracyPercent;
        this.burnoutCount    = burnoutCount;
        this.accuracyBefore  = accuracyBefore;
        this.accuracyAfter   = accuracyAfter;
        this.passageLength   = passageLength;
        this.raceMillis      = raceMillis;
        this.timestamp       = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd MMM HH:mm"));
    }

    /** Points awarded: position bonus + WPM bonus - burnout penalty */
    public int pointsEarned() {
        int pos = switch (finishPosition) {
            case 1 -> 10;
            case 2 -> 6;
            case 3 -> 3;
            default -> 1;
        };
        int wpmBonus  = (int)(wpm / 10.0);
        int burnPen   = burnoutCount * 2;
        return Math.max(0, pos + wpmBonus - burnPen);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s  P%d  %.1f WPM  %.0f%% acc  %d BO",
            timestamp, typistName, finishPosition, wpm, accuracyPercent, burnoutCount);
    }
}
