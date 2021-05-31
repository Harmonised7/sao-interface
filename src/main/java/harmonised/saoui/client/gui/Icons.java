package harmonised.saoui.client.gui;

import harmonised.saoui.util.Reference;
import net.minecraft.util.ResourceLocation;

public class Icons
{
    //HP Bar
    public static final ResourceLocation INDICATOR          = getSAOResLoc( "textures/gui/indicator.png" );
    public static final ResourceLocation HP_BAR             = getSAOResLoc( "textures/gui/hp_bar.png" );

    //UI
    public static final ResourceLocation CIRCLE_BUTTON      = getSAOResLoc( "textures/gui/circle_button.png" );
    public static final ResourceLocation RECTANGLE_BUTTON   = getSAOResLoc( "textures/gui/rectangle_button.png" );
    public static final ResourceLocation BUTTON_ARROW       = getSAOResLoc( "textures/gui/button_arrow.png" );
    public static final ResourceLocation BOX_ARROW          = getSAOResLoc( "textures/gui/box_arrow.png" );

    //Icons
    public static final ResourceLocation ICON_BASE          = getSAOResLoc( "textures/gui/icons/icon_base.png" );
    public static final ResourceLocation SWORD              = getSAOResLoc( "textures/gui/icons/sword.png" );
    public static final ResourceLocation ONE_PERSON         = getSAOResLoc( "textures/gui/icons/one_person.png" );
    public static final ResourceLocation TWO_PEOPLE         = getSAOResLoc( "textures/gui/icons/two_people.png" );
    public static final ResourceLocation GEAR               = getSAOResLoc( "textures/gui/icons/gear.png" );

    public static ResourceLocation getSAOResLoc( String path )
    {
        return new ResourceLocation(Reference.MOD_ID, path );
    }
}