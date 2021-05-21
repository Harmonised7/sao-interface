package harmonised.sao_interface.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.sao_interface.util.Reference;
import harmonised.sao_interface.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;


public class ListButton extends Button
{
    private final ResourceLocation circleButton = new ResourceLocation( Reference.MOD_ID, "textures/gui/circle_button.png" );
    private Minecraft mc = Minecraft.getInstance();
    private FontRenderer font = mc.font;
    public int style = 1;
    public ResourceLocation background, foreground;
    public String regKey, buttonText;
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    Minecraft minecraft = Minecraft.getInstance();

    public ListButton( int posX, int posY, String regKey, String buttonText, IPressable onPress )
    {
        super(posX, posY, 0, 0, new TranslationTextComponent( "" ), onPress);
        this.regKey = regKey;
        this.buttonText = buttonText;
        setStyle( 1 );
    }

    public void setStyle( int style )
    {
        switch( style )
        {
            case 1:
                background = new ResourceLocation( Reference.MOD_ID, "textures/gui/circle_button.png" );
                width = 16;
                height = 16;
                break;

            case 2:

                break;

            default:
                break;
        }
    }

    @Override
    public int getHeight()
    {
        return height;
    }



    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        int radius = getWidth()/2;
        isHovered = Util.getDistance( x+radius, y+radius, mouseX, mouseY ) < radius;
//        int midX = this.mouseX+getWidth()/2;
//        int midY = this.mouseY-getHeight()/6;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bind( circleButton );
//        this.blit( stack, this.mouseX - getWidth()/4, this.mouseY - getHeight()/4, 0, 0, 16, 16, 16, 16 );
        Renderer.mirrorBlitColor( stack, x, x + getWidth(), y, y + getHeight(), 0, 128, 128, 0, 0, 128, 128, isHovered() ? 0x00ff00 : 0xffffff, 255  );

        this.renderBg( stack, minecraft, mouseX, mouseY);
        int j = getFGColor();
//        this.drawCenteredString( stack, font, this.buttonText, midX - font.width( this.buttonText )/2, midY, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }
}