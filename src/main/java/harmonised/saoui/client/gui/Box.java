package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.saoui.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class Box extends Widget
{
    private MainWindow sr = Minecraft.getInstance().getWindow();
    private long lastRender = System.currentTimeMillis();
    public List<ListButton> buttons = new ArrayList<>();

    public float x, y;
    public int maxDisplayButtons = 7, fadeFrom = 1;
    private final int buttonHeight = 16;
    private int buttonWidth = 16;
    public final int buttonGap = 4;
    public int scrollPosGoal = 0;
    public float scrollPos = 0;
    public int backgroundColor = 0xffffff00;
    private final int midX, midY;
    private final ListButton emptyButton;
    public ListButton activeButton = null;
    public final String name;
    public boolean scrollLocked = false;

    //Box Arrow
    private static final int boxArrowWidth = 68;
    private static final int boxArrowHeight = 512;

    //Button Arrow
    private static final int buttonArrowSize = 128;

    public Box( String name )
    {
        super( 0, 0, 0, 0, new TranslationTextComponent( "" ) );
//        this.width = Renderer.getScaledWidth();
//        this.height = Renderer.getScaledHeight();
        this.name = name;
        midX = Renderer.getScaledWidth()/2;
        midY = Renderer.getScaledHeight()/2;
        emptyButton = new ListButton( this );
        emptyButton.setMsg( new TranslationTextComponent( Reference.MOD_ID + ".empty" ) );
    }

    @Override
    public int getHeight()
    {
        int buttonCount = Math.min( maxDisplayButtons, buttons.size() );
        return buttonCount*getButtonHeight() + ( Math.max( 0, buttonCount-1 ) )*buttonGap;
    }

    @Override
    public int getWidth()
    {
        return buttons.size() > 0 ? getButtonWidth() : emptyButton.getWidth();
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        if( buttons.size() > 0 )
        {
            int buttonCount = Math.min( maxDisplayButtons, buttons.size() );
            int midButton = buttonCount/2;
            boolean isEven = buttonCount%2 == 0;
            int fadeInterval = 80;
            Renderer.drawCenteredString( stack, Minecraft.getInstance().font, new StringTextComponent( "" + name ), x + getWidth()/2f, y - 10, 0xffffff );
            int fadeStep;
            for( int i = 0; i < buttonCount; i++ )
            {
                int buttonIndex = (i + scrollPosGoal )%buttonCount;
                if( buttonIndex < 0 )
                    buttonIndex += buttonCount;
                ListButton button = buttons.get( buttonIndex );
                if( i >= maxDisplayButtons )
                    break;
                button.x = x;
                button.y = y + (buttonHeight+buttonGap)*i;
                int thisMidButton = midButton;
                if( isEven && i < midButton )
                    thisMidButton--;
                fadeStep = Math.abs( i - thisMidButton );
                button.alpha = 255 - fadeInterval*fadeStep;
                button.renderButton( stack, mouseX, mouseY, partialTicks );
                Renderer.drawCenteredString( stack, Minecraft.getInstance().font, new StringTextComponent( "" + buttonIndex ), button.x, button.y, 0xffffff );
            }
        }
        else
        {
            emptyButton.x = x;
            emptyButton.y = y - emptyButton.getHeight()/2f;
            emptyButton.renderButton( stack, mouseX, mouseY, partialTicks );
        }
        lastRender = System.currentTimeMillis();
    }

    @Override
    public boolean mouseClicked( double mouseX, double mouseY, int key )
    {
        for( ListButton listButton : buttons )
        {
            if( listButton.mouseClicked( mouseX, mouseY, key ) )
                return true;
        }
        return super.mouseClicked(mouseX, mouseY, key );
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY )
    {
        super.mouseMoved( mouseX, mouseY );
        for( ListButton listButton : buttons )
        {
            listButton.mouseMoved( mouseX, mouseY );
        }
    }

    public int getButtonHeight()
    {
        return buttonHeight;
    }

    public int getButtonWidth()
    {
        return buttonWidth;
    }

//    public static enum BoxType
//    {
//        OPEN_BOX,
//        EQUIP,
//        OPEN_CRAFTING_MENU,
//        OPEN_STATS_MENU
//    }

    public int getPos()
    {
        return SAOScreen.getBoxPos( this );
    }

    @Override
    public boolean mouseScrolled( double mouseX, double mouseY, double amount )
    {
        if( !scrollLocked && mouseX > x && mouseX < x+getWidth() && mouseY > y && mouseY < y+getHeight() )
        {
            if( amount > 0 )
                scrollPosGoal--;
            else
                scrollPosGoal++;

            return true;
        }
        return false;
    }

    public void addButton( ListButton button )
    {
        this.buttons.add( button );
        if( button.getWidth() > buttonWidth )
        {
            int newButtonWidth = button.getWidth();
            buttonWidth = newButtonWidth;
            for( ListButton listButton : buttons )
            {
                listButton.setWidth( newButtonWidth );
            }
        }
        else
            button.setWidth( buttonWidth );
    }

    public void clearButtons()
    {
        buttons.clear();
    }

    public void setActiveButton( ListButton button )
    {
        activeButton = button;
    }

    public Box lockScroll()
    {
        scrollLocked = true;
        return this;
    }

    public Box unlockScroll()
    {
        scrollLocked = false;
        return this;
    }
}
