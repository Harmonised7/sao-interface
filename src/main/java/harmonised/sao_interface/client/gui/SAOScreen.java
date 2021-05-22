package harmonised.sao_interface.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.sao_interface.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class SAOScreen extends Screen
{
    private final List<Box> boxes = new ArrayList<>();

    Minecraft mc = Minecraft.getInstance();
    MainWindow sr = mc.getWindow();
    FontRenderer font = mc.font;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;


    public SAOScreen( ITextComponent titleIn )
    {
        super(titleIn );
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        boxes.clear();
        List<ListButton> mainButtons = new ArrayList<>();
        ListButton playerButton = new ListButton( 0, 0, "player", "", button ->
        {
            System.out.println( "player" );
        });
        ListButton menuButton = new ListButton( 0, 0, "menu", "", button ->
        {
            System.out.println( "menu" );
        });
        mainButtons.add( playerButton );
        mainButtons.add( menuButton );
        boxes.add( new Box( mainButtons ) );
        children.addAll( boxes );
        for( Box box : boxes )
        {
            children.addAll( box.buttons );
        }
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        renderTooltip( stack, new StringTextComponent( mouseX + " " + mouseY ), mouseX, mouseY );
        for( Box box : boxes )
        {
            box.render( stack, mouseX, mouseY, partialTicks );
        }
    }

    @Override
    public void renderBackground( MatrixStack stack, int p_renderBackground_1_)
    {
        if (this.mc != null)
        {
            this.fillGradient( stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this, stack ));
        }
        else
            this.renderBackground( stack, p_renderBackground_1_ );
    }



    @Override
    public boolean mouseScrolled( double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled( mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked( double mouseX, double mouseY, int button )
    {
        return super.mouseClicked( mouseX, mouseY, button );
    }

    @Override
    public boolean mouseReleased( double mouseX, double mouseY, int button )
    {
        return super.mouseReleased( mouseX, mouseY, button );
    }

    @Override
    public boolean mouseDragged( double mouseX, double mouseY, int button, double deltaX, double deltaY )
    {
        return super.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
    }

}