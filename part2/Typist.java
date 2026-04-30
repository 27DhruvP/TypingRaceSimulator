package part2;
public class Typist {
    private String name;
    private char symbol;
    private int progress;
    private boolean isBurntOut;
    private int burnoutTurnsRemaining;
    private double accuracy;
    private boolean justMistyped;
    private double initialAccuracy;

    public Typist(char typistSymbol, String typistName, double typistAccuracy) {
        this.name = typistName;
        this.symbol = typistSymbol;
        this.progress = 0;
        this.isBurntOut = false;
        this.burnoutTurnsRemaining = 0;
        setAccuracy(typistAccuracy);
        this.justMistyped = false;
        this.initialAccuracy = typistAccuracy;
    }

    public void burnOut(int turns) {
        if (turns > 0) { isBurntOut = true; burnoutTurnsRemaining = turns; }
    }

    public void recoverFromBurnout() {
        if (isBurntOut) {
            burnoutTurnsRemaining--;
            if (burnoutTurnsRemaining <= 0) { burnoutTurnsRemaining = 0; isBurntOut = false; }
        }
    }

    public double getAccuracy()            { return accuracy; }
    public double getInitialAccuracy()     { return initialAccuracy; }
    public int    getProgress()            { return progress; }
    public String getName()                { return name; }
    public char   getSymbol()              { return symbol; }
    public int    getBurnoutTurnsRemaining(){ return burnoutTurnsRemaining; }
    public boolean isBurntOut()            { return isBurntOut; }
    public boolean justMistyped()          { return justMistyped; }

    public void resetToStart() {
        progress = 0; isBurntOut = false; burnoutTurnsRemaining = 0; justMistyped = false;
    }

    public void typeCharacter() { if (!isBurntOut) progress++; }

    public void clearMistype() { justMistyped = false; }

    public void slideBack(int amount) {
        if (amount > 0) { progress -= amount; if (progress < 0) progress = 0; }
        justMistyped = true;
    }

    public void setAccuracy(double newAccuracy) {
        if (newAccuracy < 0.0) accuracy = 0.0;
        else if (newAccuracy > 1.0) accuracy = 1.0;
        else accuracy = newAccuracy;
    }

    public void setSymbol(char newSymbol) { symbol = newSymbol; }
    public void setName(String n)         { name = n; }
}
