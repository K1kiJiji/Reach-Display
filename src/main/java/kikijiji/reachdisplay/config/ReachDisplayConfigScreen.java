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


    private final java.util.List<ButtonWidget> leftButtons = new ArrayList<>();
    private final java.util.List<Integer> leftButtonBaseY = new ArrayList<>();

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private ButtonWidget addLeftButton(ButtonWidget.Builder builder, int x, int baseY, int w, int h)
    {
        int actualY = baseY - scrollOffset;
        ButtonWidget btn = this.addDrawableChild(builder.dimensions(x, actualY, w, h).build());

        leftButtons.add(btn);
        leftButtonBaseY.add(baseY);

        return btn;
    }

    private void updateLeftButtonPositions()
    {
        for (int i = 0; i < leftButtons.size(); i++)
        {
            ButtonWidget btn = leftButtons.get(i);
            int baseY = leftButtonBaseY.get(i);
            btn.setY(baseY - scrollOffset);
        }
    }


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

    private ButtonWidget mainColorButton;
    private ButtonWidget mainColorReset;

    private ButtonWidget shadowColorButton;
    private ButtonWidget shadowColorReset;

    private ButtonWidget backgroundColorButton;
    private ButtonWidget backgroundColorReset;

    private ButtonWidget keepLastDistanceToggle;
    private ButtonWidget keepLastDistanceReset;
    private ButtonWidget resetSecondsRowButton;
    private ButtonWidget resetSecondsReset;
    private boolean editingResetSeconds = false;
    private String resetSecondsBuffer = "";

    private ButtonWidget displayModeButton;
    private ButtonWidget displayModeReset;


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
        leftButtons.clear();
        leftButtonBaseY.clear();

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
        reachToggle = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showReach = !workingConfig.showReach;
                    updateEnableStates();
                }

        ),x - 10, y, buttonWidth, buttonHeight);
        reachReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.showReach = defaultConfig.showReach;
                    workingConfig.mainColor = defaultConfig.mainColor;

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

        // 변환
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
                buttonWidget ->
                {
                    MinecraftClient.getInstance().setScreen(new ReachDisplayPositionConfigScreen(this, this.workingConfig));
                }

        ),x - 10, y, fullButtonWidth, buttonHeight);
        y += 45;

        // 색
        mainColorButton = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    MinecraftClient.getInstance().setScreen
                    (
                            new ColorPickerScreen(this, workingConfig.mainColor, newColor ->
                            {
                                workingConfig.mainColor = newColor;
                                this.init();
                            })
                    );
                }

        ),x - 10, y, buttonWidth, buttonHeight);
        mainColorReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.mainColor = defaultConfig.mainColor;
                    this.init();
                }

        ),resetX, y, 20, buttonHeight);
        y += 25;

        shadowColorButton = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    MinecraftClient.getInstance().setScreen
                    (
                            new ColorPickerScreen(this, workingConfig.shadowColor, newColor ->
                            {
                                workingConfig.shadowColor = newColor;
                                this.init();
                            })
                    );
                }

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
                buttonWidget ->
                {
                    MinecraftClient.getInstance().setScreen
                    (
                            new ColorPickerScreen(this, workingConfig.backgroundColor, newColor ->
                            {
                                workingConfig.backgroundColor = newColor;
                                this.init();
                            })
                    );
                }

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

        keepLastDistanceToggle = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.keepLastDistance = !workingConfig.keepLastDistance;
                    updateEnableStates();
                }

        ),x - 10, y, buttonWidth, buttonHeight);
        keepLastDistanceReset = addLeftButton(ButtonWidget.builder
        (
                Text.literal(""),
                buttonWidget ->
                {
                    workingConfig.keepLastDistance = defaultConfig.keepLastDistance;
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
                    if (editingScale) applyScaleBuffer();
                    if (editingResetSeconds) applyResetSecondsBuffer();
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
                    if (editingScale) applyScaleBuffer();
                    if (editingResetSeconds) applyResetSecondsBuffer();
                    ReachDisplay.CONFIG = copyConfig(this.workingConfig);
                    ReachDisplayConfigManager.save(ReachDisplay.CONFIG);
                    MinecraftClient.getInstance().setScreen(parent);
                }

        ).dimensions(doneX, doneY, doneWidth, buttonHeight).build());

        int visibleHeight = this.height - startY - 80;

        maxScroll = Math.max(0, y - (startY + visibleHeight));
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        updateLeftButtonPositions();
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
        String text = formatSampleText(sampleDistance, config.displayMode);

        int textWidth  = this.textRenderer.getWidth(text);
        int textHeight = this.textRenderer.fontHeight;

        float scale = Math.max(0.1f, config.scale / 100.0f);

        var matrices = ctx.getMatrices();
        matrices.push();

        int centerXHud = rightInnerX + previewW / 2;
        int centerYHud = previewY   + previewH / 2;

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


    private void drawColorRow(DrawContext ctx, ButtonWidget colorButton, ButtonWidget resetButton, String label, int argb)
    {

        int bx = colorButton.getX();
        int by = colorButton.getY();
        int bw = colorButton.getWidth();
        int bh = colorButton.getHeight();

        int labelY = by + (bh - this.textRenderer.fontHeight) / 2;
        ctx.drawText
        (
                this.textRenderer,
                Text.literal(label),
                bx + 4,
                labelY,
                0xFFFFFFFF,
                false
        );

        ctx.fill(bx + bw - 16, by + 4, bx + bw - 4, by + bh - 4, 0xFF000000);
        ctx.fill(bx + bw - 15, by + 5, bx + bw - 5, by + bh - 5, argb);
    }


    /* ----- 렌더 ----- */
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta)
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
        this.renderBackground(ctx, mouseX, mouseY, delta);

        // 버튼
        super.render(ctx, mouseX, mouseY, delta);
        ctx.fill(leftWidth, 45, this.width, this.height, 0x50000000);
        renderPreview(ctx);

        // 헤더 텍스트
        ctx.drawText(this.textRenderer, Text.literal("▼"), 10, appearanceY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, appearanceY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("Appearance"), leftCenterX - this.textRenderer.getWidth(Text.literal("Appearance")) / 2, appearanceY, 0xFFFFFF, false);

        ctx.drawText(this.textRenderer, Text.literal("▼"), 10, transformY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, transformY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("Transform"), leftCenterX - this.textRenderer.getWidth(Text.literal("Transform")) / 2, transformY, 0xFFFFFF, false);

        ctx.drawText(this.textRenderer, Text.literal("▼"), 10, colorY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, colorY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("Color"), leftCenterX - this.textRenderer.getWidth(Text.literal("Color")) / 2, colorY, 0xFFFFFF, false);

        ctx.drawText(this.textRenderer, Text.literal("▼"), 10, keepY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, keepY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("Keep Last Distance"), leftCenterX - this.textRenderer.getWidth(Text.literal("Keep Last Distance")) / 2, keepY, 0xFFFFFF, false);

        ctx.drawText(this.textRenderer, Text.literal("▼"), 10, displayY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("▼"), leftWidth - 20, displayY, 0xFFFFFF, false);
        ctx.drawText(this.textRenderer, Text.literal("Display Mod"), leftCenterX - this.textRenderer.getWidth(Text.literal("Display Mod")) / 2, displayY, 0xFFFFFF, false);

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

            float alpha = backGroundToggle.active ? 1.0f : 0.4f;
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
        if (mainColorButton != null)
        {
            drawColorRow(ctx, mainColorButton, mainColorReset, "Main Color", workingConfig.mainColor);
        }
        if (mainColorReset != null)
        {
            int bx = mainColorReset.getX();
            int by = mainColorReset.getY();
            int bw = mainColorReset.getWidth();
            int bh = mainColorReset.getHeight();
            int iconSize = 16;

            int iconX = bx + (bw - iconSize) / 2;
            int iconY = by + (bh - iconSize) / 2;

            float alpha = mainColorReset.active ? 1.0f : 0.4f;
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
        if (shadowColorButton != null)
        {
            drawColorRow(ctx, shadowColorButton, shadowColorReset, "Shadow Color", workingConfig.shadowColor);
        }
        if (shadowColorReset != null)
        {
            int bx = shadowColorReset.getX();
            int by = shadowColorReset.getY();
            int bw = shadowColorReset.getWidth();
            int bh = shadowColorReset.getHeight();
            int iconSize = 16;

            int iconX = bx + (bw - iconSize) / 2;
            int iconY = by + (bh - iconSize) / 2;

            float alpha = shadowColorReset.active ? 1.0f : 0.4f;
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
        if (backgroundColorButton != null)
        {
            drawColorRow(ctx, backgroundColorButton, backgroundColorReset, "Background Color", workingConfig.backgroundColor);
        }
        if (backgroundColorReset != null)
        {
            int bx = backgroundColorReset.getX();
            int by = backgroundColorReset.getY();
            int bw = backgroundColorReset.getWidth();
            int bh = backgroundColorReset.getHeight();
            int iconSize = 16;

            int iconX = bx + (bw - iconSize) / 2;
            int iconY = by + (bh - iconSize) / 2;

            float alpha = backgroundColorReset.active ? 1.0f : 0.4f;
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
        if (keepLastDistanceToggle != null)
        {
            int kx = keepLastDistanceToggle.getX();
            int ky = keepLastDistanceToggle.getY();
            int kh = keepLastDistanceToggle.getHeight();

            String label = "Keep Last Distance";
            ctx.drawText(
                    this.textRenderer,
                    label,
                    kx + 4,
                    ky + (kh - this.textRenderer.fontHeight) / 2,
                    0xFFFFFFFF,
                    false
            );

            Identifier icon = workingConfig.keepLastDistance ? TOGGLE_ON_ICON : TOGGLE_OFF_ICON;
            int iconSize = 16;
            int iconX = kx + keepLastDistanceToggle.getWidth() - iconSize - 4;
            int iconY = ky + (kh - iconSize) / 2;
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
        if (resetSecondsRowButton != null)
        {
            int rx = resetSecondsRowButton.getX();
            int ry = resetSecondsRowButton.getY();
            int rh = resetSecondsRowButton.getHeight();

            boolean active = resetSecondsRowButton.active;
            float alpha = active ? 1.0f : 0.4f;
            int base = 0xFFFFFF;

            int a = (int)(alpha * 255.0f) & 0xFF;
            int color = (a << 24) | base;

            String label = "Reset After (sec)";
            ctx.drawText
            (
                    this.textRenderer,
                    label,
                    rx + 4,
                    ry + (rh - this.textRenderer.fontHeight) / 2,
                    color,
                    false
            );

            String valueText = editingResetSeconds ? resetSecondsBuffer : String.format("%.1f", workingConfig.resetAfterSeconds);

            int buttonWidth = leftWidth - 40;
            buttonWidth = Math.max(100, buttonWidth);
            int x = leftCenterX - buttonWidth / 2;
            int resetX = x - 10 + buttonWidth;
            int valueWidth = this.textRenderer.getWidth(valueText);
            int valueX = resetX - 4 - valueWidth;
            int valueY = ry + (rh - this.textRenderer.fontHeight) / 2;

            ctx.drawText
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
                ctx.fill(valueX, underlineY - 2, valueX + valueWidth, underlineY - 1, color);
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
        if (displayModeButton != null)
        {
            int dx = displayModeButton.getX();
            int dy = displayModeButton.getY();
            int dh = displayModeButton.getHeight();

            String label = "Display Format";
            ctx.drawText
            (
                    this.textRenderer,
                    label,
                    dx + 4,
                    dy + (dh - this.textRenderer.fontHeight) / 2,
                    0xFFFFFFFF,
                    false
            );

            String modeText = switch (workingConfig.displayMode)
            {
                case NUMBER_ONLY  -> "2.88";
                case WITH_BLOCKS  -> "2.88 blocks";
                case WITH_M       -> "2.88 M";
            };

            int w = this.textRenderer.getWidth(modeText);
            int valueX = dx + displayModeButton.getWidth() - w - 4;
            int valueY = dy + (dh - this.textRenderer.fontHeight) / 2;

            ctx.drawText
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

        ctx.fill(0, 0, this.width, startY + 14, 0xFF000000);
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
        boolean reachDefault = workingConfig.showReach == defaultConfig.showReach;
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
        boolean shadowDefault = workingConfig.showShadow == defaultConfig.showShadow;
        if (shadowReset != null)
        {
            boolean canUse = reachEnabled && !shadowDefault;
            shadowReset.active = canUse;
            shadowReset.setAlpha(canUse ? 1.0f : 0.4f);
        }

        // Background 검사
        boolean backgroundDefault = workingConfig.showBackground == defaultConfig.showBackground;
        if (backGroundReset != null)
        {
            boolean canUse = reachEnabled && !backgroundDefault;
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

        // Main color 검사
        boolean mainColorDefault = (workingConfig.mainColor == defaultConfig.mainColor);
        if (mainColorReset != null)
        {
            mainColorReset.active = !mainColorDefault;
            mainColorReset.setAlpha(mainColorDefault ? 0.4f : 1.0f);
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


        boolean keepLastDefault = (workingConfig.keepLastDistance == defaultConfig.keepLastDistance);
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

        boolean formatDefault = (workingConfig.displayMode == defaultConfig.displayMode);
        if (displayModeReset != null)
        {
            displayModeReset.active = !formatDefault;
            displayModeReset.setAlpha(formatDefault ? 0.4f : 1.0f);
        }

        if (resetSecondsRowButton != null)
        {
            boolean active = !workingConfig.keepLastDistance;
            resetSecondsRowButton.active = active;
            resetSecondsRowButton.setAlpha(active ? 1.0f : 0.4f);
        }
    }


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


    /* ----- 키보드 입력 ----- */
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int centerX   = this.width / 2;
        int leftWidth = centerX + this.width / 6;
        int clipTop   = startY + 18;

        if (mouseX < leftWidth && mouseY < clipTop)
        {
            return true;
        }

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

        if (editingResetSeconds && resetSecondsRowButton != null)
        {
            int rx = resetSecondsRowButton.getX();
            int ry = resetSecondsRowButton.getY();
            int rw = resetSecondsRowButton.getWidth();
            int rh = resetSecondsRowButton.getHeight();

            boolean inside = mouseX >= rx && mouseX < rx + rw && mouseY >= ry && mouseY < ry + rh;
            if (!inside)
            {
                editingResetSeconds = false;
                resetSecondsBuffer = Double.toString(workingConfig.resetAfterSeconds);
                updateEnableStates();
            }
        }

        return handled;
    }

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