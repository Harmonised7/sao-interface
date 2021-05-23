package harmonised.sao.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.sao.client.ClientHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class SAOScreen extends Screen
{
    private static boolean init = false;
    private static final List<Box> boxes = new ArrayList<>();
    private static float pos = 0, goalPos;
    private long lastRender = System.currentTimeMillis();

    public static Minecraft mc = Minecraft.getInstance();
    MainWindow sr = mc.getWindow();
    FontRenderer font = mc.font;
    private final int boxesGap = 6;
    private int x;
    private int y;


    public SAOScreen( ITextComponent titleIn )
    {
        super(titleIn );
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    protected void init()
    {
        initBoxes();
        updatePositions( true );
        if( !init )
        {
            init = true;
        }
//        children.addAll( boxes );
    }

    public static void initBoxes()
    {
        boxes.clear();
        boxes.add( getMainBox() );
    }

    private static void openBox( int layer, Box box )
    {
        layer++;
        int boxCount = boxes.size();
        if( layer > 0 && boxCount-1 == layer )
        {
            Box sameLayerBox = boxes.get( layer );
            if( sameLayerBox.name.equals( box.name ) )
            {
                System.out.println( "Closing box " + layer );
                boxes.remove( layer );
                return;
            }
        }
        //Close boxes if layer is too far, never close main box
        if( boxCount >= layer )
        {
            for( int i = boxCount-1; i >= layer; i-- )
            {
                if( i == 0 )
                    break;
                System.out.println( "Closing box " + i );
                boxes.remove( i );
            }
        }
        openBox( box );
    }

    private static void openBox( Box box )
    {
        System.out.println( "Opening box " + boxes.size() );
        boxes.add( box );
    }

    private static Box getMainBox()
    {
        Box box = new Box( "main" );

        box.addButton( new CircleButton( box ).setIcon( Icons.SWORD ).onPress(theButton ->
        {

            openBox( ((ListButton) theButton).box.getPos(), getPlayerBox() );
        }));

        box.addButton( new CircleButton( box ).setIcon( Icons.SWORD ).onPress( theButton ->
        {

            openBox( ((ListButton) theButton).box.getPos(), getMenuBox() );
        }));

        return box;
    }

    private static Box getPlayerBox()
    {
        Box box = new Box( "player" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.equip" ) ).onPress( theButton ->
        {

            openBox( ((ListButton) theButton).box.getPos(), getEquipTypesBox() );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.items" ) ).onPress( theButton ->
        {

            openBox( ((ListButton) theButton).box.getPos(), getItemTypesBox() );
        }));

        return box;
    }

    private static Box getMenuBox()
    {
        Box box = new Box( "menu" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.logout" ) ).onPress( theButton ->
        {
            ClientHandler.disconnect();
        }));

        return box;
    }

    private static Box getEquipTypesBox()
    {
        Box box = new Box( "equip" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.armor" ) ).onPress( theButton ->
        {
            openBox( ((ListButton) theButton).box.getPos(), getEquipBox( 0 ) );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.weapons" ) ).onPress( theButton ->
        {
            openBox( ((ListButton) theButton).box.getPos(), getEquipBox( 1 ) );
        }));

        return box;
    }

    private static Box getItemTypesBox()
    {
        Box box = new Box( "itemTypes" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.blocks" ) ).onPress( theButton ->
        {
            openBox( ((ListButton) theButton).box.getPos(), getItemsBox( 0 ) );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.items" ) ).onPress( theButton ->
        {
            openBox( ((ListButton) theButton).box.getPos(), getItemsBox( 1 ) );
        }));

        return box;
    }

    private static Box getItemsBox( int type )
    {
        Box box = new Box( type == 0 ? "blocks" : "items" );

        for( ItemStack itemStack : mc.player.inventory.items )
        {
            if( !itemStack.isEmpty() )
            {
                Item item = itemStack.getItem();
                boolean isBlock = item instanceof BlockItem;
                if( (type == 0 && isBlock) || (type == 1 && !isBlock) )
                {
                    box.addButton( new ListButton( box ).setItem( itemStack ).onPress( theButton ->
                    {
                        openBox( ((ListButton) theButton).box.getPos(), getEquipTypesBox() );
                    }));
                }
            }
        }

        return box;
    }

    private static Box getEquipBox( int type )
    {
        Box box = new Box( type == 0 ? "equipArmor" : "equipWeapon" );

        for( ItemStack itemStack : mc.player.inventory.items )
        {
            if( !itemStack.isEmpty() )
            {
                Item item = itemStack.getItem();
                box.addButton( new ListButton( box ).setItem( itemStack ).onPress( theButton ->
                {
                    openBox( ((ListButton) theButton).box.getPos(), getEquipTypesBox() );
                }));
            }
        }

        return box;
    }

    public void updatePositions( boolean force )
    {
        int middleX = sr.getGuiScaledWidth()/2;
        int middleY = sr.getGuiScaledHeight()/2;

        int compound = 0;

        int previousWidth = 0;
        int boxCount = boxes.size();

        for( int i = 0; i < boxCount; i++ )
        {
            Box box = boxes.get( i );
            if( i > 0 )
                previousWidth = boxes.get( i-1 ).getWidth();
            else
                previousWidth = box.getWidth();
            box.x = middleX + previousWidth + boxesGap + compound;
            box.y = middleY - box.getHeight()/2;
            compound += previousWidth + boxesGap;
        }

        goalPos = compound + previousWidth/2;
        if( force )
            pos = goalPos;
        else
        {
            float d = (System.currentTimeMillis() - lastRender) / 1000f;
            float posGap = goalPos - pos;
            float change = posGap*d*3;
            pos += change;
        }

        for( Box box : boxes )
        {
            box.x -= pos;
        }

        lastRender = System.currentTimeMillis();
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        updatePositions( false );
        for( Box box : boxes )
        {
            box.render( stack, mouseX, mouseY, partialTicks );
        }
    }

    public static int getBoxPos( Box box )
    {
        return boxes.indexOf( box );
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
        for( Box box : boxes )
        {
            for( ListButton listButton : box.buttons )
            {
                if( listButton.mouseClicked( mouseX, mouseY, button ) )
                    return true;
            }
        }
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