package net.ofts.artist.client.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.ofts.artist.client.Config;
import net.ofts.artist.client.comtroller.MaterialCollector;
import net.ofts.artist.client.comtroller.StockController;

import java.util.Arrays;

public class MenuManager {
    public static final int GET_CARPET_FROM_ENDER_CHEST = 0;
    public static final int OPEN_YCK = 1;
    public static final int GET_CARPET_FROM_YCK = 2;
    public static final int GET_CARPET_FROM_YCK_SHARED = 3;
    public static final int GET_CARPET_FROM_CHEST = 4;

    private static final int TYPE_COUNT = 5;

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

        boolean close = handlers[arrivedTask].handleMenu(menu);

        if (close) closeMenu(menu);
    }

    private static void closeMenu(AbstractContainerScreen<?> screen){
        Minecraft client = Minecraft.getInstance();

        client.execute(screen::onClose);
    }

    static {
        handlers[0] = new MenuHandler(GET_CARPET_FROM_ENDER_CHEST, "末影箱", StockController::checkEnderChest);
        handlers[1] = new MenuHandler(OPEN_YCK, "云仓库主菜单", StockController::checkYCKMenu);
        handlers[2] = new MenuHandler(GET_CARPET_FROM_YCK, "个人仓库", StockController::checkYCK);
        handlers[3] = new MenuHandler(GET_CARPET_FROM_YCK_SHARED, "共享仓库", StockController::checkYCK);
        handlers[4] = new MenuHandler(GET_CARPET_FROM_CHEST, "Chest", MaterialCollector::handleChest);
    }
}
