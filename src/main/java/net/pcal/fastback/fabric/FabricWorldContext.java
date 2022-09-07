package net.pcal.fastback.fabric;

import net.pcal.fastback.ModContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import net.pcal.fastback.WorldUtils;
import net.pcal.fastback.fabric.mixins.ServerAccessors;
import net.pcal.fastback.fabric.mixins.SessionAccessors;

import java.io.IOException;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

class FabricWorldContext implements ModContext.WorldContext {

    private final ModContext modContext;
    private final MinecraftServer server;
    private final LevelStorage.Session session;

    FabricWorldContext(ModContext modContext, MinecraftServer server) {
        this.modContext = requireNonNull(modContext);
        this.server = requireNonNull(server);
        this.session = requireNonNull(((ServerAccessors) server).getSession());
    }

    @Override
    public ModContext getModContext() {
        return this.modContext;
    }

    @Override
    public String getWorldUuid() throws IOException {
        return WorldUtils.getWorldUuid(this.getWorldSaveDirectory());
    }

    @Override
    public Path getWorldSaveDirectory() {
        final LevelStorage.Session session = ((ServerAccessors) server).getSession();
        return ((SessionAccessors) session).getDirectory().path();
    }

    @Override
    public String getWorldName() {
        return this.getLevelInfo().getLevelName();
    }

    @Override
    public long getSeed() {
        return server.getSaveProperties().getGeneratorOptions().getSeed();
    }

    @Override
    public String getGameMode() {
        return String.valueOf(getLevelInfo().getGameMode());
    }

    @Override
    public String getDifficulty() {
        return String.valueOf(getLevelInfo().getDifficulty());
    }

    @Override
    public String getMinecraftVersion() {
        // FIXME figure out how to move this to ModContext
        return server.getVersion();
    }

    private LevelInfo getLevelInfo() {
        return requireNonNull(session.getLevelSummary().getLevelInfo());
    }

}