package harmonised.sao_interface.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class Box extends Widget
{
    private MainWindow sr = Minecraft.getInstance().getWindow();
    public List<ListButton> buttons;

    private final int buttonHeight = 16;
    private final int buttonWidth = 16;
    private final int buttonGap = 4;
    private final int midX, midY;

    public Box( List<ListButton> buttons )
    {
        super( 0, 0, 0, 0, new TranslationTextComponent( "" ) );
        this.buttons = buttons;
        this.width = sr.getGuiScaledWidth();
        this.height = sr.getGuiScaledHeight();
        midX = sr.getGuiScaledWidth()/2;
        midY = sr.getGuiScaledHeight()/2;
    }

    @Override
    public int getHeight()
    {
        return getButtonHeight() * (buttonHeight+buttonGap);
    }

    @Override
    public int getWidth()
    {
        return getButtonWidth();
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        int buttonStart = midY - getButtonHeight() - buttonGap/2;
        int i = 0;
        for( ListButton button : buttons )
        {
            button.x = midX - button.getWidth()/2;
            button.y = buttonStart + (buttonHeight+buttonGap)*i;
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
}
