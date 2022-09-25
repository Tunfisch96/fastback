/*
 * FastBack - Fast, incremental Minecraft backups powered by Git.
 * Copyright (C) 2022 pcal.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package net.pcal.fastback.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.pcal.fastback.LifecycleUtils;
import net.pcal.fastback.ModContext;
import net.pcal.fastback.fabric.mixins.ScreenAccessors;

import java.nio.file.Path;

/**
 * Initializer that runs in a client.
 *
 * @author pcal
 * @since 0.0.1
 */
public class FabricClientModInitializer implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        final FabricServiceProvider fabricProvider = FabricServiceProvider.
                forClient(new FabricClientProviderImpl());
        final ModContext modContext = ModContext.create(fabricProvider);

        ServerLifecycleEvents.SERVER_STARTING.register(
                minecraftServer -> {
                    fabricProvider.setMinecraftServer(minecraftServer);
                    LifecycleUtils.onWorldStart(modContext);
                }
        );
        ServerLifecycleEvents.SERVER_STOPPED.register(
                minecraftServer -> {
                    LifecycleUtils.onWorldStop(modContext);
                    fabricProvider.setMinecraftServer(null);
                }
        );
        ClientLifecycleEvents.CLIENT_STOPPING.register(
                minecraftServer -> {
                    LifecycleUtils.onTermination(modContext);
                }
        );
        LifecycleUtils.onInitialize(modContext);
    }

    private static class FabricClientProviderImpl implements FabricClientProvider {

        @Override
        public void consumeSaveScreenText(Text text) {
            final MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                final Screen screen = client.currentScreen;
                if (screen instanceof MessageScreen) {
                    ((ScreenAccessors) screen).setTitle(text);
                }
            }
        }

        @Override
        public Path getClientRestoreDir() {
            return FabricLoader.getInstance().getGameDir().resolve("saves");
        }

        @Override
        public void sendClientChatMessage(Text text) {
            final MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.inGameHud.getChatHud().addMessage(text);
            }
        }
    }
}