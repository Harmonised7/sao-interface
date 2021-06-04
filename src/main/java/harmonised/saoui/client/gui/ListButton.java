package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.saoui.config.SaoConfig;
import harmonised.saoui.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;


public class ListButton extends Button
{
    public Minecraft mc = Minecraft.getInstance();
    private FontRenderer font = mc.font;
    private boolean isCircle = false;
    public String regKey, buttonText;
    public final Box box;
    public float x, y;
    public boolean displayTooltip = false, locked = false;
    public int color = SaoConfig.buttonColor,
               lockedColor = SaoConfig.buttonLockedColor,
               alpha = 255,
               hoverColor = SaoConfig.buttonHoverColor,
               activeColor = SaoConfig.buttonActiveColor,
               textColor = SaoConfig.textColor,
               iconBaseColor = SaoConfig.iconBaseColor,
               iconColor = SaoConfig.iconColor,
               iconHoverColor = SaoConfig.iconHoverColor;
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    private final ResourceLocation background = Icons.RECTANGLE_BUTTON;
    protected ResourceLocation foreground = null;
    protected ItemStack itemStack = null;
    protected IPressable onPress = null;

    public final int iconSize = 16, iconTexSize = 128;

    private static final int rectangleButtonWidth = 128;
    private static final int rectangleButtonHeight = 32;

    public ListButton( Box box )
    {
        super( 0, 0, 16, 18, new StringTextComponent( "" ), (button) -> {} );
        this.box = box;
    }

    @Override
    public int getHeight()
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
        return font.width( getMessage() );
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
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bind( background );

        int backgroundColor = color;
        if( locked )
            backgroundColor = lockedColor;
        else if( isActive() )
            backgroundColor = activeColor;
        else if( isHovered() )
            backgroundColor = hoverColor;
        else if( itemStack != null )
        {
            if( mc.player.inventory.getSelected().equals( itemStack ) )
                backgroundColor = activeColor;
            else if( itemStack.isDamageableItem() && itemStack.isDamaged() )
            {
                int damageIn8Bit = (int) Util.map( itemStack.getDamageValue(), 0, itemStack.getMaxDamage(), 200, 0 );
                backgroundColor = damageIn8Bit | damageIn8Bit << 8 | 255 << 16;
            }
        }

        Renderer.mirrorBlitColor( stack, x, x + getWidth(), y, y + getHeight(), 0, rectangleButtonWidth, rectangleButtonHeight, 0, 0, rectangleButtonWidth, rectangleButtonHeight, backgroundColor, alpha  );

//        System.out.println( x + " " + y );
//        this.renderBg( stack, mc, mouseX, mouseY);
//        int j = getFGColor();

        if( foreground != null )
        {
            mc.getTextureManager().bind( Icons.ICON_BASE );
            Renderer.mirrorBlitColor( stack, x + 2, x + getIconWidth() + 2, y + 1, y + getIconWidth() + 1, 0, iconTexSize, iconTexSize, 0, 0, iconTexSize, iconTexSize, iconBaseColor, alpha );
            mc.getTextureManager().bind( foreground );
            Renderer.mirrorBlitColor( stack, x + 2, x + getIconWidth() + 2, y + 1, y + getIconWidth() + 1, 0, iconTexSize, iconTexSize, 0, 0, iconTexSize, iconTexSize, isHovered() ? iconHoverColor : iconColor, alpha );
        }
        else if( itemStack != null )
            Renderer.renderGuiItem( itemStack, x + 2, y + 1 );

        if( foreground != null || itemStack != null )
            Renderer.drawString( stack, font, this.getMessage(), x + getIconWidth() + 4, y + getHeight()/4f, /* alpha << 24 | */ textColor );
        else
            Renderer.drawCenteredString( stack, font, this.getMessage(), x + getWidth()/2f, y + getHeight()/4f, /* alpha << 24 | */ textColor );
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
        return mouseX > x && mouseX < x+getWidth() && mouseY > y && mouseY < y+getHeight();
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
        return setMsg( new StringTextComponent( ( itemStack.getMaxStackSize() > 1 ? itemStack.getCount() + "x " : "" ) + itemStack.getHoverName().getString() ) );

    }

    public ListButton setMsg( ITextComponent msg )
    {
        super.setMessage( msg );
        int msgWidth = font.width( msg ) + 8;
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

    public ListButton unlock()
    {
        locked = false;
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