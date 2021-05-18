package harmonised.sao_interface.client;

import net.minecraftforge.common.MinecraftForge;

public class ClientHandler
{
    public static void init()
    {
        MinecraftForge.EVENT_BUS.register( new Renderer() );
    }
}