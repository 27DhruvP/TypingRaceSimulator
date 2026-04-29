package part2;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * Screen 3: The live race.
 * Displays the passage text with each typist's cursor, colour-coded progress
 * bars, and live WPM / burnout / mistype indicators.
 * Driven by a Swing Timer that calls engine.tick() every 120 ms.
 */
public class RacePanel extends JPanel {

    private final RaceEngine engine;
    private final Timer      timer;
    private long             raceStart;

    private Runnable                          onFinish;
    private List<RaceRecord>                  lastRecords;

    // ── Sub-panels ────────────────────────────────────────────────────────────
    private final PassageView  passageView;
    private final LanesPanel   lanesPanel;
    private final JLabel       turnLabel    = new JLabel("Turn 0");
    private final JLabel       statusLabel  = new JLabel("Race in progress…");
    private final JButton      skipBtn      = new JButton("Skip to end");

    // ── Constructor ───────────────────────────────────────────────────────────
    public RacePanel(RaceEngine engine) {
        this.engine = engine;
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_DARK);

        passageView = new PassageView(engine);
        lanesPanel  = new LanesPanel(engine);

        add(buildHeader(),              BorderLayout.NORTH);
        add(buildCenter(),              BorderLayout.CENTER);
        add(buildFooter(),              BorderLayout.SOUTH);

        // Swing Timer: tick every 120 ms
        timer = new Timer(120, e -> tick());
    }

    // ── Public API ────────────────────────────────────────────────────────────
    public void startRace() {
        engine.start();
        raceStart = System.currentTimeMillis();
        engine.addFinishListener((winner, records) -> {
            lastRecords = records;
            SwingUtilities.invokeLater(this::onRaceFinished);
        });
        timer.start();
    }

    public void setOnFinish(Runnable r) { onFinish = r; }
    public List<RaceRecord> getLastRecords() { return lastRecords; }

    // ── Tick ──────────────────────────────────────────────────────────────────
    private void tick() {
        engine.tick();
        long elapsed = System.currentTimeMillis() - raceStart;
        turnLabel.setText("Turn " + engine.getTurn()
            + "  |  " + String.format("%.1f s", elapsed / 1000.0));
        passageView.repaint();
        lanesPanel.repaint();
    }

    private void onRaceFinished() {
        timer.stop();
        Typist w = engine.getWinner();
        statusLabel.setText("🏆  " + (w != null ? w.getName() : "?") + " wins!");
        statusLabel.setForeground(UITheme.TEXT_GOLD);
        skipBtn.setText("View Results →");
        // keep repainting once so final state shows
        passageView.repaint();
        lanesPanel.repaint();
    }

    // ── Builder helpers ───────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(20, 40, 8, 40));

        JLabel title = new JLabel("⌨  TYPING RACE", SwingConstants.LEFT);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT);

        turnLabel.setFont(UITheme.FONT_BODY);
        turnLabel.setForeground(UITheme.TEXT_DIM);

        statusLabel.setFont(UITheme.FONT_HEAD);
        statusLabel.setForeground(UITheme.GREEN);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setBackground(UITheme.BG_DARK);
        right.add(statusLabel);
        right.add(turnLabel);

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildCenter() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(0, 30, 0, 30));

        // Passage card
        JPanel passageCard = card();
        passageCard.setLayout(new BorderLayout());
        JLabel passLbl = new JLabel("📜 Passage");
        passLbl.setFont(UITheme.FONT_HEAD);
        passLbl.setForeground(UITheme.ACCENT);
        passLbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        passageCard.add(passLbl,    BorderLayout.NORTH);
        passageCard.add(passageView, BorderLayout.CENTER);
        passageCard.setAlignmentX(LEFT_ALIGNMENT);

        // Lanes card
        JPanel lanesCard = card();
        lanesCard.setLayout(new BorderLayout());
        JLabel laneLbl = new JLabel("🏁 Race Track");
        laneLbl.setFont(UITheme.FONT_HEAD);
        laneLbl.setForeground(UITheme.ACCENT);
        laneLbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        lanesCard.add(laneLbl,   BorderLayout.NORTH);
        lanesCard.add(lanesPanel, BorderLayout.CENTER);
        lanesCard.setAlignmentX(LEFT_ALIGNMENT);

        p.add(passageCard);
        p.add(Box.createVerticalStrut(16));
        p.add(lanesCard);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(0, 40, 10, 40));

        skipBtn.setFont(UITheme.FONT_HEAD);
        skipBtn.setBackground(UITheme.ACCENT);
        skipBtn.setForeground(Color.WHITE);
        skipBtn.setFocusPainted(false);
        skipBtn.setBorder(new EmptyBorder(9, 22, 9, 22));
        skipBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        skipBtn.addActionListener(e -> {
            if (engine.isFinished()) {
                if (onFinish != null) onFinish.run();
            } else {
                // fast-forward
                timer.stop();
                while (!engine.isFinished()) engine.tick();
                passageView.repaint();
                lanesPanel.repaint();
                onRaceFinished();
            }
        });

        p.add(skipBtn);
        return p;
    }

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        return p;
    }

    // Inner: passage text with coloured cursors
    
    private static class PassageView extends JPanel {
        private final RaceEngine engine;
        PassageView(RaceEngine e) {
            this.engine = e;
            setBackground(UITheme.BG_INPUT);
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setPreferredSize(new Dimension(600, 80));
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            String passage = engine.getPassage();
            int    pLen    = engine.getPassageLength();
            FontMetrics fm = g.getFontMetrics(UITheme.FONT_MONO);
            int charW = fm.charWidth('m');
            int lineH = fm.getHeight();
            int maxCharsPerLine = Math.max(1, (getWidth() - 20) / charW);
            int x0 = 10, y0 = fm.getAscent() + 10;

            // Draw passage char by char, line-wrapping
            for (int ci = 0; ci < passage.length(); ci++) {
                int col = ci % maxCharsPerLine;
                int row = ci / maxCharsPerLine;
                int cx  = x0 + col * charW;
                int cy  = y0 + row * lineH;

                // Find who has progressed past this char
                Color bgColor = null;
                List<Typist> typists = engine.getTypists();
                for (int ti = typists.size() - 1; ti >= 0; ti--) {
                    Typist t = typists.get(ti);
                    if (t.getProgress() > ci) {
                        Color base = engine.getColor(t);
                        bgColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), 80);
                        break;
                    }
                }
                if (bgColor != null) {
                    g.setColor(bgColor);
                    g.fillRect(cx, cy - fm.getAscent(), charW, lineH);
                }

                // Draw cursor(s) at exact position
                for (Typist t : typists) {
                    if (t.getProgress() == ci) {
                        g.setColor(engine.getColor(t));
                        g.fillRect(cx, cy - fm.getAscent(), 2, lineH);
                    }
                }

                g.setFont(UITheme.FONT_MONO);
                g.setColor(UITheme.TEXT_WHITE);
                g.drawString(String.valueOf(passage.charAt(ci)), cx, cy);
            }

            // Draw each typist's symbol above their cursor
            List<Typist> typists = engine.getTypists();
            for (Typist t : typists) {
                int ci = Math.min(t.getProgress(), pLen - 1);
                if (ci < 0) ci = 0;
                int col = ci % maxCharsPerLine;
                int row = ci / maxCharsPerLine;
                int cx  = x0 + col * charW;
                int cy  = y0 + row * lineH - fm.getAscent() - 2;
                g.setFont(UITheme.FONT_SMALL);
                g.setColor(engine.getColor(t));
                g.drawString(String.valueOf(t.getSymbol()), cx, cy);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner: lanes / progress bars
    // ══════════════════════════════════════════════════════════════════════════
    private static class LanesPanel extends JPanel {
        private final RaceEngine engine;
        LanesPanel(RaceEngine e) {
            this.engine = e;
            setBackground(UITheme.BG_CARD);
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(600, engine.getTypists().size() * 56 + 10);
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            List<Typist>       typists = engine.getTypists();
            List<TypistConfig> configs = engine.getConfigs();
            int pLen  = engine.getPassageLength();
            int W     = getWidth() - 20;
            int laneH = 50;

            for (int i = 0; i < typists.size(); i++) {
                Typist t   = typists.get(i);
                Color  col = engine.getColor(t);
                int    y   = 8 + i * laneH;

                // Background track
                g.setColor(UITheme.BG_INPUT);
                g.fillRoundRect(10, y + 16, W, 18, 9, 9);

                // Progress fill
                double pct   = Math.min(1.0, (double) t.getProgress() / pLen);
                int    fillW = (int)(pct * W);
                if (fillW > 0) {
                    Color fillColor = t.isBurntOut() ? UITheme.RED :
                                      t.justMistyped() ? UITheme.ACCENT2 : col;
                    GradientPaint gp = new GradientPaint(
                        10, y + 16, fillColor.brighter(),
                        10 + fillW, y + 34, fillColor);
                    g.setPaint(gp);
                    g.fillRoundRect(10, y + 16, fillW, 18, 9, 9);
                }

                // Symbol + name
                g.setFont(UITheme.FONT_BODY);
                g.setColor(col);
                String sym = String.valueOf(t.getSymbol());
                g.drawString(sym, 10, y + 13);
                g.setColor(UITheme.TEXT_WHITE);
                g.setFont(UITheme.FONT_SMALL);
                g.drawString(t.getName(), 30, y + 13);

                // Right-side status
                String status;
                if (t.isBurntOut()) {
                    status = "🔥 BURNT OUT (" + t.getBurnoutTurnsRemaining() + ")";
                    g.setColor(UITheme.RED);
                } else if (t.justMistyped()) {
                    status = "← MISTYPED";
                    g.setColor(UITheme.ACCENT2);
                } else {
                    status = String.format("%.0f%%", pct * 100);
                    g.setColor(UITheme.TEXT_DIM);
                }
                g.setFont(UITheme.FONT_SMALL);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(status, W + 10 - fm.stringWidth(status), y + 13);

                // Finish line
                g.setColor(UITheme.BORDER);
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 1, new float[]{4, 4}, 0));
                g.drawLine(10 + W, y + 12, 10 + W, y + 36);
                g.setStroke(new BasicStroke());
            }
        }
    }
}
