/**
 * The Typist class is the class which included all the key features of the 
 * application. It will include methods like burnOut, recoverFromBurnout, 
 * getAccuracy, getProgress, getName, getSymbol, which will be used in the
 * TypingRace class to create the game.
 *
 * Starter code generously abandoned by Ty Posaurus, your predecessor,
 * who typed with two fingers and considered that "good enough".
 * He left a sticky note: "the slide-back thing is optional probably".
 * It is not optional. Good luck.
 *
 * @author Dhruv Patel
 * @version v1.0 (20/04/2026)
 */
public class Typist
{
    // Fields of class Typist

    private String name;
    private char symbol;
    private int progress;
    private boolean isBurntOut;
    private int burnoutTurnsRemaining;
    private double accuracy;




    // Constructor of class Typist
    /**
     * Constructor for objects of class Typist.
     * Creates a new typist with a given symbol, name, and accuracy rating.
     *
     * @param typistSymbol  a single Unicode character representing this typist (e.g. '①', '②', '③')
     * @param typistName    the name of the typist (e.g. "TURBOFINGERS")
     * @param typistAccuracy the typist's accuracy rating, between 0.0 and 1.0
     */
    public Typist(char typistSymbol, String typistName, double typistAccuracy)
    {
        this.name = typistName;
        this.symbol = typistSymbol;
        this.progress = 0;
        this.isBurntOut = false;
        this.burnoutTurnsRemaining = 0;
        setAccuracy(typistAccuracy);

    }


    // Methods of class Typist

    /**
     * Sets this typist into a burnout state for a given number of turns.
     * A burnt-out typist cannot type until their burnout has worn off.
     *
     * @param turns the number of turns the burnout will last
     */
    public void burnOut(int turns)
    {
        if (turns > 0)
        {
            isBurntOut = true;
            burnoutTurnsRemaining = turns;
        }
    }


    /**
     * Reduces the remaining burnout counter by one turn.
     * When the counter reaches zero, the typist recovers automatically.
     * Has no effect if the typist is not currently burnt out.
     */
    public void recoverFromBurnout()
    {
        if (isBurntOut)
        {
            burnoutTurnsRemaining--;

            if (burnoutTurnsRemaining <= 0)
            {
                burnoutTurnsRemaining = 0;
                isBurntOut = false;
            }
        }
    }

    /**
     * Returns the typist's accuracy rating.
     *
     * @return accuracy as a double between 0.0 and 1.0
     */
    public double getAccuracy()
    {
        return accuracy;
    }

    /**
     * Returns the typist's current progress through the passage.
     * Progress is measured in characters typed correctly so far.
     * Note: this value can decrease if the typist mistypes.
     *
     * @return progress as a non-negative integer
     */
    public int getProgress()
    {
        return progress;
    }

    /**
     * Returns the name of the typist.
     *
     * @return the typist's name as a String
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the character symbol used to represent this typist.
     *
     * @return the typist's symbol as a char
     */
    public char getSymbol()
    {
        return symbol;
    }

    /**
     * Returns the number of turns of burnout remaining.
     * Returns 0 if the typist is not currently burnt out.
     *
     * @return burnout turns remaining as a non-negative integer
     */
    public int getBurnoutTurnsRemaining()
    {
        return burnoutTurnsRemaining;
    }

    /**
     * Resets the typist to their initial state, ready for a new race.
     * Progress returns to zero, burnout is cleared entirely.
     */
    public void resetToStart()
    {
        progress = 0;
        isBurntOut = false;
        burnoutTurnsRemaining = 0;

    }

    /**
     * Returns true if this typist is currently burnt out, false otherwise.
     *
     * @return true if burnt out
     */
    public boolean isBurntOut()
    {
        return isBurntOut;
    }

    /**
     * Advances the typist forward by one character along the passage.
     * Should only be called when the typist is not burnt out.
     */
    public void typeCharacter()
    {
        if (!isBurntOut)
        {
            progress++;
        }
    }

    /**
     * Moves the typist backwards by a given number of characters (a mistype).
     * Progress cannot go below zero — the typist cannot slide off the start.
     *
     * @param amount the number of characters to slide back (must be positive)
     */
    public void slideBack(int amount)
    {
        if (amount > 0)
        {
            progress -= amount;
            if (progress < 0)
            {
                progress = 0;
            }
        }

    }

    /**
     * Sets the accuracy rating of the typist.
     * Values below 0.0 should be set to 0.0; values above 1.0 should be set to 1.0.
     *
     * @param newAccuracy the new accuracy rating
     */
    public void setAccuracy(double newAccuracy)
    {
        if (newAccuracy < 0.0)
        {
            accuracy = 0.0;
        }
        else if (newAccuracy > 1.0)
        {
            accuracy = 1.0;
        }
        else
        {
            accuracy = newAccuracy;
        }

    }

    /**
     * Sets the symbol used to represent this typist.
     *
     * @param newSymbol the new symbol character
     */
    public void setSymbol(char newSymbol)
    {

    }

}
