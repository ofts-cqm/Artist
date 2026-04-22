package net.ofts.artist.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Config {
    public static final int MENU_WAIT_TIME = 200;
    public static String schematicName;
    public static BlockPos offset = new BlockPos(-2240, 161, -3137);
    public static HashMap<Carpets, HashSet<BlockPos>> blockList = new HashMap<>();
    public static HashMap<BlockPos, Block> blockMap = new HashMap<>();
    public static ConcurrentHashMap<BlockPos, Carpets> remaining = new ConcurrentHashMap<>();
    public static HashSet<BlockPos> emptyPos = new HashSet<>();
    public static HashSet<Carpets> targets = new HashSet<>();
    public static Item requiredItems = null;

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
