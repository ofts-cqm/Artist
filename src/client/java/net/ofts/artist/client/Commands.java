package net.ofts.artist.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ofts.artist.client.comtroller.MaterialController;
import net.ofts.artist.client.comtroller.MovementController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Commands {
    public static void buildCommand(CommandDispatcher<FabricClientCommandSource> dispatcher){
        LiteralArgumentBuilder<FabricClientCommandSource> builder = LiteralArgumentBuilder.literal("artist");

        builder.executes(a -> sendUsageGuide());

        //builder.then(buildLoader());

        builder.then(buildQuerier());

        builder.then(buildTargeter());

        builder.then(buildStart());

        builder.then(buildStop());

        builder.then(buildOffset());

        builder.then(buildState());

        builder.then(buildAudit());

        dispatcher.register(builder);
    }

    private static void sendMessage(String s) {
        assert Minecraft.getInstance().player != null;
        Minecraft.getInstance().player.displayClientMessage(Component.literal(s), false);
    }

    private static int sendUsageGuide(){
        sendMessage("§7/artist load <schematic>");
        sendMessage("§7/artist query");
        return 1;
    }

    @Deprecated
    private static int onLoad(CommandContext<FabricClientCommandSource> ctx){
        Config.schematicName = StringArgumentType.getString(ctx, "schematic");
        Path schematicsDir = Minecraft.getInstance().gameDirectory.toPath().resolve("schematics");
        Config.schematicPath = schematicsDir.resolve(Config.schematicName);
        MaterialController.start(true);
        return 1;
    }

    private static int onQuery(CommandContext<FabricClientCommandSource> ctx){
        BlockPos pos = ctx.getSource().getPlayer().getOnPos();
        AtomicBoolean found = new AtomicBoolean(false);
        Config.blockList.forEach((type, arr) -> {
            if (arr.contains(pos)){
                found.set(true);
                Minecraft.getInstance().execute(() -> {
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("Found Carpet " + type.name() + " at position " + pos.toShortString()), false);
                });
            }
        });
        if (!found.get()){
            Minecraft.getInstance().execute(() -> {
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Data Not Found"), false);
            });
        }
        return found.get() ? 1 : 0;
    }

    private static int onOffset(CommandContext<FabricClientCommandSource> ctx) {
        BlockPos pos = ctx.getSource().getPlayer().getOnPos();
        Config.offset = pos;
        Config.placementAABB = new AABB(new Vec3(Config.offset.below()), new Vec3(Config.offset.offset(128, 1, 128)));
        assert Minecraft.getInstance().player != null;
        Minecraft.getInstance().player.displayClientMessage(Component.literal("Offset to " + pos.toShortString() + "!"), false);
        return 1;
    }

    private static int onTarget(CommandContext<FabricClientCommandSource> ctx){
        String[] targets = StringArgumentType.getString(ctx, "targets").split(" ");
        Config.targets.clear();
        for (String target : targets) {
            if (target.equals("ALL")){
                Config.targets.addAll(Arrays.asList(Config.Carpets.values()));
                break;
            }

            try {
                Config.targets.add(Config.Carpets.valueOf(target));
            } catch (IllegalArgumentException e) {
                sendMessage("Color Not Found: " + target);
                return 0;
            }
        }

        assert Minecraft.getInstance().player != null;
        Minecraft.getInstance().player.displayClientMessage(Component.literal("Loaded " + Config.targets.size() + " Colors!"), false);

        return 1;//MaterialController.updateState(true) ? 1 : 0;
    }

    private static int onState(CommandContext<FabricClientCommandSource> ctx){
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;

        player.displayClientMessage(Component.literal("Current Loaded Schematic: " + Config.lastSchematic.getName()), false);
        player.displayClientMessage(Component.literal("Current Targets: "), false);
        for (Config.Carpets target : Config.targets) {
            player.displayClientMessage(Component.literal(target.name()), false);
        }
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestSchematics(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        Path schematicsDir = Minecraft.getInstance().gameDirectory.toPath().resolve("schematics");

        if (!Files.isDirectory(schematicsDir)) {
            return builder.buildFuture();
        }

        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        try (Stream<Path> stream = Files.walk(schematicsDir)) {
            stream.filter(Files::isRegularFile)
                    .map(schematicsDir::relativize)
                    .map(path -> path.toString().replace('\\', '/')) // normalize for command input
                    .filter(path -> path.toLowerCase(Locale.ROOT).startsWith(remaining))
                    .sorted()
                    .forEach(builder::suggest);
        } catch (IOException ignored) {
        }

        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestTarget(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder){
        String remaining = builder.getRemaining(); // text being typed for this arg
        HashSet<String> alreadyUsed = new HashSet<>(Arrays.asList(remaining.trim().split(" ")));
        if (alreadyUsed.contains("ALL")) return builder.buildFuture();

        for (Config.Carpets value : Config.Carpets.values()) {
            builder.suggest(value.name());
        }

        builder.suggest("ALL");
        return builder.buildFuture();
    }

    @Deprecated
    private static LiteralArgumentBuilder<FabricClientCommandSource> buildLoader(){
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("load")
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("schematic", StringArgumentType.greedyString())
                        .executes(Commands::onLoad)
                        .suggests(Commands::suggestSchematics)
                )
                .executes(a -> {
                    sendMessage("§eUsage: /artist load <schematic>");
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildQuerier(){
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("query")
                .executes(Commands::onQuery);
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildOffset(){
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("offset")
                .executes(Commands::onOffset);
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildTargeter(){
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("target")
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("targets", StringArgumentType.greedyString())
                        .executes(Commands::onTarget)
                        .suggests(Commands::suggestTarget)
                )
                .executes(a -> {
                    sendMessage("§eUsage: /artist target <targets>");
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildStart(){
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("start")
                .executes(a -> {
                    MovementController.start();
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildStop(){
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("stop")
                .executes(a -> {
                    MovementController.pause();
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildState(){
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("state")
                .executes(Commands::onState);
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildAudit(){
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("audit")
                .executes((a) -> {
                    RawKeyInjector.enablePrinter();
                    return 1;
                });
    }
}
