package net.ofts.artist.client.comtroller;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.selection.Box;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ofts.artist.client.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class MaterialController {
    private static Minecraft client;
    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialController.class);

    public static void start(boolean async){
        client = Minecraft.getInstance();
        Config.blockList.clear();
        Config.emptyPos.clear();
        if (async )new Thread(MaterialController::querySchematic).start();
        else MaterialController.querySchematic();
    }

    public static boolean searchPlacement(){
        List<SchematicPlacement> allPlacements = DataManager.getSchematicPlacementManager().getAllSchematicsPlacements();
        client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        assert player != null;
        Vec3 playerPos = player.position();

        Path newPath = allPlacements.stream().filter(placement -> {
            Box box = placement.getEclosingBox();
            if (box == null || box.getPos1() == null || box.getPos2() == null) return false;
            return new AABB(new Vec3(box.getPos1()), new Vec3(box.getPos2())).contains(playerPos)
                    && placement.getSchematicFile() != null;
        }).map(SchematicPlacement::getSchematicFile).findFirst().orElse(null);

        if (newPath == null) {
            if (Config.schematicName == null || Config.schematicName.isEmpty()) {
                player.displayClientMessage(Component.literal("§4Error: Schematic Not Found, Please Load a Schematic via Command or Litematica"), false);
                return false;
            }

            player.displayClientMessage(Component.literal("§4Error: Schematic Not Found, Using Pre-Loaded Schematic..."), false);
            return true;
        }

        if (newPath.normalize().toAbsolutePath().equals(Config.schematicPath.normalize().toAbsolutePath())) return true;

        Path schematicsDir = client.gameDirectory.toPath().resolve("schematics");
        Config.schematicPath = newPath;

        try {
            Config.schematicName = newPath.relativize(schematicsDir).toString();
        } catch (IllegalArgumentException e) {
            Config.schematicName = newPath.getFileName().toString();
        }

        player.displayClientMessage(Component.literal("§bSchematic Has Changed, Loading new Schematic..."), false);
        start(false);

        // It's hard to make start return if its successful, because we need to consider async load.
        // So we can detect if we loaded it correctly by checking if the block list is empty or not
        return !Config.blockList.isEmpty();
    }

    private static void reportError(String name){
        LOGGER.error("Failed to find tag {} in nbt", name);
        assert client.player != null;
        client.player.displayClientMessage(Component.literal("§4Failed to fin tag: " + name), false);
        throw new RuntimeException("Tag Not Found");
    }

    private static void querySchematic(){
        assert client.player != null;
        client.execute(() ->
            client.player.displayClientMessage(Component.literal("§6Loading Schematics " + Config.schematicName), false)
        );
        CompoundTag root;

        try {
            root = NbtIo.readCompressed(Config.schematicPath, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            LOGGER.error("Cannot resolve schematic file: {}", e.getMessage());
            client.player.displayClientMessage(Component.literal("§4Cannot resolve schematic file: " + Config.schematicName), false);
            return;
        }

        Optional<ListTag> paletteOptional = root.getList("palette");
        if (paletteOptional.isEmpty()){
            reportError("palette");
            return;
        }

        ListTag palette = paletteOptional.get();
        List<Config.Carpets> mapping = new ArrayList<>(Collections.nCopies(100, null));
        int gray_mapping = -1;

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

            if (id.equals("minecraft:gray_carpet")) gray_mapping = i;

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
        int finalGray_mapping = gray_mapping;
        blocks.forEach(tag -> {
            CompoundTag compound = tag.asCompound().orElse(new CompoundTag());
            ListTag list = compound.getListOrEmpty("pos");
            if (list.size() != 3) {
                reportError("pos");
                return;
            }

            int state = compound.getIntOr("state", -1);
            int x = list.getFirst().asInt().orElse(-1);
            int y = list.get(1).asInt().orElse(-1);
            int z = list.get(2).asInt().orElse(-1);
            if (x == -1 || y == -1 || z == -1) return;

            BlockPos pos = new BlockPos(x, y, z).offset(Config.offset);

            if (state == finalGray_mapping){
                Config.emptyPos.add(pos);
                return;
            }

            if (state == -1 || state >= 100 || mapping.get(state) == null) return;

            //Config.blockMap.put(pos, mapping.get(state).block);
            Config.blockList.computeIfAbsent(mapping.get(state), a -> new HashSet<>()).add(pos);
        });

        client.execute(() ->
                client.player.displayClientMessage(Component.literal("Loading Succeeded"), false)
        );
        Config.targets.addAll(Arrays.asList(Config.Carpets.values()));
        //updateState(false);
    }
}
