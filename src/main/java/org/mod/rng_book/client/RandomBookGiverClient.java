package org.mod.rng_book.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.mod.rng_book.RandomBookGiver;

import java.util.Objects;

public class RandomBookGiverClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricKeyBinding keyBinding = FabricKeyBinding.Builder.create(
                new Identifier("rng_book_giver", "keybind.random_book_giver"),
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.misc"
        ).build();
        KeyBindingRegistry.INSTANCE.register(keyBinding);

        ClientTickCallback.EVENT.register((client) -> {
            IntegratedServer server = client.getServer();
            while (keyBinding.wasPressed() && client.world != null && client.player != null && server != null && server.getPermissionLevel(client.player.getGameProfile()) > 2) {
                RandomBookGiver.giveRandomBook(client.world.getWorld(),
                        Objects.requireNonNull(server.getPlayerManager().getPlayer(client.player.getUuid())));
            }
        });
    }
}
