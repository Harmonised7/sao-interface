package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.saoui.confefeg.SaouiConfefeg;
import harmonised.saoui.util.Util;
import net.minecraft.util.ResourceLocation;

public class CircleButton extends ListButton
{
    public final ResourceLocation background = Icons.CIRCLE_BUTTON;
    private static final int circleButtonSize = 128;

    public CircleButton( Box box )
    {
        super( box );
        this.width = iconSize;
        this.height = iconSize;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        isHovered = isHovered( mouseX, mouseY );
        RenderSystem.color4f( 1.0F, 1.0F, 1.0F, this.alpha );
        int i = this.getYImage( this.isHovered() );
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA );
        mc.getTextureManager().bindTexture( background );

        int backgroundColor = SaouiConfefeg.iconBaseColor.get();
        if( locked )
            backgroundColor = SaouiConfefeg.buttonLockedColor.get();
        else if( isActive() || isHovered() )
            backgroundColor = SaouiConfefeg.iconHoverColor.get();

        Renderer.blitColor( stack, x, x + getWidth(), y, y + getHeightRealms(), 0, circleButtonSize, circleButtonSize, 0, 0, circleButtonSize, circleButtonSize, backgroundColor, Util.multiplyAlphaColor( alpha, backgroundColor ) );

        if( foreground != null )
        {
            mc.getTextureManager().bindTexture( foreground );
            int color = locked ? SaouiConfefeg.iconColor.get() : isHovered() ? SaouiConfefeg.iconHoverColor.get() : SaouiConfefeg.iconColor.get();
            Renderer.blitColor( stack, x, x + getWidth(), y, y + getHeightRealms(), 0, iconTexSize, iconTexSize, 0, 0, iconTexSize, iconTexSize, color, Util.multiplyAlphaColor( alpha, color ) );
        }
    }

    @Override
    public boolean isHovered( double mouseX, double mouseY )
    {
        int radius = getWidth()/2;
        return Util.getDistance( x+radius, y+radius, mouseX, mouseY ) < radius;
    }

    @Override
    public int getWidth()
    {
        return width;
    }
}