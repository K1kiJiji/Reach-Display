package kikijiji.reachdisplay;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.entity.Entity;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import kikijiji.reachdisplay.config.ReachDisplayConfig;
import kikijiji.reachdisplay.config.ReachDisplayConfigManager;
import kikijiji.reachdisplay.config.ReachDisplayPositionConfigScreen;


public class ReachDisplay implements ClientModInitializer
{
    public static final String MOD_ID = "reach-display";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ReachDisplayConfig CONFIG;

    private static double lastHitDistance = 0.0;
    private static long   lastHitTime     = 0;

    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");


    @Override
    public void onInitializeClient()
    {
        CONFIG = ReachDisplayConfigManager.load();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
        {
            if (!world.isClient() || entity == null)
            {
                return ActionResult.PASS;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            lastHitDistance = computeHitDistance(client, entity);
            lastHitTime = System.currentTimeMillis();

            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register(ReachDisplay::renderHud);
    }


    /* ----- 거리 계산 ----- */
    private static double computeHitDistance(MinecraftClient client, Entity target)
    {
        var player = client.player;
        if (player == null || target == null)
        {
            return 0.0;
        }

        Vec3d eyePosition = player.getCameraPosVec(1.0F);
        Box hitBox = target.getBoundingBox();

        double clampedX = MathHelper.clamp(eyePosition.x, hitBox.minX, hitBox.maxX);
        double clampedY = MathHelper.clamp(eyePosition.y, hitBox.minY, hitBox.maxY);
        double clampedZ = MathHelper.clamp(eyePosition.z, hitBox.minZ, hitBox.maxZ);

        double dx = clampedX - eyePosition.x;
        double dy = clampedY - eyePosition.y;
        double dz = clampedZ - eyePosition.z;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /* ----- HUD 표시 ----- */
    private static void renderHud(DrawContext drawContext, RenderTickCounter tickCounter)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null)
        {
            return;
        }
        if (CONFIG == null)
        {
            return;
        }

        if (client.currentScreen instanceof ReachDisplayPositionConfigScreen)
        {
            return;
        }

        ReachDisplayConfig config = CONFIG;

        // 표시
        if (!config.showReach)
        {
            return;
        }
        long now = System.currentTimeMillis();
        double reachDisplay = lastHitDistance;

        // 표시 유지 방식
        if (!config.keepLastHitDistance && lastHitTime > 0)
        {
            double elapsedSeconds = (now - lastHitTime) / 1000.0;
            if (elapsedSeconds > config.resetAfterSeconds)
            {
                reachDisplay = 0.0;
            }
        }
        if (lastHitTime == 0)
        {
            reachDisplay = 0.0;
        }

        // 표시 포맷
        String textFormat = FORMAT.format(reachDisplay);
        String text = switch (config.displayMode)
        {
            case NUMBER_ONLY -> textFormat;
            case WITH_BLOCKS -> textFormat + " blocks";
            case WITH_M ->      textFormat + " M";
        };

        // 크기
        float scale = Math.max(0.0f, config.scale) / 100.0f;
        if (scale <= 0.0f)
        {
            return;
        }

        var matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(4 + config.offsetX, 4 + config.offsetY, 0);
        matrices.scale(scale, scale, 1.0f);


        // 배경
        if (config.showBackground)
        {
            int x1 = -2;
            int y1 = -2;
            int x2 = client.textRenderer.getWidth(text) + 2;
            int y2 = client.textRenderer.fontHeight + 2;
            drawContext.fill
            (
                    x1,
                    y1,
                    x2,
                    y2,
                    config.backgroundColor
            );
        }
        // 그림자
        if (config.showShadow)
        {
            drawContext.drawText
            (
                    client.textRenderer,
                    text,
                    1,
                    1,
                    config.shadowColor,
                    false
            );
        }
        // 텍스트
        drawContext.drawText
        (
                client.textRenderer,
                text,
                0,
                0,
                config.textColor,
                false
        );

        matrices.pop();
    }
}