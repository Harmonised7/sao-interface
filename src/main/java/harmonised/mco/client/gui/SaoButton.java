package harmonised.mco.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.UUID;

public class SaoButton extends Button
{
    public Minecraft mc = Minecraft.getInstance();
    public FontRenderer font = mc.fontRenderer;

    public float x, y, width, height;
    public boolean displayTooltip = false, locked = false;
    public int alpha = 255, customTextColor = -1, customButtonColor = -1;
    public ListBox box = null;
    public InfoBox infoBox = null;
    public String regKey;
    public UUID uuid;

    public ResourceLocation foreground = null;
    public ItemStack itemStack = null;

    public static final int iconSize = 16, iconTexSize = 128;

    public static final int rectangleButtonWidth = 128;
    public static final int rectangleButtonHeight = 32;

    public ResourceLocation background = Icons.RECTANGLE_BUTTON;

    public IPressable onPress = null;
    
    public SaoButton()
    {
        super(0, 0, 4, 18, new StringTextComponent(""), (button) -> {});
    }

    public SaoButton setMsg(ItemStack itemStack)
    {
        return setMsg(new StringTextComponent((itemStack.getMaxStackSize() > 1 ? itemStack.getCount() + "x " : "") + itemStack.getDisplayName().getString()));
    }

    public SaoButton setMsg(ITextComponent msg)
    {
        super.setMessage(msg);
        float msgWidth = Renderer.getTextCompWidth(msg) + 8;
        if(getWidthFloat() < msgWidth)
            setWidthFloat(msgWidth);
        return this;
    }

    public void setWidthFloat(float width)
    {
        this.width = width;
    }

    public float getWidthFloat()
    {
        return width;
    }

    @Override
    @Deprecated
    public int getWidth()
    {
        return (int) width;
    }

    @Override
    public void setWidth(int width)
    {
        setWidthFloat(width);
    }

    public void setHeightFloat(float height)
    {
        this.height = height;
    }

    public float getHeightFloat()
    {
        return height;
    }

    @Override
    @Deprecated
    public int getHeightRealms()
    {
        return (int) height;
    }

    @Override
    @Deprecated
    public void setHeight(int height)
    {
        setHeightFloat(height);
    }

    @Override
    public void onPress()
    {
        onPress.onPress(this);
    }

    public SaoButton onPress(IPressable onPress)
    {
        this.onPress = onPress;
        return this;
    }

    public SaoButton enableTooltip()
    {
        displayTooltip = true;
        return this;
    }

    public SaoButton disableTooltip()
    {
        displayTooltip = false;
        return this;
    }

    public SaoButton lock()
    {
        locked = true;
        return this;
    }

    public SaoButton setLock(boolean state)
    {
        locked = state;
        return this;
    }

    public SaoButton unlock()
    {
        locked = false;
        return this;
    }

    public SaoButton setTextColor(int color)
    {
        customTextColor = color;
        return this;
    }

    public SaoButton setButtonColor(int color)
    {
        customButtonColor = color;
        return this;
    }

    public boolean isHovered(double mouseX, double mouseY)
    {
        return mouseX > x && mouseX < x + getWidthFloat() && mouseY > y && mouseY < y + getHeightFloat();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(!locked && isHovered(mouseX, mouseY))
        {
            onPress.onPress(this);
            return true;
        }
        else
            return false;
    }

    public SaoButton setinfoBox(InfoBox infoBox)
    {
        this.infoBox = infoBox;
        return this;
    }

    public SaoButton setIcon(ResourceLocation icon)
    {
        itemStack = null;
        foreground = icon;
        return this;
    }

    public SaoButton setItem(Item item, boolean setName)
    {
        return this.setItemStack(new ItemStack(item), setName);
    }


    public SaoButton setItemStack(ItemStack itemStack, boolean setName)
    {
        foreground = null;
        this.itemStack = itemStack;
        if(setName)
            return setMsg(itemStack);
        return this;
    }

    public void renderTooltip(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if(displayTooltip && itemStack != null)
        {
            if(isHovered(mouseX, mouseY))
                Renderer.renderTooltip(stack, itemStack, mouseX, mouseY);
        }
    }

    public void setAsActive()
    {
        if(box != null)
            box.setActiveButton(this);
    }
}
