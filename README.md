# TypingRaceSimulator

Object Oriented Programming Project — ECS414U

## Project Structure

```
TypingRaceSimulator/
├── Part1/    # Textual simulation (Java, command-line)
└── Part2/    # GUI simulation (to be completed)
```

## Part 1 — Textual Simulation

### How to compile

```bash
cd Part1
javac Typist.java TypingRace.java
```

### How to run

The race is started by calling `startRace()` on a `TypingRace` object.
A simple way to test this is to add a `main` method to `TypingRace`, for example:

```java
public static void main(String[] args) {
    TypingRace race = new TypingRace(40);
    race.addTypist(new Typist('①', "TURBOFINGERS", 0.85), 1);
    race.addTypist(new Typist('②', "QWERTY_QUEEN",  0.60), 2);
    race.addTypist(new Typist('③', "HUNT_N_PECK",   0.30), 3);
    race.startRace();
}
```

Then run:

```bash
java TypingRace
```



## Part 2 — GUI Simulation

A graphical typing race simulator built with Java Swing. Features a 5-screen
interface with live race animation, per-typist customisation, a sponsor system,
badges, and a persistent leaderboard with an upgrade shop.

### How to compile

```bash
cd Part2
javac part2/*.java
```

### How to run

The graphical version is started by calling `startRaceGUI()` on the `Main` class:

```java
public static void main(String[] args) {
    Main.startRaceGUI();
}
```

Then run:

```bash
java part2.Main
```




### Difficulty Modifiers

- Autocorrect On: slide-back on mistype is halved
- Caffeine Mode: accuracy +10% for first 10 turns, then burnout risk increases
- Night Shift: all typists start with accuracy −8%

### Typist Options

- Touch Typist: 85% base accuracy, higher burnout chance
- Hunt and Peck: 55% base accuracy, low burnout chance
- Phone Thumbs: 70% base accuracy
- Voice-to-Text: 60% base accuracy, lowest burnout chance
- Mechanical keyboard: low mistype modifier, full speed
- Membrane keyboard: moderate mistype modifier, slight speed penalty
- Touchscreen keyboard: high mistype modifier, reduced speed
- Stenography keyboard: lowest mistype modifier, speed bonus
- Wrist Support: burnout duration reduced by 2 turns
- Energy Drink: accuracy +10% first half, −10% second half
- Noise-Cancel Headphones: mistype chance −2%

### Sponsors

- KeyCorp: finish without any burnout — +50 points
- SpeedKeys: finish in top 2 — +30 points
- AccurateType: finish with accuracy above 80% — +40 points
- IronDesk: no burnout and finish 1st — +35 points

### Badges

- Speed Demon: 3 consecutive race wins
- Iron Fingers: 5 races without burnout
- Flash Typist: personal best WPM above 60
- Veteran: 10 total races completed
- Century Club: 100 cumulative points earned

## Dependencies

- Java Development Kit (JDK) 11 or higher
- No external libraries required for Part 1
- Part 2 may use Java Swing (included in standard JDK) or JavaFX

## Notes

- All code should compile and run using standard command-line tools without any IDE-specific configuration.
- The starter code in Part1 was originally written by Ty Posaurus. It contains known issues — finding and fixing them is part of the coursework.