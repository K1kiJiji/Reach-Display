package kikijiji.reachdisplay.config;


import java.util.function.IntConsumer;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;


public class ColorPickerScreen extends Screen
{
    private final Screen parent;
    private final IntConsumer onColorPicked;
    private int argb;

    private float hue;
    private float saturation;
    private float value;

    private float alpha;

    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    private static final int ALPHA_WIDTH  = 120;
    private static final int ALPHA_HEIGHT = 8;

    private static final int SV_WIDTH  = 120;
    private static final int SV_HEIGHT = 80;
    private static final int HUE_WIDTH = 120;
    private static final int HUE_HEIGHT = 10;


    public ColorPickerScreen(Screen parent, int initialColor, IntConsumer onColorPicked)
    {
        super(Text.literal("Pick Color"));
        this.parent = parent;
        this.onColorPicked = onColorPicked;
        this.argb = initialColor;

        int a = (initialColor >> 24) & 0xFF;
        alpha = a / 255.0f;
        int r = (initialColor >> 16) & 0xFF;
        int g = (initialColor >> 8)  & 0xFF;
        int b = (initialColor)       & 0xFF;

        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        value = max;

        if (max == 0)
        {
            saturation = 0;
        }
        else
        {
            saturation = delta / max;
        }

        if (delta == 0)
        {
            hue = 0;
        }
        else if (max == rf)
        {
            hue = ((gf - bf) / delta) % 6.0f;
        }
        else if (max == gf)
        {
            hue = (bf - rf) / delta + 2.0f;
        }
        else
        {
            hue = (rf - gf) / delta + 4.0f;
        }
        hue /= 6.0f;
        if (hue < 0) hue += 1.0f;
    }

    @Override
    protected void init()
    {
        this.clearChildren();

        int buttonWidth = 60;
        int buttonHeight = 20;
        int y = this.height - 40;
        int centerX = this.width / 2;

        // Done
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Done"),
                buttonWidget ->
                {
                    if (onColorPicked != null)
                    {
                        onColorPicked.accept(argb);
                    }

                    MinecraftClient.getInstance().setScreen(parent);
                }

        ).dimensions(centerX - buttonWidth - 4, y, buttonWidth, buttonHeight).build());

        // Cancel
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Cancel"),
                buttonWidget -> MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(centerX + 4, y, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        super.render(ctx, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int topY = 40;

        int previewW = 40;
        int previewH = 20;
        int previewX = centerX - previewW / 2;
        int previewY = topY;
        ctx.fill(previewX, previewY, previewX + previewW, previewY + previewH, argb);

        topY += previewH + 10;

        int svX = centerX - SV_WIDTH / 2;
        int svY = topY;
        drawSVArea(ctx, svX, svY);

        int svCursorX = svX + (int)(saturation * (SV_WIDTH - 1));
        int svCursorY = svY + (int)((1.0f - value) * (SV_HEIGHT - 1));
        ctx.drawBorder(svCursorX - 2, svCursorY - 2, 4, 4, 0xFFFFFFFF);

        topY += SV_HEIGHT + 8;

        int hueX = centerX - HUE_WIDTH / 2;
        int hueY = topY;
        drawHueBar(ctx, hueX, hueY);

        int hueCursorX = hueX + (int)(hue * (HUE_WIDTH - 1));
        ctx.drawBorder(hueCursorX - 2, hueY - 2, 4, HUE_HEIGHT + 4, 0xFFFFFFFF);

        topY += HUE_HEIGHT + 8;

        int alphaX = centerX - ALPHA_WIDTH / 2;
        int alphaY = topY;
        drawAlphaBar(ctx, alphaX, alphaY);

        int alphaCursorX = alphaX + (int)(alpha * (ALPHA_WIDTH - 1));
        ctx.drawBorder(alphaCursorX - 2, alphaY - 2, 4, ALPHA_HEIGHT + 4, 0xFFFFFFFF);

        String hex = String.format("#%08X", argb);
        ctx.drawCenteredTextWithShadow
        (
                this.textRenderer,
                Text.literal(hex),
                centerX,
                alphaY + ALPHA_HEIGHT + 10,
                0xFFFFFFFF
        );
    }

    private void drawSVArea(DrawContext ctx, int svX, int svY)
    {
        for (int x = 0; x < SV_WIDTH; x++)
        {
            float s = x / (float)(SV_WIDTH - 1);
            for (int y = 0; y < SV_HEIGHT; y++)
            {
                float v = 1.0f - y / (float)(SV_HEIGHT - 1);
                int rgb = hsvToRgb(hue, s, v);
                ctx.fill(svX + x, svY + y, svX + x + 1, svY + y + 1, 0xFF000000 | rgb);
            }
        }
    }

    private void drawHueBar(DrawContext ctx, int hueX, int hueY)
    {
        for (int x = 0; x < HUE_WIDTH; x++)
        {
            float h = x / (float)(HUE_WIDTH - 1);
            int rgb = hsvToRgb(h, 1.0f, 1.0f);
            ctx.fill(hueX + x, hueY, hueX + x + 1, hueY + HUE_HEIGHT, 0xFF000000 | rgb);
        }
    }

    private void drawAlphaBar(DrawContext ctx, int alphaX, int alphaY)
    {
        int steps = ALPHA_WIDTH;
        for (int x = 0; x < steps; x++)
        {
            float a = x / (float)(steps - 1);
            int rgb = argb & 0x00FFFFFF;
            int color = ((int)(a * 255) << 24) | rgb;

            ctx.fill(alphaX + x, alphaY, alphaX + x + 1, alphaY + ALPHA_HEIGHT, color);
        }
    }

    private static int hsvToRgb(float h, float s, float v)
    {
        float r, g, b;

        int i = (int)(h * 6.0f);
        float f = h * 6.0f - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - f * s);
        float t = v * (1.0f - (1.0f - f) * s);

        switch (i % 6)
        {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            case 5 -> { r = v; g = p; b = q; }
            default -> { r = v; g = t; b = p; }
        }

        int ri = (int)(r * 255.0f) & 0xFF;
        int gi = (int)(g * 255.0f) & 0xFF;
        int bi = (int)(b * 255.0f) & 0xFF;
        return (ri << 16) | (gi << 8) | bi;
    }

    private void updateColorFromHSV()
    {
        int rgb = hsvToRgb(hue, saturation, value);
        int a = (int)(alpha * 255.0f) & 0xFF;
        argb = (a << 24) | rgb;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            int centerX = this.width / 2;
            int topY = 40 + 20 + 10;
            int svX = centerX - SV_WIDTH / 2;

            if (mouseX >= svX && mouseX < svX + SV_WIDTH && mouseY >= topY && mouseY < topY + SV_HEIGHT)
            {
                draggingSV = true;
                updateSVFromMouse(mouseX, mouseY, svX, topY);
                return true;
            }

            int hueX = centerX - HUE_WIDTH / 2;
            int hueY = topY + SV_HEIGHT + 8;

            if (mouseX >= hueX && mouseX < hueX + HUE_WIDTH && mouseY >= hueY && mouseY < hueY + HUE_HEIGHT)
            {
                draggingHue = true;
                updateHueFromMouse(mouseX, hueX);
                return true;
            }

            int alphaX = centerX - ALPHA_WIDTH / 2;
            int alphaY = hueY + HUE_HEIGHT + 8;

            if (mouseX >= alphaX && mouseX < alphaX + ALPHA_WIDTH && mouseY >= alphaY && mouseY < alphaY + ALPHA_HEIGHT)
            {
                draggingAlpha = true;
                updateAlphaFromMouse(mouseX, alphaX);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy)
    {
        if (button == 0)
        {
            int centerX = this.width / 2;
            int svY = 40 + 20 + 10;
            int svX = centerX - SV_WIDTH / 2;

            int hueX = centerX - HUE_WIDTH / 2;

            int alphaX = centerX - ALPHA_WIDTH / 2;

            boolean updated = false;
            if (draggingSV)
            {
                updateSVFromMouse(mouseX, mouseY, svX, svY);
                updated = true;
            }
            else if (draggingHue)
            {
                updateHueFromMouse(mouseX, hueX);
                updated = true;
            }
            else if (draggingAlpha)
            {
                updateAlphaFromMouse(mouseX, alphaX);
                updated = true;
            }

            if (updated) return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            draggingSV = draggingHue = draggingAlpha = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSVFromMouse(double mouseX, double mouseY, int svX, int svY)
    {
        double localX = Math.max(0, Math.min(mouseX - svX, SV_WIDTH  - 1));
        double localY = Math.max(0, Math.min(mouseY - svY, SV_HEIGHT - 1));
        saturation = (float)(localX / (SV_WIDTH - 1));
        value      = 1.0f - (float)(localY / (SV_HEIGHT - 1));
        updateColorFromHSV();
    }

    private void updateHueFromMouse(double mouseX, int hueX)
    {
        double localX = Math.max(0, Math.min(mouseX - hueX, HUE_WIDTH - 1));
        hue = (float)(localX / (HUE_WIDTH - 1));
        updateColorFromHSV();
    }

    private void updateAlphaFromMouse(double mouseX, int alphaX)
    {
        double localX = Math.max(0, Math.min(mouseX - alphaX, ALPHA_WIDTH - 1));
        alpha = (float)(localX / (ALPHA_WIDTH - 1));
        updateColorFromHSV();
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        MinecraftClient.getInstance().setScreen(parent);
        return false;
    }
}
