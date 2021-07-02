package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.saoui.confefeg.SaouiConfefeg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class InfoBox extends SaoButton
{
    List<ITextComponent> lines = new ArrayList<>();

    public InfoBox()
    {
        width = 32;
        height = 24;
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        renderBg( stack, Minecraft.getInstance(), mouseX, mouseY );
        int i = 0;
        Renderer.drawCenteredString( stack, font, mc.player.getName(), x + getWidthFloat()/2f, y + 4, SaouiConfefeg.textColor.get() );
        for( ITextComponent line : lines )
        {
            Renderer.drawString( stack, font, line, x + 4, y + 20 + font.FONT_HEIGHT*i, SaouiConfefeg.textColor.get() );
            i++;
        }

        Renderer.drawEntityOnScreen( stack, x, y, 1, mouseX, mouseY, mc.player );
//        Renderer.drawEntityOnScreen( stack, x, y, 1, mouseX, mouseY, Minecraft.getInstance().player );
    }

    @Override
    protected void renderBg( MatrixStack stack, Minecraft mc, int mouseX, int mouseY )
    {
        Renderer.fillGradient( stack, x, y, x + width, y + height, SaouiConfefeg.buttonColor.get(), SaouiConfefeg.buttonFadeColor.get() );
    }

    public InfoBox addLine( ITextComponent line )
    {
        lines.add( line );
        float textHeight = lines.size() * font.FONT_HEIGHT + 24;

        if( height < textHeight )
            setHeightFloat( textHeight );

        float maxWidth = 0;
        for( ITextComponent theLine : lines )
        {
            maxWidth = Math.max( maxWidth, Renderer.getTextCompWidth( theLine ) );
        }
        if( getWidthFloat() < maxWidth )
            setWidthFloat( maxWidth+32 );

        return this;
    }
}
