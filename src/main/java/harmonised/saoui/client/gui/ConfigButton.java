package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.saoui.config.Confefeger;
import harmonised.saoui.config.SaouiConfefeg;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.lang.model.type.PrimitiveType;

public class ConfigButton extends ListButton
{
    public ListSlider slider;

    public ConfigButton( Box box, Confefeger.Confefeg confefeg )
    {
        super( box );
        this.width = 16;
        this.height = 18;

        double value, min, max;
        Object confefegValue = confefeg.get();
        if( confefegValue instanceof Integer || confefegValue instanceof Long || confefegValue instanceof Float || confefegValue instanceof  Double )
        {
            if( confefegValue instanceof Integer )
            {
                value = (int) confefegValue;
                min = (int) confefeg.getMin();
                max = (int) confefeg.getMax();
            }
            else if( confefegValue instanceof Long )
            {
                value = (long) confefegValue;
                min = (long) confefeg.getMin();
                max = (long) confefeg.getMax();
            }
            else if( confefegValue instanceof Float )
            {
                value = (float) confefegValue;
                min = (float) confefeg.getMin();
                max = (float) confefeg.getMax();
            }
            else
            {
                value = (double) confefegValue;
                min = (double) confefeg.getMin();
                max = (double) confefeg.getMax();
            }

            slider = new ListSlider( x, y, width, height, value, min, max, new TranslationTextComponent( confefeg.confefeger.confefegName + "." + confefeg.name ), slider ->
            {
                if( confefegValue instanceof Integer )
                    confefeg.set( (int) ((ListSlider) slider ).value );
                else if( confefegValue instanceof Long )
                    confefeg.set( (long) ((ListSlider) slider ).value );
                else if( confefegValue instanceof Float )
                    confefeg.set( (float) ((ListSlider) slider ).value );
                else
                    confefeg.set( ((ListSlider) slider ).value );

            });
        }
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

        Renderer.blitColor( stack, x, x + getWidth(), y, y + getHeightRealms(), 0, rectangleButtonWidth, rectangleButtonHeight, 0, 0, rectangleButtonWidth, rectangleButtonHeight, backgroundColor, alpha );
        slider.x = x;
        slider.y = y;
        slider.alpha = alpha;
        slider.renderButton( stack, mouseX, mouseY, partialTicks );
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public boolean mouseDragged( double mouseX, double mouseY, int button, double dragX, double dragY )
    {
        if( slider.mouseDragged( mouseX, mouseY, button, dragX, dragY ) )
            return true;
        else
            return super.mouseDragged( mouseX, mouseY, button, dragX, dragY );
    }

    @Override
    public void setWidth( int width )
    {
        slider.setWidth( width );
        super.setWidth( width );
    }
}