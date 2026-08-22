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
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

public class FoodDeliveryGui extends JFrame {
    private static final String[] URGENCY_OPTIONS = {"LOW", "MEDIUM", "HIGH"};

    private final GuiApplicationService service;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JComboBox<LocationItem> sourceCombo = new JComboBox<>();
    private final JComboBox<LocationItem> destinationCombo = new JComboBox<>();
    private final JComboBox<String> urgencyCombo = new JComboBox<>(URGENCY_OPTIONS);
    private final JTextField categoryField = new JTextField("Food Delivery", 20);
    private final JTextField capacityField = new JTextField("1.0", 10);
    private final JTextArea resultArea = new JTextArea();
    private final JTextArea summaryArea = new JTextArea();
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
        setMinimumSize(new Dimension(960, 640));
        setLocationByPlatform(true);
        buildInterface();
        loadLocations();
        refreshSummary();
        pack();
    }

    private void buildInterface() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(header(), BorderLayout.NORTH);
        tabs.addTab("Place Order", placeOrderPanel());
        tabs.addTab("Delivery Result", resultPanel());
        tabs.addTab("Recent Requests", recentRequestsPanel());
        tabs.addTab("System Summary", summaryPanel());
        content.add(tabs, BorderLayout.CENTER);
        setContentPane(content);
    }

    private JPanel header() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Food Delivery System - Team Firewall");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel subtitle = new JLabel("Visual demo for delivery requests, routes, and rider/resource assignment");
        subtitle.setFont(subtitle.getFont().deriveFont(13f));
        panel.add(title, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel placeOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        addField(form, 0, "Source/restaurant", sourceCombo);
        addField(form, 1, "Destination", destinationCombo);
        addField(form, 2, "Category", categoryField);
        addField(form, 3, "Urgency", urgencyCombo);
        addField(form, 4, "Capacity required", capacityField);

        JButton placeButton = new JButton("Place Order");
        placeButton.addActionListener(event -> placeOrder());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(placeButton);

        JTextArea note = readOnlyArea();
        note.setText("Behind the scenes, the system uses custom data structures, database records, "
                + "and algorithms such as search, sorting, Dijkstra, and rider assignment.");
        note.setRows(3);

        panel.add(form, BorderLayout.CENTER);
        panel.add(note, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel resultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setText("Place an order to see the delivery result.");
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel recentRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(recentModel);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel summaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        summaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        summaryArea.setEditable(false);
        panel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        return panel;
    }

    private void addField(JPanel panel, int row, String labelText, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_END;
        labelConstraints.insets = new Insets(8, 8, 8, 8);
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(8, 8, 8, 8);
        panel.add(component, fieldConstraints);
    }

    private JTextArea readOnlyArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
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
            showResult(result);
            refreshRecentRequests();
            tabs.setSelectedIndex(1);
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Check Order Details", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this,
                    "The GUI could not process this request: " + exception.getMessage(),
                    "Delivery Result",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showResult(GuiOrderResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("Request ID: ").append(result.getRequestId()).append('\n');
        builder.append("Source: ").append(result.getSourceLabel()).append('\n');
        builder.append("Destination: ").append(result.getDestinationLabel()).append('\n');
        builder.append("Category: ").append(result.getCategory()).append('\n');
        builder.append("Urgency: ").append(result.getUrgencyLabel()).append('\n');
        builder.append("Capacity required: ").append(String.format("%.2f", result.getCapacityRequired())).append('\n');
        builder.append("Status: ").append(result.getStatus()).append('\n');
        builder.append("Priority score: ").append(String.format("%.3f", result.getPriorityScore())).append('\n');
        builder.append('\n');
        builder.append("Fastest route: ").append(result.getRoutePath()).append('\n');
        builder.append("Estimated effective travel time: ");
        builder.append(result.isRouteAvailable() ? String.format("%.2f", result.getEffectiveTravelTime()) : "Unavailable");
        builder.append('\n');
        builder.append("Assigned rider/resource: ").append(result.getAssignedRiderLabel()).append('\n');
        builder.append('\n').append(result.getMessage());
        resultArea.setText(builder.toString());
        resultArea.setCaretPosition(0);
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

    private void refreshSummary() {
        summaryArea.setText(service.getSystemSummary()
                + "\n\nThe visual demo shows what the user sees: place a request, view a route/result, "
                + "assign a rider/resource, and confirm dataset-backed information.");
        summaryArea.setCaretPosition(0);
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
}
