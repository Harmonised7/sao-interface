package harmonised.sao.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.sao.client.ClientHandler;
import harmonised.sao.util.Util;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
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

    private static void openBox( ListButton button, Box box )
    {
        int layer = button.box.getPos()+1;
        int boxCount = boxes.size();
        if( layer > 0 && boxCount-1 == layer )
        {
            Box prevLayerBox = boxes.get( layer-1 );
            Box sameLayerBox = boxes.get( layer );
            if( sameLayerBox.name.equals( box.name ) )
            {
                System.out.println( "Closing box " + layer );
                prevLayerBox.setActiveButton( null );
                System.out.println( sameLayerBox.getPos() );
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

        System.out.println( "Opening box " + boxes.size() );
        boxes.add( box );

        button.setAsActive();
    }

    private static Box getMainBox()
    {
        Box box = new Box( "main" );

        box.addButton( new CircleButton( box ).setIcon( Icons.SWORD ).onPress(theButton ->
        {

            openBox( (ListButton) theButton, getPlayerBox() );
        }));

        box.addButton( new CircleButton( box ).setIcon( Icons.SWORD ).onPress( theButton ->
        {

            openBox( (ListButton) theButton, getMenuBox() );
        }));

        return box;
    }

    private static Box getPlayerBox()
    {
        Box box = new Box( "player" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.equip" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getEquipTypesBox() );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.items" ) ).onPress( theButton ->
        {

            openBox( (ListButton) theButton, getItemTypesBox() );
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
            openBox( (ListButton) theButton, getEquipArmorBox() );
        }));

//        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.weapons" ) ).onPress( theButton ->
//        {
//            openBox( (ListButton) theButton, getEquipBox() );
//        }));

        return box;
    }

    private static Box getItemTypesBox()
    {
        Box box = new Box( "itemTypes" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.blocks" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getItemsBox( 0 ) );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( "sao.items" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getItemsBox( 1 ) );
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
                    box.addButton( new ListButton( box ).setItemStack( itemStack, true ).onPress(theButton ->
                    {
                        openBox( (ListButton) theButton, getEquipTypesBox() );
                    }));
                }
            }
        }

        return box;
    }

    private static Box getEquipArmorBox()
    {
        Box box = new Box( "equipArmor" );

        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_HELMET, false ).setMsg( new TranslationTextComponent( "sao.head" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.HEAD ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_CHESTPLATE, false ).setMsg( new TranslationTextComponent( "sao.chest" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.CHEST ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_LEGGINGS, false ).setMsg( new TranslationTextComponent( "sao.legs" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.LEGS ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_BOOTS, false ).setMsg( new TranslationTextComponent( "sao.feet" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.FEET ) );
        }));

        return box;
    }

    private static Box getEquipSlotTypeBox( EquipmentSlotType slot )
    {
        Box box = new Box( "equip" + slot.getName() );
        generateEquipButtons( box, slot );
        return box;
    }

    private static void generateEquipButtons( Box box, EquipmentSlotType slot )
    {
        box.clearButtons();
        PlayerInventory inv = mc.player.inventory;
        int invSize = inv.getContainerSize();
        for( int i = 0; i < invSize; i++ )
        {
            ItemStack itemStack = inv.getItem( i );
            if( !itemStack.isEmpty() )
            {
                System.out.println( i + " " + itemStack );
                if( itemStack.canEquip( slot, mc.player ) )
                {
                    int invIndex = i;
                    box.addButton( new ListButton( box ).setItemStack( itemStack, true ).onPress(theButton ->
                    {
                        if( invIndex < 36 )
                            Util.swapItems( mc.player, invIndex, Util.getEquipmentSlotInvIndex( slot ) );
                        else if( invIndex < 40 )
                            Util.unequipItem( mc.player, invIndex );
                        generateEquipButtons( box, slot );
                    }));
                }
            }
        }
    }

    public void updatePositions( boolean force )
    {
        int middleX = sr.getGuiScaledWidth()/2;
        int middleY = sr.getGuiScaledHeight()/2;

        int compound = 0;
        int lastWidth = 0;

        for ( Box box : boxes )
        {
            box.x = middleX + compound;
            box.y = middleY - box.getHeight() / 2f;
            lastWidth = box.getWidth() + boxesGap;

            compound += lastWidth;
        }

        goalPos = compound - (boxesGap+lastWidth)/2f;
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