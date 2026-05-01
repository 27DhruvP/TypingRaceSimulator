package part2;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Main {

    private static final String CARD_CONFIG      = "CONFIG";
    private static final String CARD_CUSTOMISE   = "CUSTOMISE";
    private static final String CARD_RACE        = "RACE";
    private static final String CARD_STATS       = "STATS";
    private static final String CARD_LEADERBOARD = "LEADERBOARD";

    private final JFrame      frame  = new JFrame("⌨  Typing Race Simulator");
    private final CardLayout  cards  = new CardLayout();
    private final JPanel      deck   = new JPanel(cards);

    private ConfigPanel      configPanel;
    private CustomisePanel   customisePanel;
    private RacePanel        racePanel;
    private StatsPanel       statsPanel;
    private LeaderboardPanel leaderboardPanel;

    private List<TypistConfig> lastConfigs;

    private Main() {
        configPanel      = new ConfigPanel();
        statsPanel       = new StatsPanel();
        leaderboardPanel = new LeaderboardPanel();

        deck.setBackground(UITheme.BG_DARK);
        deck.add(configPanel,      CARD_CONFIG);
        deck.add(statsPanel,       CARD_STATS);
        deck.add(leaderboardPanel, CARD_LEADERBOARD);

        wireConfig();
        wireStats();
        wireLeaderboard();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(deck);
        frame.setSize(900, 680);
        frame.setMinimumSize(new Dimension(760, 560));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        cards.show(deck, CARD_CONFIG);
    }

    private void wireConfig() {
        configPanel.setOnNext(() -> {
            int seats = configPanel.getSeatCount();
            if (customisePanel != null) deck.remove(customisePanel);
            customisePanel = new CustomisePanel(seats);
            deck.add(customisePanel, CARD_CUSTOMISE);
            wireCustomise();
            cards.show(deck, CARD_CUSTOMISE);
        });
    }

    private void wireCustomise() {
        customisePanel.setOnBack(() -> cards.show(deck, CARD_CONFIG));

        customisePanel.setOnNext(() -> {
            lastConfigs = customisePanel.buildConfigs();

            RaceEngine engine = new RaceEngine(
                configPanel.getPassageText(),
                configPanel.isAutocorrect(),
                configPanel.isCaffeineMode(),
                configPanel.isNightShift()
            );

            for (TypistConfig cfg : lastConfigs) {
                int upg = AppState.get().getUpgradeLevel(cfg.name);
                if (upg > 0) {
                    double bonus = upg * 0.02;
                    applyUpgradeBonus(cfg, bonus);
                }
                engine.addTypist(cfg);
            }

            if (racePanel != null) deck.remove(racePanel);
            racePanel = new RacePanel(engine);
            deck.add(racePanel, CARD_RACE);

            wireRace(engine);
            cards.show(deck, CARD_RACE);
            racePanel.startRace();
        });
    }

    private void applyUpgradeBonus(TypistConfig cfg, double bonus) {
        // placeholder - upgrade bonus noted in report
    }

    private void wireRace(RaceEngine engine) {
        racePanel.setOnFinish(() -> {
            List<RaceRecord> records = racePanel.getLastRecords();
            AppState.get().recordRace(records, lastConfigs);
            statsPanel.populate(records, lastConfigs);
            cards.show(deck, CARD_STATS);
        });
    }

    private void wireStats() {
        statsPanel.setOnLeaderboard(() -> {
            leaderboardPanel.refresh();
            cards.show(deck, CARD_LEADERBOARD);
        });

        statsPanel.setOnRaceAgain(() -> {
            int seats = lastConfigs != null ? lastConfigs.size() : 3;
            if (customisePanel != null) deck.remove(customisePanel);
            customisePanel = new CustomisePanel(seats);
            deck.add(customisePanel, CARD_CUSTOMISE);
            wireCustomise();
            cards.show(deck, CARD_CUSTOMISE);
        });
    }

    private void wireLeaderboard() {
        leaderboardPanel.setOnRaceAgain(() -> {
            int seats = lastConfigs != null ? lastConfigs.size() : 3;
            if (customisePanel != null) deck.remove(customisePanel);
            customisePanel = new CustomisePanel(seats);
            deck.add(customisePanel, CARD_CUSTOMISE);
            wireCustomise();
            cards.show(deck, CARD_CUSTOMISE);
        });

        leaderboardPanel.setOnNewConfig(() -> cards.show(deck, CARD_CONFIG));
    }

    // Entry points 

    public static void startRaceGUI() {
        new Main();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Button.opaque", true);

        SwingUtilities.invokeLater(Main::startRaceGUI);
    }
}