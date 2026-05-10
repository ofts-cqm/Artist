package net.ofts.artist.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.ofts.artist.client.comtroller.MovementController;
import net.ofts.artist.client.menu.MenuManager;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A watch dog that automatically stops printing when player's latency is too high (>2000ms)
 */
public class WatchDog {
    private static boolean paused = false;
    private static boolean runWatchDog = false;

    public static void start(){
        //runWatchDog = true;
    }

    public static void stop(){
        //runWatchDog = false;
    }

    private static void check(){
        try {
            if (!runWatchDog) return;

            LocalPlayer player = Minecraft.getInstance().player;
            ClientPacketListener connection = Minecraft.getInstance().getConnection();

            if (player == null || connection == null) return;

            UUID playerUuid = player.getUUID();

            // Access the player list entry for the local player
            PlayerInfo playerEntry = connection.getPlayerInfo(playerUuid);
            if (playerEntry == null) return;

            if (playerEntry.getLatency() > 2000) {
                paused = true;
                MovementController.stop();
                MenuManager.clearTaskQueue();
            } else if (paused){
                paused = false;
                MovementController.start();
            }
        } catch (Exception ignored) {}
    }

    static {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(WatchDog::check, 500, 500, TimeUnit.MILLISECONDS);
    }
}
