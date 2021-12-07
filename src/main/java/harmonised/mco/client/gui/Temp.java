package harmonised.mco.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.mco.util.Util;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TranslatableComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class Temp
{
    private static final Minecraft mc = Minecraft.getInstance();
    private static final FontRenderer fr = mc.fontRenderer;
    private static final MainWindow mw = mc.getMainWindow();

    public static void test(RenderGameOverlayEvent.Text event)
    {
        if(!Util.isReleased())
            return;
        PoseStack stack = event.getPoseStack();
        stack.push();

//            stack.scale(scale, scale, scale);
        float midX = mw.getScaledWidth()/2f;
        float midY = mw.getScaledHeight()*0.5f;
        stack.translate(midX, midY, 0);
        stack.scale(3f, 3f, 3f);
        TranslatableComponent text = new TranslatableComponent("Hey...");
        int textWidth = fr.getStringPropertyWidth(text);
        fr.func_243246_a(stack, text,
                -textWidth/2f, -fr.FONT_HEIGHT/2f, 0xffffffff);
        stack.pop();
    }
}
