package com.fooddelivery.gui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.QuadCurve2D;
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
        setPreferredSize(new Dimension(760, 320));
        setMinimumSize(new Dimension(620, 280));
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

        g.setFont(getFont().deriveFont(Font.BOLD, 16f));
        g.setColor(TEXT_DARK);
        g.drawString("Route visualization", 24, 36);

        if (result == null || !result.isRouteAvailable() || result.getRouteNodeLabels().isEmpty()) {
            g.setFont(getFont().deriveFont(13f));
            g.setColor(MUTED_TEXT);
            g.drawString("Place a request to see the fastest route diagram.", 24, 72);
            return;
        }

        g.setFont(getFont().deriveFont(12f));
        g.setColor(MUTED_TEXT);
        g.drawString("Estimated travel time: "
                + formatMinutes(result.getEffectiveTravelTime()), 24, 60);

        DisplayRoute displayRoute = displayRoute(result.getRouteNodeLabels(), result.getRouteEdgeTimes());
        List<String> nodes = displayRoute.nodes;
        int count = nodes.size();
        int left = 70;
        int right = width - 70;
        int centerY = Math.max(150, height / 2);
        int radius = 24;
        int available = Math.max(1, right - left);
        Point[] points = new Point[count];
        for (int i = 0; i < count; i++) {
            int x = left + (available * i / Math.max(1, count - 1));
            int offset = i % 2 == 0 ? -22 : 24;
            if (i == 0 || i == count - 1) {
                offset = 0;
            }
            points[i] = new Point(x, centerY + offset);
        }

        for (int i = 0; i < count - 1; i++) {
            Point start = points[i];
            Point end = points[i + 1];
            int controlX = (start.x + end.x) / 2;
            int controlY = Math.min(start.y, end.y) - 34;
            g.setColor(PRIMARY_GREEN);
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            QuadCurve2D curve = new QuadCurve2D.Double(
                    start.x + radius,
                    start.y,
                    controlX,
                    controlY,
                    end.x - radius,
                    end.y);
            g.draw(curve);
            drawArrowHead(g, end.x - radius, end.y);
            drawEdgeLabel(g, displayRoute.edgeLabels.get(i), controlX, controlY - 8);
        }

        for (int i = 0; i < count; i++) {
            drawNode(g, nodes.get(i), points[i].x, points[i].y, radius, i, count);
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
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        String marker = destination ? "D" : source ? "S" : shortNodeMarker(label);
        drawCentered(g, marker, x, y + 5);

        g.setColor(TEXT_DARK);
        g.setFont(getFont().deriveFont(Font.BOLD, 11f));
        drawCentered(g, shorten(label), x, y + 50);
    }

    private void drawArrowHead(Graphics2D g, int x, int y) {
        int[] xs = {x, x - 8, x - 8};
        int[] ys = {y, y - 5, y + 5};
        g.fillPolygon(xs, ys, 3);
    }

    private void drawEdgeLabel(Graphics2D g, String label, int centerX, int baselineY) {
        if (label == null || label.isBlank()) {
            return;
        }
        Font previousFont = g.getFont();
        g.setFont(getFont().deriveFont(Font.BOLD, 11f));
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(label) + 12;
        int height = 18;
        int x = centerX - width / 2;
        int y = baselineY - height + 4;
        g.setColor(new Color(0xF0FDF4));
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(new Color(0xBBF7D0));
        g.drawRoundRect(x, y, width, height, 10, 10);
        g.setColor(DARK_GREEN);
        drawCentered(g, label, centerX, baselineY);
        g.setFont(previousFont);
    }

    private void drawCentered(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    private static DisplayRoute displayRoute(List<String> nodes, List<Double> edgeTimes) {
        List<String> safeNodes = nodes == null ? List.of() : nodes;
        List<Double> safeEdgeTimes = edgeTimes == null ? List.of() : edgeTimes;
        if (safeNodes.size() <= 6) {
            return new DisplayRoute(safeNodes, directEdgeLabels(safeEdgeTimes, Math.max(0, safeNodes.size() - 1)));
        }
        List<String> display = new ArrayList<>();
        display.add(safeNodes.get(0));
        display.add(safeNodes.get(1));
        int hiddenStops = safeNodes.size() - 4;
        display.add("+" + hiddenStops + " more");
        display.add(safeNodes.get(safeNodes.size() - 2));
        display.add(safeNodes.get(safeNodes.size() - 1));

        List<String> labels = new ArrayList<>();
        labels.add(edgeLabel(safeEdgeTimes, 0));
        labels.add(combinedLabel(safeEdgeTimes, 1, safeNodes.size() - 3, hiddenStops));
        labels.add("+" + hiddenStops + " stops");
        labels.add(edgeLabel(safeEdgeTimes, safeNodes.size() - 2));
        return new DisplayRoute(display, labels);
    }

    private static List<String> directEdgeLabels(List<Double> edgeTimes, int expectedCount) {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < expectedCount; i++) {
            labels.add(edgeLabel(edgeTimes, i));
        }
        return labels;
    }

    private static String edgeLabel(List<Double> edgeTimes, int index) {
        if (index < 0 || index >= edgeTimes.size()) {
            return "";
        }
        return formatMinutes(edgeTimes.get(index));
    }

    private static String combinedLabel(List<Double> edgeTimes, int startInclusive, int endInclusive, int hiddenStops) {
        if (startInclusive < 0 || endInclusive >= edgeTimes.size() || startInclusive > endInclusive) {
            return "+" + hiddenStops + " stops";
        }
        double total = 0.0;
        for (int i = startInclusive; i <= endInclusive; i++) {
            total += edgeTimes.get(i);
        }
        return "middle: " + formatMinutes(total);
    }

    private static String formatMinutes(double value) {
        return String.format("%.1f min", value);
    }

    private static String shortNodeMarker(String label) {
        if (label == null || label.isBlank() || label.startsWith("+")) {
            return "+";
        }
        return shorten(label).substring(0, Math.min(2, shorten(label).length())).toUpperCase();
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

    private static final class DisplayRoute {
        private final List<String> nodes;
        private final List<String> edgeLabels;

        private DisplayRoute(List<String> nodes, List<String> edgeLabels) {
            this.nodes = nodes;
            this.edgeLabels = edgeLabels;
        }
    }
}
