package net.ofts.artist.client.comtroller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ofts.artist.client.BotInput;
import net.ofts.artist.client.Config;
import net.ofts.artist.client.DesktopNotifier;
import net.ofts.artist.client.RawKeyInjector;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class MovementController {
    private static boolean run = false;
    private static ClientInput oldInput = null;

    public static void toggle(){
        if (run) pause();
        else start();
    }

    public static void start(){
        run = true;
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        getOrInstall(player).setForward(true);
        RawKeyInjector.enablePrinter();
        player.displayClientMessage(Component.literal("Start Painting!"), false);
    }

    public static void pause(){
        run = false;
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        getOrInstall(player).setForward(false);
        player.input = oldInput;
    }

    private static void checkError(BlockPos pos){
        Vec3 dir2 = new Vec3(pos).subtract(playerPos);
        // closer error, or first error
        if (dir2.length() < minDis || cumulativeError == 0){
            minDis = dir2.length();
            direction = dir2;
            target = null;
        }

        cumulativeError++;
    }

    private static boolean checkBlocks(){
        boolean has = false;
        ClientLevel level = Minecraft.getInstance().level;
        assert level != null;

        for (Config.Carpets carpet : Config.targets){
            for (BlockPos pos : Config.blockList.getOrDefault(carpet, new HashSet<>())){
                BlockState state;
                try {
                    state = level.getBlockState(pos); } catch (Exception e) { continue; }

                if (state.is(carpet.block)) continue;

                // we require the current is air and the target is carpet.
                // also, only search if we have no error.
                // if we have error, solve error first
                if (state.isAir()){
                    if (cumulativeError != 0) continue;
                    has = true;
                    Vec3 dir2 = new Vec3(pos).subtract(playerPos);
                    if (dir2.length() < minDis){
                        minDis = dir2.length();
                        direction = dir2;
                        target = carpet.block.asItem();
                    }
                }else{
                    has = true;
                    checkError(pos);
                }
            }
        }

        for (BlockPos pos : Config.emptyPos){
            BlockState state;
            try { state = level.getBlockState(pos); } catch (Exception e) { continue; }

            if (state.isAir()) continue;
            checkError(pos);
        }

        return has;
    }

    private static Vec3 direction = new Vec3(0, 0, 0), playerPos;
    private static int cumulativeError;
    private static double minDis;
    private static Item target;

    private static void update(){
        if (!run) return;

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        assert player != null;
        playerPos = player.position();
        minDis = Double.MAX_VALUE;
        ClientLevel level = client.level;
        assert level != null;
        boolean has;
        target = null;
        cumulativeError = 0;

        List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, new AABB(playerPos.add(128, 1, 128), playerPos.add(-128, -1, -128)));

        // entity first, we need to pick up the carpets
        if (!entities.isEmpty()){
            Stream<ItemEntity> stream = entities.stream().filter(a -> a.getItem().is(ItemTags.WOOL_CARPETS));
            ItemEntity closest = Collections.min(stream.toList(), (a, b) -> {
                double dis1 = a.position().subtract(playerPos).lengthSqr();
                double dis2 = b.position().subtract(playerPos).lengthSqr();

                return (int)(dis1 - dis2);
            });

            direction = closest.position().subtract(playerPos);
            target = null;
            has = true;
        }else has = checkBlocks();

        if (!has) {
            pause();
            player.displayClientMessage(Component.literal("Task Finished!"), false);
            DesktopNotifier.notify("Artist", "Task Finished!");
        }

        direction = direction.normalize();
        float yaw = -(float)(Math.toDegrees(Math.atan2(direction.x, direction.z)));
        player.setYRot(yaw);
        player.setYBodyRot(yaw);
        player.setYHeadRot(yaw);

        if (target != null){
            for (ItemStack itemStack : player.getInventory()) {
                if (itemStack.is(target.asItem())) return;
            }
            pause();
            StockController.getCarpet(target);
        }
    }

    public static BotInput getOrInstall(LocalPlayer player) {
        if (player.input instanceof BotInput existing) {
            return existing;
        }

        BotInput botInput = new BotInput();
        oldInput = player.input;
        player.input = botInput;
        return botInput;
    }

    private static void runUpdate(){
        try{
            update();
        } catch (Exception e) {
            if (Minecraft.getInstance().player != null)
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Error During Update"), false);
        }
    }

    static {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(MovementController::runUpdate, 1000, 1000, TimeUnit.MILLISECONDS);
    }
}
