package net.ofts.artist.client.menu;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.ofts.artist.client.Config;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MenuManager {
    private static final int GET_CARPET_FROM_ENDER_CHEST = 0;

    private static final int TYPE_COUNT = 1;

    private static final MenuHandler[] handlers = new MenuHandler[TYPE_COUNT];
    private static final Task[] taskQueue = new Task[TYPE_COUNT];
    private static final AbstractContainerScreen<?>[] arrivedList = new AbstractContainerScreen<?>[TYPE_COUNT];
    public static void clearTaskQueue() {
        Arrays.fill(taskQueue, null);
        Arrays.fill(arrivedList, null);
    }

    public static void checkMenu(int id){
        if (taskQueue[id] != null) return;
        taskQueue[id] = new Task(id);
        taskQueue[id].open();
    }

    public static boolean checkMenu(String name){
        for (MenuHandler handler : handlers){
            if (handler != null && handler.name().equals(name)){
                checkMenu(handler.id());
                return true;
            }
        }
        return false;
    }

    private static void auditTask(){
        if (Minecraft.getInstance().getCurrentServer() == null) return;

        for (int i = 0; i < TYPE_COUNT; i++) {
            Task task = taskQueue[i];
            if (task == null) continue;
            if (task.time.isAfter(LocalDateTime.now().plusSeconds(1))) {
                if (task.tried >= Config.RETRY_COUNT) taskQueue[i] = null;
                else task.open();
            }
        }
    }

    public static synchronized boolean handleMenu(AbstractContainerScreen<?> menu){
        for (MenuHandler handler : handlers){
            if (handler == null || !menu.getTitle().getString().contains(handler.menuMatcher())) continue;

            int id = handler.id();
            if (arrivedList[id] != null || taskQueue[id] == null) continue;
            arrivedList[id] = menu;
            taskQueue[id] = null;

            new Thread(MenuManager::thread).start();
            return true;
        }

        return false;
    }

    public static CompletableFuture<Suggestions> getSuggestion(SuggestionsBuilder builder){
        for (MenuHandler handler : handlers) if (handler != null) builder.suggest(handler.name());
        return builder.buildFuture();
    }

    private static int anyArrived(){
        for (int i = 0; i < arrivedList.length; i++){
            if (arrivedList[i] != null) return i;
        }
        return -1;
    }

    private static void thread() {
        int arrivedTask = anyArrived();

        AbstractContainerScreen<?> menu = arrivedList[arrivedTask];
        arrivedList[arrivedTask] = null;

        try {
            Thread.sleep(Config.MENU_WAIT_TIME);
        } catch (InterruptedException ignored) {}

        handlers[arrivedTask].handleMenu(menu);

        closeMenu(menu);
    }

    private static void closeMenu(AbstractContainerScreen<?> menu){
        Minecraft client = Minecraft.getInstance();

        client.execute(() -> {
            client.setScreen(null);
        });
    }

    private static class Task {
        public int tried;
        public int id;
        public LocalDateTime time;

        public Task(int id) {
            this.id = id;
            this.tried = 0;
            time = LocalDateTime.now();
        }

        public void open() {
            handlers[id].opener().run();
        }
    }

    static {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(MenuManager::auditTask, 0, Config.MENU_WAIT_TIME, TimeUnit.MILLISECONDS);


    }
}
