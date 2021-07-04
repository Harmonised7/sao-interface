package harmonised.mco.events;

import harmonised.mco.confefeg.Confefeger;
import harmonised.mco.util.Reference;
import harmonised.mco.util.Util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@Mod.EventBusSubscriber( modid = Reference.MOD_ID )
public class EventHandler
{
    @SubscribeEvent
    public static void deathEvent( LivingDeathEvent event )
    {
        LivingEntity livingEntity = event.getEntityLiving();
        World world = livingEntity.getEntityWorld();
        if( world.isRemote() )
        {
//            Renderer.hpBars.remove( livingEntity );
        }
        else
        {

        }
    }

    @SubscribeEvent
    public static void jumpEvent( LivingEvent.LivingJumpEvent event )
    {
        if( event.getEntityLiving() instanceof PlayerEntity )
        {
            Util.multiplyAlphaColor( 100, 0x66ffffff );
//            Configs.parseConfig( Reference.MOD_ID );
//            ConfigProcessor.saveConfig( SaoConfig.class );
        }
    }

    @SubscribeEvent
    public static void playerTickEvent( TickEvent.PlayerTickEvent event )
    {
        PlayerTickHandler.handlePlayerTick( event );
    }

    @SubscribeEvent
    public static void worldTickHandler( TickEvent.WorldTickEvent event )
    {
        WorldTickHandler.handleWorldTick( event );
    }

    @SubscribeEvent
    public static void serverStartedEvent( FMLServerStartedEvent event )
    {
        Confefeger.saveAllConfefegers();
    }

    @SubscribeEvent
    public static void playerLoggedIn( PlayerEvent.PlayerLoggedInEvent event )
    {
        Confefeger.syncAllConfefegs( (ServerPlayerEntity) event.getPlayer() );
    }
}