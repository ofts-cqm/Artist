package net.ofts.artist.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.ofts.artist.client.mixin.KeyboardHandlerAccessor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Queue;

public final class RawKeyInjector {
    private static final Queue<Runnable> TASKS = new ArrayDeque<>();
    private static int delayTicks = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (delayTicks > 0) {
                delayTicks--;
                return;
            }

            Runnable task = TASKS.poll();
            if (task != null) {
                task.run();
            }
        });
    }

    public static void tapKey(int key) {
        Minecraft client = Minecraft.getInstance();

        TASKS.add(() -> {
            long window = client.getWindow().handle();
            //client.player.displayClientMessage(Component.literal("Key down"), false);
            ((KeyboardHandlerAccessor)client.keyboardHandler).invokeKeyPress(window, GLFW.GLFW_PRESS, new KeyEvent(key, 0, 0));
            delayTicks = 1;
        });

        TASKS.add(() -> {
            long window = client.getWindow().handle();
            //client.player.displayClientMessage(Component.literal("key up"), false);
            ((KeyboardHandlerAccessor)client.keyboardHandler).invokeKeyPress(window, GLFW.GLFW_RELEASE, new KeyEvent(key, 0, 0));
            delayTicks = 1;
        });
    }
}