package net.pcal.fastback.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.pcal.fastback.ModContext;
import net.pcal.fastback.tasks.RestoreSnapshotTask;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static net.minecraft.server.command.CommandManager.*;
import static net.pcal.fastback.commands.Commands.SUCCESS;
import static net.pcal.fastback.commands.Commands.commandLogger;
import static net.pcal.fastback.commands.Commands.executeStandard;

public class RestoreCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> argb, ModContext ctx) {
        final RestoreCommand rc = new RestoreCommand(ctx);
        argb.then(literal("restore").
                then(argument("snapshot", StringArgumentType.string()).
                        suggests(new SnapshotNameSuggestions(ctx)).
                        executes(rc::execute)));
    }

    private final ModContext ctx;

    private RestoreCommand(ModContext context) {
        this.ctx = requireNonNull(context);
    }

    private int execute(CommandContext<ServerCommandSource> cc) {
        return executeStandard(this.ctx, cc, (gitc, wc, tali) -> {
            final String snapshotName = cc.getLastChild().getArgument("snapshot", String.class);
            final Path restoresDir = this.ctx.getRestoresDir();
            final MinecraftServer server = cc.getSource().getServer();
            final String worldName = this.ctx.getWorldName(server);
            final Path worldSaveDir = this.ctx.getWorldSaveDirectory(server);
            this.ctx.getExecutorService().execute(
                    RestoreSnapshotTask.create(worldSaveDir, snapshotName, worldName, restoresDir, commandLogger(ctx, cc)));
            return SUCCESS;
        });
    }
}