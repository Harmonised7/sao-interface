package harmonised.mco.client;

import harmonised.mco.client.gui.Renderer;
import harmonised.mco.client.gui.SAOScreen;
import harmonised.mco.confefeg.Confefeger;
import harmonised.mco.util.Reference;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientHandler
{
    public static Minecraft mc = Minecraft.getInstance();
    private static boolean wasShowSaoInterface = false;
    public static final KeyMapping SHOW_SAO_INTERFACE = new KeyMapping("key.mco.showSaoInterface", GLFW.GLFW_KEY_H, "category.mco-interfface");

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(new Renderer());
        ClientRegistry.registerKeyBinding(SHOW_SAO_INTERFACE);
    }

    @SubscribeEvent
    public static void keyPressEvent(net.minecraftforge.client.event.InputEvent.KeyInputEvent event)
    {
        if(!wasShowSaoInterface && SHOW_SAO_INTERFACE.isDown())
        {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            ClientLevel world = (ClientLevel) player.level;
            Vec3 pos = player.position();
//            Minecraft.getInstance().particleEngine.add(new SaoParticle(world, pos.x, pos.y, pos.z));
            SAOScreen.initBoxes();
            Minecraft.getInstance().setScreen(new SAOScreen(new TranslatableComponent("")));
        }
        wasShowSaoInterface = SHOW_SAO_INTERFACE.isDown();
    }

    @SubscribeEvent
    public static void particleRegisterEvent(ParticleFactoryRegisterEvent event)
    {
//        Minecraft.getInstance().particles.registerFactory(SaoParticleTypes.TRIANGLE.get(), SaoParticle.Factory::new);
    }

    @SubscribeEvent
    public static void deathEvent(LivingDeathEvent event)
    {
        LivingEntity livingEntity = event.getEntityLiving();
        Level world = livingEntity.getLevel();
        Vec3 pos = livingEntity.position();
        float width = livingEntity.getBbWidth();
        float height = livingEntity.getBbHeight();
        for(int i = 0; i < 1000; i++)
        {
            world.addParticle(ParticleTypes.CRIMSON_SPORE, pos.x - width + Math.random()*width*2, pos.y + Math.random()*height, pos.z - width + Math.random()*width*2, 0, 0, 0);
        }
    }
    
    public static void disconnect()
    {
        //cout
//        Minecraft mc = Minecraft.getInstance();
//        boolean flag = mc.isIntegratedServerRunning();
//        boolean flag1 = mc.isConnectedToRealms();
//        mc.level.sendQuittingDisconnectingPacket();
//        if (flag)
//            mc.unloadWorld(new DirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
//        else
//            mc.unloadWorld();
//
//        if (flag)
//            mc.displayGuiScreen(new MainMenuScreen());
//        else if (flag1)
//        {
//            RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
//            realmsbridgescreen.func_231394_a_(new MainMenuScreen());
//        }
//        else
//            mc.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
    }

    //ANNOTFIG
    private static boolean isServerLocal = false;

    @SubscribeEvent
    public static void worldLoad(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        if(event.getPlayer().level.isClientSide())
            isServerLocal = Minecraft.getInstance().isLocalServer();
    }

    @SubscribeEvent
    public static void renderUI(RenderGameOverlayEvent.Pre event)
    {
        switch(event.getType())
        {
            case HEALTH:
            case EXPERIENCE:
            case FOOD:
            case POTION_ICONS:
                event.setCanceled(true);
                break;
        }
    }

    @SubscribeEvent
    public static void clientLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        if(event.getPlayer().level.isClientSide())
            isServerLocal = Minecraft.getInstance().isLocalServer();
        Confefeger.reloadAllConfefegs();
    }

    public static boolean isServerLocal()
    {
        return isServerLocal;
    }
}