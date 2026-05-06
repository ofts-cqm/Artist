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
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci){
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> previous && MenuManager.isProcessing(previous)){
            ci.cancel();
            return;
        }

        if (screen instanceof AbstractContainerScreen<?> containerScreen){
            MenuManager.handleMenu(containerScreen);
        }
        System.out.println(
                "Menu Updated, previous Menu: "
                + (Minecraft.getInstance().screen == null ? "[Null]" : Minecraft.getInstance().screen.getTitle().getString())
                + "new Menu: "
                + (screen == null ? "[Null]" : screen.getTitle().getString())
        );
    }
}
