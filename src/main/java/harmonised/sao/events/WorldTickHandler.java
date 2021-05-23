package harmonised.sao.events;

import harmonised.sao.network.MessageIntArray;
import harmonised.sao.network.NetworkHandler;
import harmonised.sao.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldTickHandler
{
    private static long lastSync = 0;
    private static final Map<ResourceLocation, Map<ServerPlayerEntity, Set<Integer>>> victimMap = new HashMap<>();

    public static void handleWorldTick( TickEvent.WorldTickEvent event )
    {
        if( !event.world.isClientSide() )
        {
            ServerWorld world = (ServerWorld) event.world;
            ResourceLocation resLoc = Util.getDimensionResLoc( world );
            if( !victimMap.containsKey( resLoc ) )
                victimMap.put( resLoc, new HashMap<>() );
            Map<ServerPlayerEntity, Set<Integer>> dimVictimMap = new HashMap<>();
            for( Int2ObjectMap.Entry<Entity> entry : world.entitiesById.int2ObjectEntrySet() )
            {
                Entity entity = entry.getValue();
                if( entity instanceof MobEntity )
                {
                    MobEntity mob = (MobEntity) entity;
                    LivingEntity target = mob.getTarget();
                    if( target instanceof ServerPlayerEntity )
                    {
                        ServerPlayerEntity player = (ServerPlayerEntity) target;
                        if( !dimVictimMap.containsKey( player ) )
                            dimVictimMap.put( player, new HashSet<>() );
                        dimVictimMap.get( player ).add( mob.getId() );
                    }
                }
            }
            victimMap.put( resLoc, dimVictimMap );

            if( world.dimension() == World.OVERWORLD )
            {
                if( System.currentTimeMillis() - lastSync > 1000 )
                {
                    for( ResourceLocation dimResLoc : victimMap.keySet() )
                    {
                        for( Map.Entry<ServerPlayerEntity, Set<Integer>> entry : victimMap.get( dimResLoc ).entrySet() )
                        {
                            NetworkHandler.sendToPlayer( new MessageIntArray( entry.getValue() ), entry.getKey() );
                        }
                    }

                    lastSync = System.currentTimeMillis();
                }
            }
        }
    }
}
