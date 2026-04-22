package net.ofts.artist.client.comtroller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.ofts.artist.client.Config;
import net.ofts.artist.client.DesktopNotifier;
import net.ofts.artist.client.RawKeyInjector;
import net.ofts.artist.client.menu.MenuHandler;
import net.ofts.artist.client.menu.MenuManager;

import java.util.Objects;

public class StockController {
    public static void getCarpet(Item target){
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;

        RawKeyInjector.disablePrinter();
        player.displayClientMessage(Component.literal("Not Enough Block: ").append(target.getName()), false);

        if (player.getInventory().contains(ItemStack::isEmpty)){
            MenuManager.checkMenu(MenuManager.GET_CARPET_FROM_ENDER_CHEST);
            Config.requiredItems = target.asItem();
            Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand("myx");
        }else{
            player.displayClientMessage(Component.literal("Not Enough Space in Inventory! Process Terminates!"), false);
            DesktopNotifier.notify("Artist", "Auto Painting Paused: Not Enough Block!");
        }
    }

    private static void sleep(){
        try {
            Thread.sleep(Config.MENU_WAIT_TIME);
        } catch (InterruptedException ignored) {}
    }

    public static boolean checkEnderChest(AbstractContainerScreen<?> screen){
        if (getFromEnderChest(screen)) return true;

        assert Minecraft.getInstance().player != null;
        Minecraft.getInstance().player.displayClientMessage(Component.literal("Not Enough Carpet in Ender Chest, Searching in YCK"), false);
        DesktopNotifier.notify("Artist", "Not Enough Carpet in Ender Chest, Searching in YCK");

        new Thread(() -> {
            sleep();
            Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand("yck OFTS_CQM");
            MenuManager.checkMenu(MenuManager.OPEN_YCK);
        }).start();
        return true;
    }

    private static boolean shared = false;

    public static boolean checkYCKMenu(AbstractContainerScreen<?> screen){
        MenuHandler.sendClick(screen.getMenu(), shared ? 11 : 10, ClickType.PICKUP);
        MenuManager.checkMenu(shared ? MenuManager.GET_CARPET_FROM_YCK_SHARED : MenuManager.GET_CARPET_FROM_YCK);
        return false;
    }

    private static boolean nextPage(AbstractContainerScreen<?> screen){
        ItemStack item = screen.getMenu().slots.get(53).getItem();
        if (item.is(Items.ARROW)){
            MenuHandler.sendClick(screen.getMenu(), 53, ClickType.PICKUP);
            MenuManager.checkMenu(shared ? MenuManager.GET_CARPET_FROM_YCK_SHARED : MenuManager.GET_CARPET_FROM_YCK);
            return true;
        }
        return false;
    }

    public static boolean checkYCK(AbstractContainerScreen<?> screen){
        if (getFromEnderChest(screen)) return true;
        if (nextPage(screen)) return false;

        if (shared) {
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Not Enough Item in YCK, Stopping"), false);
            DesktopNotifier.notify("Artist", "Not Enough Carpet in YCK, Stopping");
            shared = false;
        }else {
            new Thread(() -> {
                sleep();
                shared = true;
                Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand("yck OFTS_CQM");
                MenuManager.checkMenu(MenuManager.OPEN_YCK);
            }).start();
        }

        return true;
    }

    private static boolean getFromEnderChest(AbstractContainerScreen<?> screen){
        if (Config.requiredItems == null) return true;

        // count remaining
        assert Minecraft.getInstance().player != null;
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        int freeSlots = 0;

        for (ItemStack item : inventory){
            if (item.isEmpty()) freeSlots++;
        }

        if (freeSlots == 0){
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.displayClientMessage(Component.literal("No Free Slot in Inventory"), false);
            DesktopNotifier.notify("Artist", "No Free Slot in Inventory");
            shared = false;
            return true;
        }

        // ensure not filling all spaces
        int max = Math.max(3, freeSlots * 3 / 4);
        int clicked = 0;
        for (Slot slot : screen.getMenu().slots) {
            if (slot.getItem().is(Config.requiredItems)){
                MenuHandler.sendClick(screen.getMenu(), slot.getContainerSlot(), ClickType.QUICK_MOVE);
                clicked++;
                sleep();
                if (clicked >= max) break;
            }
        }

        if (clicked != 0){
            MovementController.start();
            shared = false;
            new Thread(() -> {
                sleep(); // enable printer with a delay
                RawKeyInjector.enablePrinter();
            }).start();
            return true;
        }

        return false;
    }
}
