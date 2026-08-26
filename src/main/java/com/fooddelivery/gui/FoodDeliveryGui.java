package com.fooddelivery.gui;

import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;

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
import javax.swing.Icon;
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
import java.awt.BasicStroke;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FoodDeliveryGui extends JFrame {
    private static final String[] URGENCY_OPTIONS = {"LOW", "MEDIUM", "HIGH"};
    private static final DateTimeFormatter QUEUE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
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
    private final JComboBox<OrderSizeItem> orderSizeCombo = new JComboBox<>(new OrderSizeItem[]{
            new OrderSizeItem("Small order", 0.5),
            new OrderSizeItem("Normal order", 1.0),
            new OrderSizeItem("Large order", 2.0),
            new OrderSizeItem("Bulk order", 3.0)
    });
    private final JLabel errorLabel = new JLabel(" ");
    private final JLabel requestIdValue = metricValue("Not placed yet");
    private final JLabel statusValue = metricValue("-");
    private final JLabel priorityValue = metricValue("-");
    private final JLabel travelTimeValue = metricValue("-");
    private final JLabel riderValue = metricValue("-");
    private final JLabel sourceValue = metricValue("-");
    private final JLabel destinationValue = metricValue("-");
    private final JLabel assignmentRiderValue = metricValue("-");
    private final JLabel assignmentStatusValue = metricValue("-");
    private final JLabel assignmentTimeValue = metricValue("-");
    private final JLabel assignmentPriorityValue = metricValue("-");
    private final JTextArea routeArea = readOnlyArea(5);
    private final JTextArea technicalDetailsArea = readOnlyArea(6);
    private final JPanel technicalDetailsPanel = card("Technical details");
    private final RouteVisualizationPanel routeVisualizationPanel = new RouteVisualizationPanel();
    private final JLabel queueEmptyLabel = new JLabel(
            "No queued requests yet. Place an order to see it ranked for dispatch.",
            SwingConstants.CENTER);
    private final DefaultTableModel recentModel = new DefaultTableModel(
            new Object[]{
                    "Queue #",
                    "Request ID",
                    "Submitted",
                    "Source",
                    "Destination",
                    "Urgency",
                    "Status",
                    "Priority",
                    "Assignment"
            },
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
        tabs.addTab("Dispatch Queue", recentRequestsPanel());
        tabs.addTab("System Overview", summaryPanel());
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
        JPanel panel = pagePanel(new BorderLayout(18, 18));

        JPanel formCard = card("Place delivery request");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 70, 10, 70));
        addField(form, 0, "Source/restaurant", sourceCombo);
        addField(form, 1, "Destination", destinationCombo);
        addField(form, 2, "Category", categoryField);
        addField(form, 3, "Urgency", urgencyCombo);
        addField(form, 4, "Order size", orderSizeCombo);

        JTextArea note = readOnlyArea(3);
        note.setBackground(new Color(0xF0FDF4));
        note.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(0xBBF7D0), 12),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        note.setText("Create a delivery request, calculate the fastest route, generate a priority score, "
                + "and assign a rider/resource using the real project backend.");

        JButton placeButton = primaryButton("Place Order");
        placeButton.setPreferredSize(new Dimension(160, 44));
        placeButton.addActionListener(event -> placeOrder());
        JButton sampleButton = secondaryButton("Use Sample Route");
        sampleButton.addActionListener(event -> useSampleRoute());
        JButton clearButton = secondaryButton("Clear Form");
        clearButton.addActionListener(event -> clearForm());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setOpaque(false);
        actions.add(clearButton);
        actions.add(sampleButton);
        actions.add(placeButton);

        errorLabel.setForeground(new Color(0xB91C1C));
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        JLabel heading = new JLabel("Order details");
        heading.setForeground(DARK_GREEN);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        top.add(heading, BorderLayout.NORTH);
        top.add(note, BorderLayout.CENTER);
        top.add(errorLabel, BorderLayout.SOUTH);

        formCard.add(top, BorderLayout.NORTH);
        formCard.add(form, BorderLayout.CENTER);
        formCard.add(actions, BorderLayout.SOUTH);

        panel.add(formCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel resultPanel() {
        JPanel panel = pagePanel(new BorderLayout(14, 14));
        JPanel metrics = new JPanel(new GridBagLayout());
        metrics.setOpaque(false);
        addMetric(metrics, 0, 0, "Request ID", requestIdValue);
        addMetric(metrics, 1, 0, "Status", statusValue);
        addMetric(metrics, 2, 0, "Priority score", priorityValue);
        addMetric(metrics, 3, 0, "Estimated travel time", travelTimeValue);
        addMetric(metrics, 0, 1, "Assigned rider/resource", riderValue);
        addMetric(metrics, 1, 1, "Source", sourceValue);
        addMetric(metrics, 2, 1, "Destination", destinationValue);

        JPanel top = card("Delivery result");
        top.add(metrics, BorderLayout.CENTER);

        JPanel routeCard = card("Fastest route and assignment");
        routeArea.setText("Place an order to see the route path.");
        JScrollPane routeScroll = new JScrollPane(routeArea);
        routeScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        routeScroll.setPreferredSize(new Dimension(900, 110));
        JPanel routeMain = new JPanel(new BorderLayout(0, 10));
        routeMain.setOpaque(false);
        routeMain.add(routeVisualizationPanel, BorderLayout.CENTER);
        routeMain.add(routeScroll, BorderLayout.SOUTH);
        routeCard.add(routeMain, BorderLayout.CENTER);
        routeCard.add(assignmentSummaryCard(), BorderLayout.EAST);

        technicalDetailsPanel.add(new JScrollPane(technicalDetailsArea), BorderLayout.CENTER);
        technicalDetailsPanel.setVisible(false);
        JButton detailsButton = secondaryButton("Show technical details");
        detailsButton.addActionListener(event -> toggleTechnicalDetails(detailsButton));
        JPanel detailsControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        detailsControls.setOpaque(false);
        detailsControls.add(detailsButton);
        JPanel detailsArea = new JPanel(new BorderLayout(0, 8));
        detailsArea.setOpaque(false);
        detailsArea.add(detailsControls, BorderLayout.NORTH);
        detailsArea.add(technicalDetailsPanel, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(routeCard, BorderLayout.CENTER);
        panel.add(detailsArea, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel recentRequestsPanel() {
        JPanel panel = pagePanel(new BorderLayout(12, 12));
        JLabel summary = new JLabel("Outgoing queue ordered by priority, then request time");
        summary.setForeground(MUTED_TEXT);
        summary.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        JTable table = new JTable(recentModel);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(LIGHT_GREEN);
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        table.getTableHeader().setBackground(DARK_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(170);
        table.getColumnModel().getColumn(4).setPreferredWidth(170);
        table.getColumnModel().getColumn(8).setPreferredWidth(220);
        queueEmptyLabel.setForeground(MUTED_TEXT);
        queueEmptyLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        queueEmptyLabel.setBorder(BorderFactory.createEmptyBorder(22, 12, 22, 12));
        JScrollPane tableScroll = new JScrollPane(table);
        JPanel queueContent = new JPanel(new BorderLayout(0, 8));
        queueContent.setOpaque(false);
        queueContent.add(queueEmptyLabel, BorderLayout.NORTH);
        queueContent.add(tableScroll, BorderLayout.CENTER);
        updateDispatchQueueEmptyState();

        JPanel card = card("Dispatch queue");
        card.add(summary, BorderLayout.NORTH);
        card.add(queueContent, BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel summaryPanel() {
        JPanel panel = pagePanel(new BorderLayout(14, 14));

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        JLabel heading = new JLabel("System Overview");
        heading.setForeground(TEXT_DARK);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        JLabel subtitle = new JLabel("Live operational snapshot for delivery requests, riders, and the road network.");
        subtitle.setForeground(MUTED_TEXT);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        JPanel title = new JPanel(new BorderLayout(0, 2));
        title.setOpaque(false);
        title.add(heading, BorderLayout.NORTH);
        title.add(subtitle, BorderLayout.SOUTH);
        top.add(title, BorderLayout.NORTH);
        top.add(statusBanner(), BorderLayout.CENTER);
        top.add(statGrid(), BorderLayout.SOUTH);

        JPanel dashboard = new JPanel(new GridBagLayout());
        dashboard.setOpaque(false);
        addDashboardSection(dashboard, 0, 0, 0.58, recentDeliveryRequestsCard());
        addDashboardSection(dashboard, 1, 0, 0.42, availableRidersCard());
        addDashboardSection(dashboard, 0, 1, 0.58, roadNetworkStatusCard());
        addDashboardSection(dashboard, 1, 1, 0.42, developerInfoCard());

        JScrollPane scrollPane = new JScrollPane(dashboard);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND);
        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
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
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.weightx = 0.0;
        labelConstraints.insets = new Insets(14, 0, 14, 20);
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setForeground(TEXT_DARK);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        label.setPreferredSize(new Dimension(170, 36));
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(14, 0, 14, 0);
        component.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        component.setPreferredSize(new Dimension(620, 38));
        component.setMinimumSize(new Dimension(360, 38));
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

    private JPanel statGrid() {
        JPanel stats = new JPanel(new GridBagLayout());
        stats.setOpaque(false);
        addSummaryCard(stats, 0, "Locations", service.getLocationCount(), "pin");
        addSummaryCard(stats, 1, "Roads", service.getRoadCount(), "road");
        addSummaryCard(stats, 2, "Service Requests", service.getRequestCount(), "bag");
        addSummaryCard(stats, 3, "Riders", service.getResourceCount(), "rider");
        return stats;
    }

    private void addSummaryCard(JPanel panel, int column, String label, int value, String iconType) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, column == 0 ? 0 : 8, 14, column == 3 ? 0 : 8);
        JPanel card = new ShadowPanel(new BorderLayout(10, 0));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER, 12),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel icon = new JLabel(new DashboardIcon(iconType, PRIMARY_GREEN, LIGHT_GREEN));
        JLabel number = new JLabel(String.valueOf(value));
        number.setForeground(PRIMARY_GREEN);
        number.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        JLabel name = new JLabel(label);
        name.setForeground(MUTED_TEXT);
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        JPanel text = new JPanel(new BorderLayout(0, 2));
        text.setOpaque(false);
        text.add(number, BorderLayout.NORTH);
        text.add(name, BorderLayout.SOUTH);
        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        panel.add(card, constraints);
    }

    private JPanel statusBanner() {
        JPanel banner = new JPanel(new BorderLayout(10, 0));
        banner.setBackground(new Color(0xECFDF5));
        banner.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(0xA7F3D0), 14),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel status = new JLabel("Dataset loaded and dispatch system ready");
        status.setForeground(DARK_GREEN);
        status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        JLabel detail = new JLabel(service.getLocationCount() + " locations, "
                + service.getRoadCount() + " roads, "
                + service.getAvailableRiderCount() + " available riders");
        detail.setForeground(MUTED_TEXT);
        detail.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        JPanel text = new JPanel(new BorderLayout(0, 2));
        text.setOpaque(false);
        text.add(status, BorderLayout.NORTH);
        text.add(detail, BorderLayout.SOUTH);
        banner.add(new JLabel(new DashboardIcon("check", DARK_GREEN, new Color(0xD1FAE5))), BorderLayout.WEST);
        banner.add(text, BorderLayout.CENTER);
        return banner;
    }

    private JPanel recentDeliveryRequestsCard() {
        JPanel panel = card("Recent Delivery Requests");
        String[] columns = {"Request", "Route", "Status", "Priority"};
        Object[][] rows = recentRequestRows();
        JTable table = dashboardTable(columns, rows);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private Object[][] recentRequestRows() {
        List<GuiOrderResult> guiRequests = service.getRecentRequests();
        if (!guiRequests.isEmpty()) {
            int count = Math.min(5, guiRequests.size());
            Object[][] rows = new Object[count][4];
            for (int i = 0; i < count; i++) {
                GuiOrderResult request = guiRequests.get(i);
                rows[i] = new Object[]{
                        request.getRequestId(),
                        shortLocation(request.getSourceLabel()) + " to " + shortLocation(request.getDestinationLabel()),
                        request.getStatus(),
                        String.format("%.3f", request.getPriorityScore())
                };
            }
            return rows;
        }

        List<DeliveryRequest> requests = service.getDatasetRequests();
        int count = Math.min(5, requests.size());
        Object[][] rows = new Object[count][4];
        for (int i = 0; i < count; i++) {
            DeliveryRequest request = requests.get(i);
            rows[i] = new Object[]{
                    request.getRequestId(),
                    request.getSourceLocationId() + " to " + request.getDestinationLocationId(),
                    request.getStatus(),
                    String.format("%.3f", request.getPriorityScore())
            };
        }
        return rows;
    }

    private JPanel availableRidersCard() {
        JPanel panel = card("Available Riders");
        String[] columns = {"Rider", "Vehicle", "Load limit"};
        List<Rider> riders = service.getRiders();
        List<Rider> available = new ArrayList<>();
        for (Rider rider : riders) {
            if (rider.isAvailable()) {
                available.add(rider);
            }
        }
        int count = Math.min(5, available.size());
        Object[][] rows = new Object[count][3];
        for (int i = 0; i < count; i++) {
            Rider rider = available.get(i);
            rows[i] = new Object[]{
                    rider.getName(),
                    rider.getVehicleType(),
                    formatLoadLimit(rider.getCarryingCapacity())
            };
        }
        panel.add(new JScrollPane(dashboardTable(columns, rows)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel roadNetworkStatusCard() {
        JPanel panel = card("Road Network Status");
        JPanel content = new JPanel(new BorderLayout(12, 0));
        content.setOpaque(false);
        content.add(new NetworkPreviewPanel(service.getLocationCount(), service.getRoadCount()), BorderLayout.WEST);

        List<Road> roads = service.getRoads();
        double totalDistance = 0.0;
        double totalTravelTime = 0.0;
        for (Road road : roads) {
            totalDistance += road.getDistanceKm();
            totalTravelTime += road.getEffectiveTime();
        }
        JPanel metrics = new JPanel(new GridBagLayout());
        metrics.setOpaque(false);
        addNetworkMetric(metrics, 0, "Total distance", String.format("%.1f km", totalDistance));
        addNetworkMetric(metrics, 1, "Average road time",
                roads.isEmpty() ? "0.0 min" : String.format("%.1f min", totalTravelTime / roads.size()));
        addNetworkMetric(metrics, 2, "Network status", roads.isEmpty() ? "Needs data" : "Connected data loaded");
        content.add(metrics, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel developerInfoCard() {
        JPanel panel = card("Developer/System Information");
        JPanel testCard = new ShadowPanel(new BorderLayout(10, 0));
        testCard.setBackground(CARD_BACKGROUND);
        testCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER, 12),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel tests = new JLabel("519");
        tests.setForeground(PRIMARY_GREEN);
        tests.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        JLabel label = new JLabel("Core Tests");
        label.setForeground(MUTED_TEXT);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        JPanel testText = new JPanel(new BorderLayout(0, 2));
        testText.setOpaque(false);
        testText.add(tests, BorderLayout.NORTH);
        testText.add(label, BorderLayout.SOUTH);
        testCard.add(new JLabel(new DashboardIcon("check", PRIMARY_GREEN, LIGHT_GREEN)), BorderLayout.WEST);
        testCard.add(testText, BorderLayout.CENTER);

        JTextArea details = readOnlyArea(5);
        details.setText("Data source: project dataset with SQLite/JDBC-capable integration\n"
                + "Algorithms: fastest route, priority scoring, rider/resource assignment\n"
                + "Dashboard values come from the loaded dataset.");
        panel.add(testCard, BorderLayout.NORTH);
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private void addDashboardSection(JPanel panel, int column, int row, double weightx, JPanel section) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.weightx = weightx;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(row == 0 ? 0 : 10, column == 0 ? 0 : 10, 0, column == 0 ? 10 : 0);
        panel.add(section, constraints);
    }

    private JTable dashboardTable(String[] columns, Object[][] rows) {
        JTable table = new JTable(new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xEEF2F7));
        table.setSelectionBackground(LIGHT_GREEN);
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0xF3F4F6));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        return table;
    }

    private void addNetworkMetric(JPanel panel, int row, String label, String value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(row == 0 ? 0 : 8, 0, 0, 0);
        panel.add(statLine(label, value), constraints);
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

    private JPanel assignmentSummaryCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 310));
        JPanel inner = card("Assignment summary");
        inner.add(summaryMetric("Assigned rider/resource", assignmentRiderValue), BorderLayout.NORTH);

        JPanel values = new JPanel(new GridBagLayout());
        values.setOpaque(false);
        addCompactMetric(values, 0, "Status", assignmentStatusValue);
        addCompactMetric(values, 1, "Estimated travel time", assignmentTimeValue);
        addCompactMetric(values, 2, "Priority score", assignmentPriorityValue);
        inner.add(values, BorderLayout.CENTER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(inner, constraints);
        return panel;
    }

    private JPanel summaryMetric(String label, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel name = new JLabel(label);
        name.setForeground(MUTED_TEXT);
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(name, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private void addCompactMetric(JPanel panel, int row, String label, JLabel value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(8, 0, 8, 0);
        panel.add(summaryMetric(label, value), constraints);
    }

    private void toggleTechnicalDetails(JButton button) {
        boolean show = !technicalDetailsPanel.isVisible();
        technicalDetailsPanel.setVisible(show);
        button.setText(show ? "Hide technical details" : "Show technical details");
        revalidate();
        repaint();
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
        orderSizeCombo.setSelectedIndex(1);
        errorLabel.setText("Sample route selected. Press Place Order to process it.");
        errorLabel.setForeground(DARK_GREEN);
    }

    private void clearForm() {
        categoryField.setText("Food Delivery");
        urgencyCombo.setSelectedItem("LOW");
        orderSizeCombo.setSelectedIndex(1);
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
        OrderSizeItem orderSize = (OrderSizeItem) orderSizeCombo.getSelectedItem();
        try {
            GuiOrderResult result = service.placeOrder(
                    source == null ? null : source.location.getLocationId(),
                    destination == null ? null : destination.location.getLocationId(),
                    categoryField.getText(),
                    (String) urgencyCombo.getSelectedItem(),
                    orderSize == null ? null : String.valueOf(orderSize.capacity));
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
                ? formatMinutes(result.getEffectiveTravelTime())
                : "Unavailable");
        riderValue.setText(result.getAssignedRiderLabel());
        sourceValue.setText(result.getSourceLabel());
        destinationValue.setText(result.getDestinationLabel());
        assignmentRiderValue.setText(result.getAssignedRiderLabel());
        assignmentStatusValue.setText(result.getStatus().toString());
        assignmentTimeValue.setText(result.isRouteAvailable()
                ? formatMinutes(result.getEffectiveTravelTime())
                : "Unavailable");
        assignmentPriorityValue.setText(String.format("%.3f", result.getPriorityScore()));
        routeArea.setText("Route path\n" + result.getRoutePath()
                + "\n\nProcessed using real fastest-route calculation, priority scoring, and rider/resource assignment.");
        routeArea.setCaretPosition(0);
        routeVisualizationPanel.setResult(result);
        technicalDetailsArea.setText(technicalDetailsText(result));
        technicalDetailsArea.setCaretPosition(0);

    }

    private String technicalDetailsText(GuiOrderResult result) {
        return "Request ID: " + result.getRequestId()
                + "\nSource: " + result.getSourceLabel()
                + "\nDestination: " + result.getDestinationLabel()
                + "\nCategory: " + result.getCategory()
                + "\nUrgency: " + result.getUrgencyLabel()
                + "\nInternal order capacity: " + String.format("%.2f", result.getCapacityRequired())
                + "\nStatus: " + result.getStatus()
                + "\nPriority score: " + String.format("%.3f", result.getPriorityScore())
                + "\nFastest route: " + result.getRoutePath()
                + "\nEstimated travel time: "
                + (result.isRouteAvailable() ? String.format("%.2f", result.getEffectiveTravelTime()) : "Unavailable")
                + "\nAssigned rider/resource: " + result.getAssignedRiderLabel()
                + "\n" + result.getMessage();
    }

    private static String formatMinutes(double value) {
        return String.format("%.1f min", value);
    }

    private static String formatLoadLimit(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.format("%.0f units", value);
        }
        return String.format("%.1f units", value);
    }

    private void refreshRecentRequests() {
        recentModel.setRowCount(0);
        List<GuiOrderResult> queue = new ArrayList<>(service.getRecentRequests());
        queue.sort(Comparator.comparingDouble(GuiOrderResult::getPriorityScore).reversed()
                .thenComparing(GuiOrderResult::getTimeSubmitted)
                .thenComparing(GuiOrderResult::getRequestId));
        for (int i = 0; i < queue.size(); i++) {
            GuiOrderResult result = queue.get(i);
            recentModel.addRow(new Object[]{
                    i + 1,
                    result.getRequestId(),
                    result.getTimeSubmitted() == null ? "-" : QUEUE_TIME_FORMAT.format(result.getTimeSubmitted()),
                    result.getSourceLabel(),
                    result.getDestinationLabel(),
                    result.getUrgencyLabel(),
                    result.getStatus(),
                    String.format("%.3f", result.getPriorityScore()),
                    result.getAssignedRiderLabel()
            });
        }
        updateDispatchQueueEmptyState();
    }

    private void updateDispatchQueueEmptyState() {
        queueEmptyLabel.setVisible(recentModel.getRowCount() == 0);
    }

    private static String shortLocation(String label) {
        if (label == null || label.isBlank()) {
            return "-";
        }
        int paren = label.indexOf(" (");
        String value = paren > 0 ? label.substring(0, paren) : label;
        return value.length() > 20 ? value.substring(0, 17) + "..." : value;
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

    private static final class OrderSizeItem {
        private final String label;
        private final double capacity;

        private OrderSizeItem(String label, double capacity) {
            this.label = label;
            this.capacity = capacity;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class ShadowPanel extends JPanel {
        private ShadowPanel(java.awt.LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(0x11000000, true));
                g.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 8, 14, 14);
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 14, 14);
            } finally {
                g.dispose();
            }
            super.paintComponent(graphics);
        }
    }

    private static final class DashboardIcon implements Icon {
        private final String type;
        private final Color stroke;
        private final Color fill;

        private DashboardIcon(String type, Color stroke, Color fill) {
            this.type = type;
            this.stroke = stroke;
            this.fill = fill;
        }

        @Override
        public int getIconWidth() {
            return 42;
        }

        @Override
        public int getIconHeight() {
            return 42;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill);
                g.fillRoundRect(x, y, 42, 42, 14, 14);
                g.setColor(stroke);
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = x + 21;
                int cy = y + 21;
                switch (type) {
                    case "pin" -> {
                        g.drawOval(cx - 7, cy - 12, 14, 14);
                        g.drawLine(cx, cy + 2, cx, cy + 12);
                    }
                    case "road" -> {
                        g.drawLine(cx - 12, cy + 12, cx - 4, cy - 12);
                        g.drawLine(cx + 12, cy + 12, cx + 4, cy - 12);
                        g.drawLine(cx, cy + 8, cx, cy + 3);
                        g.drawLine(cx, cy - 2, cx, cy - 7);
                    }
                    case "bag" -> {
                        g.drawRoundRect(cx - 11, cy - 5, 22, 18, 5, 5);
                        g.drawArc(cx - 7, cy - 13, 14, 14, 0, 180);
                    }
                    case "rider" -> {
                        g.drawOval(cx - 11, cy + 5, 8, 8);
                        g.drawOval(cx + 5, cy + 5, 8, 8);
                        g.drawLine(cx - 7, cy + 5, cx, cy - 5);
                        g.drawLine(cx, cy - 5, cx + 9, cy + 5);
                        g.drawLine(cx - 2, cy - 9, cx + 7, cy - 9);
                    }
                    default -> {
                        g.drawLine(cx - 10, cy, cx - 2, cy + 8);
                        g.drawLine(cx - 2, cy + 8, cx + 12, cy - 8);
                    }
                }
            } finally {
                g.dispose();
            }
        }
    }

    private static final class NetworkPreviewPanel extends JPanel {
        private final int locationCount;
        private final int roadCount;

        private NetworkPreviewPanel(int locationCount, int roadCount) {
            this.locationCount = locationCount;
            this.roadCount = roadCount;
            setOpaque(false);
            setPreferredSize(new Dimension(220, 150));
            setMinimumSize(new Dimension(180, 130));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g.setColor(new Color(0xF8FAFC));
                g.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g.setColor(new Color(0xE5E7EB));
                g.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

                int[][] points = {
                        {28, h - 36},
                        {w / 3, 42},
                        {w / 2, h - 54},
                        {w - 54, 48},
                        {w - 30, h - 38}
                };
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(PRIMARY_GREEN);
                for (int i = 0; i < points.length - 1; i++) {
                    g.drawLine(points[i][0], points[i][1], points[i + 1][0], points[i + 1][1]);
                }
                for (int i = 0; i < points.length; i++) {
                    boolean end = i == 0 || i == points.length - 1;
                    g.setColor(end ? DARK_GREEN : LIGHT_GREEN);
                    g.fillOval(points[i][0] - 9, points[i][1] - 9, 18, 18);
                    g.setColor(end ? DARK_GREEN.darker() : PRIMARY_GREEN);
                    g.drawOval(points[i][0] - 9, points[i][1] - 9, 18, 18);
                }
                g.setColor(MUTED_TEXT);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g.drawString(locationCount + " locations", 14, 22);
                g.drawString(roadCount + " roads", 14, h - 14);
            } finally {
                g.dispose();
            }
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
