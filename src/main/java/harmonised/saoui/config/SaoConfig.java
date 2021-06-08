package harmonised.saoui.config;

import harmonised.saoui.util.Reference;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SaoConfig
{
    @Config.Name( "Button Color" )
    @Config.Category( "Gui Colors" )
    @Config.RangeInt( min=0, max=0xffffff )
    public static int buttonColor = 0xffffff;

    @Config.Name( "Button Locked Color" )
    @Config.Category( "Gui Colors" )
    @Config.RangeInt( min=0, max=0xffffff )
    public static int buttonLockedColor = 0xaa5555;

    @Config.Name( "Button Hover Color" )
    @Config.Category( "Gui Colors" )
    @Config.RangeInt( min=0, max=0xffffff )
    public static int buttonHoverColor = 0x00ff00;

    @Config.Name( "Button Active Color" )
    @Config.Category( "Gui Colors" )
    @Config.RangeInt( min=0, max=0xffffff )
    public static int buttonActiveColor = 0xff22ff;

    @Config.Name( "Text Color" )
    @Config.Category( "Gui Colors" )
    @Config.RangeInt( min=0, max=0xffffff )
    public static int textColor = 0xdddddd;

    @Config.Name( "Icon Color" )
    @Config.Category( "Gui Colors" )
    @Config.RangeInt( min=0, max=0xffffff )
    public static int iconColor = 0xeeeeee;

    @Config.Name( "Icon Base Color" )
    @Config.Category( "Gui Colors" )
    @Config.RangeInt( min=0, max=0xffffff )
    public static int iconBaseColor = 0x8d8d8d;

    @Config.Name( "Hovered Icon Color" )
    @Config.Category( "Gui Colors" )
    @Config.RangeInt( min=0, max=0xffffff )
    public static int iconHoverColor = 0xffffff;
}
