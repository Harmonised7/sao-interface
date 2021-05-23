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
    private final int buttonGap = 4;
    private final int midX, midY;
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
    }

    @Override
    public int getHeight()
    {
        int buttonsHeight = buttons.size() * getButtonHeight();
        return buttonsHeight + buttons.size() * buttonGap/2;
    }

    @Override
    public int getWidth()
    {
        return getButtonWidth();
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
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
        buttonWidth = Math.max( buttonWidth, button.getWidth() );
    }
}
