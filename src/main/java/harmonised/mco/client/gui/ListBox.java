package harmonised.mco.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.mco.util.Reference;
import harmonised.mco.util.Util;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class ListBox extends SaoButton
{
    private MainWindow sr = Minecraft.getInstance().getMainWindow();
    private long lastRender = System.currentTimeMillis();
    public List<SaoButton> buttons = new ArrayList<>();

    public float x, y;
    public int maxDisplayButtons = 11, fadeFrom = 1;
    private float buttonWidth = 16;
    public final int buttonGap = 4;
    public int scrollPosGoal = 0;
    public float scrollPos = 0;
    public int backgroundColor = 0xffffff00;
    private final int midX, midY;
    private final SaoButton emptyButton;
    public SaoButton activeButton = null;
    public final String name;
    public boolean scrollLocked = false;

    //Box Arrow
    private static final int boxArrowWidth = 68;
    private static final int boxArrowHeight = 512;

    //Button Arrow
    private static final int buttonArrowSize = 128;

    public ListBox(String name )
    {
//        this.width = Renderer.getScaledWidth();
//        this.height = Renderer.getScaledHeight();
        this.name = name;
        midX = Renderer.getScaledWidth()/2;
        midY = Renderer.getScaledHeight()/2;
        emptyButton = new ListButton( this );
        emptyButton.setMsg( new TranslationTextComponent( Reference.MOD_ID + ".empty" ) );
    }

    @Override
    public int getHeightRealms()
    {
        return (int) getHeightFloat();
    }

    @Override
    public float getHeightFloat()
    {
        int buttonCount = Math.min( maxDisplayButtons, buttons.size() );
        float height = 0;
        for( int i = 0; i < buttonCount; i++ )
        {
            height += buttons.get( i ).getHeightFloat();
        }
        height += ( Math.max( 0, buttonCount-1 ) )*buttonGap;
        return height;
    }

    @Override
    public int getWidth()
    {
        return (int) getWidthFloat();
    }

    @Override
    public float getWidthFloat()
    {
        return buttons.size() > 0 ? getButtonWidth() : emptyButton.getWidthFloat();
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        if( buttons.size() > 0 )
        {
            int buttonCount = buttons.size();
            int displayButtonsCount = Math.min( maxDisplayButtons, buttons.size() );
            int midButton = displayButtonsCount/2;
            boolean isEven = displayButtonsCount%2 == 0;
            int fadeInterval = Math.max( 25, Math.min( 200, 560/maxDisplayButtons ) );
            if( !Util.isReleased() )
                Renderer.drawCenteredString( stack, Minecraft.getInstance().fontRenderer, new StringTextComponent( "" + name ), x + getWidthFloat()/2f, y - 10, 0xffffff );
            int fadeStep;
            for( int i = 0; i < displayButtonsCount; i++ )
            {
                int buttonIndex = (i + scrollPosGoal )%buttonCount;
                if( buttonIndex < 0 )
                    buttonIndex += buttonCount;
                SaoButton button = buttons.get( buttonIndex );
                if( i >= maxDisplayButtons )
                    break;
                button.x = x;
                button.y = y + (button.getHeightFloat()+buttonGap)*i;
                int thisMidButton = midButton;
                if( isEven && i < midButton )
                    thisMidButton--;
                fadeStep = Math.abs( i - thisMidButton );
                button.alpha = 255 - fadeInterval*fadeStep;
                button.renderButton( stack, mouseX, mouseY, partialTicks );
                if( !Util.isReleased() )
                    Renderer.drawCenteredString( stack, Minecraft.getInstance().fontRenderer, new StringTextComponent( "" + buttonIndex ), button.x, button.y, 0xffffff );
            }
        }
        else
        {
            emptyButton.x = x;
            emptyButton.y = y - emptyButton.getHeightFloat()/2f;
            emptyButton.renderButton( stack, mouseX, mouseY, partialTicks );
        }
        lastRender = System.currentTimeMillis();
    }

    @Override
    public boolean mouseClicked( double mouseX, double mouseY, int key )
    {
        for( SaoButton saoButton : buttons )
        {
            if( saoButton.mouseClicked( mouseX, mouseY, key ) )
                return true;
        }
        return super.mouseClicked(mouseX, mouseY, key );
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY )
    {
        super.mouseMoved( mouseX, mouseY );
        for( SaoButton saoButton : buttons )
        {
            saoButton.mouseMoved( mouseX, mouseY );
        }
    }

    public float getButtonWidth()
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
        if( !scrollLocked && mouseX > x && mouseX < x+getWidthFloat() && mouseY > y && mouseY < y+ getHeightFloat() )
        {
            if( amount > 0 )
                scrollPosGoal--;
            else
                scrollPosGoal++;

            return true;
        }
        return false;
    }

    public void addButton( SaoButton button )
    {
        this.buttons.add( button );
        if( button.getWidthFloat() > buttonWidth )
        {
            float newButtonWidth = button.getWidthFloat();
            buttonWidth = newButtonWidth;
            for( SaoButton saoButton : buttons )
            {
                saoButton.setWidthFloat( newButtonWidth );
            }
        }
        else
            button.setWidthFloat( buttonWidth );
    }

    public void clearButtons()
    {
        buttons.clear();
    }

    public void setActiveButton( SaoButton button )
    {
        activeButton = button;
    }

    public ListBox lockScroll()
    {
        scrollLocked = true;
        return this;
    }

    public ListBox unlockScroll()
    {
        scrollLocked = false;
        return this;
    }

    public ListBox setMaxDisplayButtons(int value )
    {
        this.maxDisplayButtons = Math.max( 1, value );
        return this;
    }
}
