package kikijiji.reachdisplay.config;


import java.util.ArrayList;

import java.text.DecimalFormat;

import net.minecraft.text.Text;

import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gui.widget.ButtonWidget;

import kikijiji.reachdisplay.ReachDisplay;
import kikijiji.reachdisplay.config.ReachDisplayConfig.DistanceColorBand;


public class ReachDisplayConfigScreen extends Screen
{
    private static final DecimalFormat PREVIEW_FORMAT = new DecimalFormat("0.00");
    private final Screen parent;

    private final ReachDisplayConfig defaultConfig = new ReachDisplayConfig();
    private ReachDisplayConfig workingConfig;

    private static final Identifier TOGGLE_ON_ICON     = Identifier.of(ReachDisplay.MOD_ID, "textures/gui/toggle_on.png");
    private static final Identifier TOGGLE_OFF_ICON    = Identifier.of(ReachDisplay.MOD_ID, "textures/gui/toggle_off.png");
    private static final Identifier RESET_ICON         = Identifier.of(ReachDisplay.MOD_ID, "textures/gui/reset.png");
    private static final Identifier PREVIEW_BACKGROUND = Identifier.of(ReachDisplay.MOD_ID, "textures/gui/preview_background.png");

    private final java.util.List<ButtonWidget> leftButtons = new ArrayList<>();
    private final java.util.List<Integer> leftButtonBaseY = new ArrayList<>();

    private ButtonWidget scaleRowButton;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private ButtonWidget textToggle;
    private ButtonWidget textReset;

    private ButtonWidget shadowToggle;
    private ButtonWidget shadowReset;

    private ButtonWidget backGroundToggle;
    private ButtonWidget backGroundReset;

    private ButtonWidget scaleReset;
    private boolean editingScale    = false;
    private String  scaleEditBuffer = "";

    private ButtonWidget positionToggle;

    private ButtonWidget textColorButton;
    private ButtonWidget textColorReset;

    private ButtonWidget shadowColorButton;
    private ButtonWidget shadowColorReset;

    private ButtonWidget backgroundColorButton;
    private ButtonWidget backgroundColorReset;

    private ButtonWidget keepLastDistanceToggle;
    private ButtonWidget keepLastDistanceReset;

    private ButtonWidget resetSecondsRowButton;
    private ButtonWidget resetSecondsReset;
    private boolean editingResetSeconds = false;
    private String  resetSecondsBuffer  = "";

    private ButtonWidget displayModeButton;
    private ButtonWidget displayModeReset;

    private final int startY = 30;

    private ButtonWidget addLeftButton(ButtonWidget.Builder builder, int x, int baseY, int w, int h)
    {
        ButtonWidget buttonWidget = this.addDrawableChild(builder.dimensions(x, baseY - scrollOffset, w, h).build());

        leftButtons.add(buttonWidget);
        leftButtonBaseY.add(baseY);

        return buttonWidget;
    }

    private void updateLeftButtonPositions()
    {
        for (int i = 0; i < leftButtons.size(); i++)
        {
            ButtonWidget buttonWidget = leftButtons.get(i);
            int baseY = leftButtonBaseY.get(i);
            buttonWidget.setY(baseY - scrollOffset);
        }
    }


    /* ----- 제목 ----- */
    public ReachDisplayConfigScreen(Screen parent)
    {
        super(Text.literal("Reach Display Config").formatted(Formatting.BOLD));

        this.parent = parent;

        this.workingConfig = copyConfig(ReachDisplay.CONFIG);
    }


    /* ----- 준비 ----- */
    @Override
    protected void init()
    {
        this.clearChildren();
        leftButtons.clear();
        leftButtonBaseY.clear();

        // 레이아웃
        int buttonHeight = 20;

        int leftWidth = this.width / 2 + this.width / 6;
        int rightWidth = this.width - leftWidth;

        int buttonWidth = leftWidth - 40;
        int fullButtonWidth = leftWidth - 20;
        buttonWidth = Math.max(100, buttonWidth);
        fullButtonWidth = Math.max(100, fullButtonWidth);

        int x = leftWidth / 2 - buttonWidth / 2;
        int y = startY + 45;

        int resetX = x - 10 + buttonWidth;

        int allResetSaveY = this.height - 50 - buttonHeight;
        int doneY = this.height - 25 - buttonHeight;

        int rightMargin = 20;
        int rightInnerX = leftWidth + rightMargin;

        int gap = 4;
        int allResetSaveWidth = (Math.max(120, rightWidth - rightMargin * 2) - gap) / 2;
        int saveX  = rightInnerX + allResetSaveWidth + gap;
        int doneWidth = Math.max(120, rightWidth - rightMargin * 2);
        int doneX = rightInnerX + (Math.max(120, rightWidth - rightMargin * 2) - doneWidth) / 2;


        // 좌측 표시
        textToggle = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showReach = !workingConfig.showReach;
                    updateEnableStates();
                }

        ),x - 10, y, buttonWidth, buttonHeight);
        textReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showReach = defaultConfig.showReach;
                    workingConfig.textColor = defaultConfig.textColor;

                    updateEnableStates();
                }

        ),resetX, y, 20, buttonHeight);
        y += 25;
        shadowToggle = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showShadow = !workingConfig.showShadow;
                    updateEnableStates();
                }
        ),x - 10, y, buttonWidth, buttonHeight);
        shadowReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showShadow = defaultConfig.showShadow;
                    workingConfig.shadowColor = defaultConfig.shadowColor;

                    updateEnableStates();
                }

        ),resetX, y, 20, buttonHeight);
        y += 25;
        backGroundToggle = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showBackground = !workingConfig.showBackground;
                    updateEnableStates();
                }

        ),x - 10, y, buttonWidth, buttonHeight);
        backGroundReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showBackground = defaultConfig.showBackground;
                    workingConfig.backgroundColor = defaultConfig.backgroundColor;

                    updateEnableStates();
                }

        ),resetX, y, 20, buttonHeight);
        y += 45;

        // 좌측 변환
        scaleRowButton = addLeftButton(ButtonWidget.builder
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

        ),x - 10, y, buttonWidth, buttonHeight);
        scaleReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.scale = defaultConfig.scale;
                    editingScale = false;
                    scaleEditBuffer = Integer.toString(defaultConfig.scale);
                    updateEnableStates();
                }

        ),resetX, y, 20, buttonHeight);
        y += 25;
        positionToggle = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget -> MinecraftClient.getInstance().setScreen(new ReachDisplayPositionConfigScreen(this, this.workingConfig))

        ),x - 10, y, fullButtonWidth, buttonHeight);
        y += 45;

        // 좌측 색
        textColorButton = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget -> MinecraftClient.getInstance().setScreen
                (
                        new ColorPickerScreen(this, workingConfig.textColor, newColor ->
                        {
                            workingConfig.textColor = newColor;
                            this.init();
                        })
                )

        ),x - 10, y, buttonWidth, buttonHeight);
        textColorReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.textColor = defaultConfig.textColor;
                    this.init();
                }

        ),resetX, y, 20, buttonHeight);
        y += 25;
        shadowColorButton = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget -> MinecraftClient.getInstance().setScreen
                (
                        new ColorPickerScreen(this, workingConfig.shadowColor, newColor ->
                        {
                            workingConfig.shadowColor = newColor;
                            this.init();
                        })
                )

        ),x - 10, y, buttonWidth, buttonHeight);
        shadowColorReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.shadowColor = defaultConfig.shadowColor;
                    this.init();
                }

        ),resetX, y, 20, buttonHeight);
        y += 25;
        backgroundColorButton = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget -> MinecraftClient.getInstance().setScreen
                (
                        new ColorPickerScreen(this, workingConfig.backgroundColor, newColor ->
                        {
                            workingConfig.backgroundColor = newColor;
                            this.init();
                        })
                )

        ),x - 10, y, buttonWidth, buttonHeight);
        backgroundColorReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.backgroundColor = defaultConfig.backgroundColor;
                    this.init();
                }

        ),resetX, y, 20, buttonHeight);
        y += 45;

        // 좌측 표시 유지 방식
        keepLastDistanceToggle = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.keepLastHitDistance = !workingConfig.keepLastHitDistance;
                    updateEnableStates();
                }

        ),x - 10, y, buttonWidth, buttonHeight);
        keepLastDistanceReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.keepLastHitDistance = defaultConfig.keepLastHitDistance;
                    updateEnableStates();
                }

        ),resetX, y, 20, buttonHeight);
        y += 25;
        resetSecondsRowButton = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    if (!editingResetSeconds)
                    {
                        editingResetSeconds = true;
                        resetSecondsBuffer = Double.toString(workingConfig.resetAfterSeconds);
                    }
                    else
                    {
                        applyResetSecondsBuffer();
                        editingResetSeconds = false;
                    }

                    updateEnableStates();
                }

        ),x - 10, y, buttonWidth, buttonHeight);
        resetSecondsReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                btn ->
                {
                    workingConfig.resetAfterSeconds = defaultConfig.resetAfterSeconds;
                    editingResetSeconds = false;
                    resetSecondsBuffer = Double.toString(workingConfig.resetAfterSeconds);
                    updateEnableStates();
                }

        ),resetX, y, 20, buttonHeight);
        y += 45;

        // 좌측 표시 포맷
        displayModeButton = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    switch (workingConfig.displayMode)
                    {
                        case NUMBER_ONLY  -> workingConfig.displayMode = ReachDisplayConfig.DisplayMode.WITH_BLOCKS;
                        case WITH_BLOCKS  -> workingConfig.displayMode = ReachDisplayConfig.DisplayMode.WITH_M;
                        case WITH_M       -> workingConfig.displayMode = ReachDisplayConfig.DisplayMode.NUMBER_ONLY;
                    }

                    updateEnableStates();
                }

        ),x - 10, y, buttonWidth, buttonHeight);
        displayModeReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.displayMode = defaultConfig.displayMode;
                    updateEnableStates();
                }

        ),resetX, y, 20, buttonHeight);


        // 우측 하단 Reset
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Reset"),
                buttonWidget ->
                {
                    this.workingConfig = new ReachDisplayConfig();
                    this.init();
                }

        ).dimensions(rightInnerX, allResetSaveY, allResetSaveWidth, buttonHeight).build());
        // 우측 하단 Save
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Save"),
                buttonWidget ->
                {
                    if (editingScale) applyScaleBuffer();
                    if (editingResetSeconds) applyResetSecondsBuffer();
                    ReachDisplay.CONFIG = copyConfig(this.workingConfig);
                    ReachDisplayConfigManager.save(ReachDisplay.CONFIG);
                }

        ).dimensions( saveX, allResetSaveY, allResetSaveWidth, buttonHeight).build());
        // 우측 하단 Done
        this.addDrawableChild(ButtonWidget.builder
        (
                Text.literal("Done"),
                buttonWidget ->
                {
                    if (editingScale) applyScaleBuffer();
                    if (editingResetSeconds) applyResetSecondsBuffer();
                    ReachDisplay.CONFIG = copyConfig(this.workingConfig);
                    ReachDisplayConfigManager.save(ReachDisplay.CONFIG);
                    MinecraftClient.getInstance().setScreen(parent);
                }

        ).dimensions(doneX, doneY, doneWidth, buttonHeight).build());

        // 스크롤
        maxScroll = Math.max(0, y - (startY + this.height - startY - 80));
        if (scrollOffset > maxScroll)
        {
            scrollOffset = maxScroll;
        }

        // 업데이트
        updateLeftButtonPositions();
        updateEnableStates();
    }


    /* ----- 프리뷰 ----- */
    private void renderPreview(DrawContext drawContext)
    {
        ReachDisplayConfig config = this.workingConfig;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || this.workingConfig == null)
        {
            return;
        }

        // 레이아웃
        int centerX = this.width / 2;

        int leftWidth = centerX + this.width / 6;
        int rightWidth = this.width - leftWidth;

        int rightMargin = 20;
        int rightInnerX = leftWidth + rightMargin;

        int previewW = Math.max(120, rightWidth - rightMargin * 2);
        int previewH = Math.max(120, rightWidth - rightMargin * 2) * 9 / 16;
        int previewY = startY + 40;

        double sampleDistance = 2.88;
        String text = formatSampleText(sampleDistance, config.displayMode);

        int textWidth  = this.textRenderer.getWidth(text);
        int textHeight = this.textRenderer.fontHeight;
        int x = -textWidth / 2;
        int y = -textHeight / 2;

        // 패널 배경
        drawContext.fill
        (
                rightInnerX - 2,
                previewY - 2,
                rightInnerX + previewW + 2,
                previewY + previewH + 2,
                0xFFFFFFFF
        );
        drawContext.fill
        (
                rightInnerX - 1,
                previewY - 1,
                rightInnerX + previewW + 1,
                previewY + previewH + 1,
                0xFF000000
        );
        drawContext.drawTexture
        (
                RenderLayer::getGuiTextured,
                PREVIEW_BACKGROUND,
                rightInnerX, previewY,
                0.0f, 0.0f,
                previewW, previewH,
                385, 215
        );

        // 크기
        float scale = Math.max(0.1f, config.scale / 100.0f);

        var matrices = drawContext.getMatrices();
        matrices.push();

        int centerXHud = rightInnerX + previewW / 2;
        int centerYHud = previewY   + previewH / 2;

        matrices.translate(centerXHud, centerYHud, 0);
        matrices.scale(scale, scale, 1.0f);

        // 배경
        if (config.showBackground)
        {
            int padding = 2;
            int x1 = x - padding;
            int y1 = y - padding;
            int x2 = x + textWidth  + padding;
            int y2 = y + textHeight + padding;
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
                    x + 1,
                    y + 1,
                    config.shadowColor,
                    false
            );
        }
        // 텍스트
        drawContext.drawText
        (
                this.textRenderer,
                text,
                x,
                y,
                config.textColor,
                false
        );

        matrices.pop();
    }


    /* ----- 렌더 ----- */
    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        // 레이아웃
        int centerX = this.width / 2;
        int leftWidth = centerX + this.width / 6;
        int leftCenterX = leftWidth / 2;

        int scrollY = scrollOffset;

        int appearanceY = startY + 25 - scrollY;
        int transformY  = startY + 125 - scrollY;
        int colorY      = startY + 195 - scrollY;
        int keepY       = startY + 290 - scrollY;
        int displayY    = startY + 360 - scrollY;

        // 배경
        this.renderBackground(drawContext, mouseX, mouseY, delta);

        // 버튼
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.fill(leftWidth, 45, this.width, this.height, 0x50000000);
        renderPreview(drawContext);

        // 헤더 텍스트
        drawContext.drawText(this.textRenderer, Text.literal("▼"), 10, appearanceY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, appearanceY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("Appearance"), leftCenterX - this.textRenderer.getWidth(Text.literal("Appearance")) / 2, appearanceY, 0xFFFFFF, false);

        drawContext.drawText(this.textRenderer, Text.literal("▼"), 10, transformY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, transformY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("Transform"), leftCenterX - this.textRenderer.getWidth(Text.literal("Transform")) / 2, transformY, 0xFFFFFF, false);

        drawContext.drawText(this.textRenderer, Text.literal("▼"), 10, colorY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, colorY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("Color"), leftCenterX - this.textRenderer.getWidth(Text.literal("Color")) / 2, colorY, 0xFFFFFF, false);

        drawContext.drawText(this.textRenderer, Text.literal("▼"), 10, keepY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, keepY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("Keep Last Distance"), leftCenterX - this.textRenderer.getWidth(Text.literal("Keep Last Distance")) / 2, keepY, 0xFFFFFF, false);

        drawContext.drawText(this.textRenderer, Text.literal("▼"), 10, displayY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, displayY, 0xFFFFFF, false);
        drawContext.drawText(this.textRenderer, Text.literal("Display Mod"), leftCenterX - this.textRenderer.getWidth(Text.literal("Display Mod")) / 2, displayY, 0xFFFFFF, false);

        // 버튼 글씨 텍스트
        if (textToggle != null)
        {
            int tx = textToggle.getX();
            int ty = textToggle.getY();
            int tw = textToggle.getWidth();
            int th = textToggle.getHeight();

            String label = "Enable Reach";
            drawContext.drawText
            (
                    this.textRenderer,
                    label,
                    tx + 4,
                    ty + (th - this.textRenderer.fontHeight) / 2,
                    0xFFFFFF,
                    false
            );

            Identifier icon = workingConfig.showReach ? TOGGLE_ON_ICON : TOGGLE_OFF_ICON;
            int iconSize = 16;
            int iconX = tx + tw - iconSize - 4;
            int iconY = ty + (th - iconSize) / 2;

            drawContext.drawTexture
            (
                    RenderLayer::getGuiTextured,
                    icon,
                    iconX, iconY,
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize
            );
        }
        if (textReset != null)
        {
            int tx = textReset.getX();
            int ty = textReset.getY();
            int tw = textReset.getWidth();
            int th = textReset.getHeight();
            int iconSize = 16;

            int iconX = tx + (tw - iconSize) / 2;
            int iconY = ty + (th - iconSize) / 2;

            float alpha = textReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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
        // 버튼 글씨 그림자
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

            drawContext.drawText
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

            drawContext.drawTexture
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
            int sx = shadowReset.getX();
            int sy = shadowReset.getY();
            int sw = shadowReset.getWidth();
            int sh = shadowReset.getHeight();
            int iconSize = 16;

            int iconX = sx + (sw - iconSize) / 2;
            int iconY = sy + (sh - iconSize) / 2;

            float alpha = shadowReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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
        // 버튼 글씨 배경
        if (backGroundToggle != null)
        {
            int bgx = backGroundToggle.getX();
            int bgy = backGroundToggle.getY();
            int bgw = backGroundToggle.getWidth();
            int bgh = backGroundToggle.getHeight();

            String label = "Text Background";

            float alpha = backGroundToggle.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawText
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

            drawContext.drawTexture
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
            int bgx = backGroundReset.getX();
            int bgy = backGroundReset.getY();
            int bgw = backGroundReset.getWidth();
            int bgh = backGroundReset.getHeight();
            int iconSize = 16;

            int iconX = bgx + (bgw - iconSize) / 2;
            int iconY = bgy + (bgh - iconSize) / 2;

            float alpha = backGroundReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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
        // 버튼 글씨 크기
        if (scaleRowButton != null)
        {
            int srx = scaleRowButton.getX();
            int sry = scaleRowButton.getY();
            int srh = scaleRowButton.getHeight();

            String label = "Scale";
            drawContext.drawText
            (
                    this.textRenderer,
                    label,
                    srx + 4,
                    sry + (srh - this.textRenderer.fontHeight) / 2,
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

            int buttonWidth = Math.max(100, leftWidth - 40);

            int valueWidth = this.textRenderer.getWidth(valueText);
            int valueX = leftCenterX - buttonWidth / 2 - 10 + buttonWidth - 4 - valueWidth;
            int valueY = sry + (srh - this.textRenderer.fontHeight) / 2;

            drawContext.drawText
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
                drawContext.fill
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
            int sx = scaleReset.getX();
            int sy = scaleReset.getY();
            int sw = scaleReset.getWidth();
            int sh = scaleReset.getHeight();
            int iconSize = 16;

            int iconX = sx + (sw - iconSize) / 2;
            int iconY = sy + (sh - iconSize) / 2;

            float alpha = scaleReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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
        // 버튼 글씨 위치
        if (positionToggle != null)
        {
            int px = positionToggle.getX();
            int py = positionToggle.getY();
            int ph = positionToggle.getHeight();

            String label = "Edit Position";
            drawContext.drawText
            (
                    this.textRenderer,
                    label,
                    px + 4,
                    py + (ph - this.textRenderer.fontHeight) / 2,
                    0xFFFFFF,
                    false
            );
        }
        // 버튼 글씨 색
        if (textColorButton != null)
        {
            drawColorRow(drawContext, textColorButton, "Main Color", workingConfig.textColor);
        }
        if (textColorReset != null)
        {
            int tcx = textColorReset.getX();
            int tcy = textColorReset.getY();
            int tcw = textColorReset.getWidth();
            int tch = textColorReset.getHeight();
            int iconSize = 16;

            int iconX = tcx + (tcw - iconSize) / 2;
            int iconY = tcy + (tch - iconSize) / 2;

            float alpha = textColorReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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
        if (shadowColorButton != null)
        {
            drawColorRow(drawContext, shadowColorButton, "Shadow Color", workingConfig.shadowColor);
        }
        if (shadowColorReset != null)
        {
            int scx = shadowColorReset.getX();
            int scy = shadowColorReset.getY();
            int scw = shadowColorReset.getWidth();
            int sch = shadowColorReset.getHeight();
            int iconSize = 16;

            int iconX = scx + (scw - iconSize) / 2;
            int iconY = scy + (sch - iconSize) / 2;

            float alpha = shadowColorReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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
        if (backgroundColorButton != null)
        {
            drawColorRow(drawContext, backgroundColorButton, "Background Color", workingConfig.backgroundColor);
        }
        if (backgroundColorReset != null)
        {
            int bgcx = backgroundColorReset.getX();
            int bgcy = backgroundColorReset.getY();
            int bgcw = backgroundColorReset.getWidth();
            int bgch = backgroundColorReset.getHeight();
            int iconSize = 16;

            int iconX = bgcx + (bgcw - iconSize) / 2;
            int iconY = bgcy + (bgch - iconSize) / 2;

            float alpha = backgroundColorReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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
        // 버튼 글씨 표시 유지 방식
        if (keepLastDistanceToggle != null)
        {
            int kldx = keepLastDistanceToggle.getX();
            int kldy = keepLastDistanceToggle.getY();
            int kldh = keepLastDistanceToggle.getHeight();

            String label = "Keep Last Distance";
            drawContext.drawText(
                    this.textRenderer,
                    label,
                    kldx + 4,
                    kldy + (kldh - this.textRenderer.fontHeight) / 2,
                    0xFFFFFFFF,
                    false
            );

            Identifier icon = workingConfig.keepLastHitDistance ? TOGGLE_ON_ICON : TOGGLE_OFF_ICON;
            int iconSize = 16;
            int iconX = kldx + keepLastDistanceToggle.getWidth() - iconSize - 4;
            int iconY = kldy + (kldh - iconSize) / 2;
            drawContext.drawTexture
            (
                    RenderLayer::getGuiTextured,
                    icon,
                    iconX, iconY,
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize
            );
        }
        if (keepLastDistanceReset != null)
        {
            int kldx = keepLastDistanceReset.getX();
            int kldy = keepLastDistanceReset.getY();
            int kldw = keepLastDistanceReset.getWidth();
            int kldh = keepLastDistanceReset.getHeight();
            int iconSize = 16;

            int iconX = kldx + (kldw - iconSize) / 2;
            int iconY = kldy + (kldh - iconSize) / 2;

            float alpha = keepLastDistanceReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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
        if (resetSecondsRowButton != null)
        {
            int rsrx = resetSecondsRowButton.getX();
            int rsry = resetSecondsRowButton.getY();
            int rsrh = resetSecondsRowButton.getHeight();

            boolean active = resetSecondsRowButton.active;

            float alpha = active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            String label = "Reset After (Sec)";
            drawContext.drawText
            (
                    this.textRenderer,
                    label,
                    rsrx + 4,
                    rsry + (rsrh - this.textRenderer.fontHeight) / 2,
                    color,
                    false
            );

            String valueText = editingResetSeconds ? resetSecondsBuffer : String.format("%.1f", workingConfig.resetAfterSeconds);

            int buttonWidth = Math.max(100, leftWidth - 40);
            int valueWidth = this.textRenderer.getWidth(valueText);
            int valueX = leftCenterX - buttonWidth / 2 - 10 + buttonWidth - 4 - valueWidth;
            int valueY = rsry + (rsrh - this.textRenderer.fontHeight) / 2;

            drawContext.drawText
            (
                    this.textRenderer,
                    valueText,
                    valueX,
                    valueY,
                    color,
                    false
            );

            if (editingResetSeconds)
            {
                int underlineY = valueY + this.textRenderer.fontHeight + 1;
                drawContext.fill(valueX, underlineY - 2, valueX + valueWidth, underlineY - 1, color);
            }
        }
        if (resetSecondsReset != null)
        {
            int rsx = resetSecondsReset.getX();
            int rsy = resetSecondsReset.getY();
            int rsw = resetSecondsReset.getWidth();
            int rsh = resetSecondsReset.getHeight();
            int iconSize = 16;

            int iconX = rsx + (rsw - iconSize) / 2;
            int iconY = rsy + (rsh - iconSize) / 2;

            float alpha = resetSecondsReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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
        // 버튼 글씨 표시 포맷
        if (displayModeButton != null)
        {
            int dmx = displayModeButton.getX();
            int dmy = displayModeButton.getY();
            int dmh = displayModeButton.getHeight();

            String label = "Display Format";
            drawContext.drawText
            (
                    this.textRenderer,
                    label,
                    dmx + 4,
                    dmy + (dmh - this.textRenderer.fontHeight) / 2,
                    0xFFFFFFFF,
                    false
            );

            String modeText = switch (workingConfig.displayMode)
            {
                case NUMBER_ONLY  -> "2.88";
                case WITH_BLOCKS  -> "2.88 blocks";
                case WITH_M       -> "2.88 M";
            };

            int valueX = dmx + displayModeButton.getWidth() - this.textRenderer.getWidth(modeText) - 4;
            int valueY = dmy + (dmh - this.textRenderer.fontHeight) / 2;

            drawContext.drawText
            (
                    this.textRenderer,
                    modeText,
                    valueX,
                    valueY,
                    0xFFFFFFFF,
                    false
            );
        }
        if (displayModeReset != null)
        {
            int dmx = displayModeReset.getX();
            int dmy = displayModeReset.getY();
            int dmw = displayModeReset.getWidth();
            int dmh = displayModeReset.getHeight();
            int iconSize = 16;

            int iconX = dmx + (dmw - iconSize) / 2;
            int iconY = dmy + (dmh - iconSize) / 2;

            float alpha = displayModeReset.active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;
            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            drawContext.drawTexture
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

        // 제목
        drawContext.drawCenteredTextWithShadow
        (
                this.textRenderer,
                this.title,
                centerX,
                startY - 15,
                0xFFFFFF
        );

        // 제목 구분 선
        drawContext.fill(0, startY + 15, this.width, startY + 14, 0xFFFFFFFF);
        drawContext.fill(0, startY + 16, this.width, startY + 15, 0xFF000000);

        // 패널 구분 선
        drawContext.fill(leftWidth - 1, this.height, leftWidth, 45, 0xFFFFFFFF);
        drawContext.fill(leftWidth, this.height, leftWidth + 1, 45, 0xFF000000);

        drawContext.fill(0, 0, this.width, startY + 14, 0xFF000000);
    }


    // 색
    private void drawColorRow(DrawContext drawContext, ButtonWidget colorButton, String label, int argb)
    {
        int cbx = colorButton.getX();
        int cby = colorButton.getY();
        int cbw = colorButton.getWidth();
        int cbh = colorButton.getHeight();

        int labelY = cby + (cbh - this.textRenderer.fontHeight) / 2;
        drawContext.drawText
        (
                this.textRenderer,
                Text.literal(label),
                cbx + 4,
                labelY,
                0xFFFFFFFF,
                false
        );

        drawContext.fill(cbx + cbw - 16, cby + 4, cbx + cbw - 4, cby + cbh - 4, 0xFF000000);
        drawContext.fill(cbx + cbw - 15, cby + 5, cbx + cbw - 5, cby + cbh - 5, argb);
    }


    /* ----- Config -----*/
    private static ReachDisplayConfig copyConfig(ReachDisplayConfig src)
    {
        ReachDisplayConfig c = new ReachDisplayConfig();

        c.showReach = src.showReach;
        c.showShadow = src.showShadow;
        c.showBackground = src.showBackground;

        c.textColor = src.textColor;
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
            DistanceColorBand dc = new DistanceColorBand();
            dc.maxDistance     = band.maxDistance;
            dc.textColor       = band.textColor;
            dc.shadowColor     = band.shadowColor;
            dc.backgroundColor = band.backgroundColor;
            c.distanceBands.add(dc);
        }

        c.keepLastHitDistance = src.keepLastHitDistance;
        c.resetAfterSeconds = src.resetAfterSeconds;

        c.displayMode = src.displayMode;

        return c;
    }


    /* ----- 활성화 검사 ----- */
    private void updateEnableStates()
    {
        if (textToggle == null)
        {
            return;
        }

        textToggle.active = true;
        textToggle.setAlpha(1.0f);

        // text 검사
        boolean textDefault = workingConfig.showReach == defaultConfig.showReach;
        if (textReset != null)
        {
            textReset.active = !textDefault;
            textReset.setAlpha(textDefault ? 0.4f : 1.0f);
        }

        boolean textEnabled = workingConfig.showReach;
        if (shadowToggle != null)
        {
            shadowToggle.active = textEnabled;
            shadowToggle.setAlpha(textEnabled ? 1.0f : 0.4f);
        }
        if (backGroundToggle != null)
        {
            backGroundToggle.active = textEnabled;
            backGroundToggle.setAlpha(textEnabled ? 1.0f : 0.4f);
        }

        // Shadow 검사
        boolean shadowDefault = workingConfig.showShadow == defaultConfig.showShadow;
        if (shadowReset != null)
        {
            boolean canUse = textEnabled && !shadowDefault;
            shadowReset.active = canUse;
            shadowReset.setAlpha(canUse ? 1.0f : 0.4f);
        }

        // Background 검사
        boolean backgroundDefault = workingConfig.showBackground == defaultConfig.showBackground;
        if (backGroundReset != null)
        {
            boolean canUse = textEnabled && !backgroundDefault;
            backGroundReset.active = canUse;
            backGroundReset.setAlpha(canUse ? 1.0f : 0.4f);
        }

        // Scale 검사
        boolean scaleDefault = workingConfig.scale == defaultConfig.scale;
        if (scaleReset != null)
        {
            scaleReset.active = !scaleDefault;
            scaleReset.setAlpha(scaleDefault ? 0.4f : 1.0f);
        }

        // text color 검사
        boolean mainColorDefault = (workingConfig.textColor == defaultConfig.textColor);
        if (textColorReset != null)
        {
            textColorReset.active = !mainColorDefault;
            textColorReset.setAlpha(mainColorDefault ? 0.4f : 1.0f);
        }

        // Shadow color 검사
        boolean shadowColorDefault = (workingConfig.shadowColor == defaultConfig.shadowColor);
        if (shadowColorReset != null)
        {
            shadowColorReset.active = !shadowColorDefault;
            shadowColorReset.setAlpha(shadowColorDefault ? 0.4f : 1.0f);
        }

        // Background color 검사
        boolean bgColorDefault = (workingConfig.backgroundColor == defaultConfig.backgroundColor);
        if (backgroundColorReset != null)
        {
            backgroundColorReset.active = !bgColorDefault;
            backgroundColorReset.setAlpha(bgColorDefault ? 0.4f : 1.0f);
        }

        // 표시 유지 방식 검사
        boolean keepLastDefault = (workingConfig.keepLastHitDistance == defaultConfig.keepLastHitDistance);
        if (keepLastDistanceReset != null)
        {
            keepLastDistanceReset.active = !keepLastDefault;
            keepLastDistanceReset.setAlpha(keepLastDefault ? 0.4f : 1.0f);
        }
        boolean resetSecDefault = (workingConfig.resetAfterSeconds == defaultConfig.resetAfterSeconds);
        if (resetSecondsReset != null)
        {
            resetSecondsReset.active = !resetSecDefault;
            resetSecondsReset.setAlpha(resetSecDefault ? 0.4f : 1.0f);
        }

        // 포맷 검사
        boolean formatDefault = (workingConfig.displayMode == defaultConfig.displayMode);
        if (displayModeReset != null)
        {
            displayModeReset.active = !formatDefault;
            displayModeReset.setAlpha(formatDefault ? 0.4f : 1.0f);
        }
        if (resetSecondsRowButton != null)
        {
            boolean active = !workingConfig.keepLastHitDistance;
            resetSecondsRowButton.active = active;
            resetSecondsRowButton.setAlpha(active ? 1.0f : 0.4f);
        }
    }


    /* ----- 크기 버퍼 ----- */
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
    /* ----- 표시 유지 초 버퍼 ----- */
    private void applyResetSecondsBuffer()
    {
        String txt = resetSecondsBuffer.trim();
        if (!txt.isEmpty())
        {
            try
            {
                double value = Double.parseDouble(txt);
                value = Math.max(0.0, Math.min(600.0, value));
                workingConfig.resetAfterSeconds = value;
            }

            catch (NumberFormatException ignored)
            {
                // ignore
            }
        }
    }


    /* ----- 포맷 ----- */
    private String formatSampleText(double distance, ReachDisplayConfig.DisplayMode mode)
    {
        String numberText = PREVIEW_FORMAT.format(distance);
        return switch (mode)
        {
            case NUMBER_ONLY  -> numberText;
            case WITH_BLOCKS  -> numberText + " blocks";
            case WITH_M       -> numberText + " M";
        };
    }


    /* ----- 글자 제한 ----- */
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
        else if (editingResetSeconds)
        {
            if (Character.isDigit(chr) || chr == '.')
            {
                if (resetSecondsBuffer.length() < 6)
                {
                    resetSecondsBuffer += chr;
                    applyResetSecondsBuffer();
                    updateEnableStates();
                }
            }
            return true;
        }

        return super.charTyped(chr, modifiers);
    }

    /* ----- 키 입력 ----- */
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
        else if (editingResetSeconds)
        {
            // Backspace
            if (keyCode == 259 && !resetSecondsBuffer.isEmpty())
            {
                resetSecondsBuffer = resetSecondsBuffer.substring(0, resetSecondsBuffer.length() - 1);
                applyResetSecondsBuffer();
                updateEnableStates();
                return true;
            }
            // Enter / Numpad Enter
            if (keyCode == 257 || keyCode == 335)
            {
                applyResetSecondsBuffer();
                editingResetSeconds = false;
                updateEnableStates();
                return true;
            }
            // Esc
            if (keyCode == 256)
            {
                editingResetSeconds = false;
                resetSecondsBuffer = Double.toString(workingConfig.resetAfterSeconds);
                updateEnableStates();
                return true;
            }

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /* ----- 마우스 클릭 ----- */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int leftWidth = this.width / 2 + this.width / 6;
        int clipTop   = startY + 18;

        if (mouseX < leftWidth && mouseY < clipTop)
        {
            return true;
        }

        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if (editingScale && scaleRowButton != null)
        {
            int srx = scaleRowButton.getX();
            int sry = scaleRowButton.getY();
            int srw = scaleRowButton.getWidth();
            int srh = scaleRowButton.getHeight();

            boolean inside = mouseX >= srx && mouseX < srx + srw && mouseY >= sry && mouseY < sry + srh;
            if (!inside)
            {
                editingScale = false;
                scaleEditBuffer = Integer.toString(workingConfig.scale);
                updateEnableStates();
            }
        }

        if (editingResetSeconds && resetSecondsRowButton != null)
        {
            int rsrx = resetSecondsRowButton.getX();
            int rsry = resetSecondsRowButton.getY();
            int rsrw = resetSecondsRowButton.getWidth();
            int rsrh = resetSecondsRowButton.getHeight();

            boolean inside = mouseX >= rsrx && mouseX < rsrx + rsrw && mouseY >= rsry && mouseY < rsry + rsrh;
            if (!inside)
            {
                editingResetSeconds = false;
                resetSecondsBuffer = Double.toString(workingConfig.resetAfterSeconds);
                updateEnableStates();
            }
        }

        return handled;
    }

    /* ----- 마우스 스크롤 ----- */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        if (verticalAmount != 0)
        {
            int delta = (int)(verticalAmount * -10);

            scrollOffset += delta;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

            updateLeftButtonPositions();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}