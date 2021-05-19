package harmonised.sao_interface.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorldTickHandler
{
    public static final Map<ServerPlayerEntity, Set<Integer>> victimMap = new HashMap<>();

    public static void handleWorldTick( TickEvent.WorldTickEvent event )
    {
        World world = event.world;
    }
}
