package net.ofts.artist.client;


import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

public final class BotInput extends ClientInput {
    private static final Input FORWARD_ONLY = new Input(
            true,   // forward
            false,  // backward
            false,  // left
            false,  // right
            false,  // jump
            false,  // sneak
            false   // sprint
    );

    private static final Input EMPTY = Input.EMPTY;

    private boolean forward;

    public void setForward(boolean forward) {
        this.forward = forward;
        this.keyPresses = forward ? FORWARD_ONLY : EMPTY;
        this.moveVector = forward ? Vec2.UNIT_Y : Vec2.ZERO;
    }

    @Override
    public void tick() {
        if (!forward){
            super.tick();
            return;
        }

        // Re-apply the cached state every tick.
        // This keeps the input stable and ignores physical keyboard input.
        this.keyPresses = FORWARD_ONLY;
        this.moveVector = Vec2.UNIT_Y;
    }
}