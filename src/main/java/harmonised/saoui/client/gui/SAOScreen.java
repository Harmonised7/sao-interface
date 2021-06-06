package harmonised.saoui.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import harmonised.saoui.SAOMod;
import harmonised.saoui.client.ClientHandler;
import harmonised.saoui.network.MessageCraft;
import harmonised.saoui.network.NetworkHandler;
import harmonised.saoui.util.Reference;
import harmonised.saoui.util.Util;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ToolType;

import java.util.*;

public class SAOScreen extends Screen
{
    private static boolean init = false;
    public static final List<Box> boxes = new ArrayList<>();
    private static float pos = 0, goalPos;
    private long lastRender = System.currentTimeMillis();

    public static Minecraft mc = Minecraft.getInstance();
    MainWindow sr = mc.getWindow();
    FontRenderer font = mc.font;
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
        initBoxes();
        updatePositions( true );
        if( !init )
        {
            init = true;
        }
//        children.addAll( boxes );
    }

    public static void initBoxes()
    {
        boxes.clear();
        boxes.add( getMainBox() );
    }

    private static void openBox( ListButton button, Box box )
    {
        int layer = button.box.getPos()+1;
        int boxCount = boxes.size();
        if( layer > 0 && boxCount-1 == layer )
        {
            Box prevLayerBox = boxes.get( layer-1 );
            Box sameLayerBox = boxes.get( layer );
            if( sameLayerBox.name.equals( box.name ) )
            {
                System.out.println( "Closing box " + layer );
                prevLayerBox.setActiveButton( null );
                boxes.remove( layer );
                return;
            }
        }
        //Close boxes if layer is too far, never close main box
        if( boxCount >= layer )
        {
            for( int i = boxCount-1; i >= layer; i-- )
            {
                if( i == 0 )
                    break;
                System.out.println( "Closing box " + i );
                boxes.remove( i );
            }
        }

        System.out.println( "Opening box " + boxes.size() );
        boxes.add( box );

        button.setAsActive();
    }

    public void updatePositions( boolean force )
    {
        int middleX = Renderer.getScaledWidth()/2;
        int middleY = Renderer.getScaledHeight()/2;

        int compound = 0;
        int lastWidth = 0;

        for ( Box box : boxes )
        {
            box.x = (int) xOffset + middleX + compound;
            box.y = (int) yOffset + middleY - box.getHeight() / 2f;
            lastWidth = box.getWidth() + boxesGap;

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

        for( Box box : boxes )
        {
            box.x -= pos;
        }

        lastRender = System.currentTimeMillis();
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        updatePositions( false );
        for( Box box : boxes )
        {

            box.render( stack, mouseX, mouseY, partialTicks );
        }
        for( Box box : boxes )
        {
            for( ListButton button : box.buttons )
            {
                button.renderTooltip( stack, mouseX, mouseY, partialTicks );
            }
        }
    }

    public static int getBoxPos( Box box )
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
        for( Box box : boxes )
        {
            if( box.mouseScrolled( mouseX, mouseY, scroll ) )
                return true;
        }
        return super.mouseScrolled( mouseX, mouseY, scroll );
    }

    @Override
    public boolean mouseClicked( double mouseX, double mouseY, int button )
    {
        for( Box box : boxes )
        {
            for( ListButton listButton : box.buttons )
            {
                if( listButton.mouseClicked( mouseX, mouseY, button ) )
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
        SAOScreen.xOffset += deltaX;
        SAOScreen.yOffset += deltaY;
        SAOScreen.xOffset = Util.cap( SAOScreen.xOffset, -sr.getGuiScaledWidth()/2D, sr.getGuiScaledWidth()/2D );
        SAOScreen.yOffset = Util.cap( SAOScreen.yOffset, -sr.getGuiScaledHeight()/2D, sr.getGuiScaledHeight()/2D );
        return super.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
    }

    private static Box getMainBox()
    {
        Box box = new Box( "main" );

        box.addButton( new CircleButton( box ).setIcon( Icons.ONE_PERSON ).onPress(theButton ->
        {

            openBox( (ListButton) theButton, getPlayerBox() );
        }));

        CircleButton partyButton = (CircleButton) new CircleButton( box ).setIcon( Icons.TWO_PEOPLE ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getPartyBox() );
        });

        CircleButton skillsButton = (CircleButton) new CircleButton( box ).setIcon( Icons.STATS ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getPmmoHiscoreBox( "totalLevel" ) );
        });
        if( !SAOMod.pmmoLoaded )
        {
            partyButton.lock();
            skillsButton.lock();
        }

        box.addButton( partyButton );
        box.addButton( skillsButton );

        box.addButton( new CircleButton( box ).setIcon( Icons.GEAR ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getMenuBox() );
        }));

        return box;
    }

    private static Box getPlayerBox()
    {
        Box box = new Box( "player" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".equip" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getEquipTypesBox() );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".items" ) ).onPress( theButton ->
        {

            openBox( (ListButton) theButton, getSelectItemsBox() );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".crafting" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getCraftingTypesBox() );
        }));

        return box;
    }

    private static Box getCraftingTypesBox()
    {
        Box box = new Box( "craft" );

        box.addButton( new ListButton( box ).setItem( Items.COMPASS, false  ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".all" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_SEARCH ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.REDSTONE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".redstone" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_REDSTONE ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.BRICKS, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".blocks" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_BUILDING_BLOCKS ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.IRON_AXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".equipment" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_EQUIPMENT ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.LAVA_BUCKET, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".miscellaneous" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getCraftingTypeBox( RecipeBookCategories.CRAFTING_MISC ) );
        }));

        return box;
    }

    private static Box getCraftingTypeBox( RecipeBookCategories type )
    {
        Box box = new Box( type.name() );

        generateCraftingButtons( box, type );

        return box;
    }

    private static void generateCraftingButtons( Box box, RecipeBookCategories category )
    {
        PlayerInventory inv = mc.player.inventory;
        RecipeItemHelper stackedContents = new RecipeItemHelper();
        inv.fillStackedContents( stackedContents );
        ClientRecipeBook book = mc.player.getRecipeBook();
        for( RecipeList recipeList : book.getCollection( category ) )
        {
            recipeList.canCraft( stackedContents, 3, 3, book );
            for( IRecipe recipe : recipeList.getRecipes() )
            {

                if( recipeList.isCraftable( recipe ) )
                {
                    box.addButton( new ListButton( box ).setItemStack( recipe.getResultItem(), true ).onPress( theButton ->
                    {
                        NetworkHandler.sendToServer( new MessageCraft( recipe.getId(), 1 ) );
                        System.out.println( "Client crafting " + recipe.getResultItem().getDisplayName().getString() );
                    }));
                }
            }
        }
    }

    private static Box getPmmoHiscoreBox( String skill )
    {
        Box box = new Box( "hiscore." + skill );

        String playerName = mc.player.getDisplayName().getString();

        List<ListButton> playerButtons = new ArrayList<>();

        for( Map.Entry<UUID, String> entry : XP.playerNames.entrySet() )
        {
            double level;
            if( skill.equals( "totalLevel" ) )
                level = XP.getTotalLevelFromMap( XP.getOfflineXpMap( entry.getKey() ) );
            else
                level = Skill.getLevelDecimal( skill, entry.getKey() );

            ListButton button = new ListButton( box ).setIcon( Icons.ONE_PERSON ).setTextColor( Skill.getSkillColor( skill ) ).setMsg( new StringTextComponent( entry.getValue() + " " + DP.dpSoft( level ) ) ).onPress( theButton ->
            {
                openBox( (ListButton) theButton, getPmmoSkillsBox( entry.getKey() ) );
            });

            if( entry.getValue().equals( playerName ) )
                box.addButton( button );
            else
                playerButtons.add( button );
        }

        if( skill.equals( "totalLevel" ) )
            playerButtons.sort( Comparator.comparingDouble( b -> XP.getTotalLevelFromMap( XP.getOfflineXpMap( ((ListButton) b).uuid ) ) ).reversed() );
        else
            playerButtons.sort( Comparator.comparingDouble( b -> XP.getOfflineXp( skill, ((ListButton) b).uuid ) ).reversed() );

        for( ListButton button : playerButtons )
        {
            box.addButton( button );
        }

        return box;
    }

    private static Box getPartyBox()
    {
        Box box = new Box( "party" );

        CompoundNBT partyData = PartyPendingSystem.offlineData;

        if( partyData.size() > 0 )
        {
            box.addButton( new ListButton( box ).setIcon( Icons.TWO_PEOPLE ).setMsg( new TranslationTextComponent( "saoui.members" ) ).onPress( theButton ->
            {
                System.out.println( "List Party Members" );
            }));

            box.addButton( new ListButton( box ).setIcon( Icons.MINUS ).setMsg( new TranslationTextComponent( "saoui.leave" ) ).onPress( theButton ->
            {
                System.out.println( "Leave Party" );
            }));
        }
        else
        {
            box.addButton( new ListButton( box ).setIcon( Icons.PLUS ).setMsg( new TranslationTextComponent( "saoui.create" ) ).onPress( theButton ->
            {
                System.out.println( "Create Party" );
            }));

            box.addButton( new ListButton( box ).setIcon( Icons.CHECKMARK ).setMsg( new TranslationTextComponent( "saoui.accept" ) ).onPress( theButton ->
            {
                System.out.println( "Accept Party" );
            }));

            box.addButton( new ListButton( box ).setIcon( Icons.X ).setMsg( new TranslationTextComponent( "saoui.decline" ) ).onPress( theButton ->
            {
                System.out.println( "Decline Party" );
            }));
        }

        return box;
    }

    private static Box getPmmoSkillsBox( UUID uuid )
    {
        Box box = new Box( "skills." + uuid.toString() );

        Map<String, Double> xpMap = XP.getOfflineXpMap( uuid );
        List<ListButton> playerButtons = new ArrayList<>();

        ListButton totalLevelButton = new ListButton( box ).setIcon( Icons.STATS ).setMsg( new TranslationTextComponent( "pmmo.levelDisplay", DP.dpSoft( XP.getTotalXpFromMap( xpMap ) ), new TranslationTextComponent( "pmmo.totalLevel" ) ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getPmmoHiscoreBox( "totalLevel" ) );
        });
        totalLevelButton.regKey = "totalLevel";
        totalLevelButton.uuid = uuid;
        box.addButton( totalLevelButton );

        for( Map.Entry<String, Double> skill : xpMap.entrySet() )
        {
            ListButton button = new ListButton( box ).setIcon( Icons.STATS ).setMsg( new TranslationTextComponent( "pmmo.levelDisplay", DP.dpSoft( XP.levelAtXpDecimal( skill.getValue() ) ), new TranslationTextComponent( "pmmo." + skill.getKey() ) ) ).onPress(theButton ->
            {
                openBox( (ListButton) theButton, getPmmoHiscoreBox( skill.getKey() ) );
            });
            button.textColor = Skill.getSkillColor( skill.getKey() );
            button.regKey = skill.getKey();
            button.uuid = uuid;
            playerButtons.add( button );
        }

        playerButtons.sort( Comparator.comparingDouble( b -> XP.getOfflineXp( ((ListButton) b).regKey, uuid ) ).reversed() );

        for( ListButton button : playerButtons )
        {
            box.addButton( button );
        }

        return box;
    }

    private static Box getMenuBox()
    {
        Box box = new Box( "menu" );

        ListButton logoutButton = new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".logout" ) ).onPress( theButton ->
        {
            ClientHandler.disconnect();
        });

        if( Math.random() <= 0.01 )
            logoutButton.lock();

        box.addButton( logoutButton );

        return box;
    }

    private static Box getEquipTypesBox()
    {
        Box box = new Box( "equip" );

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".armor" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getEquipArmorBox() );
        }));

        box.addButton( new ListButton( box ).setIcon( Icons.SWORD ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".weapons" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getSelectWeaponBox() );
        }));

        return box;
    }

    private static Box getSelectWeaponBox()
    {
        Box box = new Box( "selectWeapon" );

        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_SWORD, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".swords" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_AXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".axes" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.BOW, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".bows" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.CROSSBOW, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".crossbows" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        return box;
    }

    private static Box getSelectItemsBox()
    {
        Box box = new Box( "selectItem" );

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_AXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".tools" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSelectToolsBox() );
        }));

        box.addButton( new ListButton( box ).setItem( Items.FLINT_AND_STEEL, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".items" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.STONE_BRICKS, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".blocks" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_CARROT, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".food" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.REDSTONE_TORCH, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".redstone" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.POTION, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".potions" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        return box;
    }

    private static Box getSelectToolsBox()
    {
        Box box = new Box( "selectTool" );

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_PICKAXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".pickaxes" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_AXE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".axes" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_SHOVEL, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".shovels" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        box.addButton( new ListButton( box ).setItem( Items.GOLDEN_HOE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".hoes" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        return box;
    }

    private static Box getSwapSlotTypeBox( String type )
    {
        Box box = new Box( type );

        generateSwapSlotButtons( box, type );

        return box;
    }

    private static void generateSwapSlotButtons( Box box, String type )
    {
        box.clearButtons();
        PlayerInventory inv = mc.player.inventory;
        int invSize = inv.getContainerSize();
        for( int i = 0; i < invSize; i++ )
        {
            ItemStack itemStack = inv.getItem( i );
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
                int slot = inv.selected;
                switch( type )
                {
                    case "blocks":
                        if( !(item instanceof BlockItem || itemGroups.contains( ItemGroup.TAB_BUILDING_BLOCKS ) ) )
                            continue;
                        break;

                    case "potions":
                        if( !(item instanceof PotionItem || itemGroups.contains( ItemGroup.TAB_BREWING )) )
                            continue;
                        break;

                    case "food":
                        if( item.getFoodProperties() == null || !itemGroups.contains( ItemGroup.TAB_FOOD ) )
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
                        if( !itemGroups.contains( ItemGroup.TAB_REDSTONE ) )
                            continue;
                        break;
                }

                int invIndex = i;
                int finalSlot = slot;
                ListButton boxButton = new ListButton( box ).setItemStack( itemStack, true ).enableTooltip().onPress(listButton ->
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

    private static Box getEquipArmorBox()
    {
        Box box = new Box( "equipArmor" );

        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_HELMET, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".head" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.HEAD ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_CHESTPLATE, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".chest" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.CHEST ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_LEGGINGS, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".legs" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.LEGS ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.DIAMOND_BOOTS, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".feet" ) ).onPress( theButton ->
        {
            openBox( (ListButton) theButton, getEquipSlotTypeBox( EquipmentSlotType.FEET ) );
        }));
        box.addButton( new ListButton( box ).setItem( Items.SHIELD, false ).setMsg( new TranslationTextComponent( Reference.MOD_ID + ".shields" ) ).onPress(theButton ->
        {
            openBox( (ListButton) theButton, getSwapSlotTypeBox( ((TranslationTextComponent)theButton.getMessage()).getKey().substring( Reference.MOD_ID.length() + 1 ) ) );
        }));

        return box;
    }

    private static Box getEquipSlotTypeBox( EquipmentSlotType slot )
    {
        Box box = new Box( "equip" + slot.getName() );
        generateEquipButtons( box, slot );
        return box;
    }

    private static void generateEquipButtons( Box box, EquipmentSlotType slot )
    {
        box.clearButtons();
        PlayerInventory inv = mc.player.inventory;
        int invSize = inv.getContainerSize();
        for( int i = 0; i < invSize; i++ )
        {
            ItemStack itemStack = inv.getItem( i );
            if( !itemStack.isEmpty() )
            {
                if( itemStack.canEquip( slot, mc.player ) )
                {
                    int invIndex = i;
                    ListButton boxButton = new ListButton( box ).setItemStack( itemStack, true ).onPress( listButton ->
                    {
//                        ListButton theButton = (ListButton) listButton;
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
        List<RecipeList> list = book.getCollection( category );
        RecipeItemHelper stackedContents = new RecipeItemHelper();
        list.forEach((p_193944_1_) -> {
            p_193944_1_.canCraft( stackedContents, 3, 3, book );
        });
        List<RecipeList> list1 = Lists.newArrayList(list);
        list1.removeIf((p_193952_0_) -> {
            return !p_193952_0_.hasKnownRecipes();
        });
        list1.removeIf((p_193953_0_) -> {
            return !p_193953_0_.hasFitting();
        });
        String s = "";
        if (!s.isEmpty())
        {
            ObjectSet<RecipeList> objectset = new ObjectLinkedOpenHashSet<>( mc.getSearchTree(SearchTreeManager.RECIPE_COLLECTIONS).search(s.toLowerCase(Locale.ROOT)));
            list1.removeIf((p_193947_1_) -> {
                return !objectset.contains(p_193947_1_);
            });
        }

//        this.recipeBookPage.updateCollections(list1, p_193003_1_);
    }
}