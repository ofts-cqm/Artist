package net.ofts.artist.client.comtroller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
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
            Config.requiredItems = target.asItem();
            Config.requiredCount = getRequiredCount();
            Config.reversed = false;
            if (Config.requiredCount == 0) return;
            MenuManager.checkMenu(MenuManager.GET_CARPET_FROM_ENDER_CHEST);
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
        if (putOrGetFromChest(screen)) return true;

        String target = Config.reversed ? "Space" : "Carpet";
        assert Minecraft.getInstance().player != null;
        Minecraft.getInstance().player.displayClientMessage(Component.literal("Not Enough " + target + " in Ender Chest, Searching in YCK"), false);
        DesktopNotifier.notify("Artist", "Not Enough " + target + " in Ender Chest, Searching in YCK");

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
        if (putOrGetFromChest(screen)) return true;
        if (nextPage(screen)) return false;

        String target = Config.reversed ? "Space" : "Carpet";
        if (shared) {
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Not Enough " + target + " in YCK, Stopping"), false);
            DesktopNotifier.notify("Artist", "Not Enough " + target + " in YCK, Stopping");
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

    private static int getRequiredCount(){
        // count remaining
        int freeSlots = InventoryUtils.countFreeSlots();
        int typeCount = InventoryUtils.countType();

        if (freeSlots == 0){
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.displayClientMessage(Component.literal("No Free Slot in Inventory"), false);
            DesktopNotifier.notify("Artist", "No Free Slot in Inventory");
            shared = false;
            return 0;
        }

        int reserved = Math.max(0, Config.blockList.size() - typeCount);

        // ensure not filling all spaces
        int max = Math.max(1, freeSlots / 2);
        max = Math.min(max, Math.max(1, freeSlots - reserved));
        return max;
    }

    private static boolean putOrGetFromChest(AbstractContainerScreen<?> screen){
        if (Config.reversed) return putToChest(screen);
        else return getFromEnderChest(screen);
    }

    private static boolean putToChest(AbstractContainerScreen<?> screen){
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        Config.requiredCount -= InventoryUtils.getFromChest(screen, Config.requiredItems, Config.requiredCount, 54, 90, screen.getMenu().slots.getFirst().container);

        if (Config.requiredCount == 0){
            shared = false;
            new Thread(() -> {
                sleep();
                MaterialCollector.nextChest();
            }).start();
            return true;
        }

        return false;
    }

    private static boolean getFromEnderChest(AbstractContainerScreen<?> screen){
        if (Config.requiredItems == null) return true;

        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        player.displayClientMessage(Component.literal("Plan to pick up " + Config.requiredCount + " carpets."), false);

        if (InventoryUtils.getFromChest(screen, Config.requiredItems, Config.requiredCount, 0, 54, player.getInventory()) != 0){
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
