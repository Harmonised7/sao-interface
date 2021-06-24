package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.saoui.config.SaouiConfefeg;
import harmonised.saoui.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.UUID;


public class ListButton extends Button
{
    public Minecraft mc = Minecraft.getInstance();
    public FontRenderer font = mc.fontRenderer;
    private boolean isCircle = false;
    public String regKey, buttonText;
    public UUID uuid;
    public final Box box;
    public Box extraBox = null;
    public float x, y;
    public boolean displayTooltip = false, locked = false;
    public int alpha = 255, customTextColor = -1;
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public ResourceLocation background = Icons.RECTANGLE_BUTTON;
    public ResourceLocation foreground = null;
    public ItemStack itemStack = null;
    public IPressable onPress = null;

    public static final int iconSize = 16, iconTexSize = 128;

    public static final int rectangleButtonWidth = 128;
    public static final int rectangleButtonHeight = 32;

    public ListButton( Box box )
    {
        super( 0, 0, 16, 18, new StringTextComponent( "" ), (button) -> {} );
        this.box = box;
    }

    @Override
    public int getHeightRealms()
    {
        return height;
    }



    @Override
    public int getWidth()
    {
        return Math.max( getTextWidth() + getIconWidth() + 6, width );
    }

    public int getTextWidth()
    {
        return font.getStringPropertyWidth( getMessage() );
    }

    public int getIconWidth()
    {
        return ( foreground != null || itemStack != null ) ? iconSize : 0;
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
        if( locked )
            backgroundColor = SaouiConfefeg.buttonLockedColor.get();
        else if( isActive() )
            backgroundColor = SaouiConfefeg.buttonActiveColor.get();
        else if( isHovered() )
            backgroundColor = SaouiConfefeg.buttonHoverColor.get();
        else if( itemStack != null )
        {
            if( SaouiConfefeg.buttonColor.get() != SaouiConfefeg.buttonActiveColor.get() && itemStack.isDamageable() && itemStack.isDamaged() )
            {
                int damageIn8Bit = (int) Util.map( itemStack.getDamage(), 0, itemStack.getMaxDamage(), 200, 0 );
                backgroundColor = damageIn8Bit | damageIn8Bit << 8 | 255 << 16;
            }
        }

        Renderer.blitColor( stack, x, x + getWidth(), y, y + getHeightRealms(), 0, rectangleButtonWidth, rectangleButtonHeight, 0, 0, rectangleButtonWidth, rectangleButtonHeight, backgroundColor, alpha  );

//        System.out.println( x + " " + y );
//        this.renderBg( stack, mc, mouseX, mouseY);
//        int j = getFGColor();

        if( foreground != null )
        {
            mc.getTextureManager().bindTexture( Icons.ICON_BASE );
            Renderer.blitColor( stack, x + 2, x + getIconWidth() + 2, y + 1, y + getIconWidth() + 1, 0, iconTexSize, iconTexSize, 0, 0, iconTexSize, iconTexSize, SaouiConfefeg.iconBaseColor.get(), alpha );
            mc.getTextureManager().bindTexture( foreground );
            Renderer.blitColor( stack, x + 2, x + getIconWidth() + 2, y + 1, y + getIconWidth() + 1, 0, iconTexSize, iconTexSize, 0, 0, iconTexSize, iconTexSize, isHovered() ? SaouiConfefeg.iconHoverColor.get() : SaouiConfefeg.iconColor.get(), alpha );
        }
        else if( itemStack != null )
            Renderer.renderGuiItem( itemStack, x + 2, y + 1 );

        int textColor = customTextColor == -1 ? SaouiConfefeg.textColor.get() : customTextColor;
        if( foreground != null || itemStack != null )
            Renderer.drawString( stack, font, this.getMessage(), x + getIconWidth() + 4, y + getHeightRealms()/4f, /* alpha << 24 | */ textColor );
        else
            Renderer.drawCenteredString( stack, font, this.getMessage(), x + getWidth()/2f, y + getHeightRealms()/4f, /* alpha << 24 | */ textColor );
    }

    public void renderTooltip( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        if( displayTooltip && itemStack != null )
        {
            if( isHovered( mouseX, mouseY ) )
                Renderer.renderTooltip( stack, itemStack, mouseX, mouseY );
        }
    }

    public boolean isHovered( double mouseX, double mouseY )
    {
        return mouseX > x && mouseX < x+getWidth() && mouseY > y && mouseY < y+ getHeightRealms();
    }

    @Override
    public boolean mouseClicked( double mouseX, double mouseY, int button )
    {
        if( !locked && isHovered( mouseX, mouseY ) )
        {
            onPress.onPress( this );
            return true;
        }
        else
            return false;
    }

    public ListButton setIcon(ResourceLocation icon )
    {
        itemStack = null;
        foreground = icon;
        return this;
    }

    public ListButton setItem( Item item, boolean setName )
    {
        return this.setItemStack( new ItemStack( item ), setName );
    }
    public ListButton setItemStack( ItemStack itemStack, boolean setName )
    {
        foreground = null;
        this.itemStack = itemStack;
        if( setName )
            return setMsg( itemStack );
        return this;
    }

    public ListButton setMsg( ItemStack itemStack )
    {
        return setMsg( new StringTextComponent( ( itemStack.getMaxStackSize() > 1 ? itemStack.getCount() + "x " : "" ) + itemStack.getDisplayName().getString() ) );

    }

    public ListButton setMsg( ITextComponent msg )
    {
        super.setMessage( msg );
        int msgWidth = font.getStringPropertyWidth( msg ) + 8;
        if( getWidth() < msgWidth )
            setWidth( msgWidth );
        return this;
    }

    public ListButton onPress( IPressable onPress )
    {
        this.onPress = onPress;
        return this;
    }

    public ListButton enableTooltip()
    {
        displayTooltip = true;
        return this;
    }

    public ListButton disableTooltip()
    {
        displayTooltip = false;
        return this;
    }

    public ListButton lock()
    {
        locked = true;
        return this;
    }

    public ListButton setLock( boolean state )
    {
        locked = state;
        return this;
    }

    public ListButton unlock()
    {
        locked = false;
        return this;
    }

    public ListButton setTextColor( int color )
    {
        customTextColor = color;
        return this;
    }

    public ListButton setExtraBox( Box extraBox )
    {
        this.extraBox = extraBox;
        return this;
    }

    public int getPos()
    {
        return box.buttons.indexOf( this );
    }

    public void setAsActive()
    {
        box.setActiveButton( this );
    }

    public boolean isActive()
    {
        return box.activeButton == this;
    }
}