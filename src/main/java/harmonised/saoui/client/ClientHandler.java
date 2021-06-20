package harmonised.saoui.client;

import harmonised.saoui.client.gui.Renderer;
import harmonised.saoui.client.gui.SAOScreen;
import harmonised.saoui.config.Confefeger;
import harmonised.saoui.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
        if( !wasShowSaoInterface && SHOW_SAO_INTERFACE.isKeyDown() )
        {
            Minecraft mc = Minecraft.getInstance();
            PlayerEntity player = mc.player;
            ClientWorld world = (ClientWorld) player.world;
            Vector3d pos = player.getPositionVec();
//            Minecraft.getInstance().particleEngine.add( new SaoParticle( world, pos.x, pos.y, pos.z ) );
            Minecraft.getInstance().displayGuiScreen( new SAOScreen( new TranslationTextComponent( "" ) ) );
        }
        wasShowSaoInterface = SHOW_SAO_INTERFACE.isKeyDown();
    }

    @SubscribeEvent
    public static void particleRegisterEvent( ParticleFactoryRegisterEvent event )
    {
//        Minecraft.getInstance().particles.registerFactory( SaoParticleTypes.TRIANGLE.get(), SaoParticle.Factory::new );
    }

    @SubscribeEvent
    public static void deathEvent( LivingDeathEvent event )
    {
        LivingEntity livingEntity = event.getEntityLiving();
        World world = livingEntity.getEntityWorld();
        Vector3d pos = livingEntity.getPositionVec();
        float width = livingEntity.getWidth();
        float height = livingEntity.getHeight();
        for( int i = 0; i < 1000; i++ )
        {
            world.addParticle( ParticleTypes.CRIMSON_SPORE, pos.x - width + Math.random()*width*2, pos.y + Math.random()*height, pos.z - width + Math.random()*width*2, 0, 0, 0 );
        }
    }
    
    public static void disconnect()
    {
        Minecraft mc = Minecraft.getInstance();
        boolean flag = mc.isIntegratedServerRunning();
        boolean flag1 = mc.isConnectedToRealms();
        mc.world.sendQuittingDisconnectingPacket();
        if (flag)
            mc.unloadWorld(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
        else
            mc.unloadWorld();

        if (flag)
            mc.displayGuiScreen(new MainMenuScreen());
        else if (flag1)
        {
            RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
            realmsbridgescreen.func_231394_a_(new MainMenuScreen());
        }
        else
            mc.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
    }

    //ANNOTFIG
    private static boolean isServerLocal = false;

    @SubscribeEvent
    public static void worldLoad( ClientPlayerNetworkEvent.LoggedInEvent event )
    {
        if( event.getPlayer().world.isRemote() )
            isServerLocal = Minecraft.getInstance().isIntegratedServerRunning();
    }

    @SubscribeEvent
    public static void renderUI( RenderGameOverlayEvent.Pre event )
    {
        switch( event.getType() )
        {
            case HEALTH:
            case EXPERIENCE:
            case FOOD:
            case POTION_ICONS:
                event.setCanceled( true );
                break;
        }
    }

    @SubscribeEvent
    public static void clientLoggedIn( ClientPlayerNetworkEvent.LoggedInEvent event )
    {
        if( event.getPlayer().world.isRemote() )
            isServerLocal = Minecraft.getInstance().isIntegratedServerRunning();
        Confefeger.reloadAllConfefegs();
    }

    public static boolean isServerLocal()
    {
        return isServerLocal;
    }
}