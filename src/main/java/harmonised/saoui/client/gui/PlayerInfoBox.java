package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;

public class PlayerInfoBox extends Box
{
    public PlayerInfoBox( String name )
    {
        super( name );
        width = 96;
        height = 128;
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        renderBg( stack, Minecraft.getInstance(), mouseX, mouseY );
//        Renderer.drawEntityOnScreen( stack, x, y, 1, mouseX, mouseY, Minecraft.getInstance().player );
    }

    @Override
    protected void renderBg( MatrixStack stack, Minecraft mc, int mouseX, int mouseY )
    {
        Renderer.fillGradient( stack, x, y, x + width, y + height, 0xffffffff, 0xddddddff );
    }

    @Override
    public int getHeightRealms()
    {
        return (int) getHeightFloat();
    }

    @Override
    public int getWidth()
    {
        return (int) getWidthFloat();
    }
}
