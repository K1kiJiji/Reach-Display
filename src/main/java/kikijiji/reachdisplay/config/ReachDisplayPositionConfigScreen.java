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
        if (client != null && ReachDisplay.CONFIG != null)
        {
            String text = "2.88 blocks";
            ReachDisplayConfig cfg = this.config;

            int w = this.textRenderer.getWidth(text);
            int h = this.textRenderer.fontHeight;
            int pad = cfg.showBackground ? 2 : 0;

            int hudW = w + pad * 2;
            int hudH = h + pad * 2;

            int baseX = tempOffsetX;
            int baseY = tempOffsetY;

            int maxX = this.width  - hudW;
            int maxY = this.height - hudH - 40;

            if (maxX < 0) maxX = 0;
            if (maxY < 0) maxY = 0;

            baseX = Math.max(0, Math.min(baseX, maxX));
            baseY = Math.max(0, Math.min(baseY, maxY));

            tempOffsetX = baseX;
            tempOffsetY = baseY;

            // 배경 박스
            if (cfg.showBackground)
            {
                ctx.fill(baseX, baseY, baseX + hudW, baseY + hudH, cfg.backgroundColor);
            }

            // 그림자
            if (cfg.showShadow)
            {
                ctx.drawText(this.textRenderer, text, baseX + pad + 1, baseY + pad + 1, cfg.shadowColor, false);
            }

            // 메인 텍스트
            if (cfg.showReach)
            {
                ctx.drawText(this.textRenderer, text, baseX + pad, baseY + pad, cfg.mainColor, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            String text = "2.88 blocks";
            ReachDisplayConfig config = this.config;
            int w = this.textRenderer.getWidth(text);
            int h = this.textRenderer.fontHeight;
            int pad = config.showBackground ? 2 : 0;
            int hudW = w + pad * 2;
            int hudH = h + pad * 2;

            int baseX = tempOffsetX;
            int baseY = tempOffsetY;

            boolean inside = mouseX >= baseX && mouseX < baseX + hudW && mouseY >= baseY && mouseY < baseY + hudH;

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
            int rawX = startOffsetX + (int)(mouseX - dragStartX);
            int rawY = startOffsetY + (int)(mouseY - dragStartY);

            String text = "2.88 blocks";
            ReachDisplayConfig config = this.config;
            int w = this.textRenderer.getWidth(text);
            int h = this.textRenderer.fontHeight;
            int pad = config.showBackground ? 2 : 0;
            int hudW = w + pad * 2;
            int hudH = h + pad * 2;

            int maxX = this.width  - hudW;
            int maxY = this.height - hudH - 40;

            if (maxX < 0) maxX = 0;
            if (maxY < 0) maxY = 0;

            tempOffsetX = Math.max(0, Math.min(rawX, maxX));
            tempOffsetY = Math.max(0, Math.min(rawY, maxY));
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