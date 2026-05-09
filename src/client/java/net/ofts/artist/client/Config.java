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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private enum HandleMethod {
        SHULKER_BOX,
        YCK,
        NONE
    }

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

    public record ConfigDetails(HandleMethod handleMethod, String enderChestCommand){}

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
    public static ConfigDetails CONFIG = createDefaultConfig();

    private static final File CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("artist.json")
            .toFile();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                // Read existing file
                CONFIG = GSON.fromJson(reader, CONFIG.getClass());
            } catch (IOException e) {
                CONFIG = createDefaultConfig();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(CONFIG, writer);
        } catch (IOException ignored) {
        }
    }

    private static ConfigDetails createDefaultConfig(){
        return new ConfigDetails(HandleMethod.SHULKER_BOX, "myx");
    }

    public static boolean useShulkerBox(){
        return CONFIG.handleMethod == HandleMethod.SHULKER_BOX;
    }

    public static boolean useYCK(){
        return CONFIG.handleMethod == HandleMethod.YCK;
    }
}

