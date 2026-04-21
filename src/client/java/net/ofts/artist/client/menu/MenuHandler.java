package net.ofts.artist.client.menu;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record MenuHandler(int id, String name, String menuMatcher, Consumer<AbstractContainerScreen<?>> slotHandler, Runnable opener) {

    public static final Logger LOGGER = LoggerFactory.getLogger("Auto Clicker");

    public void handleMenu(AbstractContainerScreen<?> screen){
        slotHandler.accept(screen);
    }

    public static void sendClick(AbstractContainerMenu menu, int slot, ClickType type){
        LOGGER.info("sending click @ slot={} to menu {}", slot, menu.containerId);

        Minecraft client = Minecraft.getInstance();
        int syncId = menu.containerId;
        int stateId = menu.getStateId();

        if (client.getConnection() == null) return;
        HashedStack carriedItem = HashedStack.create(menu.getCarried(), client.getConnection().decoratedHashOpsGenenerator());

        client.getConnection().send(new ServerboundContainerClickPacket(syncId, stateId, (short) slot, (byte) 0, type, new Int2ObjectOpenHashMap<>(), carriedItem));
    }
}
