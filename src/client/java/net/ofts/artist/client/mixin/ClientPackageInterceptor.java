package net.ofts.artist.client.mixin;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.ofts.artist.client.menu.MenuManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientPackageInterceptor {
    // intercept any attempt to close the container while we are processing it
    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private static void onSend(Packet<?> packet, CallbackInfo ci){
        if (packet instanceof ServerboundContainerClosePacket container && MenuManager.isProcessing(container.getContainerId())){
            ci.cancel();
        }
    }
}
