package net.ofts.artist.client;

import net.minecraft.client.Minecraft;

import java.util.Objects;

public final class RawKeyInjector {
    public static void enablePrinter(){
        Minecraft.getInstance().execute(() -> Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand("ofts-printer enable"));
    }

    public static void disablePrinter(){
        Minecraft.getInstance().execute(() -> Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand("ofts-printer disable"));
    }
}