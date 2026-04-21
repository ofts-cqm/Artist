package net.ofts.artist.client.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.ofts.artist.client.Config;
import net.ofts.artist.client.DesktopNotifier;
import net.ofts.artist.client.RawKeyInjector;
import net.ofts.artist.client.comtroller.MovementController;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class MenuManager {
    public static final int GET_CARPET_FROM_ENDER_CHEST = 0;
    public static final int AUDIT_INVENTORY = 1;

    private static final int TYPE_COUNT = 2;

    private static final MenuHandler[] handlers = new MenuHandler[TYPE_COUNT];
    private static final boolean[] taskQueue = new boolean[TYPE_COUNT];
    private static final AbstractContainerScreen<?>[] arrivedList = new AbstractContainerScreen<?>[TYPE_COUNT];

    public static void clearTaskQueue() {
        Arrays.fill(taskQueue, false);
        Arrays.fill(arrivedList, null);
    }

    public static void checkMenu(int id){
        if (taskQueue[id]) return;
        taskQueue[id] = true;
    }

    public static synchronized void handleMenu(AbstractContainerScreen<?> menu){
        for (MenuHandler handler : handlers){
            if (handler == null || !menu.getTitle().getString().contains(handler.menuMatcher())) continue;

            int id = handler.id();
            if (arrivedList[id] != null || !taskQueue[id]) continue;
            arrivedList[id] = menu;
            taskQueue[id] = false;

            new Thread(MenuManager::thread).start();
            return;
        }
    }

    private static int anyArrived(){
        for (int i = 0; i < arrivedList.length; i++){
            if (arrivedList[i] != null) return i;
        }
        return -1;
    }

    private static void sleep(){
        try {
            Thread.sleep(Config.MENU_WAIT_TIME);
        } catch (InterruptedException ignored) {}
    }

    private static void thread() {
        int arrivedTask = anyArrived();

        AbstractContainerScreen<?> menu = arrivedList[arrivedTask];
        arrivedList[arrivedTask] = null;

        sleep();

        handlers[arrivedTask].handleMenu(menu);

        closeMenu();
    }

    private static void closeMenu(){
        Minecraft client = Minecraft.getInstance();

        client.execute(() -> client.setScreen(null));
    }

    private static void getFromEnderChest(AbstractContainerScreen<?> screen){
        if (Config.requiredItems == null) return;

        // count remaining
        assert Minecraft.getInstance().player != null;
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        int freeSlots = 0;

        for (ItemStack item : inventory){
            if (item.isEmpty()) freeSlots++;
        }

        // ensure not filling all spaces
        int max = Math.max(3, freeSlots * 3 / 4);
        int clicked = 0;
        for (Slot slot : screen.getMenu().slots) {
            if (slot.getItem().is(Config.requiredItems)){
                MenuHandler.sendClick(screen.getMenu(), slot.getContainerSlot(), ClickType.QUICK_MOVE);
                clicked++;

                sleep();
            }

            if (clicked >= max) break;
        }

        RawKeyInjector.tapKey(GLFW.GLFW_KEY_CAPS_LOCK);

        if (clicked != 0) MovementController.start();
        else DesktopNotifier.notify("Artist", "Not Enough Carpet in Ender Chest");
    }

    private static void auditInventory(AbstractContainerScreen<?> screen){
        for (Slot slot : screen.getMenu().slots) {
            MenuHandler.sendClick(screen.getMenu(), slot.getContainerSlot(), ClickType.CLONE);
        }
    }

    static {
        handlers[0] = new MenuHandler(GET_CARPET_FROM_ENDER_CHEST, "末影箱", MenuManager::getFromEnderChest);
        handlers[1] = new MenuHandler(AUDIT_INVENTORY, "Crafting", MenuManager::auditInventory);
    }
}
