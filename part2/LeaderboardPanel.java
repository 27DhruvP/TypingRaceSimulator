package part2;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Screen 5: Leaderboard & Sponsor/Earnings view.
 * Shows cumulative rankings, badges, sponsor earnings and the upgrade shop.
 */
public class LeaderboardPanel extends JPanel {

    private final JPanel  contentArea  = new JPanel();
    private final JButton raceAgainBtn = new JButton("Race Again");
    private final JButton configBtn    = new JButton("New Configuration");

    private Runnable onRaceAgain;
    private Runnable onNewConfig;

    // Constructor 
    public LeaderboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_DARK);

        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setBackground(UITheme.BG_DARK);
        contentArea.setBorder(new EmptyBorder(20, 40, 20, 40));

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.setBackground(UITheme.BG_DARK);
        scroll.getViewport().setBackground(UITheme.BG_DARK);

        add(buildHeader(), BorderLayout.NORTH);
        add(scroll,        BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // Public API 
    public void refresh() {
        contentArea.removeAll();

        contentArea.add(sectionLabel(" Global Leaderboard"));
        contentArea.add(Box.createVerticalStrut(10));
        contentArea.add(buildLeaderboardTable());
        contentArea.add(Box.createVerticalStrut(24));

        contentArea.add(sectionLabel(" Badges"));
        contentArea.add(Box.createVerticalStrut(8));
        contentArea.add(buildBadgesPanel());
        contentArea.add(Box.createVerticalStrut(24));

        contentArea.add(sectionLabel(" Sponsor Earnings & Shop"));
        contentArea.add(Box.createVerticalStrut(8));
        contentArea.add(buildEarningsPanel());
        contentArea.add(Box.createVerticalStrut(24));

        contentArea.add(sectionLabel(" Full Race History"));
        contentArea.add(Box.createVerticalStrut(8));
        contentArea.add(buildHistoryTable());

        contentArea.revalidate();
        contentArea.repaint();
    }

    public void setOnRaceAgain (Runnable r) { onRaceAgain = r; }
    public void setOnNewConfig (Runnable r) { onNewConfig = r; }

    // Builders 

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(24, 40, 10, 40));
        JLabel title = new JLabel("TYPING RACE  — Leaderboard", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT);
        p.add(title);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        p.setBackground(UITheme.BG_DARK);
        styleBtn(raceAgainBtn, UITheme.GREEN,  Color.BLACK);
        styleBtn(configBtn,    UITheme.ACCENT, Color.WHITE);
        raceAgainBtn.addActionListener(e -> { if (onRaceAgain != null) onRaceAgain.run(); });
        configBtn.addActionListener   (e -> { if (onNewConfig != null) onNewConfig.run(); });
        p.add(raceAgainBtn);
        p.add(configBtn);
        return p;
    }

    private JScrollPane buildLeaderboardTable() {
        List<Map.Entry<String,Integer>> lb = AppState.get().getLeaderboard();
        String[] cols = {"Rank", "Name", "Points", "Best WPM"};
        Object[][] data = new Object[lb.size()][cols.length];
        String[] medals = {"🥇","🥈","🥉"};

        for (int i = 0; i < lb.size(); i++) {
            Map.Entry<String,Integer> e = lb.get(i);
            data[i][0] = i < medals.length ? medals[i] : "#" + (i + 1);
            data[i][1] = e.getKey();
            data[i][2] = e.getValue();
            data[i][3] = String.format("%.1f", AppState.get().getBestWpm(e.getKey()));
        }

        return styledScrollTable(data, cols, lb.size());
    }

    private JPanel buildBadgesPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(UITheme.BG_DARK);
        p.setAlignmentX(LEFT_ALIGNMENT);

        List<Map.Entry<String,Integer>> lb = AppState.get().getLeaderboard();
        boolean any = false;
        for (Map.Entry<String,Integer> entry : lb) {
            Set<String> bs = AppState.get().getBadges(entry.getKey());
            for (String badge : bs) {
                JLabel lbl = new JLabel(badge + " " + entry.getKey());
                lbl.setFont(UITheme.FONT_BADGE);
                lbl.setForeground(UITheme.TEXT_GOLD);
                lbl.setOpaque(true);
                lbl.setBackground(UITheme.BG_CARD);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.ACCENT, 1, true),
                    new EmptyBorder(4, 8, 4, 8)));
                p.add(lbl);
                any = true;
            }
        }
        if (!any) {
            JLabel none = new JLabel("No badges earned yet. Keep racing to unlock them!");
            none.setFont(UITheme.FONT_BODY); none.setForeground(UITheme.TEXT_DIM);
            p.add(none);
        }
        return p;
    }

    private JPanel buildEarningsPanel() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(UITheme.BG_DARK);
        outer.setAlignmentX(LEFT_ALIGNMENT);

        List<Map.Entry<String,Integer>> lb = AppState.get().getLeaderboard();
        if (lb.isEmpty()) {
            JLabel none = new JLabel("No earnings yet.");
            none.setFont(UITheme.FONT_BODY); none.setForeground(UITheme.TEXT_DIM);
            outer.add(none);
            return outer;
        }

        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 8));
        grid.setBackground(UITheme.BG_DARK);
        grid.setAlignmentX(LEFT_ALIGNMENT);

        for (Map.Entry<String,Integer> entry : lb) {
            String name = entry.getKey();
            int coins   = AppState.get().getEarnings(name);
            int upg     = AppState.get().getUpgradeLevel(name);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
            row.setBackground(UITheme.BG_CARD);
            row.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));

            JLabel nameLbl = new JLabel(name);
            nameLbl.setFont(UITheme.FONT_BODY); nameLbl.setForeground(UITheme.TEXT_WHITE);

            JLabel coinsLbl = new JLabel(coins + " coins");
            coinsLbl.setFont(UITheme.FONT_BODY); coinsLbl.setForeground(UITheme.TEXT_GOLD);

            String upgText = upg == 0 ? "No upgrades" : "Upgrade Lv" + upg;
            JLabel upgLbl = new JLabel(upgText);
            upgLbl.setFont(UITheme.FONT_SMALL); upgLbl.setForeground(UITheme.TEXT_DIM);

            // Buy button — costs 50 coins per level
            int nextLevel = upg + 1;
            int cost      = nextLevel * 50;
            JButton buyBtn = new JButton("Buy Lv" + nextLevel + " (" + cost + "¢)");
            buyBtn.setFont(UITheme.FONT_SMALL);
            buyBtn.setBackground(UITheme.BG_INPUT);
            buyBtn.setForeground(UITheme.TEXT_WHITE);
            buyBtn.setFocusPainted(false);
            buyBtn.setBorder(new EmptyBorder(4, 10, 4, 10));
            buyBtn.setEnabled(coins >= cost && nextLevel <= 3);
            buyBtn.addActionListener(e -> {
                AppState.get().purchaseUpgrade(name, nextLevel);
                refresh();
            });

            row.add(nameLbl); row.add(coinsLbl); row.add(upgLbl); row.add(buyBtn);
            grid.add(row);
        }

        JLabel hint = new JLabel("Upgrades improve starting accuracy in the next race. Lv1=+2%, Lv2=+4%, Lv3=+6%");
        hint.setFont(UITheme.FONT_SMALL); hint.setForeground(UITheme.TEXT_DIM);
        hint.setAlignmentX(LEFT_ALIGNMENT);

        outer.add(grid);
        outer.add(Box.createVerticalStrut(6));
        outer.add(hint);
        return outer;
    }

    private JScrollPane buildHistoryTable() {
        List<RaceRecord> all = AppState.get().getAllRecords();
        // newest first
        List<RaceRecord> rev = new ArrayList<>(all);
        Collections.reverse(rev);

        String[] cols = {"Time", "Name", "Pos", "WPM", "Accuracy", "Burnouts", "Points"};
        Object[][] data = new Object[rev.size()][cols.length];

        for (int i = 0; i < rev.size(); i++) {
            RaceRecord r = rev.get(i);
            data[i][0] = r.timestamp;
            data[i][1] = r.typistName;
            data[i][2] = r.finishPosition;
            data[i][3] = String.format("%.1f", r.wpm);
            data[i][4] = String.format("%.0f%%", r.accuracyPercent);
            data[i][5] = r.burnoutCount;
            data[i][6] = r.pointsEarned();
        }

        return styledScrollTable(data, cols, Math.min(rev.size(), 12));
    }

    // Helpers 

    private JScrollPane styledScrollTable(Object[][] data, String[] cols, int visRows) {
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setBackground(UITheme.BG_CARD);
        table.setForeground(UITheme.TEXT_WHITE);
        table.setFont(UITheme.FONT_BODY);
        table.setGridColor(UITheme.BORDER);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(UITheme.BG_INPUT);
        table.getTableHeader().setForeground(UITheme.ACCENT);
        table.getTableHeader().setFont(UITheme.FONT_HEAD);
        table.setSelectionBackground(UITheme.ACCENT);
        DefaultTableCellRenderer centre = new DefaultTableCellRenderer();
        centre.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(centre);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        sp.setBackground(UITheme.BG_CARD);
        sp.setPreferredSize(new Dimension(700, Math.max(60, visRows * 26 + 28)));
        sp.setAlignmentX(LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, visRows * 26 + 28));
        return sp;
    }

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
}
