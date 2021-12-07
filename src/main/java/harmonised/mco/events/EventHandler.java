package harmonised.mco.events;

import harmonised.mco.client.gui.Temp;
import harmonised.mco.confefeg.Confefeger;
import harmonised.mco.confefeg.McoConfefeg;
import harmonised.mco.util.Reference;
import harmonised.mco.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ServerPlayerEntity;
import net.minecraft.level.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class EventHandler
{
    @SubscribeEvent
    public static void deathEvent(LivingDeathEvent event)
    {
        LivingEntity livingEntity = event.getEntityLiving();
        Level world = livingEntity.getEntityWorld();
        if(world.isClientSide())
        {
//            Renderer.hpBars.remove(livingEntity);
        }
        else
        {

        }
    }

    @SubscribeEvent
    public static void jumpEvent(LivingEvent.LivingJumpEvent event)
    {
        if(event.getEntityLiving() instanceof Player && !Util.isReleased())
        {
            System.out.println(McoConfefeg.buttonColor);
        }
    }

    @SubscribeEvent
    public static void playerTickEvent(TickEvent.PlayerTickEvent event)
    {
        PlayerTickHandler.handlePlayerTick(event);
    }

    @SubscribeEvent
    public static void worldTickHandler(TickEvent.WorldTickEvent event)
    {
        WorldTickHandler.handleWorldTick(event);
    }

    @SubscribeEvent
    public static void serverStartedEvent(FMLServerStartedEvent event)
    {
        Confefeger.saveAllConfefegers();
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        Confefeger.syncAllConfefegs((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public static void textRender(RenderGameOverlayEvent.Text event)
    {
        Temp.test(event);
    }
}