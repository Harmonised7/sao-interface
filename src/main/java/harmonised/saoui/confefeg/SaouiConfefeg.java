package harmonised.saoui.confefeg;

import harmonised.saoui.util.Reference;

public class SaouiConfefeg
{
    public static Confefeger confefeger = Confefeger.registerConfefeg( Reference.MOD_ID );
    //Buttons
    public static Confefeger.Confefeg<Integer> buttonColor;
    public static Confefeger.Confefeg<Integer> buttonLockedColor;
    public static Confefeger.Confefeg<Integer> buttonHoverColor;
    public static Confefeger.Confefeg<Integer> buttonActiveColor;
    public static Confefeger.Confefeg<Integer> textColor;
    public static Confefeger.Confefeg<Integer> iconColor;
    public static Confefeger.Confefeg<Integer> iconBaseColor;
    public static Confefeger.Confefeg<Integer> iconHoverColor;

    //Hp Bar
    public static Confefeger.Confefeg<Boolean> hpBarEnabled;
    public static Confefeger.Confefeg<Integer> hpBarGainIndicatorColor;
    public static Confefeger.Confefeg<Integer> hpBarLossIndicatorColor;
    public static Confefeger.Confefeg<Integer> hpBarPolyCount;
    public static Confefeger.Confefeg<Integer> renderDistance;

    public static Confefeger.Confefeg<Float> hpBarScalePlayer;
    public static Confefeger.Confefeg<Float> hpBarScaleOthers;
    public static Confefeger.Confefeg<Float> hpBarOffset;
    public static Confefeger.Confefeg<Float> hpBarOffsetDeg;
    public static Confefeger.Confefeg<Float> hpBarPitchPlayer;
    public static Confefeger.Confefeg<Float> hpBarStartHue;
    public static Confefeger.Confefeg<Float> hpBarEndHue;

    public static Confefeger.Confefeg<Integer> hpBarOutsideColor;

    //Hunger Bar
    public static Confefeger.Confefeg<Integer> HungerBarWarningColor;
    public static Confefeger.Confefeg<Integer> HungerBarSaturationColor;
    public static Confefeger.Confefeg<Double> HungerBarHungerStartHue;
    public static Confefeger.Confefeg<Double> HungerBarHungerEndHue;

    //NPC Indicator
    public static Confefeger.Confefeg<Boolean> npcIndicatorEnabled;
    public static Confefeger.Confefeg<Integer> npcIndicatorAggresiveColor;
    public static Confefeger.Confefeg<Integer> npcIndicatorHostileColor;
    public static Confefeger.Confefeg<Integer> npcIndicatorPassiveColor;
    public static Confefeger.Confefeg<Integer> npcIndicatorPlayerColor;

    //Effect Indicator
    public static Confefeger.Confefeg<Float> effectIndicatorBaseSize;
    public static Confefeger.Confefeg<Float> effectIndicatorIconSize;
    public static Confefeger.Confefeg<Integer> effectIndicatorHarmfulColor;
    public static Confefeger.Confefeg<Integer> effectIndicatorNeutralColor;
    public static Confefeger.Confefeg<Integer> effectIndicatorBeneficialColor;

    public static void init()
    {
        buttonColor = confefeger
                .build( "buttonColor" )
                .category( "gui.buttons" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xeeeeee );
        buttonLockedColor = confefeger
                .build( "buttonLockedColor" )
                .category( "gui.buttons" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xaa5555 );
        buttonHoverColor = confefeger
                .build( "buttonHoverColor" )
                .category( "gui.buttons" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0x00ff00 );
        buttonActiveColor = confefeger
                .build( "buttonActiveColor" )
                .category( "gui.buttons" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xff22ff );
        textColor = confefeger
                .build( "textColor" )
                .category( "gui.buttons" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xdddddd );
        iconColor = confefeger
                .build( "iconColor" )
                .category( "gui.buttons" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xeeeeee );
        iconBaseColor = confefeger
                .build( "iconBaseColor" )
                .category( "gui.buttons" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0x8d8d8d );
        iconHoverColor = confefeger
                .build( "iconHoverColor" )
                .category( "gui.buttons" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xffffff );

        hpBarEnabled = confefeger
                .build( "hpBarEnabled" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( true );
        hpBarGainIndicatorColor = confefeger
                .build( "hpBarGainIndicatorColor" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xffffff );
        hpBarLossIndicatorColor = confefeger
                .build( "hpBarLossIndicatorColor" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xff0000 );
        hpBarScalePlayer = confefeger
                .build( "hpBarScalePlayer" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0.3f, 0, 15.23f );
        hpBarScaleOthers = confefeger
                .build( "hpBarScaleOthers" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( 1, 0, 15.23f );
        hpBarOffset = confefeger
                .build( "hpBarOffset" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( 1.2f, 0, 15.23f );
        hpBarPolyCount = confefeger
                .build( "hpBarPolyCount" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( 32, 1, 256 );
        hpBarOffsetDeg = confefeger
                .build( "hpBarOffsetDeg" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( -30f, -180f, 180f );
        hpBarPitchPlayer = confefeger
                .build( "hpBarPitchPlayer" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( -25f, -180f, 180f );
        hpBarStartHue = confefeger
                .build( "hpBarStartHue" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( 240f, 0, 360 );
        hpBarEndHue = confefeger
                .build( "hpBarEndHue" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( 360f, 0, 360 );
        hpBarOutsideColor = confefeger
                .build( "hpBarOutsideColor" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0x777777 );

        renderDistance = confefeger
                .build( "renderDistance" )
                .category( "gui.hpBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( 150, 5, 8192 );

        HungerBarWarningColor = confefeger
                .build( "HungerBarWarningColor" )
                .category( "gui.hungerBar" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xff0000 );
        HungerBarSaturationColor = confefeger
                .build( "HungerBarSaturationColor" )
                .category( "gui.hungerBar" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xffff00 );
        HungerBarHungerStartHue = confefeger
                .build( "HungerBarHungerStartHue" )
                .category( "gui.hungerBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( 320d, 0, 360 );
        HungerBarHungerEndHue = confefeger
                .build( "HungerBarHungerEndHue" )
                .category( "gui.hungerBar" )
                .side( Confefeger.Side.LOCAL )
                .submit( 360d, 0, 360 );

        effectIndicatorBaseSize = confefeger
                .build( "effectIndicatorBaseSize" )
                .category( "gui.effectIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submit( 1f, 0, 15.23f );
        effectIndicatorIconSize = confefeger
                .build( "effectIndicatorIconSize" )
                .category( "gui.effectIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submit( 0.8f, 0, 15.23f );

        npcIndicatorEnabled = confefeger
                .build( "npcIndicatorEnabled" )
                .category( "gui.npcIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submit( true );
        effectIndicatorHarmfulColor = confefeger
                .build( "effectIndicatorHarmfulColor" )
                .category( "gui.npcIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xcc0000 );
        effectIndicatorNeutralColor = confefeger
                .build( "effectIndicatorNeutralColor" )
                .category( "gui.npcIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xcccccc );
        effectIndicatorBeneficialColor = confefeger
                .build( "effectIndicatorBeneficialColor" )
                .category( "gui.npcIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0x00cc00 );
        npcIndicatorAggresiveColor = confefeger
                .build( "npcIndicatorAggresiveColor" )
                .category( "gui.npcIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xff0000 );
        npcIndicatorHostileColor = confefeger
                .build( "npcIndicatorHostileColor" )
                .category( "gui.npcIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0xffaa00 );
        npcIndicatorPassiveColor = confefeger
                .build( "npcIndicatorPassiveColor" )
                .category( "gui.npcIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0x00ff00 );
        npcIndicatorPlayerColor = confefeger
                .build( "npcIndicatorPlayerColor" )
                .category( "gui.npcIndicator" )
                .side( Confefeger.Side.LOCAL )
                .submitRGBA( 0x0000ff );
    }
}
