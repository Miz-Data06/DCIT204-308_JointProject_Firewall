package com.fooddelivery.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public final class FoodDeliveryGuiApp {
    private FoodDeliveryGuiApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(preferredLookAndFeel());
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException exception) {
                // Fall back to the default Swing look and feel.
            }
            new FoodDeliveryGui(new GuiApplicationService()).setVisible(true);
        });
    }

    private static String preferredLookAndFeel() {
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                return info.getClassName();
            }
        }
        return UIManager.getSystemLookAndFeelClassName();
    }
}
