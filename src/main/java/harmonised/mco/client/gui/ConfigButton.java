package harmonised.mco.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.mco.confefeg.Confefeger;
import harmonised.mco.confefeg.McoConfefeg;
import harmonised.mco.util.Reference;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigButton extends SaoButton
{
    private static final Logger LOGGER = LogManager.getLogger();

    public SaoButton button1 = null;
    public Slider slider1 = null, slider2 = null, slider3 = null, slider4 = null;
    public final Confefeger.Confefeg confefeg;
    public final Confefeger.ValueType valueType;

    public ConfigButton(ListBox box, Confefeger.Confefeg confefeg )
    {
        this.width = 16;
        this.height = 18;
        this.box = box;
        Object confefegValue = confefeg.get();
        this.confefeg = confefeg;
        this.valueType = this.confefeg.valueType;
        switch( valueType )
        {
            case VALUE:
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

                    slider1 = (Slider) new Slider( width, height, value, min, max, new StringTextComponent( "" ) ).setDecimals( confefegValue instanceof Integer ? 0 : -1 ).onPress( slider ->
                    {
                        Confefeger.Confefeg.setSmart( confefeg, slider1.value );
                    });
                }
            }
                break;

            case RGB:
            {
                int value = (int) confefegValue;
                slider1 = (Slider) new Slider( width, height, value & 0xff0000 >> 16, 0, 255, new StringTextComponent( "" ) ).setDecimals( 0 ).onPress( slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xff00ffff | (int) ( (Slider) slider ).value << 16 );
                }).setButtonColor( 0xff0000 );
                slider2 = (Slider) new Slider( width, height, value & 0x00ff00 >> 8, 0, 255, new StringTextComponent( "" ) ).setDecimals( 0 ).onPress( slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xffff00ff | (int) ( (Slider) slider ).value << 8 );
                }).setButtonColor( 0x00ff00 );
                slider3 = (Slider) new Slider( width, height, value & 0x0000ff, 0, 255, new StringTextComponent( "" ) ).setDecimals( 0 ).onPress( slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xffffff00 | (int) ( (Slider) slider ).value );
                }).setButtonColor( 0x0000ff );
            }
                break;

            case RGBA:
            {
                int value = (int) confefegValue;
//                if( value <= 0xffffff )
//                    value = value << 8;
                slider1 = (Slider) new Slider( width, height, ( value & 0x00ff0000 ) >> 16, 0, 255, new StringTextComponent( "" ) ).setDecimals( 0 ).onPress( slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xff00ffff | (int) ( (Slider) slider ).value << 16 );
                }).setButtonColor( 0xff0000 );
                slider2 = (Slider) new Slider( width, height, ( value & 0x0000ff00 ) >> 8, 0, 255, new StringTextComponent( "" ) ).setDecimals( 0 ).onPress( slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xffff00ff | (int) ( (Slider) slider ).value << 8 );
                }).setButtonColor( 0x00ff00 );
                slider3 = (Slider) new Slider( width, height, value & 0xff, 0, 255, new StringTextComponent( "" ) ).setDecimals( 0 ).onPress( slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0xffffff00 | (int) ( (Slider) slider ).value );
                }).setButtonColor( 0x0000ff );
                slider4 = (Slider) new Slider( width, height, ( value & 0xff000000 ) >> 24 & 0xff, 0, 255, new StringTextComponent( "" ) ).setDecimals( 0 ).onPress( slider ->
                {
                    confefeg.set( (int) confefeg.get() & 0x00ffffff | (int) ( (Slider) slider ).value << 24 );
                }).setButtonColor( 0xffffff );
            }
                break;

            case BOOLEAN:
            {
                button1 = new ListButton( box ).setButtonColor( (boolean) confefeg.get() ? McoConfefeg.buttonActiveColor.get() : McoConfefeg.buttonColor.get() ).setMsg( new TranslationTextComponent( "mco." + confefeg.name ) ).onPress(theButton ->
                {
                    if( (boolean) confefeg.get() )
                        confefeg.set( false );
                    else
                        confefeg.set( true );
//                    ((ListButton) theButton).setMsg( new TranslationTextComponent( "mco." + ((boolean) confefeg.get() ? "on" : "off" ) ) );
                    ((ListButton) theButton).setButtonColor( (boolean) confefeg.get() ? McoConfefeg.buttonActiveColor.get() : McoConfefeg.buttonColor.get() );
                });
                setMsg( new TranslationTextComponent( Reference.MOD_ID + "." + confefeg.name ) );
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

//        int backgroundColor = McoConfefeg.buttonColor.get();
//        if( locked )
//            backgroundColor = McoConfefeg.buttonLockedColor.get();
//        else if( isActive() )
//            backgroundColor = McoConfefeg.buttonActiveColor.get();
//        else if( isHovered() )
//            backgroundColor = McoConfefeg.buttonHoverColor.get();

//        if( valueType == Confefeger.ValueType.RGB )
//        {
//            Renderer.blitColor( stack, x, x + getWidth(), y, y + getHeightRealms(), 0, rectangleButtonWidth, rectangleButtonHeight, 0, 0, rectangleButtonWidth, rectangleButtonHeight, backgroundColor, Util.multiplyAlphaColor( alpha, backgroundColor ) );
//        }
        if( valueType == Confefeger.ValueType.BOOLEAN )
        {
            button1.x = x;
            button1.y = y;
            button1.renderButton( stack, mouseX, mouseY, partialTicks );
        }
        else
        {
            slider1.x = x;
            slider1.y = y;
            slider1.alpha = alpha;
            slider1.renderButton( stack, mouseX, mouseY, partialTicks );
            if( valueType == Confefeger.ValueType.RGB || valueType == Confefeger.ValueType.RGBA )
            {
                slider2.x = slider1.x + slider1.getWidthFloat();
                slider2.y = y;
                slider2.alpha = alpha;
                slider2.renderButton( stack, mouseX, mouseY, partialTicks );

                slider3.x = slider2.x + slider2.getWidthFloat();
                slider3.y = y;
                slider3.alpha = alpha;
                slider3.renderButton( stack, mouseX, mouseY, partialTicks );
                if( valueType == Confefeger.ValueType.RGBA )
                {
                    slider4.x = slider3.x + slider3.getWidthFloat();
                    slider4.y = y;
                    slider4.alpha = alpha;
                    slider4.renderButton( stack, mouseX, mouseY, partialTicks );
                }
            }
            Renderer.drawCenteredString( stack, font, getMessage(), x + width/2f, y, 0xffffff );
        }
    }

    @Override
    public boolean mouseClicked( double mouseX, double mouseY, int button )
    {
        switch( valueType )
        {
            case BOOLEAN:
                this.button1.mouseClicked( mouseX, mouseY, button );
                break;

            case VALUE:
                slider1.mouseClicked( mouseX, mouseY, button );
                break;

            case RGB:
                slider1.mouseClicked( mouseX, mouseY, button );
                slider2.mouseClicked( mouseX, mouseY, button );
                slider3.mouseClicked( mouseX, mouseY, button );
                break;

            case RGBA:
                slider1.mouseClicked( mouseX, mouseY, button );
                slider2.mouseClicked( mouseX, mouseY, button );
                slider3.mouseClicked( mouseX, mouseY, button );
                slider4.mouseClicked( mouseX, mouseY, button );
                break;
        }
        return false;
    }

    @Override
    public boolean mouseDragged( double mouseX, double mouseY, int button, double dragX, double dragY )
    {
        if( valueType == Confefeger.ValueType.BOOLEAN )
            return false;

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
    public void setWidthFloat( float width )
    {
        try
        {
            switch( valueType )
            {
                case BOOLEAN:
                    button1.setWidthFloat( width );
                    break;

                case VALUE:
                    slider1.setWidthFloat( width );
                    break;

                case RGB:
                    slider1.setWidthFloat( width/3f );
                    slider2.setWidthFloat( width/3f );
                    slider3.setWidthFloat( width/3f );
                    break;

                case RGBA:
                    slider1.setWidthFloat( width/4f );
                    slider2.setWidthFloat( width/4f );
                    slider3.setWidthFloat( width/4f );
                    slider4.setWidthFloat( width/4f );
                    break;
            }
        }
        catch( Exception e )
        {
            LOGGER.error( "Tried to set width before ConfigButton initialized!" );
        }
        super.setWidthFloat( width );
    }
}