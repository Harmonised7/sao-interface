package harmonised.sao_interface.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class ClientHandler
{
    public static void init()
    {
        MinecraftForge.EVENT_BUS.register( new Renderer() );
    }
}