package harmonised.saoui.config;

import harmonised.saoui.util.Reference;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SaouiConfefeg
{
    public static Confefeger confefeger = Confefeger.registerConfefeg( Reference.MOD_ID );
    public static Confefeger.Confefeg<Integer> buttonColor;
    public static Confefeger.Confefeg<Integer> buttonLockedColor;
    public static Confefeger.Confefeg<Integer> buttonHoverColor;
    public static Confefeger.Confefeg<Integer> buttonActiveColor;
    public static Confefeger.Confefeg<Integer> textColor;
    public static Confefeger.Confefeg<Integer> iconColor;
    public static Confefeger.Confefeg<Integer> iconBaseColor;
    public static Confefeger.Confefeg<Integer> iconHoverColor;

    public static void init()
    {
        buttonColor = confefeger
                .build( "buttonColor" )
                .category( "GUI" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0xffffff, 0, Integer.MAX_VALUE );
        buttonLockedColor = confefeger
                .build( "buttonLockedColor" )
                .category( "GUI" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0xaa5555, 0, Integer.MAX_VALUE );
        buttonHoverColor = confefeger
                .build( "buttonHoverColor" )
                .category( "GUI" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0x00ff00, 0, Integer.MAX_VALUE );
        buttonActiveColor = confefeger
                .build( "buttonActiveColor" )
                .category( "GUI" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0xff22ff, 0, Integer.MAX_VALUE );
        textColor = confefeger
                .build( "textColor" )
                .category( "GUI" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0xdddddd, 0, Integer.MAX_VALUE );
        iconColor = confefeger
                .build( "iconColor" )
                .category( "GUI" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0xeeeeee, 0, Integer.MAX_VALUE );
        iconBaseColor = confefeger
                .build( "iconBaseColor" )
                .category( "GUI" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0x8d8d8d, 0, Integer.MAX_VALUE );
        iconHoverColor = confefeger
                .build( "iconHoverColor" )
                .category( "GUI" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0xffffff, 0, Integer.MAX_VALUE );
    }
}
