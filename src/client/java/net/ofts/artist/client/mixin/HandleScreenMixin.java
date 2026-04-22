package net.ofts.artist.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.ofts.artist.client.menu.MenuManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class HandleScreenMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onSetScreen(Screen screen, CallbackInfo ci){
        if (screen instanceof AbstractContainerScreen<?> containerScreen){
            MenuManager.handleMenu(containerScreen);
            System.out.println("Menu Updated, new Menu: " + containerScreen.getTitle().getString());
        }
    }
}
