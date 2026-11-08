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
        WHITE("minecraft:white_carpet", Blocks.CARPET.white()),
        LIGHT_GRAY("minecraft:light_gray_carpet", Blocks.CARPET.lightGray()),
        BLACK("minecraft:black_carpet", Blocks.CARPET.black()),
        BROWN("minecraft:brown_carpet", Blocks.CARPET.brown()),
        RED("minecraft:red_carpet", Blocks.CARPET.red()),
        ORANGE("minecraft:orange_carpet", Blocks.CARPET.orange()),
        YELLOW("minecraft:yellow_carpet", Blocks.CARPET.yellow()),
        LIME("minecraft:lime_carpet", Blocks.CARPET.lime()),
        GREEN("minecraft:green_carpet", Blocks.CARPET.green()),
        LIGHT_BLUE("minecraft:light_blue_carpet", Blocks.CARPET.lightBlue()),
        CYAN("minecraft:cyan_carpet", Blocks.CARPET.cyan()),
        BLUE("minecraft:blue_carpet", Blocks.CARPET.blue()),
        PINK("minecraft:pink_carpet", Blocks.CARPET.pink()),
        MAGENTA("minecraft:magenta_carpet", Blocks.CARPET.magenta()),
        PURPLE("minecraft:purple_carpet", Blocks.CARPET.purple());

        public final String id;
        public final Block block;

        Carpets(String id, Block block) {
            this.id = id;
            this.block = block;
        }
    }

    public record ConfigDetails(HandleMethod handleMethod, String enderChestCommand){
        public ConfigDetails(){
            this(HandleMethod.SHULKER_BOX, "yck");
        }
    }

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
    public static ConfigDetails CONFIG = new ConfigDetails();

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
                CONFIG = new ConfigDetails();
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

    public static boolean useShulkerBox(){
        return CONFIG.handleMethod == HandleMethod.SHULKER_BOX;
    }

    public static boolean useYCK(){
        return CONFIG.handleMethod == HandleMethod.YCK;
    }
}

