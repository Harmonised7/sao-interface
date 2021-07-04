package harmonised.mco.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import harmonised.mco.MCOMod;
import harmonised.mco.client.ClientHandler;
import harmonised.mco.confefeg.Confefeger;
import harmonised.mco.network.MessageCraft;
import harmonised.mco.network.NetworkHandler;
import harmonised.mco.util.Reference;
import harmonised.mco.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ToolType;

import java.util.*;

public class SAOScreen extends Screen
{
    private static boolean init = false, dirty = false;
    public static long lastSave = System.currentTimeMillis();
    public static final List<ListBox> boxes = new ArrayList<>();
    public static ListBox infoBoxParent = null;
    public static InfoBox infoBox = null;
    public static final ListBox partyBox = new ListBox( "party" ), partyMembersBox = new ListBox( "partyMembers" );
    private static float pos = 0, goalPos;
    private static CompoundNBT partyDataCopy = new CompoundNBT();
    private long lastRender = System.currentTimeMillis();
    private long lastUpdate = System.currentTimeMillis();

    public static Minecraft mc = Minecraft.getInstance();
    MainWindow sr = mc.getMainWindow();
    FontRenderer font = mc.fontRenderer;
    private final int boxesGap = 6;
    private int x;
    private int y;
    private static double xOffset;
    private static double yOffset;

    public SAOScreen( ITextComponent titleIn )
    {
        super(titleIn );
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    protected void init()
    {
        if( !init )
        {
            initBoxes();
            init = true;
        }
        updatePositions( true );
//        children.addAll( boxes );
    }

    public static void initBoxes()
    {
        boxes.clear();
        boxes.add( getMainBox() );
    }

    private static void closeBoxes( int layer )
    {
        int boxCount = boxes.size();
        if( boxCount >= layer )
        {
            for( int i = boxCount-1; i >= layer; i-- )
            {
                if( i == 0 )
                    break;
//                System.out.println( "Closing box " + i );
                boxes.remove( i );
            }
        }
    }

    private static void openBox( SaoButton button, ListBox box )
    {
        int layer = button.box.getPos()+1;
        int boxCount = boxes.size();
        if( layer > 0 && boxCount-1 == layer )
        {
            ListBox prevLayerBox = boxes.get( layer-1 );
            ListBox sameLayerBox = boxes.get( layer );
            if( sameLayerBox.name.equals( box.name ) )
            {
//                System.out.println( "Closing box " + layer );
                prevLayerBox.setActiveButton( null );
                boxes.remove( layer );
                return;
            }
        }
        //Close boxes if layer is too far, never close main box
        closeBoxes( button.box.getPos()+1 );

//        System.out.println( "Opening box " + boxes.size() );
        boxes.add( box );

        if( button.infoBox != null )
        {
            infoBox = button.infoBox;
            infoBoxParent = box;
        }

        button.setAsActive();
    }

    public void updateLiveInfo()
    {
        if( MCOMod.pmmoLoaded )
        {
            updatePartyBox();
            updatePartyMembersBox();
        }
    }

    public void updatePositions( boolean force )
    {
        if( infoBoxParent == null || !boxes.contains(infoBoxParent) )
            infoBox = null;

        int middleX = Renderer.getScaledWidth()/2;
        int middleY = Renderer.getScaledHeight()/2;

        int compound = 0;
        float lastWidth = 0;

        for ( ListBox box : boxes )
        {
            box.x = (int) xOffset + middleX + compound;
            box.y = (int) yOffset + middleY - box.getHeightFloat() / 2f;
            lastWidth = box.getWidthFloat() + boxesGap;

            compound += lastWidth;
        }

        goalPos = compound - (boxesGap+lastWidth)/2f;
        if( force )
            pos = goalPos;
        else
        {
            float d = Math.min( 1, (System.currentTimeMillis() - lastRender) / 1000f );
            float posGap = goalPos - pos;
            float change = posGap*d*3;
            pos += change;
        }

        for( ListBox box : boxes )
        {
            box.x -= pos;
        }

        if( infoBox != null )
        {
            infoBox = getinfoBox();
            infoBox.x = (float) xOffset + middleX - pos - infoBox.getWidthFloat() - boxesGap;
            infoBox.y = (float) yOffset + middleY - infoBox.getHeightFloat()/2f;
        }

        lastRender = System.currentTimeMillis();
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        if( System.currentTimeMillis() - lastUpdate > 500 )
        {
            updateLiveInfo();
            lastUpdate = System.currentTimeMillis();
        }

        updatePositions( false );
        if( infoBox != null )
            infoBox.render( stack, mouseX, mouseY, partialTicks );
        for( ListBox box : boxes )
        {
            box.render( stack, mouseX, mouseY, partialTicks );
        }
        for( ListBox box : boxes )
        {
            for( SaoButton button : box.buttons )
            {
                button.renderTooltip( stack, mouseX, mouseY, partialTicks );
            }
        }
//        renderTooltip( stack, new StringTextComponent( mouseX + " " + mouseY ), mouseX, mouseY );
        if( dirty && System.currentTimeMillis() - lastSave > 1000 )
        {
            Confefeger.saveAllConfefegers();
            lastSave = System.currentTimeMillis();
            dirty = false;
        }
    }

    public static int getBoxPos( ListBox box )
    {
        return boxes.indexOf( box );
    }

    @Override
    public void renderBackground( MatrixStack stack, int p_renderBackground_1_)
    {
        if (this.mc != null)
        {
            this.fillGradient( stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this, stack ));
        }
        else
            this.renderBackground( stack, p_renderBackground_1_ );
    }

    @Override
    public boolean mouseScrolled( double mouseX, double mouseY, double scroll)
    {
        for( ListBox box : boxes )
        {
            if( box.mouseScrolled( mouseX, mouseY, scroll ) )
                return true;
        }
        return super.mouseScrolled( mouseX, mouseY, scroll );
    }

    @Override
    public boolean mouseClicked( double mouseX, double mouseY, int button )
    {
        if( button != 0 )
            return false;
        for( ListBox box : boxes )
        {
            for( SaoButton saoButton : box.buttons )
            {
                if( saoButton.mouseClicked( mouseX, mouseY, button ) )
                    return true;
            }
        }
        return super.mouseClicked( mouseX, mouseY, button );
    }

    @Override
    public boolean mouseReleased( double mouseX, double mouseY, int button )
    {
        return super.mouseReleased( mouseX, mouseY, button );
    }

    @Override
    public boolean mouseDragged( double mouseX, double mouseY, int button, double deltaX, double deltaY )
    {
        if( button == 1 )
        {
            SAOScreen.xOffset += deltaX;
            SAOScreen.yOffset += deltaY;
            float boxesWidth = 0;
            for( ListBox box : boxes )
            {
                boxesWidth += box.getWidthFloat();
            }
            SAOScreen.xOffset = Util.cap( SAOScreen.xOffset, -( sr.getScaledWidth()/2f - boxesWidth/2f ), sr.getScaledWidth()/2f + boxesWidth/2f );

//            SAOScreen.xOffset = 0;
            SAOScreen.yOffset = Util.cap( SAOScreen.yOffset, -sr.getScaledHeight()/2D, sr.getScaledHeight()/2D );
        }
        else
        {
            for( ListBox box : boxes )
            {
                for( SaoButton saoButton : box.buttons )
                {
                    saoButton.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
                }
            }
        }
        return super.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
    }

    private static ListBox getMainBox()
    {
        ListBox box = new ListBox( "main" );

        box.addButton( new CircleButton( box ).setIcon( Icons.ONE_PERSON ).setinfoBox( getinfoBox() ).onPress(theButton ->
        {

            openBox( (SaoButton) theButton, getPlayerBox() );
        }));

        box.addButton( new CircleButton( box ).setLock( !MCOMod.pmmoLoaded ).setIcon( Icons.TWO_PEOPLE ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, updatePartyBox() );
        }));

        box.addButton( ( new CircleButton( box ).setLock( !MCOMod.pmmoLoaded ) ).setIcon( Icons.STATS ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getPmmoHiscoreBox( "totalLevel" ) );
        }));

        box.addButton( new CircleButton( box ).setIcon( Icons.GEAR ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getMenuBox() );
        }));

        return box;
    }

    private static InfoBox getinfoBox()
    {
        InfoBox infoBox = new InfoBox();

        infoBox.addLine( new TranslationTextComponent( "mco.healthX", DP.dpSoft( mc.player.getHealth() ), DP.dpSoft( mc.player.getMaxHealth() ) ) );
        infoBox.addLine( new TranslationTextComponent( "mco.armorX", DP.dpSoft( mc.player.getTotalArmorValue() ) ) );
        infoBox.addLine( new TranslationTextComponent( "mco.levelX", mc.player.experienceLevel ) );
        infoBox.addLine( new TranslationTextComponent( "mco.xpX", DP.dpSoft( mc.player.experience*100f ) ) );
        infoBox.addLine( new TranslationTextComponent( "mco.hungerX", mc.player.getFoodStats().getFoodLevel(), 20 ) );
        infoBox.addLine( new TranslationTextComponent( "mco.saturationX", mc.player.getFoodStats().getSaturationLevel(), 20 ) );

        return infoBox;
    }

    private static ListBox getPlayerBox()
    {
        ListBox box = new ListBox( "player" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".equip" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getEquipTypesBox() );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".items" ) ).onPress( theButton ->
        {

            openBox( (SaoButton) theButton, getSelectItemsBox() );
        }));

        if( !Util.isReleased() )
        {
            box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".crafting" ) ).onPress( theButton ->
            {
                openBox( (SaoButton) theButton, getCraftingTypesBox() );
            }));
        }

        return box;
    }

    private static ListBox getCraftingTypesBox()
    {
        ListBox box = new ListBox( "craft" );

        box.addButton( new ListButton( box ).setItem( Items.COMPASS, false  ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".all" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_SEARCH ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.REDSTONE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".redstone" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_REDSTONE ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.BRICKS, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".blocks" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_BUILDING_BLOCKS ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.IRON_AXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".equipment" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_EQUIPMENT ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.LAVA_BUCKET, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".miscellaneous" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_MISC ) );
        }));

        return box;
    }

    private static ListBox getCraftingTypeBox(RecipeBookCategories type )
    {
        ListBox box = new ListBox( type.name() );

        generateCraftingButtons( box, type );

        return box;
    }

    private static void generateCraftingButtons(ListBox box, RecipeBookCategories category )
    {
        PlayerInventory inv = mc.player.inventory;
        RecipeItemHelper stackedContents = new RecipeItemHelper();
        inv.accountStacks( stackedContents );
        ClientRecipeBook book = mc.player.getRecipeBook();
        for( RecipeList recipeList : book.getRecipes( category ) )
        {
            recipeList.canCraft( stackedContents, 3, 3, book );
            for( IRecipe recipe : recipeList.getRecipes() )
            {

                if( recipeList.isCraftable( recipe ) && !recipe.isDynamic() )
                {
                    box.addButton( new ListButton( box ).setItemStack( recipe.getRecipeOutput(), true ).onPress( theButton ->
                    {
                        NetworkHandler.sendToServer( new MessageCraft( recipe.getId(), 1 ) );
                        System.out.println( "Client crafting " + recipe.getRecipeOutput().getDisplayName().getString() );
                    }));
                }
            }
        }
    }

    private static ListBox getPmmoHiscoreBox(String skill )
    {
        ListBox box = new ListBox( "hiscore." + skill );

        String playerName = mc.player.getDisplayName().getString();

        List<SaoButton> playerButtons = new ArrayList<>();

        for( Map.Entry<UUID, String> entry : XP.playerNames.entrySet() )
        {
            double level;
            if( skill.equals( "totalLevel" ) )
                level = XP.getTotalLevelFromMap( XP.getOfflineXpMap( entry.getKey() ) );
            else
                level = Skill.getLevelDecimal( skill, entry.getKey() );

            SaoButton button = new ListButton( box ).setIcon( Icons.ONE_PERSON ).setTextColor( Skill.getSkillColor( skill ) ).setMsg( new StringTextComponent( entry.getValue() + " " + DP.dpSoft( level ) ) ).onPress( theButton ->
            {
                openBox( (SaoButton) theButton, getPmmoSkillsBox( entry.getKey() ) );
            });

            if( entry.getValue().equals( playerName ) )
                box.addButton( button );
            else
                playerButtons.add( button );
        }

        if( skill.equals( "totalLevel" ) )
            playerButtons.sort( Comparator.comparingDouble( b -> XP.getTotalLevelFromMap( XP.getOfflineXpMap( ((SaoButton) b).uuid ) ) ).reversed() );
        else
            playerButtons.sort( Comparator.comparingDouble( b -> XP.getOfflineXp( skill, ((SaoButton) b).uuid ) ).reversed() );

        for( SaoButton button : playerButtons )
        {
            box.addButton( button );
        }

        return box;
    }

    private static boolean inParty()
    {
        return PartyPendingSystem.offlineData.size() > 0;
    }

    private static ListBox updatePartyBox()
    {
        partyBox.clearButtons();

        generatePartyButtons( partyBox, inParty() );
        if( !inParty() && boxes.contains( partyMembersBox ) )
            closeBoxes( partyMembersBox.getPos() );

        return partyBox;
    }

    private static ListBox updatePartyMembersBox()
    {
        partyMembersBox.clearButtons();
        double reqDistance = Config.getConfig( "partyRange" );
        if( reqDistance == -1 )
            reqDistance = Config.forgeConfig.partyRange.get();

        for( String playerName : PartyPendingSystem.offlineData.keySet() )
        {
            if( playerName.equals( mc.player.getDisplayName().getString() ) )
            {
                partyMembersBox.addButton( new ListButton( partyMembersBox ).setTextColor( 0xff00ff ).setIcon( Icons.ONE_PERSON ).setMsg( new TranslationTextComponent( "mco.you" ) ).onPress(theButton ->
                {
                    System.out.println( "Clicked " + playerName );
                }));
            }
            else
            {
                CompoundNBT playerData = PartyPendingSystem.offlineData.getCompound( playerName );
                boolean online = playerData.contains( "dim" );
                int textColor;
                String distanceText = "";
                if( online )
                {
                    double distance = Util.getDistance( mc.player.getPositionVec(), new Vector3d( playerData.getDouble( "x" ), playerData.getDouble( "y" ), playerData.getDouble( "z" ) ) );
                    textColor = distance < reqDistance ? 0x44ff44 : 0xff4444;
                    distanceText = distance + "m";
                }
                else
                    textColor = 0x555555;
                partyMembersBox.addButton( new ListButton( partyMembersBox ).setLock( !online ).setTextColor( textColor ).setIcon( Icons.ONE_PERSON ).setMsg( new StringTextComponent( playerName + distanceText ) ).onPress(theButton ->
                {
                    System.out.println( "Clicked " + playerName );
                }));
            }
        }

        return partyMembersBox;
    }

    private static void generatePartyButtons(ListBox box, boolean inParty )
    {
        box.clearButtons();

        if( inParty )
        {
            TranslationTextComponent textComp;
            if( PartyPendingSystem.offlineData.size() == 1 )
                textComp = new TranslationTextComponent( "mco.1Member" );
            else
                textComp = new TranslationTextComponent( "mco.xMembers", PartyPendingSystem.offlineData.size() );
            box.addButton( new ListButton( box ).setIcon( Icons.TWO_PEOPLE ).setMsg( textComp ).onPress( theButton ->
            {
                if( inParty() )
                    openBox( (SaoButton) theButton, updatePartyMembersBox() );
            }));

            box.addButton( new ListButton( box ).setIcon( Icons.MINUS ).setMsg( new TranslationTextComponent( "mco.leave" ) ).onPress( theButton ->
            {
                mc.getConnection().sendPacket( new CChatMessagePacket( "/pmmo party leave" ) );
                PartyPendingSystem.offlineData = new CompoundNBT();
                generatePartyButtons( box, false );
            }));
        }
        else
        {
            box.addButton( new ListButton( box ).setIcon( Icons.PLUS ).setMsg( new TranslationTextComponent( "mco.create" ) ).onPress( theButton ->
            {
                mc.getConnection().sendPacket( new CChatMessagePacket( "/pmmo party create" ) );
                generatePartyButtons( box, inParty() );
            }));

            box.addButton( new ListButton( box ).setIcon( Icons.CHECKMARK ).setMsg( new TranslationTextComponent( "mco.accept" ) ).onPress( theButton ->
            {
                mc.getConnection().sendPacket( new CChatMessagePacket( "/pmmo party accept" ) );
                generatePartyButtons( box, PartyPendingSystem.offlineData.size() > 0 );
            }));

            box.addButton( new ListButton( box ).setIcon( Icons.X ).setMsg( new TranslationTextComponent( "mco.decline" ) ).onPress( theButton ->
            {
                mc.getConnection().sendPacket( new CChatMessagePacket( "/pmmo party decline" ) );
                generatePartyButtons( box, inParty() );
            }));
        }
    }

    private static ListBox getPmmoSkillsBox( UUID uuid )
    {
        ListBox box = new ListBox( "skills." + uuid.toString() );

        Map<String, Double> xpMap = XP.getOfflineXpMap( uuid );
        List<SaoButton> playerButtons = new ArrayList<>();

        SaoButton totalLevelButton = new ListButton( box ).setIcon( Icons.STATS ).setMsg( new TranslationTextComponent( "pmmo.levelDisplay", DP.dpSoft( XP.getTotalLevelFromMap( xpMap ) ), new TranslationTextComponent( "pmmo.totalLevel" ) ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getPmmoHiscoreBox( "totalLevel" ) );
        });
        totalLevelButton.regKey = "totalLevel";
        totalLevelButton.uuid = uuid;
        box.addButton( totalLevelButton );

        for( Map.Entry<String, Double> skill : xpMap.entrySet() )
        {
            SaoButton button = new ListButton( box ).setIcon( Icons.STATS ).setMsg( new TranslationTextComponent( "pmmo.levelDisplay", DP.dpSoft( XP.levelAtXpDecimal( skill.getValue() ) ), new TranslationTextComponent( "pmmo." + skill.getKey() ) ) ).onPress(theButton ->
            {
                openBox( (SaoButton) theButton, getPmmoHiscoreBox( skill.getKey() ) );
            });
            button.customTextColor = Skill.getSkillColor( skill.getKey() );
            button.regKey = skill.getKey();
            button.uuid = uuid;
            playerButtons.add( button );
        }

        playerButtons.sort( Comparator.comparingDouble( b -> XP.getOfflineXp( ((SaoButton) b).regKey, uuid ) ).reversed() );

        for( SaoButton button : playerButtons )
        {
            box.addButton( button );
        }

        return box;
    }

    private static ListBox getMenuBox()
    {
        ListBox box = new ListBox( "menu" );

        box.addButton( new ListButton( box ).setIcon( Icons.GEAR ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".settings" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSettingsBox() );
        }));

        box.addButton( new ListButton( box ).setLock( Math.random() <= 0.01 ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".logout" ) ).onPress( theButton ->
        {
            ClientHandler.disconnect();
        }));

        return box;
    }

    private static ListBox getSettingsBox()
    {
        ListBox box = new ListBox( "settings" );

        box.addButton( new ListButton( box ).setIcon( Icons.GEAR ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".reloadAll" ) ).onPress( theButton ->
        {
            Confefeger.reloadAllConfefegs();
        }));

        for( Confefeger confefeger : Confefeger.confefegers.values() )
        {
            box.addButton( new ListButton( box ).setIcon( Icons.GEAR ).setMsg( new TranslationTextComponent( "confefeg." + confefeger.confefegName ) ).onPress( theButton ->
            {
                openBox( (SaoButton) theButton, getConfefegerBox( confefeger ) );
            }));
        }

        return box;
    }

    private static ListBox getConfefegerBox(Confefeger confefeger )
    {
        ListBox box = new ListBox( "confefeg." + confefeger.confefegName );

        Map<String, Confefeger.Confefeg> confefegs = confefeger.getConfefegs();
        Map<String, Set<Confefeger.Confefeg>> categoryConfefegs = new HashMap<>();

        for( Confefeger.Confefeg confefeg : confefegs.values() )
        {
            if( !categoryConfefegs.containsKey( confefeg.category ) )
                categoryConfefegs.put( confefeg.category, new HashSet<>() );
            categoryConfefegs.get( confefeg.category ).add( confefeg );
        }

        box.addButton( new ListButton( box ).setIcon( Icons.GEAR ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".reload" ) ).onPress( theButton ->
        {
            confefeger.reloadConfefegs();
        }));

        for( Map.Entry<String, Set<Confefeger.Confefeg>> entry : categoryConfefegs.entrySet() )
        {
            box.addButton( new ListButton( box ).setIcon( Icons.GEAR ).setMsg( new TranslationTextComponent( Reference.MOD_ID + "." + entry.getKey() ) ).onPress( theButton ->
            {
                openBox( (SaoButton) theButton, getConfefegsBox( entry.getKey(), entry.getValue() ) );
            }));
        }

        return box;
    }

    private static ListBox getConfefegsBox(String boxKey, Set<Confefeger.Confefeg> confefegs )
    {
        ListBox box = new ListBox( boxKey );

        List<SaoButton> configButtons = new ArrayList<>();
        for( Confefeger.Confefeg confefeg : confefegs )
        {
            configButtons.add( new ConfigButton( box, confefeg ).setIcon( Icons.GEAR ).setMsg( new TranslationTextComponent( Reference.MOD_ID + "." + confefeg.name ) ).onPress( theButton ->
            {
            }));
        }
        configButtons.sort( Comparator.comparingInt( button ->
        {
            switch( ((ConfigButton) button).confefeg.valueType )
            {
                case BOOLEAN:
                    return 0;
                case RGBA:
                    return 1;
                case RGB:
                    return 2;
                case VALUE:
                    return 3;
            }
            return 0;
        }));
        for( SaoButton button : configButtons )
        {
            box.addButton( button );
        }

        return box;
    }

    private static ListBox getEquipTypesBox()
    {
        ListBox box = new ListBox( "equip" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".armor" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getEquipArmorBox() );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".weapons" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getSelectWeaponBox() );
        }));

        return box;
    }

    private static ListBox getSelectWeaponBox()
    {
        ListBox box = new ListBox( "selectWeapon" );

        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_SWORD, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".swords" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_AXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".axes" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.BOW, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".bows" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.CROSSBOW, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".crossbows" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        return box;
    }

    private static ListBox getSelectItemsBox()
    {
        ListBox box = new ListBox( "selectItem" );

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_AXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".tools" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSelectToolsBox() );
        }));

        box.addButton( new ListButton( box ).setItem( Items.FLINT_AND_STEEL, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".items" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.STONE_BRICKS, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".blocks" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_CARROT, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".food" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.REDSTONE_TORCH, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".redstone" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.POTION, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".potions" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        return box;
    }

    private static ListBox getSelectToolsBox()
    {
        ListBox box = new ListBox( "selectTool" );

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_PICKAXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".pickaxes" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_AXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".axes" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_SHOVEL, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".shovels" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_HOE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".hoes" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        return box;
    }

    private static ListBox getSwapSlotTypeBox(String type )
    {
        ListBox box = new ListBox( type );

        generateSwapSlotButtons( box, type );

        return box;
    }

    private static void generateSwapSlotButtons(ListBox box, String type )
    {
        box.clearButtons();
        PlayerInventory inv = mc.player.inventory;
        int invSize = inv.getSizeInventory();
        for( int i = 0; i < invSize; i++ )
        {
            ItemStack itemStack = inv.getStackInSlot( i );
            if( !itemStack.isEmpty() )
            {
                Item item = itemStack.getItem();
//                Multimap<Attribute, AttributeModifier> attributes = itemStack.getAttributeModifiers( EquipmentSlotType.MAINHAND );
                Set<ToolType> toolTypes = itemStack.getToolTypes();
                Collection<ItemGroup> itemGroups = item.getCreativeTabs();
//                if( attributes.size() > 0 )
//                {
//                    for( Map.Entry<Attribute, AttributeModifier> attribute : attributes.entries() )
//                    {
//                        System.out.println( attribute.getKey().getRegistryName() );
//                    }
//                }
                int slot = inv.currentItem;
                switch( type )
                {
                    case "blocks":
                        if( !(item instanceof BlockItem || itemGroups.contains( ItemGroup.BUILDING_BLOCKS ) ) )
                            continue;
                        break;

                    case "potions":
                        if( !(item instanceof PotionItem || itemGroups.contains( ItemGroup.BREWING )) )
                            continue;
                        break;

                    case "food":
                        if( item.getFood() == null || !itemGroups.contains( ItemGroup.FOOD ) )
                            continue;
                        break;

                    case "swords":
                        if( !(item instanceof SwordItem) )
                            continue;
                        break;

                    case "bows":
                        if( !(item instanceof BowItem) )
                            continue;
                        break;

                    case "crossbows":
                        if( !(item instanceof CrossbowItem) )
                            continue;
                        break;

                    case "shields":
                        if( !(item instanceof ShieldItem) )
                            continue;
                        slot = 40;
                        break;

                    case "pickaxes":
                        if( !toolTypes.contains( ToolType.PICKAXE ) )
                            continue;
                        break;

                    case "shovels":
                        if( !toolTypes.contains( ToolType.SHOVEL ) )
                            continue;
                        break;

                    case "axes":
                        if( !toolTypes.contains( ToolType.AXE ) )
                            continue;
                        break;

                    case "hoes":
                        if( !toolTypes.contains( ToolType.HOE ) )
                            continue;
                        break;

                    case "redstone":
                        if( !itemGroups.contains( ItemGroup.REDSTONE ) )
                            continue;
                        break;
                }

                int invIndex = i;
                int finalSlot = slot;
                SaoButton boxButton = new ListButton( box ).setItemStack( itemStack, true ).enableTooltip().onPress(listButton ->
                {
                    if( invIndex == finalSlot )
                        Util.unequipItem( mc.player, finalSlot );
                    else
                        Util.swapItems( mc.player, invIndex, finalSlot);
                    generateSwapSlotButtons( box, type );
                });
                if( invIndex == finalSlot )
                    boxButton.setAsActive();
                box.addButton( boxButton );
                box.buttons = Lists.reverse( box.buttons );
            }
        }
    }

    private static ListBox getEquipArmorBox()
    {
        ListBox box = new ListBox( "equipArmor" );

        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_HELMET, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".head" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.HEAD ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_CHESTPLATE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".chest" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.CHEST ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_LEGGINGS, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".legs" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.LEGS ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_BOOTS, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".feet" ) ).onPress( theButton ->
        {
            openBox( (SaoButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.FEET ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.SHIELD, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".shields" ) ).onPress(theButton ->
        {
            openBox( (SaoButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        return box;
    }

    private static ListBox getEquipSlotTypeBox(EquipmentSlotType slot )
    {
        ListBox box = new ListBox( "equip" + slot.getName() );
        generateEquipButtons( box, slot );
        return box;
    }

    private static void generateEquipButtons(ListBox box, EquipmentSlotType slot )
    {
        box.clearButtons();
        PlayerInventory inv = mc.player.inventory;
        int invSize = inv.getSizeInventory();
        for( int i = 0; i < invSize; i++ )
        {
            ItemStack itemStack = inv.getStackInSlot( i );
            if( !itemStack.isEmpty() )
            {
                if( itemStack.canEquip( slot, mc.player ) )
                {
                    int invIndex = i;
                    SaoButton boxButton = new ListButton( box ).setItemStack( itemStack, true ).onPress( listButton ->
                    {
//                        ListButton theButton = (SaoButton) listButton;
                        if( invIndex < 36 )
                        {
                            Util.swapItems( mc.player, invIndex, Util.getEquipmentSlotInvIndex( slot ) );
                        }
                        else if( invIndex < 40 )
                        {
                            Util.unequipItem( mc.player, invIndex );
                        }
                        generateEquipButtons( box, slot );
                    });
                    if( invIndex >= 36 && invIndex <= 40 )
                        boxButton.setAsActive();
                    box.addButton( boxButton );
                    box.buttons = Lists.reverse( box.buttons );
                }
            }
        }
    }

    public static void updateCollections( boolean p_193003_1_, RecipeBookCategories category )
    {
        ClientRecipeBook book = mc.player.getRecipeBook();
        List<RecipeList> list = book.getRecipes( category );
        RecipeItemHelper stackedContents = new RecipeItemHelper();
        list.forEach((p_193944_1_) -> {
            p_193944_1_.canCraft( stackedContents, 3, 3, book );
        });
        List<RecipeList> list1 = Lists.newArrayList(list);
        list1.removeIf((p_193952_0_) -> {
            return !p_193952_0_.isNotEmpty();
        });
        list1.removeIf((p_193953_0_) -> {
            return !p_193953_0_.containsValidRecipes();
        });
        String s = "";
        if (!s.isEmpty())
        {
            ObjectSet<RecipeList> objectset = new ObjectLinkedOpenHashSet<>( mc.getSearchTree(SearchTreeManager.RECIPES).search(s.toLowerCase(Locale.ROOT)));
            list1.removeIf((p_193947_1_) -> {
                return !objectset.contains(p_193947_1_);
            });
        }

//        this.recipeBookPage.updateCollections(list1, p_193003_1_);
    }

    public static void markDirty()
    {
        dirty = true;
    }
}