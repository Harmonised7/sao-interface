package harmonised.mco.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public class PlayerInfoBox extends InfoBox
{
    public PlayerInfoBox()
    {
        width = 96;
        height = 128;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBg(stack, Minecraft.getInstance(), mouseX, mouseY);
//        Renderer.drawEntityOnScreen(stack, x, y, 1, mouseX, mouseY, Minecraft.getInstance().player);
    }
}
