package part2;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Screen 1: Race Configuration.
 * Users choose a passage, seat count, and difficulty modifiers.
 */
public class ConfigPanel extends JPanel {

    // Predefined passages
    private static final String[][] PASSAGES = {
        { "Short  (~40 chars)",
          "The quick brown fox jumps over the lazy dog." },
        { "Medium (~80 chars)",
          "Typing fast is a skill that takes years of practice, dedication, and a good keyboard." },
        { "Long   (~160 chars)",
          "In a world driven by speed and precision, those who master the keyboard wield a quiet "
        + "superpower. Every keystroke tells a story, and every typo is a chapter best forgotten." },
        { "Epic   (~250 chars)",
          "To type well is to think clearly, to express swiftly, and to communicate with a fluency "
        + "that transcends mere words. The keyboard is the modern pen, and the screen a canvas upon "
        + "which ideas bloom or wither depending on the fingers that tend them." }
    };

    //  Widgets 
    private final JComboBox<String> passageBox   = new JComboBox<>();
    private final JTextArea         passageArea  = new JTextArea(4, 40);
    private final JSpinner          seatSpinner  = new JSpinner(new SpinnerNumberModel(3, 2, 6, 1));
    private final JCheckBox         autocorrect  = new JCheckBox("Autocorrect On");
    private final JCheckBox         caffeine     = new JCheckBox("Caffeine Mode");
    private final JCheckBox         nightShift   = new JCheckBox("Night Shift");
    private final JButton           nextBtn      = new JButton("Set Up Typists →");

    private Runnable onNext;

    // Constructor 
    public ConfigPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(buildTitle(),    BorderLayout.NORTH);
        add(buildCenter(),   BorderLayout.CENTER);
        add(buildSouth(),    BorderLayout.SOUTH);
    }

    // Build helpers

    private JLabel buildTitle() {
        JLabel lbl = new JLabel("⌨  TYPING RACE  — Configure Race", SwingConstants.CENTER);
        lbl.setFont(UITheme.FONT_TITLE);
        lbl.setForeground(UITheme.ACCENT);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        return lbl;
    }

    private JPanel buildCenter() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BG_DARK);

        p.add(sectionLabel("📜 Passage Selection"));
        p.add(Box.createVerticalStrut(8));
        p.add(buildPassageSection());
        p.add(Box.createVerticalStrut(20));

        p.add(sectionLabel("🪑 Typist Count"));
        p.add(Box.createVerticalStrut(8));
        p.add(buildSeatSection());
        p.add(Box.createVerticalStrut(20));

        p.add(sectionLabel("⚙ Difficulty Modifiers"));
        p.add(Box.createVerticalStrut(8));
        p.add(buildModifiers());

        return p;
    }

    private JPanel buildPassageSection() {
        JPanel card = card();
        card.setLayout(new BorderLayout(10, 10));

        // Combo
        for (String[] pair : PASSAGES) passageBox.addItem(pair[0]);
        passageBox.addItem("Custom…");
        style(passageBox);
        passageBox.addActionListener(e -> onPassageSelected());

        // Text area
        passageArea.setLineWrap(true);
        passageArea.setWrapStyleWord(true);
        passageArea.setFont(UITheme.FONT_MONO);
        passageArea.setBackground(UITheme.BG_INPUT);
        passageArea.setForeground(UITheme.TEXT_WHITE);
        passageArea.setCaretColor(UITheme.TEXT_WHITE);
        passageArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(passageArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        scroll.setBackground(UITheme.BG_INPUT);

        JLabel hint = dim("Select a preset or type your own passage. Track length = passage length.");

        card.add(passageBox, BorderLayout.NORTH);
        card.add(scroll,     BorderLayout.CENTER);
        card.add(hint,       BorderLayout.SOUTH);

        onPassageSelected(); // populate initial text
        return card;
    }

    private JPanel buildSeatSection() {
        JPanel card = card();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 6));

        seatSpinner.setFont(UITheme.FONT_HEAD);
        seatSpinner.setBackground(UITheme.BG_INPUT);
        seatSpinner.setForeground(UITheme.TEXT_WHITE);
        ((JSpinner.DefaultEditor) seatSpinner.getEditor()).getTextField()
            .setBackground(UITheme.BG_INPUT);
        ((JSpinner.DefaultEditor) seatSpinner.getEditor()).getTextField()
            .setForeground(UITheme.TEXT_WHITE);
        seatSpinner.setPreferredSize(new Dimension(70, 32));

        card.add(dim("Number of typists (2 – 6):"));
        card.add(seatSpinner);

        return card;
    }

    private JPanel buildModifiers() {
        JPanel card = card();
        card.setLayout(new GridLayout(1, 3, 20, 0));

        styleCheck(autocorrect,
            "<html><b>Autocorrect On</b><br><small>Slide-back halved</small></html>");
        styleCheck(caffeine,
            "<html><b>Caffeine Mode</b><br><small>Speed boost first 10 turns,<br>then burnout risk ↑</small></html>");
        styleCheck(nightShift,
            "<html><b>Night Shift</b><br><small>All accuracy −8%</small></html>");

        card.add(autocorrect);
        card.add(caffeine);
        card.add(nightShift);
        return card;
    }

    private JPanel buildSouth() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(UITheme.BG_DARK);

        nextBtn.setFont(UITheme.FONT_HEAD);
        nextBtn.setBackground(UITheme.ACCENT);
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setFocusPainted(false);
        nextBtn.setBorder(new EmptyBorder(10, 28, 10, 28));
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.addActionListener(e -> proceed());

        p.add(nextBtn);
        return p;
    }

    // Logic

    private void onPassageSelected() {
        int idx = passageBox.getSelectedIndex();
        if (idx >= 0 && idx < PASSAGES.length) {
            passageArea.setText(PASSAGES[idx][1]);
            passageArea.setEditable(false);
            passageArea.setForeground(UITheme.TEXT_WHITE);
        } else {
            passageArea.setText("");
            passageArea.setEditable(true);
            passageArea.setForeground(UITheme.TEXT_WHITE);
            passageArea.requestFocusInWindow();
        }
    }

    private void proceed() {
        String text = passageArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter or select a passage.",
                "No passage", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (text.length() < 10) {
            JOptionPane.showMessageDialog(this, "Passage must be at least 10 characters.",
                "Too short", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (onNext != null) onNext.run();
    }

    // Public getters for next screen 

    public String  getPassageText()    { return passageArea.getText().trim(); }
    public boolean isAutocorrect()     { return autocorrect.isSelected(); }
    public boolean isCaffeineMode()    { return caffeine.isSelected(); }
    public boolean isNightShift()      { return nightShift.isSelected(); }
    public int     getSeatCount()      { return (Integer) seatSpinner.getValue(); }

    public void setOnNext(Runnable r) { onNext = r; }

    // Style helpers

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_HEAD);
        l.setForeground(UITheme.ACCENT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel dim(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_DIM);
        return l;
    }

    private void style(JComboBox<?> cb) {
        cb.setBackground(UITheme.BG_INPUT);
        cb.setForeground(UITheme.TEXT_WHITE);
        cb.setFont(UITheme.FONT_BODY);
        cb.setFocusable(false);
    }

    private void styleCheck(JCheckBox cb, String html) {
        cb.setText(html);
        cb.setBackground(UITheme.BG_CARD);
        cb.setForeground(UITheme.TEXT_WHITE);
        cb.setFont(UITheme.FONT_BODY);
        cb.setFocusable(false);
        cb.setVerticalAlignment(SwingConstants.TOP);
    }
}
