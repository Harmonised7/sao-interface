package harmonised.saoui.client;

import harmonised.saoui.client.gui.Renderer;
import harmonised.saoui.client.gui.SAOScreen;
import harmonised.saoui.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber( value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ClientHandler
{
    public static Minecraft mc = Minecraft.getInstance();
    private static boolean wasShowSaoInterface = false;
    public static final KeyBinding SHOW_SAO_INTERFACE = new KeyBinding( "key.saoui.showSaoInterface", GLFW.GLFW_KEY_H, "category.saoui-interfface" );

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
            Minecraft mc = Minecraft.getInstance();
            PlayerEntity player = mc.player;
            ClientWorld world = (ClientWorld) player.level;
            Vector3d pos = player.position();
//            Minecraft.getInstance().particleEngine.add( new SaoParticle( world, pos.x, pos.y, pos.z ) );
            Minecraft.getInstance().setScreen( new SAOScreen( new TranslationTextComponent( "" ) ) );
        }
        wasShowSaoInterface = SHOW_SAO_INTERFACE.isDown();
    }

    @SubscribeEvent
    public static void particleRegisterEvent( ParticleFactoryRegisterEvent event )
    {
        Minecraft.getInstance().particleEngine.register( SaoParticleTypes.TRIANGLE.get(), SaoParticle.Factory::new );
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