package harmonised.sao.client;

import harmonised.sao.client.gui.Renderer;
import harmonised.sao.client.gui.SAOScreen;
import harmonised.sao.client.gui.SaoCraftingContainer;
import harmonised.sao.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IRecipeHolder;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientHandler
{
    public static Minecraft mc = Minecraft.getInstance();
    private static boolean wasShowSaoInterface = false;
    public static final KeyBinding SHOW_SAO_INTERFACE = new KeyBinding( "key.sao.showSaoInterface", GLFW.GLFW_KEY_H, "category.sao-interfface" );

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register( new Renderer() );
        ClientRegistry.registerKeyBinding( SHOW_SAO_INTERFACE );
    }

    @SubscribeEvent
    public static void keyPressEvent( net.minecraftforge.client.event.InputEvent.KeyInputEvent event )
    {
        if( !wasShowSaoInterface && SHOW_SAO_INTERFACE.isDown() )
        {
            Minecraft.getInstance().setScreen( new SAOScreen( new TranslationTextComponent( "" ) ) );
        }
        wasShowSaoInterface = SHOW_SAO_INTERFACE.isDown();
    }
    
    public static void disconnect()
    {
        Minecraft mc = Minecraft.getInstance();
        boolean flag = mc.isLocalServer();
        boolean flag1 = mc.isConnectedToRealms();
        mc.level.disconnect();
        if (flag)
            mc.clearLevel(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
        else
            mc.clearLevel();

        if (flag)
            mc.setScreen(new MainMenuScreen());
        else if (flag1)
        {
            RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
            realmsbridgescreen.switchToRealms(new MainMenuScreen());
        }
        else
            mc.setScreen(new MultiplayerScreen(new MainMenuScreen()));
    }
}