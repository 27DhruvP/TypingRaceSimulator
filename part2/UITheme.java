package part2;

import java.awt.*;
import javax.swing.*;

public final class UITheme {

    private UITheme() {}

    // Palette — Light Theme

    // Backgrounds
    public static final Color BG_DARK   = new Color(0xF0F4FF); // page background (soft blue-white)
    public static final Color BG_CARD   = new Color(0xFFFFFF); // card / panel surface
    public static final Color BG_INPUT  = new Color(0xE8EDF8); // input fields, table rows

    // Accent colours
    public static final Color ACCENT    = new Color(0x5B5BD6); // primary purple-blue
    public static final Color ACCENT2   = new Color(0xE05252); // warning / mistype (red)

    // Semantic colours
    public static final Color GREEN     = new Color(0x2D9E5F); // success / wins
    public static final Color RED       = new Color(0xD63B3B); // burnout / error
    public static final Color TEXT_GOLD = new Color(0xB07D10); // badges / highlights

    // Text
    public static final Color TEXT_WHITE = new Color(0x1E2130); // "white" → near-black on light bg
    public static final Color TEXT_MAIN  = new Color(0x1E2130); // alias kept for apply()
    public static final Color TEXT_DIM   = new Color(0x7A82A0); // secondary / dimmed text

    // Borders
    public static final Color BORDER    = new Color(0xCDD4EC);

    // Fonts
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD,  22);
    public static final Font FONT_HEAD  = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FONT_BODY  = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO  = new Font("Monospaced", Font.PLAIN, 13);
    public static final Font FONT_BADGE = new Font("SansSerif", Font.BOLD,  12);

    // Legacy aliases (some panels still use the short names)
    public static final Font TITLE  = FONT_TITLE;
    public static final Font HEAD   = FONT_HEAD;
    public static final Font BODY   = FONT_BODY;
    public static final Font SMALL  = FONT_SMALL;
    public static final Font MONO   = FONT_MONO;

    // Global apply
    public static void apply(JComponent c) {
        c.setBackground(BG_DARK);
        c.setForeground(TEXT_MAIN);
        c.setFont(FONT_BODY);
        c.setOpaque(true);
    }

    // Inputs 
    public static void styleInput(JComponent c) {
        c.setBackground(BG_INPUT);
        c.setForeground(TEXT_MAIN);
        c.setFont(FONT_BODY);
        c.setBorder(BorderFactory.createLineBorder(BORDER));
        c.setOpaque(true);
    }

    public static void styleButton(JButton b) {
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_HEAD);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setOpaque(true);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        return p;
    }
}