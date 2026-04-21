package net.ofts.artist.client.comtroller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.ofts.artist.client.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class MaterialController {
    private static Minecraft client;
    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialController.class);

    public static void start(){
        client = Minecraft.getInstance();
        Config.blockList.clear();
        Config.blockMap.clear();
        new Thread(MaterialController::querySchematic).start();
    }

    private static void reportError(String name){
        LOGGER.error("Failed to find tag {} in nbt", name);
        assert client.player != null;
        client.player.displayClientMessage(Component.literal("Failed to fin tag: " + name), false);
        throw new RuntimeException("Tag Not Found");
    }

    @Deprecated
    public static boolean updateState(boolean print){
        ClientLevel level = client.level;
        if (level == null){
            LOGGER.error("Failed to Load Level");
            if (print){
                assert client.player != null;
                client.execute(() -> client.player.displayClientMessage(Component.literal("Failed to Load Level"), false));
            }
            return false;
        }

        for (Config.Carpets carpet : Config.targets){
            LOGGER.debug("Searching for carpet {}", carpet);
            for (BlockPos pos : Config.blockList.get(carpet)){
                if (!level.getBlockState(pos).is(carpet.block)){
                    Config.remaining.put(pos, carpet);
                }
            }
        }

        LOGGER.info("Found {} remaining blocks", Config.remaining.size());
        if (print){
            assert client.player != null;
            client.execute(() -> client.player.displayClientMessage(Component.literal("Found " + Config.remaining.size() + " remaining blocks"), false));
        }
        return true;
    }

    private static void querySchematic(){
        assert client.player != null;
        client.execute(() ->
            client.player.displayClientMessage(Component.literal("Loading Schematics " + Config.schematicName), false)
        );
        Path schematicsDir = client.gameDirectory.toPath().resolve("schematics");
        CompoundTag root;

        try {
            root = NbtIo.readCompressed(schematicsDir.resolve(Config.schematicName), NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            LOGGER.error("Cannot resolve schematic file: {}", e.getMessage());
            client.player.displayClientMessage(Component.literal("Cannot resolve schematic file: " + Config.schematicName), false);
            return;
        }

        Optional<ListTag> paletteOptional = root.getList("palette");
        if (paletteOptional.isEmpty()){
            reportError("palette");
            return;
        }

        ListTag palette = paletteOptional.get();
        List<Config.Carpets> mapping = new ArrayList<>(Collections.nCopies(100, null));

        for (int i = 0; i < palette.size(); i++) {
            String id = palette.getCompoundOrEmpty(i).getStringOr("Name", "");
            boolean found = false;

            for (Config.Carpets carpet : Config.Carpets.values()){
                if (carpet.id.equals(id)){
                    mapping.set(i, carpet);
                    found = true;
                    break;
                }
            }

            if (!found && !id.equals("minecraft:gray_carpet")){
                LOGGER.warn("Block Not Found: {}", id);
            }
        }

        Optional<ListTag> blocksOptional = root.getList("blocks");
        if (blocksOptional.isEmpty()){
            reportError("blocks");
            return;
        }

        ListTag blocks = blocksOptional.get();
        blocks.forEach(tag -> {
            CompoundTag compound = tag.asCompound().orElse(new CompoundTag());
            int state = compound.getIntOr("state", -1);
            if (state == -1 || state >= 100 || mapping.get(state) == null) return;

            ListTag list = compound.getListOrEmpty("pos");
            if (list.size() != 3) {
                reportError("pos");
                return;
            }

            int x = list.getFirst().asInt().orElse(-1);
            int y = list.get(1).asInt().orElse(-1);
            int z = list.get(2).asInt().orElse(-1);
            if (x == -1 || y == -1 || z == -1) return;

            BlockPos pos = new BlockPos(x, y, z).offset(Config.offset);
            Config.blockMap.put(pos, mapping.get(state).block);
            Config.blockList.computeIfAbsent(mapping.get(state), a -> new HashSet<>()).add(pos);
        });

        client.execute(() ->
                client.player.displayClientMessage(Component.literal("Loading Succeeded"), false)
        );
        //updateState(false);
    }
}
