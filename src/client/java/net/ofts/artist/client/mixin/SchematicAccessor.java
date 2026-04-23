package net.ofts.artist.client.mixin;

import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(LitematicaSchematic.class)
public interface SchematicAccessor {
    @Accessor("blockContainers")
    @Final
    Map<String, LitematicaBlockStateContainer> getBlockContainers();
}
