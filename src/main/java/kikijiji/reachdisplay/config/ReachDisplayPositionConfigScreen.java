package kikijiji.reachdisplay.config;


import net.minecraft.text.Text;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import kikijiji.reachdisplay.ReachDisplay;
import net.minecraft.util.Formatting;


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


    /* ----- 제목 ----- */
    public ReachDisplayPositionConfigScreen(Screen parent, ReachDisplayConfig config)
    {
        super(Text.literal("Adjust Reach Position").formatted(Formatting.BOLD));

        this.parent = parent;

        this.config = config;

        this.tempOffsetX = config.offsetX;
        this.tempOffsetY = config.offsetY;
    }


    /* ----- 준비 ----- */
    @Override
    protected void init()
    {
        this.clearChildren();

        int x = (this.width - 3 * 80 + 2 * 8) / 2;
        int y = this.height - 40;

        // Reset
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Reset"),
                buttonWidget ->
                {
                    tempOffsetX = 0;
                    tempOffsetY = 0;
                }

        ).dimensions(x, y, 80, 20).build());
        // Save
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Save"),
                buttonWidget ->
                {
                    config.offsetX = tempOffsetX;
                    config.offsetY = tempOffsetY;
                }

        ).dimensions(x + 80 + 8, y, 80, 20).build());
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

        ).dimensions(x + (80 + 8) * 2, y, 80, 20).build());
    }


    /* ----- 표시 ----- */
    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(drawContext, mouseX, mouseY, delta);
        MinecraftClient client = MinecraftClient.getInstance();

        // 하단 버튼
        super.render(drawContext, mouseX, mouseY, delta);

        // 제목
        drawContext.drawCenteredTextWithShadow
        (
                this.textRenderer,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF
        );


        // HUD 표시
        if (client != null && ReachDisplay.CONFIG != null)
        {
            ReachDisplayConfig config = this.config;

            // 표시 포맷
            String text = "";
            switch (config.displayMode)
            {
                case NUMBER_ONLY  -> text = String.format("%.2f", 2.88);
                case WITH_BLOCKS  -> text = String.format("%.2f blocks", 2.88);
                case WITH_M       -> text = String.format("%.2f M", 2.88);
            }

            // 크기
            float scale = Math.max(0.1f, config.scale / 100.0f);

            var matrices = drawContext.getMatrices();
            matrices.push();
            matrices.translate(4 + tempOffsetX, 4 + tempOffsetY, 0);
            matrices.scale(scale, scale, 1.0f);

            int padding = config.showBackground ? 2 : 0;

            // 배경
            if (config.showBackground)
            {
                int x1 = -padding;
                int y1 = -padding;
                int x2 = this.textRenderer.getWidth(text) + padding;
                int y2 = this.textRenderer.fontHeight + padding;
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
                        this.textRenderer,
                        text,
                        1,
                        1,
                        config.shadowColor,
                        false
                );
            }
            // 텍스트
            if (config.showReach)
            {
                drawContext.drawText
                (
                        this.textRenderer,
                        text,
                        0,
                        0,
                        config.textColor,
                        false
                );
            }

            matrices.pop();
        }
    }


    /* ----- 마우스 클릭 ----- */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            String text = "2.88 blocks";
            ReachDisplayConfig config = this.config;

            int padding = config.showBackground ? 2 : 0;

            int hudW = this.textRenderer.getWidth(text) + padding * 2;
            int hudH = this.textRenderer.fontHeight + padding * 2;

            int scaledW = (int)Math.ceil(hudW * Math.max(0.1f, config.scale / 100.0f));
            int scaledH = (int)Math.ceil(hudH * Math.max(0.1f, config.scale / 100.0f));

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

    /* ----- 마우스 드래그 ----- */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy)
    {
        if (dragging && button == 0)
        {
            ReachDisplayConfig config = this.config;
            String text = "2.88 blocks";

            int padding = config.showBackground ? 2 : 0;

            int rawOffsetX = startOffsetX + (int)(mouseX - dragStartX);
            int rawOffsetY = startOffsetY + (int)(mouseY - dragStartY);

            int hudW = this.textRenderer.getWidth(text) + padding * 2;
            int hudH = this.textRenderer.fontHeight + padding * 2;

            int scaledW = (int) Math.ceil(hudW * Math.max(0.1f, config.scale / 100.0f));
            int scaledH = (int) Math.ceil(hudH * Math.max(0.1f, config.scale / 100.0f));

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

    /* ----- 마우스 릴리즈 ----- */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            dragging = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }


    /* ----- ESC ----- */
    @Override
    public boolean shouldCloseOnEsc()
    {
        MinecraftClient.getInstance().setScreen(parent);
        return false;
    }
}