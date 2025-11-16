package kikijiji.reachdisplay.config;


import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import kikijiji.reachdisplay.ReachDisplay;


public class ReachDisplayPositionConfigScreen extends Screen
{
    private final Screen parent;
    private final ReachDisplayConfig config;

    private int tempOffsetX;
    private int tempOffsetY;

    private int dragStartX;
    private int dragStartY;
    private int startOffsetX;
    private int startOffsetY;
    private boolean dragging = false;

    public ReachDisplayPositionConfigScreen(Screen parent, ReachDisplayConfig config)
    {
        super(Text.literal("Adjust Reach Position"));
        this.parent = parent;
        this.config = config;
        this.tempOffsetX = config.offsetX;
        this.tempOffsetY = config.offsetY;
    }

    @Override
    protected void init()
    {
        this.clearChildren();

        int buttonHeight = 20;
        int y = this.height - 40;

        int widthTotal = 3 * 80 + 2 * 8;
        int x = (this.width - widthTotal) / 2;

        // Reset
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Reset"),
                buttonWidget ->
                {
                    tempOffsetX = 0;
                    tempOffsetY = 0;
                }

        ).dimensions(x, y, 80, buttonHeight).build());

        // Save
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Save"),
                buttonWidget ->
                {
                    config.offsetX = tempOffsetX;
                    config.offsetY = tempOffsetY;
                }

        ).dimensions(x + 80 + 8, y, 80, buttonHeight).build());

        // Done
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Done"),
                buttonWidget ->
                {
                    config.offsetX = tempOffsetX;
                    config.offsetY = tempOffsetY;
                    MinecraftClient.getInstance().setScreen(parent);
                }

        ).dimensions(x + (80 + 8) * 2, y, 80, buttonHeight).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        MinecraftClient client = MinecraftClient.getInstance();

        // 하단 버튼
        super.render(ctx, mouseX, mouseY, delta);

        // 안내 텍스트
        ctx.drawCenteredTextWithShadow
        (
                this.textRenderer,
                Text.literal("Position Preview"),
                this.width / 2,
                20,
                0xFFFFFF
        );

        if (client != null && ReachDisplay.CONFIG != null)
        {
            String text = "2.88 blocks";
            ReachDisplayConfig config = this.config;

            int w   = this.textRenderer.getWidth(text);
            int h   = this.textRenderer.fontHeight;
            int pad = config.showBackground ? 2 : 0;

            float scale = Math.max(0.1f, config.scale / 100.0f);

            int anchorX = 4 + tempOffsetX;
            int anchorY = 4 + tempOffsetY;

            var matrices = ctx.getMatrices();
            matrices.push();

            matrices.translate(anchorX, anchorY, 0);
            matrices.scale(scale, scale, 1.0f);

            int x = 0;
            int y = 0;

            // 배경 박스
            if (config.showBackground)
            {
                int x1 = x - pad;
                int y1 = y - pad;
                int x2 = x + w + pad;
                int y2 = y + h + pad;
                ctx.fill(x1, y1, x2, y2, config.backgroundColor);
            }

            // 그림자
            if (config.showShadow)
            {
                ctx.drawText(this.textRenderer, text, x + 1, y + 1, config.shadowColor, false);
            }

            // 메인 텍스트
            if (config.showReach)
            {
                ctx.drawText(this.textRenderer, text, x, y, config.mainColor, false);
            }

            matrices.pop();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            String text = "2.88 blocks";
            ReachDisplayConfig cfg = this.config;
            int w   = this.textRenderer.getWidth(text);
            int h   = this.textRenderer.fontHeight;
            int pad = cfg.showBackground ? 2 : 0;

            int hudW = w + pad * 2;
            int hudH = h + pad * 2;

            float scale = Math.max(0.1f, cfg.scale / 100.0f);
            int scaledW = (int)Math.ceil(hudW * scale);
            int scaledH = (int)Math.ceil(hudH * scale);

            int anchorX = 4 + tempOffsetX;
            int anchorY = 4 + tempOffsetY;

            boolean inside = mouseX >= anchorX && mouseX < anchorX + scaledW && mouseY >= anchorY && mouseY < anchorY + scaledH;

            if (inside)
            {
                dragging = true;
                dragStartX = (int) mouseX;
                dragStartY = (int) mouseY;
                startOffsetX = tempOffsetX;
                startOffsetY = tempOffsetY;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy)
    {
        if (dragging && button == 0)
        {
            int rawOffsetX = startOffsetX + (int)(mouseX - dragStartX);
            int rawOffsetY = startOffsetY + (int)(mouseY - dragStartY);

            ReachDisplayConfig cfg = this.config;
            String text = "2.88 blocks";
            int w   = this.textRenderer.getWidth(text);
            int h   = this.textRenderer.fontHeight;
            int pad = cfg.showBackground ? 2 : 0;

            int hudW = w + pad * 2;
            int hudH = h + pad * 2;

            float scale = Math.max(0.1f, cfg.scale / 100.0f);
            int scaledW = (int) Math.ceil(hudW * scale);
            int scaledH = (int) Math.ceil(hudH * scale);

            int anchorX = 4 + rawOffsetX;
            int anchorY = 4 + rawOffsetY;

            int leftMargin   = 0;
            int rightMargin  = 0;
            int topMargin    = 0;
            int bottomMargin = 0;

            int maxX = this.width  - scaledW - rightMargin;
            int maxY = this.height - scaledH - bottomMargin;

            if (maxX < 4) maxX = leftMargin;
            if (maxY < 4) maxY = topMargin;

            anchorX = Math.max(4, Math.min(anchorX, maxX));
            anchorY = Math.max(4, Math.min(anchorY, maxY));

            tempOffsetX = anchorX - 4;
            tempOffsetY = anchorY - 4;

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            dragging = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        MinecraftClient.getInstance().setScreen(parent);
        return false;
    }
}