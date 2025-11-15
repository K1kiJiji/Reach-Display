package kikijiji.reachdisplay.config;


import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import java.util.ArrayList;
import java.text.DecimalFormat;
import kikijiji.reachdisplay.ReachDisplay;
import kikijiji.reachdisplay.config.ReachDisplayConfig.DistanceColorBand;


public class ReachDisplayConfigScreen extends Screen
{
    private static final DecimalFormat PREVIEW_FORMAT = new DecimalFormat("0.00");
    private final Screen parent;

    private final ReachDisplayConfig defaultConfig = new ReachDisplayConfig();
    private ReachDisplayConfig workingConfig;

    private static final Identifier TOGGLE_ON_ICON  = Identifier.of(ReachDisplay.MOD_ID, "textures/gui/toggle_on.png");
    private static final Identifier TOGGLE_OFF_ICON = Identifier.of(ReachDisplay.MOD_ID, "textures/gui/toggle_off.png");
    private static final Identifier RESET_ICON      = Identifier.of(ReachDisplay.MOD_ID, "textures/gui/reset.png");
    private static final Identifier PREVIEW_BG      = Identifier.of(ReachDisplay.MOD_ID, "textures/gui/preview_bg.png");

    private ButtonWidget scaleRowButton;

    private ButtonWidget reachToggle;
    private ButtonWidget reachReset;

    private ButtonWidget shadowToggle;
    private ButtonWidget shadowReset;

    private ButtonWidget backGroundToggle;
    private ButtonWidget backGroundReset;

    private ButtonWidget scaleReset;
    private boolean editingScale = false;
    private String scaleEditBuffer = "";

    private ButtonWidget positionToggle;


    private final int startY = 30;


    public ReachDisplayConfigScreen(Screen parent)
    {
        super(Text.literal("Reach Display Config").formatted(Formatting.BOLD));
        this.parent = parent;

        this.workingConfig = copyConfig(ReachDisplay.CONFIG);
    }


    /* ----- 버튼 ----- */
    @Override
    protected void init()
    {
        this.clearChildren();

        // 레이아웃
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int leftWidth = centerX + this.width / 6;
        int rightWidth = this.width - leftWidth;
        int leftCenterX = leftWidth / 2;

        int buttonWidth = leftWidth - 40;
        int fullButtonWidth = leftWidth - 20;
        buttonWidth = Math.max(100, buttonWidth);
        fullButtonWidth = Math.max(100, fullButtonWidth);

        int x = leftCenterX - buttonWidth / 2;

        int resetX = x - 10 + buttonWidth;

        int resetSaveY = this.height - 50 - buttonHeight;
        int doneY = this.height - 25 - buttonHeight;

        int rightMargin = 20;
        int rightInnerX = leftWidth + rightMargin;
        int rightInnerWidth = rightWidth - rightMargin * 2;
        rightInnerWidth = Math.max(120, rightInnerWidth);

        int gap = 4;
        int resetSaveWidth = (rightInnerWidth - gap) / 2;
        int saveX  = rightInnerX + resetSaveWidth + gap;

        int doneWidth = rightInnerWidth;
        doneWidth = Math.max(100, doneWidth);
        int doneX = rightInnerX + (rightInnerWidth - doneWidth) / 2;

        int y = startY + 45;


        // 좌측
        // 표시
        reachToggle = this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showReach = !workingConfig.showReach;
                    updateEnableStates();
                }

        ).dimensions(x - 10, y, buttonWidth, buttonHeight).build());
        reachReset = this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showReach = defaultConfig.showReach;
                    workingConfig.mainColor = defaultConfig.mainColor;

                    updateEnableStates();
                }

        ).dimensions(resetX, y, 20, buttonHeight).build());
        y += 25;

        shadowToggle = this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showShadow = !workingConfig.showShadow;
                    updateEnableStates();
                }
        ).dimensions(x - 10, y, buttonWidth, buttonHeight).build());
        shadowReset = this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showShadow = defaultConfig.showShadow;
                    workingConfig.shadowColor = defaultConfig.shadowColor;

                    updateEnableStates();
                }

        ).dimensions(resetX, y, 20, buttonHeight).build());
        y += 25;

        backGroundToggle = this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showBackground = !workingConfig.showBackground;
                    updateEnableStates();
                }

        ).dimensions(x - 10, y, buttonWidth, buttonHeight).build());
        backGroundReset = this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showBackground = defaultConfig.showBackground;
                    workingConfig.backgroundColor = defaultConfig.backgroundColor;

                    updateEnableStates();
                }

        ).dimensions(resetX, y, 20, buttonHeight).build());
        y += 45;

        // 변환
        scaleRowButton = this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    if (!editingScale)
                    {
                        editingScale = true;
                        scaleEditBuffer = Integer.toString(workingConfig.scale);
                    }
                    else
                    {
                        applyScaleBuffer();
                        editingScale = false;
                    }

                    updateEnableStates();
                }
        ).dimensions(x - 10, y, buttonWidth, buttonHeight).build());
        scaleReset = this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.scale = defaultConfig.scale;
                    editingScale = false;
                    scaleEditBuffer = Integer.toString(defaultConfig.scale);
                    updateEnableStates();
                }
        ).dimensions(resetX, y, 20, buttonHeight).build());
        y += 25;

        positionToggle = this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    MinecraftClient.getInstance().setScreen(new ReachDisplayPositionConfigScreen(this, this.workingConfig));
                }

        ).dimensions(x - 10, y, fullButtonWidth, buttonHeight).build());
        y += 25;


        // 우측 하단
        // Reset
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Reset"),
                buttonWidget ->
                {
                    this.workingConfig = new ReachDisplayConfig();
                    this.init();
                }

        ).dimensions(rightInnerX, resetSaveY, resetSaveWidth, buttonHeight).build());

        // Save
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Save"),
                buttonWidget ->
                {
                    ReachDisplay.CONFIG = copyConfig(this.workingConfig);
                    ReachDisplayConfigManager.save(ReachDisplay.CONFIG);
                }

        ).dimensions( saveX, resetSaveY, resetSaveWidth, buttonHeight).build());

        // Done
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Done"),
                buttonWidget ->
                {
                    ReachDisplay.CONFIG = copyConfig(this.workingConfig);
                    ReachDisplayConfigManager.save(ReachDisplay.CONFIG);
                    MinecraftClient.getInstance().setScreen(parent);
                }

        ).dimensions(doneX, doneY, doneWidth, buttonHeight).build());

        updateEnableStates();
    }


    /* ----- 프리뷰 ----- */
    private void renderPreview(DrawContext ctx)
    {
        // 레이아웃
        int centerX = this.width / 2;
        int leftWidth = centerX + this.width / 6;
        int rightWidth = this.width - leftWidth;

        int rightMargin = 20;
        int rightInnerX = leftWidth + rightMargin;
        int rightInnerWidth = rightWidth - rightMargin * 2;
        rightInnerWidth = Math.max(120, rightInnerWidth);

        int previewW = rightInnerWidth;
        int previewH = rightInnerWidth * 9 / 16;
        int previewY = startY + 40;

        ReachDisplayConfig config = this.workingConfig;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || this.workingConfig == null)
        {
            return;
        }

        // 패널 배경
        ctx.fill
        (
                rightInnerX - 2,
                previewY - 2,
                rightInnerX + previewW + 2,
                previewY + previewH + 2,
                0xFFFFFFFF
        );
        ctx.fill
        (
                rightInnerX - 1,
                previewY - 1,
                rightInnerX + previewW + 1,
                previewY + previewH + 1,
                0xFF000000
        );

        int texW = 385;
        int texH = 215;
        ctx.drawTexture
        (
                RenderLayer::getGuiTextured,
                PREVIEW_BG,
                rightInnerX, previewY,
                0.0f, 0.0f,
                previewW, previewH,
                texW, texH
        );

        /* ----- HUD 텍스트 미리보기 ----- */
        // 샘플 거리
        double sampleDistance = 2.88;
        String numberText = PREVIEW_FORMAT.format(sampleDistance);

        // displayMode
        String text = numberText + " blocks";

        int textWidth  = this.textRenderer.getWidth(text);
        int textHeight = this.textRenderer.fontHeight;

        if (!config.showReach)
        {
            int descY = previewY + previewH + 6;
            ctx.drawText
            (
                    this.textRenderer,
                    Text.literal("Preview disabled (Enable Reach is OFF)"),
                    rightInnerX,
                    descY,
                    0xFF888888,
                    false
            );
            return;
        }

        float scale = Math.max(0.1f, config.scale / 100.0f);

        var matrices = ctx.getMatrices();
        matrices.push();

        int centerXHud = rightInnerX + previewW / 2;
        int centerYHud = previewY + previewH / 2;
        matrices.translate(centerXHud, centerYHud, 0);

        matrices.scale(scale, scale, 1.0f);

        int x = -textWidth / 2;
        int y = -textHeight / 2;

        // 배경 박스
        if (config.showBackground)
        {
            int padding = 2;
            int x1 = x - padding;
            int y1 = y - padding;
            int x2 = x + textWidth  + padding;
            int y2 = y + textHeight + padding;
            ctx.fill(x1, y1, x2, y2, config.backgroundColor);
        }

        // 그림자
        if (config.showShadow)
        {
            ctx.drawText
            (
                    this.textRenderer,
                    text,
                    x + 1,
                    y + 1,
                    config.shadowColor,
                    false
            );
        }

        // 메인 텍스트
        ctx.drawText
        (
                this.textRenderer,
                text,
                x,
                y,
                config.mainColor,
                false
        );

        matrices.pop();
    }


    /* ----- 렌더 ----- */
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta)
    {
        // 레이아웃
        int centerX = this.width / 2;
        int leftWidth = centerX + this.width / 6;
        int leftCenterX = leftWidth / 2;

        // 배경
        this.renderBackground(ctx, mouseX, mouseY, delta);

        // 버튼
        super.render(ctx, mouseX, mouseY, delta);
        renderPreview(ctx);

        // 버튼 글씨
        if (reachToggle != null)
        {
            int rx = reachToggle.getX();
            int ry = reachToggle.getY();
            int rw = reachToggle.getWidth();
            int rh = reachToggle.getHeight();

            String label = "Enable Reach";
            ctx.drawText
            (
                    this.textRenderer,
                    label,
                    rx + 4,
                    ry + (rh - this.textRenderer.fontHeight) / 2,
                    0xFFFFFF,
                    false
            );

            Identifier icon = workingConfig.showReach ? TOGGLE_ON_ICON : TOGGLE_OFF_ICON;
            int iconSize = 16;
            int iconX = rx + rw - iconSize - 4;
            int iconY = ry + (rh - iconSize) / 2;

            ctx.drawTexture
            (
                    RenderLayer::getGuiTextured,
                    icon,
                    iconX, iconY,
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize
            );
        }
        if (reachReset != null)
        {
            int bx = reachReset.getX();
            int by = reachReset.getY();
            int bw = reachReset.getWidth();
            int bh = reachReset.getHeight();
            int iconSize = 16;

            int iconX = bx + (bw - iconSize) / 2;
            int iconY = by + (bh - iconSize) / 2;

            float alpha = reachReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;

            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            ctx.drawTexture
            (
                    RenderLayer::getGuiTextured,
                    RESET_ICON,
                    iconX, iconY,
                    0.0f, 0.0f,
                    iconSize, iconSize,
                    iconSize, iconSize,
                    color
            );
        }
        if (shadowToggle != null)
        {
            int sx = shadowToggle.getX();
            int sy = shadowToggle.getY();
            int sw = shadowToggle.getWidth();
            int sh = shadowToggle.getHeight();

            String label = "Text Shadow";

            float alpha = shadowToggle.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;

            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            ctx.drawText
            (
                    this.textRenderer,
                    label,
                    sx + 4,
                    sy + (sh - this.textRenderer.fontHeight) / 2,
                    color,
                    false
            );


            Identifier icon = workingConfig.showShadow ? TOGGLE_ON_ICON : TOGGLE_OFF_ICON;
            int iconSize = 16;
            int iconX = sx + sw - iconSize - 4;
            int iconY = sy + (sh - iconSize) / 2;

            ctx.drawTexture
            (
                    RenderLayer::getGuiTextured,
                    icon,
                    iconX, iconY,
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize,
                    color
            );
        }
        if (shadowReset != null)
        {
            int bx = shadowReset.getX();
            int by = shadowReset.getY();
            int bw = shadowReset.getWidth();
            int bh = shadowReset.getHeight();
            int iconSize = 16;

            int iconX = bx + (bw - iconSize) / 2;
            int iconY = by + (bh - iconSize) / 2;

            float alpha = shadowReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;

            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            ctx.drawTexture
            (
                    RenderLayer::getGuiTextured,
                    RESET_ICON,
                    iconX, iconY,
                    0.0f, 0.0f,
                    iconSize, iconSize,
                    iconSize, iconSize,
                    color
            );
        }
        if (backGroundToggle != null)
        {
            int bgx = backGroundToggle.getX();
            int bgy = backGroundToggle.getY();
            int bgw = backGroundToggle.getWidth();
            int bgh = backGroundToggle.getHeight();

            String label = "Text Background";

            float alpha = shadowToggle.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;

            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            ctx.drawText
            (
                    this.textRenderer,
                    label,
                    bgx + 4,
                    bgy + (bgh - this.textRenderer.fontHeight) / 2,
                    color,
                    false
            );

            Identifier icon = workingConfig.showBackground ? TOGGLE_ON_ICON : TOGGLE_OFF_ICON;
            int iconSize = 16;
            int iconX = bgx + bgw - iconSize - 4;
            int iconY = bgy + (bgh - iconSize) / 2;

            ctx.drawTexture
            (
                    RenderLayer::getGuiTextured,
                    icon,
                    iconX, iconY,
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize,
                    color
            );
        }
        if (backGroundReset != null)
        {
            int bx = backGroundReset.getX();
            int by = backGroundReset.getY();
            int bw = backGroundReset.getWidth();
            int bh = backGroundReset.getHeight();
            int iconSize = 16;

            int iconX = bx + (bw - iconSize) / 2;
            int iconY = by + (bh - iconSize) / 2;

            float alpha = backGroundReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;

            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            ctx.drawTexture
            (
                    RenderLayer::getGuiTextured,
                    RESET_ICON,
                    iconX, iconY,
                    0.0f, 0.0f,
                    iconSize, iconSize,
                    iconSize, iconSize,
                    color
            );
        }
        if (scaleRowButton != null)
        {
            int sx = scaleRowButton.getX();
            int sy = scaleRowButton.getY();
            int sh = scaleRowButton.getHeight();

            String label = "Scale";
            ctx.drawText
            (
                    this.textRenderer,
                    label,
                    sx + 4,
                    sy + (sh - this.textRenderer.fontHeight) / 2,
                    0xFFFFFFFF,
                    false
            );

            String numText;
            if (editingScale)
            {
                numText = scaleEditBuffer.isEmpty() ? Integer.toString(workingConfig.scale) : scaleEditBuffer;
            }
            else
            {
                numText = Integer.toString(workingConfig.scale);
            }

            String valueText = numText;

            int buttonWidth = leftWidth - 40;
            buttonWidth = Math.max(100, buttonWidth);

            int x = leftCenterX - buttonWidth / 2;

            int resetX = x - 10 + buttonWidth;
            int valueWidth = this.textRenderer.getWidth(valueText);
            int valueX = resetX - 4 - valueWidth;
            int valueY = sy + (sh - this.textRenderer.fontHeight) / 2;

            ctx.drawText
            (
                    this.textRenderer,
                    valueText,
                    valueX,
                    valueY,
                    0xFFFFFFFF,
                    false
            );

            if (editingScale)
            {
                int underlineY = valueY + this.textRenderer.fontHeight + 1;
                ctx.fill
                (
                        valueX,
                        underlineY - 2,
                        valueX + valueWidth,
                        underlineY - 1,
                        0xFFFFFFFF
                );
            }
        }
        if (scaleReset != null)
        {
            int bx = scaleReset.getX();
            int by = scaleReset.getY();
            int bw = scaleReset.getWidth();
            int bh = scaleReset.getHeight();
            int iconSize = 16;

            int iconX = bx + (bw - iconSize) / 2;
            int iconY = by + (bh - iconSize) / 2;

            float alpha = scaleReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;

            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            ctx.drawTexture
            (
                    RenderLayer::getGuiTextured,
                    RESET_ICON,
                    iconX, iconY,
                    0.0f, 0.0f,
                    iconSize, iconSize,
                    iconSize, iconSize,
                    color
            );
        }
        if (positionToggle != null)
        {
            int bgx = positionToggle.getX();
            int bgy = positionToggle.getY();
            int bgh = positionToggle.getHeight();

            String label = "Edit Position";
            ctx.drawText
            (
                    this.textRenderer,
                    label,
                    bgx + 4,
                    bgy + (bgh - this.textRenderer.fontHeight) / 2,
                    0xFFFFFF,
                    false
            );
        }


        // 제목
        ctx.drawCenteredTextWithShadow
        (
                this.textRenderer,
                this.title,
                centerX,
                startY - 15,
                0xFFFFFF
        );

        // 제목 아래 구분 선
        ctx.fill(0, startY + 15, this.width, startY + 14, 0xFFFFFFFF);
        ctx.fill(0, startY + 16, this.width, startY + 15, 0xFF000000);

        // 패널 구분 선
        ctx.fill(leftWidth - 1, this.height, leftWidth, 45, 0xFFFFFFFF);
        ctx.fill(leftWidth, this.height, leftWidth + 1, 45, 0xFF000000);

        // 헤더 텍스트
        ctx.drawText(this.textRenderer, Text.literal("▼"), 10, startY + 25, 0xFFFFFF, true);
        ctx.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, startY + 25, 0xFFFFFF, true);
        ctx.drawText(this.textRenderer, Text.literal("Appearance"), leftCenterX, startY + 25, 0xFFFFFF, true);

        ctx.drawText(this.textRenderer, Text.literal("▼"), 10, startY + 125, 0xFFFFFF, true);
        ctx.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, startY + 125, 0xFFFFFF, true);
        ctx.drawText(this.textRenderer, Text.literal("transform"), leftCenterX, startY + 125, 0xFFFFFF, true);
    }


    /* ----- Config 복사 -----*/
    private static ReachDisplayConfig copyConfig(ReachDisplayConfig src)
    {
        ReachDisplayConfig c = new ReachDisplayConfig();

        c.showReach = src.showReach;
        c.showShadow = src.showShadow;
        c.showBackground = src.showBackground;

        c.mainColor = src.mainColor;
        c.shadowColor = src.shadowColor;
        c.backgroundColor = src.backgroundColor;

        c.scale = src.scale;
        c.offsetX = src.offsetX;
        c.offsetY = src.offsetY;

        c.useWhitelist = src.useWhitelist;
        c.whitelist = new ArrayList<>(src.whitelist);
        c.blacklist = new ArrayList<>(src.blacklist);

        c.enableDistanceColor = src.enableDistanceColor;
        c.distanceBands.clear();
        for (DistanceColorBand band : src.distanceBands)
        {
            DistanceColorBand nb = new DistanceColorBand();
            nb.maxDistance = band.maxDistance;
            nb.mainColor = band.mainColor;
            nb.shadowColor = band.shadowColor;
            nb.backgroundColor = band.backgroundColor;
            c.distanceBands.add(nb);
        }

        c.keepLastDistance = src.keepLastDistance;
        c.resetAfterSeconds = src.resetAfterSeconds;
        c.displayMode = src.displayMode;

        return c;
    }

    /* ----- 활성화 검사 ----- */
    private void updateEnableStates()
    {
        if (reachToggle == null)
        {
            return;
        }

        reachToggle.active = true;
        reachToggle.setAlpha(1.0f);

        // Reach 검사
        boolean reachDefault = workingConfig.showReach == defaultConfig.showReach && workingConfig.mainColor == defaultConfig.mainColor;
        if (reachReset != null)
        {
            reachReset.active = !reachDefault;
            reachReset.setAlpha(reachDefault ? 0.4f : 1.0f);
        }

        boolean reachEnabled = workingConfig.showReach;
        if (shadowToggle != null)
        {
            shadowToggle.active = reachEnabled;
            shadowToggle.setAlpha(reachEnabled ? 1.0f : 0.4f);
        }
        if (backGroundToggle != null)
        {
            backGroundToggle.active = reachEnabled;
            backGroundToggle.setAlpha(reachEnabled ? 1.0f : 0.4f);
        }

        // Shadow 검사
        boolean shadowDefault = workingConfig.showShadow == defaultConfig.showShadow && workingConfig.shadowColor == defaultConfig.shadowColor;
        if (shadowReset != null)
        {
            boolean canUse = reachEnabled && !shadowDefault;
            shadowReset.active = canUse;
            shadowReset.setAlpha(canUse ? 1.0f : 0.4f);
        }

        // Background 검사
        boolean bgDefault = workingConfig.showBackground == defaultConfig.showBackground && workingConfig.backgroundColor == defaultConfig.backgroundColor;
        if (backGroundReset != null)
        {
            boolean canUse = reachEnabled && !bgDefault;
            backGroundReset.active = canUse;
            backGroundReset.setAlpha(canUse ? 1.0f : 0.4f);
        }

        // Scale 검사
        boolean scaleDefault = (workingConfig.scale == defaultConfig.scale);
        if (scaleReset != null)
        {
            scaleReset.active = !scaleDefault;
            scaleReset.setAlpha(scaleDefault ? 0.4f : 1.0f);
        }
    }


    /* ----- 키보드 입력 ----- */
    private void applyScaleBuffer()
    {
        String txt = scaleEditBuffer.trim();
        if (!txt.isEmpty())
        {
            try
            {
                int value = Integer.parseInt(txt);
                value = Math.max(10, Math.min(400, value));
                workingConfig.scale = value;
            }
            catch (NumberFormatException ignored)
            {
                // ignore
            }
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (editingScale)
        {
            if (Character.isDigit(chr))
            {
                if (scaleEditBuffer.length() < 3)
                {
                    scaleEditBuffer += chr;
                    applyScaleBuffer();
                    updateEnableStates();
                }
            }

            return true;
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (editingScale)
        {
            // Backspace
            if (keyCode == 259 && !scaleEditBuffer.isEmpty())
            {
                scaleEditBuffer = scaleEditBuffer.substring(0, scaleEditBuffer.length() - 1);
                applyScaleBuffer();
                updateEnableStates();
                return true;
            }
            // Enter / Numpad Enter
            if (keyCode == 257 || keyCode == 335)
            {
                applyScaleBuffer();
                editingScale = false;
                updateEnableStates();
                return true;
            }
            // Esc
            if (keyCode == 256)
            {
                editingScale = false;
                scaleEditBuffer = Integer.toString(workingConfig.scale);
                updateEnableStates();
                return true;
            }

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if (editingScale && scaleRowButton != null)
        {
            int sx = scaleRowButton.getX();
            int sy = scaleRowButton.getY();
            int sw = scaleRowButton.getWidth();
            int sh = scaleRowButton.getHeight();

            boolean inside = mouseX >= sx && mouseX < sx + sw && mouseY >= sy && mouseY < sy + sh;
            if (!inside)
            {
                editingScale = false;
                scaleEditBuffer = Integer.toString(workingConfig.scale);
                updateEnableStates();
            }
        }

        return handled;
    }
}