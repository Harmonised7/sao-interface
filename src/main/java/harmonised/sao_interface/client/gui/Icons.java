package harmonised.sao_interface.client.gui;

import harmonised.sao_interface.util.Reference;
import net.minecraft.util.ResourceLocation;

public class Icons
{
    public static final ResourceLocation CIRCLE_BUTTON      = getSAOResLoc( "textures/gui/circle_button.png" );
    public static final ResourceLocation RECTANGLE_BUTTON   = getSAOResLoc( "textures/gui/rectangle_button.png" );
    public static final ResourceLocation BUTTON_ARROW       = getSAOResLoc( "textures/gui/button_arrow.png" );
    public static final ResourceLocation BOX_ARROW          = getSAOResLoc( "textures/gui/box_arrow.png" );
    public static final ResourceLocation INDICATOR          = getSAOResLoc( "textures/gui/indicator.png" );
    public static final ResourceLocation HP_BAR             = getSAOResLoc( "textures/gui/hp_bar.png" );

    public static final ResourceLocation SWORD              = getSAOResLoc( "textures/gui/icons/sword_icon.png" );
    public static final ResourceLocation ICON_BASE          = getSAOResLoc( "textures/gui/icons/icon_base.png" );

    public static ResourceLocation getSAOResLoc( String path )
    {
        return new ResourceLocation(Reference.MOD_ID, path );
    }
}
