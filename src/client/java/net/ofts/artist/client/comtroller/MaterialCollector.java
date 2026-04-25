package net.ofts.artist.client.comtroller;

import com.mojang.datafixers.util.Pair;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListUtils;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.ofts.artist.client.Config;
import net.ofts.artist.client.menu.MenuManager;

import java.util.*;

public class MaterialCollector {
    private static HashMap<Item, List<BlockPos>> chests;
    private static final List<Pair<Item, Integer>> required = new ArrayList<>();
    private static int currentIndex = 0;
    private static Item currentCarpet;
    private static int remaining;
    private static int chestIndex;

    public static void collectMaterial(LitematicaSchematic schematic){
        List<MaterialListEntry> materials = MaterialListUtils.createMaterialListFor(schematic, schematic.getAreas().keySet());
        required.clear();

        for (MaterialListEntry material : materials) {
            ItemStack item = material.getStack();
            if (!item.is(ItemTags.WOOL_CARPETS) || item.is(Items.GRAY_CARPET)) continue;

            int stack = (material.getCountTotal() + 64) / 64;
            required.add(new Pair<>(item.getItem(), (int)(stack * 1.05)));
        }

        if (required.isEmpty()) return;

        required.sort(Comparator.comparingInt(Pair::getSecond));
        currentIndex = 0;
        currentCarpet = required.getFirst().getFirst();
        remaining = required.getFirst().getSecond();
        chestIndex = 0;

        loadChests();
        nextChest();
    }

    public static void nextChest(){
        if (remaining == 0){
            currentIndex++;

            if (currentIndex == required.size()){
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.displayClientMessage(Component.literal("All Materials Are Collected!"), false);
                return;
            }

            Item oldCarpet = currentCarpet;
            currentCarpet = required.get(currentIndex).getFirst();
            remaining = required.get(currentIndex).getSecond();
            chestIndex = 0;

            LocalPlayer player = Minecraft.getInstance().player;
            assert player != null;

            Config.requiredItems = oldCarpet;
            Config.requiredCount = InventoryUtils.countSlotOf(player.getInventory(), oldCarpet) - 3;
            Config.reversed = true;
            MenuManager.checkMenu(MenuManager.GET_CARPET_FROM_ENDER_CHEST);
            Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand("myx");
            return;
        }

        openChest(chests.get(currentCarpet).get(chestIndex));
        MenuManager.checkMenu(MenuManager.GET_CARPET_FROM_CHEST);
    }

    public static boolean handleChest(AbstractContainerScreen<?> screen){
        remaining -= InventoryUtils.getFromChest(screen, currentCarpet, remaining, 0, 54, screen.getMenu().slots.getLast().container);

        if (InventoryUtils.countFreeSlots() == 0){
            Config.requiredItems = currentCarpet;
            assert Minecraft.getInstance().player != null;
            Config.requiredCount = InventoryUtils.countSlotOf(Minecraft.getInstance().player.getInventory(), currentCarpet);
            Config.reversed = true;
            MenuManager.checkMenu(MenuManager.GET_CARPET_FROM_ENDER_CHEST);
            Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendCommand("myx");
        }else{
            chestIndex++;
            if (chestIndex == chests.get(currentCarpet).size()){
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Not Enough Carpets In Stock!"), false);
                return true;
            }
        }

        new Thread(() -> {
            InventoryUtils.sleep();
            nextChest();
        }).start();

        return true;
    }

    private static void openChest(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            LocalPlayer player = mc.player;
            MultiPlayerGameMode gameMode = mc.gameMode;

            if (player == null || gameMode == null || mc.level == null) return;

            Vec3 hitPos = Vec3.atCenterOf(pos);

            BlockHitResult hit = new BlockHitResult(
                    hitPos,
                    Direction.UP,
                    pos,
                    false
            );

            InteractionResult result =
                    gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);

            if (result.consumesAction()) {
                player.swing(InteractionHand.MAIN_HAND);
            }
        });
    }

    private static void loadChests(){
        if (chests != null) return;
        chests = new HashMap<>();

        ClientLevel level = Minecraft.getInstance().level;
        assert level != null;
        assert Minecraft.getInstance().player != null;
        BlockPos playerPos = Minecraft.getInstance().player.blockPosition();

        for (int i = -10; i < 10; i++){
            for (int j = 0; j < 3; j++){
                for (int k = -10; k < 10; k++){
                    BlockEntity entity = level.getBlockEntity(playerPos.offset(i, j, k));
                    if (entity instanceof ChestBlockEntity){
                        BlockState wool = level.getBlockState(playerPos.offset(i, 152, k));
                        if (!wool.is(BlockTags.WOOL_CARPETS)) continue;

                        chests.computeIfAbsent(wool.getBlock().asItem(), a -> new ArrayList<>()).add(playerPos.offset(i, j, k));
                    }
                }
            }
        }
    }
}
