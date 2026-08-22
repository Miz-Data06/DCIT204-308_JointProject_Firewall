package com.fooddelivery.gui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

public class RouteVisualizationPanel extends JPanel {
    private static final Color PRIMARY_GREEN = new Color(0x16A34A);
    private static final Color DARK_GREEN = new Color(0x166534);
    private static final Color LIGHT_GREEN = new Color(0xDCFCE7);
    private static final Color BORDER = new Color(0xE5E7EB);
    private static final Color TEXT_DARK = new Color(0x111827);
    private static final Color MUTED_TEXT = new Color(0x6B7280);

    private GuiOrderResult result;

    public RouteVisualizationPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(680, 210));
    }

    public void setResult(GuiOrderResult result) {
        this.result = result;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawPanel(g);
        } finally {
            g.dispose();
        }
    }

    private void drawPanel(Graphics2D g) {
        int width = getWidth();
        int height = getHeight();
        g.setColor(BORDER);
        g.drawRoundRect(8, 8, width - 16, height - 16, 18, 18);

        g.setFont(getFont().deriveFont(Font.BOLD, 15f));
        g.setColor(TEXT_DARK);
        g.drawString("Route visualization", 24, 34);

        if (result == null || !result.isRouteAvailable() || result.getRouteNodeLabels().isEmpty()) {
            g.setFont(getFont().deriveFont(13f));
            g.setColor(MUTED_TEXT);
            g.drawString("Place a request to see the fastest route diagram.", 24, 72);
            return;
        }

        g.setFont(getFont().deriveFont(12f));
        g.setColor(MUTED_TEXT);
        g.drawString("Estimated effective travel time: "
                + String.format("%.2f", result.getEffectiveTravelTime()), 24, 56);

        List<String> nodes = displayNodes(result.getRouteNodeLabels());
        int count = nodes.size();
        int left = 44;
        int right = width - 44;
        int y = 112;
        int radius = 18;
        int available = Math.max(1, right - left);

        for (int i = 0; i < count - 1; i++) {
            int x1 = left + (available * i / Math.max(1, count - 1));
            int x2 = left + (available * (i + 1) / Math.max(1, count - 1));
            g.setColor(PRIMARY_GREEN);
            g.setStroke(new BasicStroke(3f));
            g.drawLine(x1 + radius, y, x2 - radius, y);
            drawArrowHead(g, x2 - radius, y);
        }

        for (int i = 0; i < count; i++) {
            int x = left + (available * i / Math.max(1, count - 1));
            drawNode(g, nodes.get(i), x, y, radius, i, count);
        }
    }

    private void drawNode(Graphics2D g, String label, int x, int y, int radius, int index, int count) {
        boolean source = index == 0;
        boolean destination = index == count - 1;
        Color fill = destination ? DARK_GREEN : source ? PRIMARY_GREEN : LIGHT_GREEN;
        Color text = destination || source ? Color.WHITE : TEXT_DARK;
        g.setColor(fill);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g.setColor(destination || source ? fill.darker() : BORDER);
        g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

        g.setColor(text);
        g.setFont(getFont().deriveFont(Font.BOLD, 12f));
        String marker = destination ? "D" : source ? "S" : String.valueOf(index);
        drawCentered(g, marker, x, y + 5);

        g.setColor(TEXT_DARK);
        g.setFont(getFont().deriveFont(11f));
        drawCentered(g, shorten(label), x, y + 44);
    }

    private void drawArrowHead(Graphics2D g, int x, int y) {
        int[] xs = {x, x - 8, x - 8};
        int[] ys = {y, y - 5, y + 5};
        g.fillPolygon(xs, ys, 3);
    }

    private void drawCentered(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    private static List<String> displayNodes(List<String> nodes) {
        if (nodes.size() <= 6) {
            return nodes;
        }
        List<String> display = new ArrayList<>();
        display.add(nodes.get(0));
        display.add(nodes.get(1));
        display.add(nodes.get(2));
        display.add("...");
        display.add(nodes.get(nodes.size() - 2));
        display.add(nodes.get(nodes.size() - 1));
        return display;
    }

    private static String shorten(String label) {
        if (label == null || label.isBlank()) {
            return "";
        }
        String value = label;
        int paren = value.indexOf(" (");
        if (paren > 0) {
            value = value.substring(0, paren);
        }
        if (value.length() > 18) {
            return value.substring(0, 15) + "...";
        }
        return value;
    }
}
