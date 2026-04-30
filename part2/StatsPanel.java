package part2;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Screen 4: Post-race statistics.
 * Shows a table of every typist's WPM, accuracy, burnout count, accuracy change,
 * and points earned.  Also shows personal bests and badges earned.
 */
public class StatsPanel extends JPanel {

    private final JButton raceAgainBtn  = new JButton("Race Again");
    private final JButton leaderboardBtn= new JButton("Leaderboard →");
    private final JPanel  contentArea   = new JPanel();

    private Runnable onRaceAgain;
    private Runnable onLeaderboard;

    // ── Constructor ───────────────────────────────────────────────────────────
    public StatsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_DARK);

        add(buildHeader(),  BorderLayout.NORTH);
        add(new JScrollPane(contentArea), BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setBackground(UITheme.BG_DARK);
        contentArea.setBorder(new EmptyBorder(20, 40, 20, 40));
    }

    // ── Public API ────────────────────────────────────────────────────────────
    public void populate(List<RaceRecord> records, List<TypistConfig> configs) {
        contentArea.removeAll();

        // Winner banner
        if (!records.isEmpty()) {
            RaceRecord w = records.get(0);
            JLabel banner = new JLabel("🏆  " + w.typistName + " wins with "
                + String.format("%.1f WPM", w.wpm) + "!", SwingConstants.CENTER);
            banner.setFont(UITheme.FONT_TITLE);
            banner.setForeground(UITheme.TEXT_GOLD);
            banner.setAlignmentX(CENTER_ALIGNMENT);
            banner.setBorder(new EmptyBorder(0, 0, 20, 0));
            contentArea.add(banner);
        }

        // Results table
        contentArea.add(sectionLabel("📊 Race Results"));
        contentArea.add(Box.createVerticalStrut(8));
        contentArea.add(buildResultsTable(records));
        contentArea.add(Box.createVerticalStrut(24));

        // Accuracy changes
        contentArea.add(sectionLabel("📈 Accuracy Changes"));
        contentArea.add(Box.createVerticalStrut(8));
        contentArea.add(buildAccuracyPanel(records, configs));
        contentArea.add(Box.createVerticalStrut(24));

        // Badges
        contentArea.add(sectionLabel("🎖 Badges Earned This Race"));
        contentArea.add(Box.createVerticalStrut(8));
        contentArea.add(buildBadgesPanel(records));

        contentArea.revalidate();
        contentArea.repaint();
    }

    public void setOnRaceAgain  (Runnable r) { onRaceAgain   = r; }
    public void setOnLeaderboard(Runnable r) { onLeaderboard = r; }

    // ── Builder helpers ───────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(24, 40, 10, 40));
        JLabel title = new JLabel("⌨  TYPING RACE  — Results", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT);
        p.add(title);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        p.setBackground(UITheme.BG_DARK);

        styleBtn(raceAgainBtn,   UITheme.GREEN,  Color.BLACK);
        styleBtn(leaderboardBtn, UITheme.ACCENT, Color.WHITE);

        raceAgainBtn.addActionListener  (e -> { if (onRaceAgain   != null) onRaceAgain.run(); });
        leaderboardBtn.addActionListener(e -> { if (onLeaderboard != null) onLeaderboard.run(); });

        p.add(raceAgainBtn);
        p.add(leaderboardBtn);
        return p;
    }

    private JScrollPane buildResultsTable(List<RaceRecord> records) {
        String[] cols = {"Pos", "Name", "WPM", "Accuracy %", "Burnouts", "Points"};
        Object[][] data = new Object[records.size()][cols.length];
        String[] medals = {"🥇","🥈","🥉","4th","5th","6th"};

        for (int i = 0; i < records.size(); i++) {
            RaceRecord r = records.get(i);
            data[i][0] = i < medals.length ? medals[i] : (i + 1) + "th";
            data[i][1] = r.typistName;
            data[i][2] = String.format("%.1f", r.wpm);
            data[i][3] = String.format("%.0f%%", r.accuracyPercent);
            data[i][4] = r.burnoutCount;
            data[i][5] = r.pointsEarned();
        }

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(UITheme.BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        sp.setPreferredSize(new Dimension(600, records.size() * 30 + 30));
        sp.setAlignmentX(LEFT_ALIGNMENT);
        return sp;
    }

    private JPanel buildAccuracyPanel(List<RaceRecord> records, List<TypistConfig> configs) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BG_DARK);
        p.setAlignmentX(LEFT_ALIGNMENT);

        for (RaceRecord r : records) {
            double delta = r.accuracyAfter - r.accuracyBefore;
            String arrow = delta > 0 ? "▲" : delta < 0 ? "▼" : "—";
            Color  col   = delta > 0 ? UITheme.GREEN : delta < 0 ? UITheme.RED : UITheme.TEXT_DIM;

            JLabel lbl = new JLabel(String.format(
                "%s  %s %.2f → %.2f  (%s%.3f)",
                r.typistName, arrow, r.accuracyBefore, r.accuracyAfter,
                delta >= 0 ? "+" : "", delta));
            lbl.setFont(UITheme.FONT_BODY);
            lbl.setForeground(col);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            p.add(lbl);
            p.add(Box.createVerticalStrut(4));

            // Personal best note
            double best = AppState.get().getBestWpm(r.typistName);
            if (r.wpm >= best - 0.01) {
                JLabel pb = new JLabel("   ⭐ New personal best WPM: " +
                    String.format("%.1f", r.wpm));
                pb.setFont(UITheme.FONT_SMALL);
                pb.setForeground(UITheme.TEXT_GOLD);
                pb.setAlignmentX(LEFT_ALIGNMENT);
                p.add(pb);
                p.add(Box.createVerticalStrut(4));
            }
        }
        return p;
    }

    private JPanel buildBadgesPanel(List<RaceRecord> records) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(UITheme.BG_DARK);
        p.setAlignmentX(LEFT_ALIGNMENT);

        boolean any = false;
        for (RaceRecord r : records) {
            for (String badge : AppState.get().getBadges(r.typistName)) {
                JLabel lbl = new JLabel(badge + "  " + r.typistName);
                lbl.setFont(UITheme.FONT_BADGE);
                lbl.setForeground(UITheme.TEXT_GOLD);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.ACCENT, 1, true),
                    new EmptyBorder(4, 8, 4, 8)));
                lbl.setOpaque(true);
                lbl.setBackground(UITheme.BG_CARD);
                p.add(lbl);
                any = true;
            }
        }
        if (!any) {
            JLabel none = new JLabel("No new badges this race. Keep racing!");
            none.setFont(UITheme.FONT_BODY);
            none.setForeground(UITheme.TEXT_DIM);
            p.add(none);
        }
        return p;
    }

    // ── Style helpers ─────────────────────────────────────────────────────────

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_HEAD);
        l.setForeground(UITheme.ACCENT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void styleBtn(JButton b, Color bg, Color fg) {
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(UITheme.FONT_HEAD); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 24, 10, 24));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable t) {
        t.setBackground(UITheme.BG_CARD);
        t.setForeground(UITheme.TEXT_WHITE);
        t.setFont(UITheme.FONT_BODY);
        t.setGridColor(UITheme.BORDER);
        t.setRowHeight(28);
        t.setFillsViewportHeight(true);
        t.setShowGrid(true);
        t.getTableHeader().setBackground(UITheme.BG_INPUT);
        t.getTableHeader().setForeground(UITheme.ACCENT);
        t.getTableHeader().setFont(UITheme.FONT_HEAD);
        t.setSelectionBackground(UITheme.ACCENT);
        t.setSelectionForeground(Color.WHITE);
        // Centre-align all columns
        DefaultTableCellRenderer centre = new DefaultTableCellRenderer();
        centre.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(centre);
    }
}
