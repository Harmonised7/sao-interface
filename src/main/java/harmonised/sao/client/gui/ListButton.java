package harmonised.sao.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    private final ResourceLocation background = Icons.RECTANGLE_BUTTON;
    protected ResourceLocation foreground = null;
    protected ItemStack itemStack = null;
    protected IPressable onPress = null;

    private static final int iconSize = 128;

    private static final int rectangleButtonWidth = 4;
    private static final int rectangleButtonHeight = 1;

    public ListButton( Box box )
    {
        super( 0, 0, 64, 16, new StringTextComponent( "" ), (button) -> {} );
        this.box = box;
    }

    @Override
    public int getHeight()
    {
        return height;
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
        Renderer.mirrorBlitColor( stack, x, x + getWidth(), y, y + getHeight(), 0, rectangleButtonWidth, rectangleButtonHeight, 0, 0, rectangleButtonWidth, rectangleButtonHeight, isHovered() ? 0x00ff00 : ( isActive() ? 0xff22ff : 0xffffff ), 255  );

//        System.out.println( x + " " + y );
//        this.renderBg( stack, mc, mouseX, mouseY);
//        int j = getFGColor();
        Renderer.drawCenteredString( stack, font, this.getMessage(), x + getWidth()/2f - font.width( this.buttonText )/2f, y + getHeight()/4f, 0xdddddd );
    }

    public boolean isHovered( double mouseX, double mouseY )
    {
        return mouseX > x && mouseX < x+getWidth() && mouseY > y && mouseY < y+getHeight();
    }

    @Override
    public boolean mouseClicked( double mouseX, double mouseY, int button )
    {
        if( isHovered( mouseX, mouseY ) )
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