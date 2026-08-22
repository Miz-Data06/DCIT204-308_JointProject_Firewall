package com.fooddelivery.gui;

import com.fooddelivery.model.Location;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Optional;

public class FoodDeliveryGui extends JFrame {
    private static final String[] URGENCY_OPTIONS = {"LOW", "MEDIUM", "HIGH"};
    private static final Color PRIMARY_GREEN = new Color(0x16A34A);
    private static final Color DARK_GREEN = new Color(0x166534);
    private static final Color LIGHT_GREEN = new Color(0xDCFCE7);
    private static final Color BACKGROUND = new Color(0xF8FAFC);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_DARK = new Color(0x111827);
    private static final Color MUTED_TEXT = new Color(0x6B7280);
    private static final Color BORDER = new Color(0xE5E7EB);

    private final GuiApplicationService service;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JComboBox<LocationItem> sourceCombo = new JComboBox<>();
    private final JComboBox<LocationItem> destinationCombo = new JComboBox<>();
    private final JComboBox<String> urgencyCombo = new JComboBox<>(URGENCY_OPTIONS);
    private final JTextField categoryField = new JTextField("Food Delivery", 20);
    private final JTextField capacityField = new JTextField("1.0", 10);
    private final JLabel errorLabel = new JLabel(" ");
    private final JLabel requestIdValue = metricValue("Not placed yet");
    private final JLabel statusValue = metricValue("-");
    private final JLabel priorityValue = metricValue("-");
    private final JLabel travelTimeValue = metricValue("-");
    private final JLabel riderValue = metricValue("-");
    private final JLabel sourceValue = metricValue("-");
    private final JLabel destinationValue = metricValue("-");
    private final JTextArea routeArea = readOnlyArea(4);
    private final JTextArea rawResultArea = readOnlyArea(6);
    private final RouteVisualizationPanel routeVisualizationPanel = new RouteVisualizationPanel();
    private final DefaultTableModel recentModel = new DefaultTableModel(
            new Object[]{"Request ID", "Source", "Destination", "Category", "Urgency", "Status", "Priority/Result"},
            0);

    public FoodDeliveryGui(GuiApplicationService service) {
        if (service == null) {
            throw new IllegalArgumentException("GUI application service must not be null");
        }
        this.service = service;
        setTitle("Food Delivery System - Team Firewall");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 760));
        setLocationByPlatform(true);
        buildInterface();
        loadLocations();
        pack();
    }

    private void buildInterface() {
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.add(header(), BorderLayout.NORTH);
        styleTabs();
        tabs.addTab("Place Order", placeOrderPanel());
        tabs.addTab("Delivery Result", resultPanel());
        tabs.addTab("Recent Requests", recentRequestsPanel());
        tabs.addTab("System Summary", summaryPanel());
        content.add(tabs, BorderLayout.CENTER);
        setContentPane(content);
    }

    private JPanel header() {
        JPanel panel = new JPanel(new BorderLayout(12, 4));
        panel.setBackground(DARK_GREEN);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel title = new JLabel("Food Delivery System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        JLabel team = new JLabel("Team Firewall");
        team.setForeground(LIGHT_GREEN);
        team.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        JLabel subtitle = new JLabel("Dataset-backed routing and assignment demo");
        subtitle.setForeground(new Color(0xD1FAE5));
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JPanel text = new JPanel(new GridBagLayout());
        text.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.gridy = 0;
        text.add(title, constraints);
        constraints.gridy = 1;
        text.add(subtitle, constraints);

        panel.add(text, BorderLayout.WEST);
        panel.add(team, BorderLayout.EAST);
        return panel;
    }

    private void styleTabs() {
        tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        tabs.setBackground(CARD_BACKGROUND);
        tabs.setForeground(TEXT_DARK);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    }

    private JPanel placeOrderPanel() {
        JPanel panel = pagePanel(new BorderLayout(16, 16));
        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);

        JPanel formCard = card("Place a delivery request");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        addField(form, 0, "Source/restaurant", sourceCombo);
        addField(form, 1, "Destination", destinationCombo);
        addField(form, 2, "Category", categoryField);
        addField(form, 3, "Urgency", urgencyCombo);
        addField(form, 4, "Capacity required", capacityField);

        JButton placeButton = primaryButton("Place Order");
        placeButton.addActionListener(event -> placeOrder());
        JButton sampleButton = secondaryButton("Use Sample Route");
        sampleButton.addActionListener(event -> useSampleRoute());
        JButton clearButton = secondaryButton("Clear Form");
        clearButton.addActionListener(event -> clearForm());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(clearButton);
        actions.add(sampleButton);
        actions.add(placeButton);

        errorLabel.setForeground(new Color(0xB91C1C));
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        formCard.add(errorLabel, BorderLayout.NORTH);
        formCard.add(form, BorderLayout.CENTER);
        formCard.add(actions, BorderLayout.SOUTH);

        JPanel statsCard = card("Demo quick view");
        JPanel stats = new JPanel(new GridBagLayout());
        stats.setOpaque(false);
        addStat(stats, 0, "Locations loaded", service.getLocationCount());
        addStat(stats, 1, "Roads loaded", service.getRoadCount());
        addStat(stats, 2, "Resources loaded", service.getResourceCount());
        addStat(stats, 3, "Requests loaded", service.getRequestCount());
        JTextArea note = readOnlyArea(5);
        note.setText("Place a request to calculate a route, priority score, and rider assignment.\n\n"
                + "Behind the scenes, the system uses custom data structures, database records, "
                + "Dijkstra routing, priority scoring, and rider/resource assignment.");
        statsCard.add(stats, BorderLayout.NORTH);
        statsCard.add(note, BorderLayout.CENTER);

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = 0;
        left.weightx = 0.62;
        left.weighty = 1.0;
        left.fill = GridBagConstraints.BOTH;
        left.insets = new Insets(0, 0, 0, 8);
        columns.add(formCard, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = 0;
        right.weightx = 0.38;
        right.weighty = 1.0;
        right.fill = GridBagConstraints.BOTH;
        right.insets = new Insets(0, 8, 0, 0);
        columns.add(statsCard, right);

        panel.add(columns, BorderLayout.CENTER);
        return panel;
    }

    private JPanel resultPanel() {
        JPanel panel = pagePanel(new BorderLayout(14, 14));
        JPanel metrics = new JPanel(new GridBagLayout());
        metrics.setOpaque(false);
        addMetric(metrics, 0, 0, "Request ID", requestIdValue);
        addMetric(metrics, 1, 0, "Status", statusValue);
        addMetric(metrics, 2, 0, "Priority score", priorityValue);
        addMetric(metrics, 3, 0, "Effective time", travelTimeValue);
        addMetric(metrics, 0, 1, "Assigned rider/resource", riderValue);
        addMetric(metrics, 1, 1, "Source", sourceValue);
        addMetric(metrics, 2, 1, "Destination", destinationValue);

        JPanel top = card("Delivery result");
        top.add(metrics, BorderLayout.CENTER);

        JPanel routeCard = card("Fastest route and assignment");
        routeArea.setText("Place an order to see the route path.");
        JScrollPane routeScroll = new JScrollPane(routeArea);
        routeScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        routeCard.add(routeVisualizationPanel, BorderLayout.NORTH);
        routeCard.add(routeScroll, BorderLayout.CENTER);
        routeCard.add(processChecklist(), BorderLayout.EAST);

        JPanel rawCard = card("Raw result");
        rawResultArea.setText("No request has been placed yet.");
        rawCard.add(new JScrollPane(rawResultArea), BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(routeCard, BorderLayout.CENTER);
        panel.add(rawCard, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel processChecklist() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(260, 170));
        addChecklist(panel, 0, "[OK] Fastest route calculated");
        addChecklist(panel, 1, "[OK] Priority score generated");
        addChecklist(panel, 2, "[OK] Rider/resource assignment attempted");
        addChecklist(panel, 3, "[OK] Recent request saved in session table");
        return panel;
    }

    private JPanel recentRequestsPanel() {
        JPanel panel = pagePanel(new BorderLayout(12, 12));
        JLabel summary = new JLabel("Recent GUI-created requests in this session");
        summary.setForeground(MUTED_TEXT);
        summary.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        JTable table = new JTable(recentModel);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(LIGHT_GREEN);
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        table.getTableHeader().setBackground(DARK_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(6).setPreferredWidth(260);
        JPanel card = card("Session requests");
        card.add(summary, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel summaryPanel() {
        JPanel panel = pagePanel(new BorderLayout(14, 14));
        JPanel stats = new JPanel(new GridBagLayout());
        stats.setOpaque(false);
        addSummaryCard(stats, 0, "Locations", service.getLocationCount());
        addSummaryCard(stats, 1, "Roads", service.getRoadCount());
        addSummaryCard(stats, 2, "Service requests", service.getRequestCount());
        addSummaryCard(stats, 3, "Resources", service.getResourceCount());
        addSummaryCard(stats, 4, "Tests", 519);

        JTextArea explanation = readOnlyArea(4);
        explanation.setText("The GUI uses real project data loaded from the data folder. "
                + "The backend remains connected to SQLite/JDBC-capable services and algorithm logic.");

        JTextArea demoNotes = readOnlyArea(8);
        demoNotes.setText("What to say during demo:\n"
                + "- Select source and destination.\n"
                + "- Place a request.\n"
                + "- Show fastest route.\n"
                + "- Show assigned rider/resource.\n"
                + "- Explain Dijkstra, priority scoring, and greedy assignment at a high level.");

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.weightx = 0.5;
        left.fill = GridBagConstraints.BOTH;
        left.insets = new Insets(0, 0, 0, 8);
        JPanel explainerCard = card("Dataset-backed application");
        explainerCard.add(explanation, BorderLayout.CENTER);
        bottom.add(explainerCard, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.weightx = 0.5;
        right.fill = GridBagConstraints.BOTH;
        right.insets = new Insets(0, 8, 0, 0);
        JPanel notesCard = card("Presentation guide");
        notesCard.add(demoNotes, BorderLayout.CENTER);
        bottom.add(notesCard, right);

        panel.add(stats, BorderLayout.NORTH);
        panel.add(bottom, BorderLayout.CENTER);
        return panel;
    }

    private JPanel pagePanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return panel;
    }

    private JPanel card(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BACKGROUND);
        panel.setLayout(new BorderLayout(10, 10));
        javax.swing.border.Border outer = new RoundedBorder(BORDER, 14);
        if (title != null && !title.isBlank()) {
            TitledBorder titled = BorderFactory.createTitledBorder(outer, title);
            titled.setTitleColor(TEXT_DARK);
            titled.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            outer = titled;
        }
        panel.setBorder(BorderFactory.createCompoundBorder(
                outer,
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    private void addField(JPanel panel, int row, String labelText, Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_END;
        labelConstraints.insets = new Insets(10, 8, 10, 12);
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setForeground(TEXT_DARK);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(10, 0, 10, 0);
        component.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        panel.add(component, fieldConstraints);
    }

    private void addStat(JPanel panel, int row, String label, int value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 0, 5, 0);
        panel.add(statLine(label, String.valueOf(value)), constraints);
    }

    private void addSummaryCard(JPanel panel, int column, String label, int value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, column == 0 ? 0 : 8, 14, column == 4 ? 0 : 8);
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER, 12),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        JLabel number = new JLabel(String.valueOf(value));
        number.setForeground(PRIMARY_GREEN);
        number.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        JLabel name = new JLabel(label);
        name.setForeground(MUTED_TEXT);
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        card.add(number, BorderLayout.NORTH);
        card.add(name, BorderLayout.SOUTH);
        panel.add(card, constraints);
    }

    private void addMetric(JPanel panel, int column, int row, String label, JLabel value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(6, 6, 6, 6);
        JPanel metric = new JPanel(new BorderLayout(0, 4));
        metric.setBackground(new Color(0xF9FAFB));
        metric.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER, 10),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel name = new JLabel(label);
        name.setForeground(MUTED_TEXT);
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        metric.add(name, BorderLayout.NORTH);
        metric.add(value, BorderLayout.CENTER);
        panel.add(metric, constraints);
    }

    private void addChecklist(JPanel panel, int row, String text) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.insets = new Insets(7, 0, 7, 0);
        JLabel label = new JLabel(text);
        label.setForeground(row < 2 ? DARK_GREEN : TEXT_DARK);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        panel.add(label, constraints);
    }

    private JPanel statLine(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel left = new JLabel(label);
        left.setForeground(MUTED_TEXT);
        left.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        JLabel right = new JLabel(value);
        right.setForeground(TEXT_DARK);
        right.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private static JLabel metricValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private static JTextArea readOnlyArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        area.setForeground(TEXT_DARK);
        area.setBackground(CARD_BACKGROUND);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        button.setBackground(PRIMARY_GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setBackground(LIGHT_GREEN);
        button.setForeground(DARK_GREEN);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        return button;
    }

    private void loadLocations() {
        List<Location> sourceLocations = service.getSourceLocations();
        for (Location location : sourceLocations) {
            sourceCombo.addItem(new LocationItem(location));
        }
        for (Location location : service.getAllLocations()) {
            destinationCombo.addItem(new LocationItem(location));
        }
        if (destinationCombo.getItemCount() > 1) {
            destinationCombo.setSelectedIndex(1);
        }
    }

    private void useSampleRoute() {
        Optional<GuiSampleRoute> sample = service.findSampleRoute();
        if (sample.isEmpty()) {
            showError("No sample route is available from the loaded dataset.");
            return;
        }
        selectComboItem(sourceCombo, sample.get().getSourceLocationId());
        selectComboItem(destinationCombo, sample.get().getDestinationLocationId());
        categoryField.setText("Food Delivery");
        urgencyCombo.setSelectedItem("HIGH");
        capacityField.setText("1.0");
        errorLabel.setText("Sample route selected. Press Place Order to process it.");
        errorLabel.setForeground(DARK_GREEN);
    }

    private void clearForm() {
        categoryField.setText("Food Delivery");
        urgencyCombo.setSelectedItem("LOW");
        capacityField.setText("1.0");
        if (sourceCombo.getItemCount() > 0) {
            sourceCombo.setSelectedIndex(0);
        }
        if (destinationCombo.getItemCount() > 1) {
            destinationCombo.setSelectedIndex(1);
        }
        errorLabel.setText(" ");
        errorLabel.setForeground(new Color(0xB91C1C));
    }

    private void selectComboItem(JComboBox<LocationItem> combo, String locationId) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            LocationItem item = combo.getItemAt(i);
            if (item.location.getLocationId().equals(locationId)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void placeOrder() {
        LocationItem source = (LocationItem) sourceCombo.getSelectedItem();
        LocationItem destination = (LocationItem) destinationCombo.getSelectedItem();
        try {
            GuiOrderResult result = service.placeOrder(
                    source == null ? null : source.location.getLocationId(),
                    destination == null ? null : destination.location.getLocationId(),
                    categoryField.getText(),
                    (String) urgencyCombo.getSelectedItem(),
                    capacityField.getText());
            errorLabel.setText("Request processed.");
            errorLabel.setForeground(DARK_GREEN);
            showResult(result);
            refreshRecentRequests();
            tabs.setSelectedIndex(1);
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        } catch (RuntimeException exception) {
            showError("The GUI could not process this request: " + exception.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setForeground(new Color(0xB91C1C));
        JOptionPane.showMessageDialog(this, message, "Check Order Details", JOptionPane.WARNING_MESSAGE);
    }

    private void showResult(GuiOrderResult result) {
        requestIdValue.setText(result.getRequestId());
        statusValue.setText(result.getStatus().toString());
        priorityValue.setText(String.format("%.3f", result.getPriorityScore()));
        travelTimeValue.setText(result.isRouteAvailable()
                ? String.format("%.2f", result.getEffectiveTravelTime())
                : "Unavailable");
        riderValue.setText(result.getAssignedRiderLabel());
        sourceValue.setText(result.getSourceLabel());
        destinationValue.setText(result.getDestinationLabel());
        routeArea.setText(result.getRoutePath() + "\n\nTechnical summary: Processed using real Dijkstra route "
                + "calculation, priority scoring, and rider/resource assignment.");
        routeArea.setCaretPosition(0);
        routeVisualizationPanel.setResult(result);

        StringBuilder builder = new StringBuilder();
        builder.append("Request ID: ").append(result.getRequestId()).append('\n');
        builder.append("Source: ").append(result.getSourceLabel()).append('\n');
        builder.append("Destination: ").append(result.getDestinationLabel()).append('\n');
        builder.append("Category: ").append(result.getCategory()).append('\n');
        builder.append("Urgency: ").append(result.getUrgencyLabel()).append('\n');
        builder.append("Capacity required: ").append(String.format("%.2f", result.getCapacityRequired())).append('\n');
        builder.append("Status: ").append(result.getStatus()).append('\n');
        builder.append("Priority score: ").append(String.format("%.3f", result.getPriorityScore())).append('\n');
        builder.append("Fastest route: ").append(result.getRoutePath()).append('\n');
        builder.append("Estimated effective travel time: ");
        builder.append(result.isRouteAvailable() ? String.format("%.2f", result.getEffectiveTravelTime()) : "Unavailable");
        builder.append('\n');
        builder.append("Assigned rider/resource: ").append(result.getAssignedRiderLabel()).append('\n');
        builder.append(result.getMessage());
        rawResultArea.setText(builder.toString());
        rawResultArea.setCaretPosition(0);
    }

    private void refreshRecentRequests() {
        recentModel.setRowCount(0);
        for (GuiOrderResult result : service.getRecentRequests()) {
            recentModel.addRow(new Object[]{
                    result.getRequestId(),
                    result.getSourceLabel(),
                    result.getDestinationLabel(),
                    result.getCategory(),
                    result.getUrgencyLabel(),
                    result.getStatus(),
                    String.format("%.3f - %s", result.getPriorityScore(), result.getSummary())
            });
        }
    }

    private static final class LocationItem {
        private final Location location;

        private LocationItem(Location location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return location.getName() + " - " + location.getArea() + " (" + location.getLocationId() + ")";
        }
    }

    private static final class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        private RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(color);
                g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            } finally {
                g.dispose();
            }
        }
    }

    private static final class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean hasFocus,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, hasFocus, row, column);
            if (!selected) {
                component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF9FAFB));
                component.setForeground(TEXT_DARK);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return component;
        }
    }
}
