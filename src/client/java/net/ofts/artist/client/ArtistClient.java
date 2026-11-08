package net.ofts.artist.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.ofts.artist.client.comtroller.MovementController;
import net.ofts.artist.client.menu.MenuManager;

public class ArtistClient implements ClientModInitializer {
    private static KeyMapping keyBinding;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, a) -> Commands.buildCommand(dispatcher));
        ClientPlayConnectionEvents.DISCONNECT.register((a, b) -> {
            MovementController.pause();
            MenuManager.clearTaskQueue();
            WatchDog.stop();
        });
        ClientPlayConnectionEvents.JOIN.register((a, b, c) -> WatchDog.start());
        keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Toggle Painting",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_Y,
                KeyMapping.Category.GAMEPLAY
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.consumeClick()) MovementController.toggle();
        });
        DesktopNotifier.init();
        Config.load();
    }
}
