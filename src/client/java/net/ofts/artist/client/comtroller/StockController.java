package net.ofts.artist.client.comtroller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.ofts.artist.client.Config;
import net.ofts.artist.client.DesktopNotifier;
import net.ofts.artist.client.RawKeyInjector;
import net.ofts.artist.client.menu.MenuHandler;
import net.ofts.artist.client.menu.MenuManager;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
            Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand(Config.CONFIG.enderChestCommand());
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
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        player.displayClientMessage(Component.literal("Not Enough " + target + " in Ender Chest"), false);
        DesktopNotifier.notify("Artist", "Not Enough " + target + " in Ender Chest");

        if (Config.useYCK()) {
            new Thread(() -> {
                sleep();
                Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand("yck OFTS_CQM");
                MenuManager.checkMenu(MenuManager.OPEN_YCK);
            }).start();
        }else if (Config.useShulkerBox()){
            Inventory inventory = player.getInventory();
            int i = getSlotWithTargetShulkerBox(inventory);

            if (i == -1){
                checkAndStop(player, "No Target Shulker Box Found, Stopping");
                return true;
            }

            MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
            assert gameMode != null;

            if (i < 9)
                inventory.setSelectedSlot(i);
            else
                gameMode.handleInventoryMouseClick(screen.getMenu().containerId, i + 45, inventory.getSelectedSlot(), ClickType.SWAP, player);
                //MenuHandler.sendClick(screen.getMenu(), i + 45, ClickType.SWAP, (byte)inventory.getSelectedSlot());

            new Thread(() -> {
                sleep();
                MovementController.getOrInstall(player).setSneak(true);
                sleep();
                doRightClick = true;
                MenuManager.checkMenu(MenuManager.GET_CARPET_FROM_SHULKER_BOX);
            }).start();
        }
        return true;
    }

    private static int getSlotWithTargetShulkerBox(Inventory inventory){
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.is(ItemTags.SHULKER_BOXES)) continue;

            ItemContainerContents contents = itemStack.getOrDefault(
                    DataComponents.CONTAINER,
                    ItemContainerContents.EMPTY
            );

            if(contents.stream().anyMatch(stack -> stack.is(Config.requiredItems))) return i;
        }
        return -1;
    }

    private static boolean shared = false;

    public static boolean checkYCKMenu(AbstractContainerScreen<?> screen){
        int slot = shared ? 11 : 10;
        if (!screen.getMenu().getSlot(slot).getItem().is(ItemTags.SHULKER_BOXES)){
            // uh oh, we are in an error state.
            new Thread(() -> {
                sleep();
                Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand("yck OFTS_CQM");
                MenuManager.checkMenu(MenuManager.OPEN_YCK);
            }).start();

            return true;
        }

        MenuHandler.sendClick(screen.getMenu(), slot, ClickType.PICKUP, (byte) 0);
        MenuManager.checkMenu(shared ? MenuManager.GET_CARPET_FROM_YCK_SHARED : MenuManager.GET_CARPET_FROM_YCK);
        return false;
    }

    public static boolean checkShulkerBox(AbstractContainerScreen<?> screen){
        doRightClick = false;
        failedCount = 0;
        getFromEnderChest(screen, false);
        MovementController.startIfNot();
        return true;
    }

    private static boolean nextPage(AbstractContainerScreen<?> screen){
        ItemStack item = screen.getMenu().slots.get(53).getItem();
        if (item.is(Items.ARROW)){
            MenuHandler.sendClick(screen.getMenu(), 53, ClickType.PICKUP, (byte) 0);
            MenuManager.checkMenu(shared ? MenuManager.GET_CARPET_FROM_YCK_SHARED : MenuManager.GET_CARPET_FROM_YCK);
            return true;
        }
        return false;
    }

    private static void checkAndStop(LocalPlayer player, String message){
        // before stopping, check stock one last time
        MovementController.checkBlocks(false);
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack.is(MovementController.target)) {
                MovementController.start();
                shared = false;
                return;
            }
        }

        player.displayClientMessage(Component.literal(message), false);
        DesktopNotifier.notify("Artist", message);
        shared = false;
    }

    public static boolean checkYCK(AbstractContainerScreen<?> screen){
        if (putOrGetFromChest(screen)) return true;
        if (nextPage(screen)) return false;

        if (shared) {
            LocalPlayer player = Minecraft.getInstance().player;
            assert player != null;

            checkAndStop(player, "Not Enough Items in YCK, Stopping");
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
        else return getFromEnderChest(screen, true);
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

    private static boolean getFromEnderChest(AbstractContainerScreen<?> screen, boolean large){
        if (Config.requiredItems == null) return true;

        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        player.displayClientMessage(Component.literal("Plan to pick up " + Config.requiredCount + " carpets."), false);

        if (InventoryUtils.getFromChest(screen, Config.requiredItems, Config.requiredCount, 0, large ? 54 : 27, player.getInventory()) != 0){
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

    public static boolean doRightClick = false;
    public static int failedCount = 0;

    private static void rightClick(){
        if (!doRightClick) return;

        try {
            MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
            LocalPlayer player = Minecraft.getInstance().player;

            if (gameMode == null || player == null) return;

            if (!player.getInventory().getSelectedItem().is(ItemTags.SHULKER_BOXES)){
                Inventory inventory = player.getInventory();
                int i = getSlotWithTargetShulkerBox(inventory);

                if (i == -1){
                    checkAndStop(player, "No Target Shulker Box Found, Stopping");
                    doRightClick = false;
                    failedCount = 0;
                    return;
                }

                if (i < 9) {
                    inventory.setSelectedSlot(i);
                } else {
                    player.displayClientMessage(Component.literal("Error: Inventory Changed, Stopping..."), false);
                    DesktopNotifier.notify("Artist", "Error: Inventory Changed, Stopping...");
                    doRightClick = false;
                    failedCount = 0;
                }
            }

            gameMode.useItem(player, InteractionHand.MAIN_HAND);
            failedCount++;
            if (failedCount >= 5){
                player.displayClientMessage(Component.literal("Error: Open Shulker Box Not Working, Stopping..."), false);
                DesktopNotifier.notify("Artist", "Error: Open Shulker Box Not Working, Stopping...");
            }else if (failedCount >= 3){
                MovementController.getOrInstall(player).setSneak(false);
                sleep();
                MovementController.getOrInstall(player).setSneak(true);
                sleep();
                failedCount = 0;
            }
        } catch (Exception ignored) {
            doRightClick = false;
            failedCount = 0;
        }
    }

    static {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(StockController::rightClick, 500, 500, TimeUnit.MILLISECONDS);
    }
}
