package part2;

import java.awt.Color;

/**
 * Mutable configuration for one typist seat, capturing all GUI choices
 * before the race begins.  The engine converts this into a live Typist.
 */
public class TypistConfig {

    // ── Identity ──────────────────────────────────────────────────────────────
    public String name   = "Typist";
    public String symbol = "①";
    public Color  color  = new Color(0x4A90D9);

    // ── Style (affects base accuracy & burnout) ───────────────────────────────
    public enum Style {
        TOUCH_TYPIST   ("Touch Typist",    0.85, 0.05),
        HUNT_N_PECK    ("Hunt & Peck",     0.55, 0.02),
        PHONE_THUMBS   ("Phone Thumbs",    0.70, 0.03),
        VOICE_TO_TEXT  ("Voice-to-Text",   0.60, 0.01);

        public final String label;
        public final double baseAccuracy;
        public final double burnoutChanceMod;  // added to engine's burnout calc
        Style(String l, double a, double b) { label=l; baseAccuracy=a; burnoutChanceMod=b; }
        @Override public String toString() { return label; }
    }

    // ── Keyboard (affects mistype chance & speed) ─────────────────────────────
    public enum Keyboard {
        MECHANICAL    ("Mechanical",    0.03,  1.0),
        MEMBRANE      ("Membrane",      0.05,  0.95),
        TOUCHSCREEN   ("Touchscreen",   0.08,  0.85),
        STENOGRAPHY   ("Stenography",   0.02,  1.10);

        public final String label;
        public final double mistypeMod;   // added to base mistype chance
        public final double speedMul;     // multiplies typeCharacter probability
        Keyboard(String l, double m, double s) { label=l; mistypeMod=m; speedMul=s; }
        @Override public String toString() { return label; }
    }

    // ── Accessories ───────────────────────────────────────────────────────────
    public boolean wristSupport        = false;  // burnout duration -2
    public boolean energyDrink         = false;  // acc +0.10 first half, -0.10 second
    public boolean noiseCancelHeadphones = false; // mistype chance -0.02

    // ── Sponsor ───────────────────────────────────────────────────────────────
    public enum Sponsor {
        NONE        ("No Sponsor",   "",                         0),
        KEYCORP     ("KeyCorp",      "Finish without burnout → +50 pts",     50),
        SPEEDKEYS   ("SpeedKeys",    "Finish top 2 → +30 pts",               30),
        ACCURATETYPE("AccurateType", "Accuracy > 80% → +40 pts",             40),
        IRONDESK    ("IronDesk",     "No mistypes in last 10 turns → +35 pts",35);

        public final String label;
        public final String condition;
        public final int    bonusPoints;
        Sponsor(String l, String c, int b) { label=l; condition=c; bonusPoints=b; }
        @Override public String toString() { return label; }
    }

    // ── Selected values ───────────────────────────────────────────────────────
    public Style    style    = Style.TOUCH_TYPIST;
    public Keyboard keyboard = Keyboard.MECHANICAL;
    public Sponsor  sponsor  = Sponsor.NONE;

    /** Compute final base accuracy from style + keyboard speed multiplier */
    public double computeBaseAccuracy() {
        return Math.min(1.0, style.baseAccuracy * keyboard.speedMul);
    }

    /** Compute effective mistype chance modifier */
    public double computeMistypeMod() {
        double mod = keyboard.mistypeMod;
        if (noiseCancelHeadphones) mod -= 0.02;
        return Math.max(0, mod);
    }

    /** Compute burnout duration modifier */
    public int computeBurnoutDurationMod() {
        return wristSupport ? -2 : 0;
    }

    /** Deep copy */
    public TypistConfig copy() {
        TypistConfig c = new TypistConfig();
        c.name    = name; c.symbol = symbol; c.color = color;
        c.style   = style; c.keyboard = keyboard; c.sponsor = sponsor;
        c.wristSupport = wristSupport;
        c.energyDrink  = energyDrink;
        c.noiseCancelHeadphones = noiseCancelHeadphones;
        return c;
    }
}
