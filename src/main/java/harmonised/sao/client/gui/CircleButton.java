package harmonised.sao.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.sao.config.Config;
import harmonised.sao.util.Util;
import net.minecraft.util.ResourceLocation;

public class CircleButton extends ListButton
{
    public final ResourceLocation background = Icons.CIRCLE_BUTTON;
    private static final int circleButtonSize = 128;

//    public int color = Config.iconBaseColor,
//               hoverColor = Config.hoverColor,
//               activeColor = Config.activeColor;

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
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bind( background );

        int backgroundColor = iconBaseColor;
        if( locked )
            backgroundColor = lockedColor;
        else if( isActive() || isHovered() )
            backgroundColor = hoverColor;

        Renderer.mirrorBlitColor( stack, x, x + getWidth(), y, y + getHeight(), 0, circleButtonSize, circleButtonSize, 0, 0, circleButtonSize, circleButtonSize, backgroundColor, alpha  );

        if( foreground != null )
        {
            mc.getTextureManager().bind( foreground );
            Renderer.mirrorBlitColor( stack, x, x + getWidth(), y, y + getHeight(), 0, iconTexSize, iconTexSize, 0, 0, iconTexSize, iconTexSize, locked ? iconColor : isHovered() ? iconHoverColor : iconColor, alpha  );
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
