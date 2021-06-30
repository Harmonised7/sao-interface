package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.saoui.confefeg.SaouiConfefeg;
import harmonised.saoui.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;


public class ListButton extends SaoButton
{
    private boolean isCircle = false;

    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public ListButton( ListBox box )
    {
        width = 16;
        height = 18;
        this.box = box;
    }

    public int getIconWidth()
    {
        return ( foreground != null || itemStack != null ) ? iconSize : 0;
    }

    @Override
    public float getWidthFloat()
    {
        return Math.max( getTextWidth() + getIconWidth() + 6, width );
    }

    public float getTextWidth()
    {
        return Renderer.getTextCompWidth( getMessage() );
    }

    @Override
    public void renderButton( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        isHovered = isHovered( mouseX, mouseY );
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bindTexture( background );

        int backgroundColor = SaouiConfefeg.buttonColor.get();
        if( customButtonColor != -1 )
            backgroundColor = customButtonColor;
        if( locked )
            backgroundColor = SaouiConfefeg.buttonLockedColor.get();
        else if( isActive() )
            backgroundColor = SaouiConfefeg.buttonActiveColor.get();
        else if( isHovered() )
            backgroundColor = SaouiConfefeg.buttonHoverColor.get();
        else if( itemStack != null )
        {
            if(!SaouiConfefeg.buttonColor.get().equals( SaouiConfefeg.buttonActiveColor.get() ) && itemStack.isDamageable() && itemStack.isDamaged() )
            {
                int damageIn8Bit = (int) Util.map( itemStack.getDamage(), 0, itemStack.getMaxDamage(), 200, 0 );
                backgroundColor = damageIn8Bit | damageIn8Bit << 8 | 255 << 16;
            }
        }

        Renderer.blitColor( stack, x, x + getWidthFloat(), y, y + getHeightFloat(), 0, rectangleButtonWidth, rectangleButtonHeight, 0, 0, rectangleButtonWidth, rectangleButtonHeight, backgroundColor, Util.multiplyAlphaColor( alpha, backgroundColor ) );

        if( foreground != null )
        {
            mc.getTextureManager().bindTexture( Icons.ICON_BASE );
            Renderer.blitColor( stack, x + 2, x + getIconWidth() + 2, y + 1, y + getIconWidth() + 1, 0, iconTexSize, iconTexSize, 0, 0, iconTexSize, iconTexSize, SaouiConfefeg.iconBaseColor.get(), Util.multiplyAlphaColor( alpha, SaouiConfefeg.iconBaseColor.get() ) );
            mc.getTextureManager().bindTexture( foreground );
            int color = isHovered() ? SaouiConfefeg.iconHoverColor.get() : SaouiConfefeg.iconColor.get();
            Renderer.blitColor( stack, x + 2, x + getIconWidth() + 2, y + 1, y + getIconWidth() + 1, 0, iconTexSize, iconTexSize, 0, 0, iconTexSize, iconTexSize, color, Util.multiplyAlphaColor( alpha, color ) );
        }
        else if( itemStack != null )
            Renderer.renderGuiItem( itemStack, x + 2, y + 1 );

        int textColor = customTextColor == -1 ? SaouiConfefeg.textColor.get() : customTextColor;
        if( foreground != null || itemStack != null )
            Renderer.drawString( stack, font, this.getMessage(), x + getIconWidth() + 4, y + getHeightFloat()/4f, /* alpha << 24 | */ textColor );
        else
            Renderer.drawCenteredString( stack, font, this.getMessage(), x + getWidthFloat()/2f, y + getHeightFloat()/4f, /* alpha << 24 | */ textColor );
    }

    public int getPos()
    {
        return box.buttons.indexOf( this );
    }

    public boolean isActive()
    {
        return box.activeButton == this;
    }
}