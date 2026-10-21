package dev.shadowclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ShadowEspClient implements ClientModInitializer {
    static boolean blockEsp = true;
    static boolean playerEsp = true;
    static boolean vanishDiagnostics = true;

    private static final int BLOCK_RADIUS = 12;
    private static final Set<BlockPos> BLOCKS_TO_DRAW = new HashSet<>();
    private static final Set<UUID> WARNED = new HashSet<>();

    private static KeyBinding toggleBlocks;
    private static KeyBinding togglePlayers;
    private static KeyBinding toggleVanish;
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        toggleBlocks = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.goonclient.block_esp",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KeyBinding.MISC_CATEGORY));

        togglePlayers = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.goonclient.player_esp",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                KeyBinding.MISC_CATEGORY));

        toggleVanish = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.goonclient.vanish_diagnostics",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KeyBinding.MISC_CATEGORY));

        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.goonclient.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.MISC_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) {
                client.setScreen(new GoonClientScreen());
            }

            while (toggleBlocks.wasPressed()) {
                blockEsp = !blockEsp;
                message(client, "Block ESP: " + state(blockEsp));
            }

            while (togglePlayers.wasPressed()) {
                playerEsp = !playerEsp;
                message(client, "Player ESP: " + state(playerEsp));
            }

            while (toggleVanish.wasPressed()) {
                vanishDiagnostics = !vanishDiagnostics;
                WARNED.clear();
                message(client, "Vanish-Diagnose: " + state(vanishDiagnostics));
            }

            if (client.world != null && client.player != null
                    && vanishDiagnostics) {
                checkLoadedPlayersAgainst
