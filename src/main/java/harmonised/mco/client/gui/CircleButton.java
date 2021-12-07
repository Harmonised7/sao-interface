package harmonised.mco.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.mco.confefeg.McoConfefeg;
import harmonised.mco.util.Util;
import net.minecraft.resources.ResourceLocation;

public class CircleButton extends SaoButton
{
    public final ResourceLocation background = Icons.CIRCLE_BUTTON;
    private static final int circleButtonSize = 128;

    public CircleButton(ListBox box)
    {
        this.width = iconSize;
        this.height = iconSize;
        this.box = box;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        isHovered = isHovered(mouseX, mouseY);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderTexture(0,background);

        int backgroundColor = McoConfefeg.iconBaseColor.get();
        if(locked)
            backgroundColor = McoConfefeg.buttonLockedColor.get();
        else if(isActive() || isHovered())
            backgroundColor = McoConfefeg.iconHoverColor.get();

        Renderer.blitColor(stack, x, x + getWidthFloat(), y, y + getHeightFloat(), 0, circleButtonSize, circleButtonSize, 0, 0, circleButtonSize, circleButtonSize, backgroundColor, Util.multiplyAlphaColor(alpha, backgroundColor));

        if(foreground != null)
        {
            RenderSystem.setShaderTexture(0,foreground);
            int color = locked ? McoConfefeg.iconColor.get() : isHovered() ? McoConfefeg.iconHoverColor.get() : McoConfefeg.iconColor.get();
            Renderer.blitColor(stack, x, x + getWidthFloat(), y, y + getHeightFloat(), 0, iconTexSize, iconTexSize, 0, 0, iconTexSize, iconTexSize, color, Util.multiplyAlphaColor(alpha, color));
        }
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY)
    {
        float radius = getWidthFloat()/2;
        return Util.getDistance(x+radius, y+radius, mouseX, mouseY) < radius;
    }

    @Override
    public int getWidth()
    {
        return (int) width;
    }

    public boolean isActive()
    {
        return box.activeButton == this;
    }
}