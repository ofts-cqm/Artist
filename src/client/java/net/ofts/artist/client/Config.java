package net.ofts.artist.client;

import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;

public class Config {
    public static final int MENU_WAIT_TIME = 200;
    @Deprecated
    public static Path schematicPath;
    public static SchematicPlacement lastSchematic;
    public static AABB placementAABB = new AABB(0, 0, 0, 0, 0, 0);
    public static HashMap<Carpets, HashSet<BlockPos>> blockList = new HashMap<>();
    public static HashSet<BlockPos> emptyPos = new HashSet<>();
    public static HashSet<Carpets> targets = new HashSet<>();
    public static Item requiredItems = null;
    public static int requiredCount;
    public static boolean reversed;

    public enum Carpets {
        WHITE("minecraft:white_carpet", Blocks.WHITE_CARPET),
        LIGHT_GRAY("minecraft:light_gray_carpet", Blocks.LIGHT_GRAY_CARPET),
        BLACK("minecraft:black_carpet", Blocks.BLACK_CARPET),
        BROWN("minecraft:brown_carpet", Blocks.BROWN_CARPET),
        RED("minecraft:red_carpet", Blocks.RED_CARPET),
        ORANGE("minecraft:orange_carpet", Blocks.ORANGE_CARPET),
        YELLOW("minecraft:yellow_carpet", Blocks.YELLOW_CARPET),
        LIME("minecraft:lime_carpet", Blocks.LIME_CARPET),
        GREEN("minecraft:green_carpet", Blocks.GREEN_CARPET),
        LIGHT_BLUE("minecraft:light_blue_carpet", Blocks.LIGHT_BLUE_CARPET),
        CYAN("minecraft:cyan_carpet", Blocks.CYAN_CARPET),
        BLUE("minecraft:blue_carpet", Blocks.BLUE_CARPET),
        PINK("minecraft:pink_carpet", Blocks.PINK_CARPET),
        MAGENTA("minecraft:magenta_carpet", Blocks.MAGENTA_CARPET),
        PURPLE("minecraft:purple_carpet", Blocks.PURPLE_CARPET);

        public final String id;
        public final Block block;

        Carpets(String id, Block block) {
            this.id = id;
            this.block = block;
        }
    }
}
