package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.saoui.config.SaouiConfefeg;
import harmonised.saoui.util.Util;

public class ConfigButton extends ListButton
{
    public ConfigButton( Box box )
    {
        super( box );
        this.width = 32;
        this.height = 96;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        isHovered = isHovered( mouseX, mouseY );
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bindTexture( background );

        int backgroundColor = SaouiConfefeg.buttonColor.get();
        if( locked )
            backgroundColor = SaouiConfefeg.buttonLockedColor.get();
        else if( isActive() )
            backgroundColor = SaouiConfefeg.buttonActiveColor.get();
        else if( isHovered() )
            backgroundColor = SaouiConfefeg.buttonHoverColor.get();

        Renderer.mirrorBlitColor( stack, x, x + getWidth(), y, y + getHeightRealms(), 0, rectangleButtonWidth, rectangleButtonHeight, 0, 0, rectangleButtonWidth, rectangleButtonHeight, backgroundColor, alpha  );
    }

    @Override
    public int getWidth()
    {
        return width;
    }
}