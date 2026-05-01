package part2;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen 2: Typist Customisation.
 * One tab per seat.  Each tab lets the user configure name, symbol, colour,
 * typing style, keyboard, accessories, and sponsor.
 */
public class CustomisePanel extends JPanel {

    private final List<SeatEditor> editors = new ArrayList<>();
    private final JTabbedPane      tabs    = new JTabbedPane();
    private final JButton          backBtn = new JButton("← Back");
    private final JButton          nextBtn = new JButton("Start Race →");

    private Runnable onBack;
    private Runnable onNext;

    // Default symbols and colours per seat
    private static final String[] DEFAULT_SYMBOLS = {"①","②","③","④","⑤","⑥"};
    private static final Color[]  DEFAULT_COLORS  = {
        new Color(0x7C5CE8), new Color(0xFF6B6B), new Color(0x4AE26A),
        new Color(0xFFD700), new Color(0x00BFFF), new Color(0xFF8C00)
    };
    private static final String[] DEFAULT_NAMES = {
        "TURBOFINGERS","QWERTY_QUEEN","HUNT_N_PECK","SPEED_DEMON","NIGHT_OWL","IRON_FIST"
    };

    // Constructor
    public CustomisePanel(int seatCount) {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_DARK);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTabs(seatCount), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // Builder helpers 
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(24, 40, 10, 40));

        JLabel title = new JLabel("⌨  TYPING RACE  — Customise Typists", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT);
        p.add(title, BorderLayout.CENTER);
        return p;
    }

    private JTabbedPane buildTabs(int seatCount) {
        tabs.setBackground(UITheme.BG_DARK);
        tabs.setForeground(UITheme.TEXT_WHITE);
        tabs.setFont(UITheme.FONT_HEAD);
        tabs.setBorder(new EmptyBorder(0, 30, 0, 30));

        for (int i = 0; i < seatCount; i++) {
            SeatEditor ed = new SeatEditor(i,
                DEFAULT_NAMES[i],
                DEFAULT_SYMBOLS[i],
                DEFAULT_COLORS[i]);
            editors.add(ed);
            tabs.addTab("Seat " + (i + 1), ed);
            // colour the tab label
            tabs.setForegroundAt(i, DEFAULT_COLORS[i]);
        }
        return tabs;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(12, 40, 20, 40));

        styleBtn(backBtn, UITheme.BG_INPUT,  UITheme.TEXT_DIM);
        styleBtn(nextBtn, UITheme.ACCENT,    Color.WHITE);

        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });
        nextBtn.addActionListener(e -> { if (onNext != null) onNext.run(); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UITheme.BG_DARK);
        right.add(backBtn);
        right.add(nextBtn);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // Public API 
    public List<TypistConfig> buildConfigs() {
        List<TypistConfig> list = new ArrayList<>();
        for (SeatEditor ed : editors) list.add(ed.toConfig());
        return list;
    }

    public void setOnBack(Runnable r) { onBack = r; }
    public void setOnNext(Runnable r) { onNext = r; }

    // Style helper 
    private void styleBtn(JButton b, Color bg, Color fg) {
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(UITheme.FONT_HEAD); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 24, 10, 24));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    
    // Inner class: one editor per seat
    
    private static class SeatEditor extends JPanel {

        private final JTextField  nameField;
        private final JTextField  symbolField;
        private final JButton     colourBtn;
        private final JComboBox<TypistConfig.Style>    styleBox;
        private final JComboBox<TypistConfig.Keyboard> keyboardBox;
        private final JCheckBox   wristCB;
        private final JCheckBox   energyCB;
        private final JCheckBox   headphonesCB;
        private final JComboBox<TypistConfig.Sponsor>  sponsorBox;
        private final JLabel      impactLabel;

        private Color chosenColor;

        SeatEditor(int idx, String defName, String defSym, Color defColor) {
            setLayout(new BorderLayout(20, 0));
            setBackground(UITheme.BG_DARK);
            setBorder(new EmptyBorder(20, 40, 20, 40));

            chosenColor = defColor;

            // Fields 
            nameField    = styledField(defName);
            symbolField  = styledField(defSym);
            symbolField.setPreferredSize(new Dimension(60, 32));

            colourBtn = new JButton("  ■  ");
            colourBtn.setBackground(defColor);
            colourBtn.setForeground(defColor.darker().darker());
            colourBtn.setFont(UITheme.FONT_HEAD);
            colourBtn.setFocusPainted(false);
            colourBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
            colourBtn.addActionListener(e -> pickColour());

            styleBox    = new JComboBox<>(TypistConfig.Style.values());
            keyboardBox = new JComboBox<>(TypistConfig.Keyboard.values());
            styleCombo(styleBox); styleCombo(keyboardBox);

            wristCB      = styledCheck("Wrist Support  (burnout −2 turns)");
            energyCB     = styledCheck("Energy Drink   (acc +10% first half, −10% second)");
            headphonesCB = styledCheck("Noise-Cancelling Headphones  (mistype chance −2%)");

            sponsorBox = new JComboBox<>(TypistConfig.Sponsor.values());
            styleCombo(sponsorBox);

            impactLabel = new JLabel(" ");
            impactLabel.setFont(UITheme.FONT_SMALL);
            impactLabel.setForeground(UITheme.TEXT_DIM);

            // update impact line whenever anything changes
            styleBox.addActionListener(e -> updateImpact());
            keyboardBox.addActionListener(e -> updateImpact());
            wristCB.addActionListener(e -> updateImpact());
            energyCB.addActionListener(e -> updateImpact());
            headphonesCB.addActionListener(e -> updateImpact());
            sponsorBox.addActionListener(e -> updateImpact());

            add(buildLeft(),  BorderLayout.WEST);
            add(buildRight(), BorderLayout.CENTER);
            updateImpact();
        }

        // Left column: identity
        private JPanel buildLeft() {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(UITheme.BG_DARK);
            p.setPreferredSize(new Dimension(260, 0));

            p.add(card(section("Identity"), row("Name", nameField),
                       row("Symbol", symbolField), colourRow()));
            p.add(Box.createVerticalStrut(16));
            p.add(card(section("Sponsor"), sponsorBox, sponsorHint()));
            return p;
        }

        // Right column: style + accessories
        private JPanel buildRight() {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(UITheme.BG_DARK);

            p.add(card(section("Typing Style"), styleBox, styleDesc()));
            p.add(Box.createVerticalStrut(16));
            p.add(card(section("Keyboard"), keyboardBox, keyboardDesc()));
            p.add(Box.createVerticalStrut(16));
            p.add(card(section("Accessories"), wristCB, energyCB, headphonesCB));
            p.add(Box.createVerticalStrut(12));
            p.add(impactLabel);
            return p;
        }

        // Colour picker 
        private void pickColour() {
            Color c = JColorChooser.showDialog(this, "Choose typist colour", chosenColor);
            if (c != null) {
                chosenColor = c;
                colourBtn.setBackground(c);
            }
        }

        // Impact summary 
        private void updateImpact() {
            TypistConfig cfg = toConfig();
            double acc = cfg.computeBaseAccuracy() * 100;
            double mis = cfg.computeMistypeMod()   * 100;
            int    bdur = 8 + cfg.computeBurnoutDurationMod();
            String sponsor = cfg.sponsor == TypistConfig.Sponsor.NONE
                ? "none"
                : cfg.sponsor.label + " — " + cfg.sponsor.condition;
            impactLabel.setText(String.format(
                "<html><small>Base accuracy: <b>%.0f%%</b> · "
                + "Mistype modifier: <b>+%.0f%%</b> · "
                + "Burnout duration: <b>%d turns</b> · "
                + "Sponsor: <b>%s</b></small></html>",
                acc, mis, bdur, sponsor));
        }

        // toConfig 
        TypistConfig toConfig() {
            TypistConfig c = new TypistConfig();
            c.name     = nameField.getText().trim().isEmpty() ? "Typist" : nameField.getText().trim();
            c.symbol   = symbolField.getText().trim().isEmpty() ? "?" : symbolField.getText().trim();
            c.color    = chosenColor;
            c.style    = (TypistConfig.Style)    styleBox.getSelectedItem();
            c.keyboard = (TypistConfig.Keyboard) keyboardBox.getSelectedItem();
            c.sponsor  = (TypistConfig.Sponsor)  sponsorBox.getSelectedItem();
            c.wristSupport          = wristCB.isSelected();
            c.energyDrink           = energyCB.isSelected();
            c.noiseCancelHeadphones = headphonesCB.isSelected();
            return c;
        }

        // Layout utilities 
        private JPanel card(Component... comps) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(UITheme.BG_CARD);
            p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
            ));
            p.setAlignmentX(LEFT_ALIGNMENT);
            for (Component c : comps) {
                if (c instanceof JComponent jc) jc.setAlignmentX(LEFT_ALIGNMENT);
                p.add(c);
                p.add(Box.createVerticalStrut(6));
            }
            return p;
        }

        private JLabel section(String t) {
            JLabel l = new JLabel(t);
            l.setFont(UITheme.FONT_HEAD);
            l.setForeground(UITheme.ACCENT);
            return l;
        }

        private JPanel row(String label, JComponent field) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            p.setBackground(UITheme.BG_CARD);
            JLabel l = new JLabel(label + ":");
            l.setFont(UITheme.FONT_BODY); l.setForeground(UITheme.TEXT_DIM);
            p.add(l); p.add(field);
            return p;
        }

        private JPanel colourRow() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            p.setBackground(UITheme.BG_CARD);
            JLabel l = new JLabel("Colour:");
            l.setFont(UITheme.FONT_BODY); l.setForeground(UITheme.TEXT_DIM);
            p.add(l); p.add(colourBtn);
            return p;
        }

        private JLabel styleDesc() {
            JLabel l = new JLabel("<html><small>Affects base accuracy &amp; burnout risk</small></html>");
            l.setForeground(UITheme.TEXT_DIM); l.setFont(UITheme.FONT_SMALL);
            return l;
        }

        private JLabel keyboardDesc() {
            JLabel l = new JLabel("<html><small>Affects mistype rate &amp; speed multiplier</small></html>");
            l.setForeground(UITheme.TEXT_DIM); l.setFont(UITheme.FONT_SMALL);
            return l;
        }

        private JLabel sponsorHint() {
            JLabel l = new JLabel("<html><small>Earn bonus points if condition met</small></html>");
            l.setForeground(UITheme.TEXT_DIM); l.setFont(UITheme.FONT_SMALL);
            return l;
        }

        private JTextField styledField(String def) {
            JTextField f = new JTextField(def, 14);
            f.setBackground(UITheme.BG_INPUT); f.setForeground(UITheme.TEXT_WHITE);
            f.setCaretColor(UITheme.TEXT_WHITE); f.setFont(UITheme.FONT_BODY);
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(4, 6, 4, 6)));
            return f;
        }

        private JCheckBox styledCheck(String text) {
            JCheckBox cb = new JCheckBox(text);
            cb.setBackground(UITheme.BG_CARD);
            cb.setForeground(UITheme.TEXT_WHITE);
            cb.setFont(UITheme.FONT_BODY);
            cb.setFocusPainted(false);
            return cb;
        }

        private <T> void styleCombo(JComboBox<T> cb) {
            cb.setBackground(UITheme.BG_INPUT);
            cb.setForeground(UITheme.TEXT_WHITE);
            cb.setFont(UITheme.FONT_BODY);
            cb.setFocusable(false);
            cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        }
    }
}