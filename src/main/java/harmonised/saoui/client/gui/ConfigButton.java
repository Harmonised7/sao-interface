package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.util.DP;
import harmonised.saoui.confefeg.Confefeger;
import harmonised.saoui.confefeg.SaouiConfefeg;
import harmonised.saoui.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigButton extends ListButton
{
    public Slider slider1 = null, slider2 = null, slider3 = null, slider4 = null;
    public final Confefeger.Confefeg confefeg;
    public final Confefeger.ValueType valueType;

    public ConfigButton( Box box, Confefeger.Confefeg confefeg )
    {
        super( box );
        this.width = 16;
        this.height = 18;
        Object confefegValue = confefeg.get();
        this.confefeg = confefeg;
        this.valueType = this.confefeg.valueType;

        switch( valueType )
        {
            case NORMAL:
            {
                double value, min, max;
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

                    slider1 = new Slider( x, y, width, height, value, min, max, new TranslationTextComponent( confefeg.confefeger.confefegName + "." + confefeg.name ), slider ->
                    {
                        Confefeger.Confefeg.setSmart( confefeg, value );
                    }).setDecimals( confefegValue instanceof Integer ? 0 : -1 );
                }
            }
                break;

            case RGB:
            {
                int value = (int) confefegValue;
                slider1 = new Slider( x, y, width, height, value & 0xff0000 >> 16, 0, 255, new TranslationTextComponent( confefeg.confefeger.confefegName + "." + confefeg.name ), slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xff << 16 | (int) ( (Slider) slider ).value );
                }).setButtonColor( 0xff0000 ).setDecimals( 0 );
                slider2 = new Slider( x, y, width, height, value & 0x00ff00 >> 8, 0, 255, new TranslationTextComponent( confefeg.confefeger.confefegName + "." + confefeg.name ), slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xff << 8 | (int) ( (Slider) slider ).value );
                }).setButtonColor( 0x00ff00 ).setDecimals( 0 );
                slider3 = new Slider( x, y, width, height, value & 0x0000ff, 0, 255, new TranslationTextComponent( confefeg.confefeger.confefegName + "." + confefeg.name ), slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xff | (int) ( (Slider) slider ).value );
                }).setButtonColor( 0x00000ff ).setDecimals( 0 );
            }
                break;

            case RGBA:
            {
                int value = (int) confefegValue;
//                if( value <= 0xffffff )
//                    value = value << 8;
                slider1 = new Slider( x, y, width, height, value & 0x00ff0000 >> 16, 0, 255, new TranslationTextComponent( confefeg.confefeger.confefegName + "." + confefeg.name ), slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xff00ffff | (int) ( (Slider) slider ).value << 16 );
                }).setButtonColor( 0xff0000 ).setDecimals( 0 );
                slider2 = new Slider( x, y, width, height, value & 0x0000ff00 >> 8, 0, 255, new TranslationTextComponent( confefeg.confefeger.confefegName + "." + confefeg.name ), slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xffff00ff | (int) ( (Slider) slider ).value << 8 );
                }).setButtonColor( 0x00ff00 ).setDecimals( 0 );
                slider3 = new Slider( x, y, width, height, value & 0x000000ff, 0, 255, new TranslationTextComponent( confefeg.confefeger.confefegName + "." + confefeg.name ), slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xffffff00 | (int) ( (Slider) slider ).value );
                }).setButtonColor( 0x0000ff ).setDecimals( 0 );
                slider4 = new Slider( x, y, width, height, value & 0xff000000 >> 24 & 0xff, 0, 255, new TranslationTextComponent( confefeg.confefeger.confefegName + "." + confefeg.name ), slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0x00ffffff | (int) ( (Slider) slider ).value << 24 );
                }).setButtonColor( 0xffffff ).setDecimals( 0 );
            }
                break;
        }
    }

    @Override
    public void renderButton( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
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
//        else if( isHovered() )
//            backgroundColor = SaouiConfefeg.buttonHoverColor.get();

        Renderer.blitColor( stack, x, x + getWidth(), y, y + getHeightRealms(), 0, rectangleButtonWidth, rectangleButtonHeight, 0, 0, rectangleButtonWidth, rectangleButtonHeight, backgroundColor, Util.multiplyAlphaColor( alpha, backgroundColor ) );
        slider1.x = x;
        slider1.y = y;
        slider1.alpha = alpha;
        slider1.renderButton( stack, mouseX, mouseY, partialTicks );
        if( valueType == Confefeger.ValueType.RGB || valueType == Confefeger.ValueType.RGBA )
        {
            slider2.x = slider1.x + slider1.getWidth();
            slider2.y = y;
            slider2.alpha = alpha;
            slider2.renderButton( stack, mouseX, mouseY, partialTicks );

            slider3.x = slider2.x + slider2.getWidth();
            slider3.y = y;
            slider3.alpha = alpha;
            slider3.renderButton( stack, mouseX, mouseY, partialTicks );
            if( valueType == Confefeger.ValueType.RGBA )
            {
                slider4.x = slider3.x + slider3.getWidth();
                slider4.y = y;
                slider4.alpha = alpha;
                slider4.renderButton( stack, mouseX, mouseY, partialTicks );
            }
        }
        Renderer.drawCenteredString( stack, font, getMessage(), x + width/2f, y, 0xffffff );
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public boolean mouseDragged( double mouseX, double mouseY, int button, double dragX, double dragY )
    {
        if( slider1.mouseDragged( mouseX, mouseY, button, dragX, dragY ) )
            return true;

        if( valueType == Confefeger.ValueType.RGB || valueType == Confefeger.ValueType.RGBA )
        {
            if( slider2.mouseDragged( mouseX, mouseY, button, dragX, dragY ) )
                return true;
            if( slider3.mouseDragged( mouseX, mouseY, button, dragX, dragY ) )
                return true;
            if( valueType == Confefeger.ValueType.RGBA && slider4.mouseDragged( mouseX, mouseY, button, dragX, dragY ) )
                return true;
        }

        return super.mouseDragged( mouseX, mouseY, button, dragX, dragY );
    }

    @Override
    public void setWidth( int width )
    {
        switch( valueType )
        {
            case NORMAL:
                slider1.setWidth( width );
                break;

            case RGB:
                slider1.setWidth( width/3 );
                slider2.setWidth( width/3 );
                slider3.setWidth( width/3 );
                break;

            case RGBA:
                slider1.setWidth( width/4 );
                slider2.setWidth( width/4 );
                slider3.setWidth( width/4 );
                slider4.setWidth( width/4 );
                break;
        }
        super.setWidth( width );
    }

    public int getColor()
    {
        int color = -1;
        switch( valueType )
        {
            case RGB:
                color = ( (int) slider1.value << 16 ) | ( (int) slider2.value << 8 ) | ( (int) slider3.value );
                break;

            case RGBA:
                color = ( (int) slider1.value << 24 ) | ( (int) slider2.value << 16 ) | ( (int) slider3.value << 8 ) | ( (int) slider4.value );
                break;
        }
        return color;
    }
}