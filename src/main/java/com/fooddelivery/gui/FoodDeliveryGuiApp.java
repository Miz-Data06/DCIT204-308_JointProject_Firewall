package com.fooddelivery.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class FoodDeliveryGuiApp {
    private FoodDeliveryGuiApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException exception) {
                // Fall back to the default Swing look and feel.
            }
            new FoodDeliveryGui(new GuiApplicationService()).setVisible(true);
        });
    }
}
