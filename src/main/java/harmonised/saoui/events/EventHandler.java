package harmonised.saoui.events;

import harmonised.saoui.config.Configs;
import harmonised.saoui.config.SaoConfig;
import harmonised.saoui.util.Reference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber( modid = Reference.MOD_ID )
public class EventHandler
{
    @SubscribeEvent
    public static void deathEvent( LivingDeathEvent event )
    {
        LivingEntity livingEntity = event.getEntityLiving();
        World world = livingEntity.getCommandSenderWorld();
        if( world.isClientSide() )
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
}