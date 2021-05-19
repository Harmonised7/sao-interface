package harmonised.sao_interface.events;

import harmonised.sao_interface.client.Renderer;
import harmonised.sao_interface.util.Reference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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