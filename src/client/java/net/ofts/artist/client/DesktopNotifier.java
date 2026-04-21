package net.ofts.artist.client;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DesktopNotifier {
    private static TrayIcon trayIcon;
    private static boolean initialized = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopNotifier.class);

    public static void init() {
        if (initialized) return;
        initialized = true;

        if (!SystemTray.isSupported()) {
            System.out.println("[AutoLogin] System tray not supported.");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            // tiny blank icon; replace with a real png later if you want
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            trayIcon = new TrayIcon(image, "Artist");
            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);
        } catch (Exception e) {
            LOGGER.error("Failed to send notification: {}", e.getMessage());
        }
    }

    public static void notify(String title, String message) {
        if (Minecraft.getInstance().isWindowActive()) return;
        if (trayIcon == null) {
            try {
                new ProcessBuilder("notify-send", title, message).start();
            } catch (Exception ignored) {
            }
            return;
        }
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }
}