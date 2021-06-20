package harmonised.saoui.events;

import harmonised.saoui.network.MessageIntArray;
import harmonised.saoui.network.MessageNBT;
import harmonised.saoui.network.NetworkHandler;
import harmonised.saoui.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;

import java.util.*;

public class WorldTickHandler
{
    private static Map<ResourceLocation, Long> lastSyncs = new HashMap<>();

    public static void handleWorldTick( TickEvent.WorldTickEvent event )
    {
        if( !event.world.isRemote() )
        {
            ServerWorld world = (ServerWorld) event.world;
            ResourceLocation dimResLoc = Util.getDimensionResLoc( world );
            if( !lastSyncs.containsKey( dimResLoc ) )
                lastSyncs.put( dimResLoc, System.currentTimeMillis() );
            if( System.currentTimeMillis() - lastSyncs.get( dimResLoc ) > 1000 )
            {
                Map<ServerPlayerEntity, Set<Integer>> dimVictimMap = new HashMap<>();
                Map<Integer, List<EffectInstance>> effects = new HashMap<>();
                for( Int2ObjectMap.Entry<Entity> entry : world.entitiesById.int2ObjectEntrySet() )
                {
                    Entity entity = entry.getValue();
                    if( entity instanceof MobEntity )
                    {
                        MobEntity mob = (MobEntity) entity;
                        LivingEntity target = mob.getAttackTarget();
                        if( target instanceof ServerPlayerEntity )
                        {
                            ServerPlayerEntity player = (ServerPlayerEntity) target;
                            if( !dimVictimMap.containsKey( player ) )
                                dimVictimMap.put( player, new HashSet<>() );
                            dimVictimMap.get( player ).add( mob.getEntityId() );
                        }
                        effects.put( mob.getEntityId(), new ArrayList<>( mob.getActivePotionEffects() ) );
                    }
                }
                for( ServerPlayerEntity player : dimVictimMap.keySet() )
                {
                    if( dimResLoc.equals( Util.getDimensionResLoc( player.getServerWorld() ) ) )
                        NetworkHandler.sendToPlayer( new MessageIntArray( 0, dimVictimMap.get( player ) ), player );
                }
                for( ServerPlayerEntity player : world.getPlayers() )
                {
                    NetworkHandler.sendToPlayer( new MessageNBT( 0, Util.entityEffectInstanceMapToNBT( effects ) ), player );
                }
                lastSyncs.put( dimResLoc, System.currentTimeMillis() );
            }
        }
    }
}
