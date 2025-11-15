package kikijiji.reachdisplay;


import java.text.DecimalFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import kikijiji.reachdisplay.config.ReachDisplayConfig;
import kikijiji.reachdisplay.config.ReachDisplayConfigManager;
import kikijiji.reachdisplay.config.ReachDisplayPositionConfigScreen;


public class ReachDisplay implements ClientModInitializer
{
    public static final String MOD_ID = "reach-display";
    public static ReachDisplayConfig CONFIG;

    private static double lastDistance = 0.0;
    private static long lastHitTime    = 0;

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

            if (!isEntityAllowed(entity))
            {
                return ActionResult.PASS;
            }

            lastDistance = Math.sqrt(player.squaredDistanceTo(entity));
            lastHitTime  = System.currentTimeMillis();

            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register(ReachDisplay::renderHud);
    }


    // 엔티티 화이트리스트/블랙리스트
    private static boolean isEntityAllowed(Entity entity)
    {
        ReachDisplayConfig config = CONFIG;
        Identifier id = Registries.ENTITY_TYPE.getId(entity.getType());
        String key = id.toString();

        if (config.useWhitelist && !config.whitelist.isEmpty())
        {
            if (!config.whitelist.contains(key))
            {
                return false;
            }
        }

        return config.blacklist.isEmpty() || !config.blacklist.contains(key);
    }

    // Hud 표시
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

        // 표시 유지 방식
        ReachDisplayConfig config = CONFIG;

        if (!config.showReach)
        {
            return;
        }

        long now = System.currentTimeMillis();
        double reachDisplay = lastDistance;
        if (!config.keepLastDistance && lastHitTime > 0)
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
        String numberText = FORMAT.format(reachDisplay);
        String text = switch (config.displayMode)
        {
            case NUMBER_ONLY -> numberText;
            case WITH_BLOCKS -> numberText + " blocks";
            case WITH_M ->      numberText + " M";
        };

        int textWidth  = client.textRenderer.getWidth(text);
        int textHeight = client.textRenderer.fontHeight;

        // 크기
        float scale = Math.max(0.0f, config.scale) / 100.0f;
        if (scale <= 0.0f)
        {
            return;
        }

        int anchorX = 4 + config.offsetX;
        int anchorY = 4 + config.offsetY;

        var matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(anchorX, anchorY, 0);
        matrices.scale(scale, scale, 1.0f);

        // 거리별 색상
        int mainColor       = config.mainColor;
        int shadowColor     = config.shadowColor;
        int backgroundColor = config.backgroundColor;

        int x = 0;
        int y = 0;

        if (config.enableDistanceColor && !config.distanceBands.isEmpty())
        {
            ReachDisplayConfig.DistanceColorBand chosen = null;
            for (ReachDisplayConfig.DistanceColorBand band : config.distanceBands)
            {
                if (reachDisplay <= band.maxDistance)
                {
                    chosen = band;
                    break;
                }
            }

            if (chosen == null)
            {
                chosen = config.distanceBands.getLast();
            }

            mainColor       = chosen.mainColor;
            shadowColor     = chosen.shadowColor;
            backgroundColor = chosen.backgroundColor;
        }

        // 배경
        if (config.showBackground)
        {
            int padding = 2;
            int x1 = x - padding;
            int y1 = y - padding;
            int x2 = x + textWidth + padding;
            int y2 = y + textHeight + padding;
            drawContext.fill(x1, y1, x2, y2, backgroundColor);
        }

        // 그림자
        if (config.showShadow)
        {
            drawContext.drawText
            (
                    client.textRenderer,
                    text,
                    x + 1,
                    y + 1,
                    shadowColor,
                    false
            );
        }

        // 메인 텍스트
        drawContext.drawText
        (
                client.textRenderer,
                text,
                x,
                y,
                mainColor,
                false
        );

        matrices.pop();
    }
}