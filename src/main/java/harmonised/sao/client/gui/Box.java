package harmonised.sao.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class Box extends Widget
{
    private MainWindow sr = Minecraft.getInstance().getWindow();
    public final List<ListButton> buttons = new ArrayList<>();

    public float x, y;
    private final int buttonHeight = 16;
    private int buttonWidth = 16;
    public final int buttonGap = 4;
    private final int midX, midY;
    private final ListButton emptyButton;
    public ListButton activeButton = null;
    public final String name;

    //Box Arrow
    private static final int boxArrowWidth = 68;
    private static final int boxArrowHeight = 512;

    //Button Arrow
    private static final int buttonArrowSize = 128;

    public Box( String name )
    {
        super( 0, 0, 0, 0, new TranslationTextComponent( "" ) );
        this.width = sr.getGuiScaledWidth();
        this.height = sr.getGuiScaledHeight();
        this.name = name;
        midX = sr.getGuiScaledWidth()/2;
        midY = sr.getGuiScaledHeight()/2;
        emptyButton = new ListButton( this );
        emptyButton.setMsg( new TranslationTextComponent( "sao.empty" ) );
    }

    @Override
    public int getHeight()
    {
        return buttons.size()*getButtonHeight() + ( Math.max( 0, buttons.size()-1 ) )*buttonGap;
    }

    @Override
    public int getWidth()
    {
        return getButtonWidth();
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        if( buttons.size() > 0 )
        {
            int i = 0;
            for( ListButton button : buttons )
            {
                button.x = x;
                button.y = y + (buttonHeight+buttonGap)*i;
                button.renderButton( stack, mouseX, mouseY, partialTicks );
                i++;
            }
        }
        else
        {
            emptyButton.x = x;
            emptyButton.y = y - emptyButton.getHeight()/2f;
            emptyButton.renderButton( stack, mouseX, mouseY, partialTicks );
        }
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
}
