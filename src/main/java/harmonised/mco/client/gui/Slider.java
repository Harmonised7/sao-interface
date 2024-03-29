package harmonised.mco.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.util.DP;
import harmonised.mco.confefeg.McoConfefeg;
import harmonised.mco.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class Slider extends SaoButton
{
    public Minecraft mc = Minecraft.getInstance();
    public FontRenderer font = mc.fontRenderer;
    public double value, min, max;
    public int buttonWidth = 4, buttonColor = 0xffff00ff, alpha = 255, decimals = -1;

    public Slider(float width, float height, double value, double min, double max, ITextComponent title)
    {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
        this.value = value;
        this.min = min;
        this.max = max;
        setMsg(title);
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderTexture(0,Icons.RECTANGLE_BUTTON);

        float buttonX = getButtonX();
        Renderer.blitColor(stack, x, x + width, y, y + height, 0, 128, 32, 0, 0, 128, 32, McoConfefeg.buttonColor.get(), Util.multiplyAlphaColor(alpha, McoConfefeg.buttonColor.get()));
        Renderer.blitColor(stack, buttonX, buttonX + buttonWidth, y, y + getHeightFloat(), 0, 128, 32, 0, 0, 128, 32, buttonColor, Util.multiplyAlphaColor(alpha, buttonColor));
//        Renderer.drawCenteredString(stack, font, getMessage(), x + width/2f, y + getHeightRealms()/2f, Util.multiplyAlphaColor(alpha, McoConfefeg.textColor.get()));
        Renderer.drawCenteredString(stack, font, new StringTextComponent(decimals < 0 ? DP.dpSoft(value) : DP.dpCustom(value, decimals)), x + width/2f, y + getHeightFloat()/2f, McoConfefeg.textColor.get());
    }
    
    public float getButtonX()
    {
        return x + (float) Util.map(value, min, max, 0, width - buttonWidth);
    }

    public double getPos()
    {
        return Util.map(value, min, max, 0, 1);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if(mouseX > x + buttonWidth/2D && mouseX < x + width - buttonWidth/2D && mouseY > y && mouseY < y + height)
        {
            value = Util.mapCapped(mouseX, x + buttonWidth, x + width - buttonWidth, min, max);
            onClick(mouseX, mouseY);
            SAOScreen.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(button == 0 && mouseX > x + buttonWidth/2D && mouseX < x + width - buttonWidth/2D && mouseY > y && mouseY < y + height)
        {
            value = Util.mapCapped(mouseX, x + buttonWidth, x + width - buttonWidth, min, max);
            onClick(mouseX, mouseY);
            SAOScreen.markDirty();
            return true;
        }
        return false;
    }

    public Slider setButtonColor(int color)
    {
        buttonColor = color <= 0xffffff ? color | 0xff << 24 : color;
        return this;
    }

    public Slider setDecimals(int decimals)
    {
        this.decimals = decimals;
        return this;
    }
}
